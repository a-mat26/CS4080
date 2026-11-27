#include <stdlib.h>
#include <string.h>
#include "compiler.h"
#include "memory.h"
#include "vm.h"

#ifdef DEBUG_LOG_GC
#include <stdio.h>
#include "debug.h"
#endif

#define GC_HEAP_GROW_FACTOR 2

void* reallocate(void* pointer, size_t oldSize, size_t newSize) {
  vm.bytesAllocated += newSize - oldSize;

  if (newSize == 0) {
    free(pointer);
    return NULL;
  }

  void* result = realloc(pointer, newSize);
  if (result == NULL) exit(1);
  return result;
}

static size_t objectSize(Obj* object) {
  switch (object->type) {
    case OBJ_CLOSURE: return sizeof(ObjClosure);
    case OBJ_FUNCTION: return sizeof(ObjFunction);
    case OBJ_NATIVE: return sizeof(ObjNative);
    case OBJ_STRING: return sizeof(ObjString);
    case OBJ_UPVALUE: return sizeof(ObjUpvalue);
  }
  return 0;
}

Obj* copyObject(Obj* object) {
  if (object == NULL) return NULL;
  if (object->forward != NULL) return object->forward;

  size_t size = objectSize(object);
  Obj* copy = (Obj*)vm.heapTop;
  vm.heapTop += size;
  memcpy(copy, object, size);

  if (object->type == OBJ_UPVALUE && ((ObjUpvalue*)object)->location == &((ObjUpvalue*)object)->closed) {
    ((ObjUpvalue*)copy)->location = &((ObjUpvalue*)copy)->closed;
  }

#ifdef DEBUG_LOG_GC
  printf("%p copy to %p ", (void*)object, (void*)copy);
  printValue(OBJ_VAL(copy));
  printf("\n");
#endif

  object->forward = copy;
  return copy;
}

void copyValue(Value* value) {
  if (IS_OBJ(*value)) *value = OBJ_VAL(copyObject(AS_OBJ(*value)));
}

static void copyArray(ValueArray* array) {
  for (int i = 0; i < array->count; i++) {
    copyValue(&array->values[i]);
  }
}

static void blackenObject(Obj* object) {
#ifdef DEBUG_LOG_GC
  printf("%p blacken ", (void*)object);
  printValue(OBJ_VAL(object));
  printf("\n");
#endif

  switch (object->type) {
    case OBJ_CLOSURE: {
      ObjClosure* closure = (ObjClosure*)object;
      closure->function = (ObjFunction*)copyObject((Obj*)closure->function);
      for (int i = 0; i < closure->upvalueCount; i++) {
        closure->upvalues[i] = (ObjUpvalue*)copyObject((Obj*)closure->upvalues[i]);
      }
      break;
    }
    case OBJ_FUNCTION: {
      ObjFunction* function = (ObjFunction*)object;
      function->name = (ObjString*)copyObject((Obj*)function->name);
      copyArray(&function->chunk.constants);
      break;
    }
    case OBJ_UPVALUE:
      copyValue(&((ObjUpvalue*)object)->closed);
      break;
    case OBJ_NATIVE:
    case OBJ_STRING:
      break;
  }
}

static void freeObject(Obj* object) {
#ifdef DEBUG_LOG_GC
  printf("%p free type %d\n", (void*)object, object->type);
#endif

  switch (object->type) {
    case OBJ_CLOSURE: {
      ObjClosure* closure = (ObjClosure*)object;
      FREE_ARRAY(ObjUpvalue*, closure->upvalues, closure->upvalueCount);
      break;
    }
    case OBJ_FUNCTION: {
      ObjFunction* function = (ObjFunction*)object;
      freeChunk(&function->chunk);
      break;
    }
    case OBJ_STRING: {
      ObjString* string = (ObjString*)object;
      FREE_ARRAY(char, string->chars, string->length + 1);
      break;
    }
    case OBJ_NATIVE:
    case OBJ_UPVALUE:
      break;
  }
  vm.bytesAllocated -= objectSize(object);
}

static void copyRoots() {
  for (Value* slot = vm.stack; slot < vm.stackTop; slot++) {
    copyValue(slot);
  }

  for (int i = 0; i < vm.frameCount; i++) {
    vm.frames[i].closure = (ObjClosure*)copyObject((Obj*)vm.frames[i].closure);
  }

  vm.openUpvalues = (ObjUpvalue*)copyObject((Obj*)vm.openUpvalues);
  for (ObjUpvalue* upvalue = vm.openUpvalues; upvalue != NULL; upvalue = upvalue->next) {
    upvalue->next = (ObjUpvalue*)copyObject((Obj*)upvalue->next);
  }

  copyTable(&vm.globals);
}

static void traceReferences() {
  char* scan = vm.toSpace;
  while (scan < vm.heapTop) {
    Obj* object = (Obj*)scan;
    blackenObject(object);
    scan += objectSize(object);
  }
}

static void sweep(char* fromTop) {
  char* scan = vm.fromSpace;
  while (scan < fromTop) {
    Obj* object = (Obj*)scan;
    scan += objectSize(object);
    if (object->forward == NULL) freeObject(object);
  }
}

void collectGarbage() {
#ifdef DEBUG_LOG_GC
  printf("-- gc begin\n");
  size_t before = vm.bytesAllocated;
#endif

  char* fromTop = vm.heapTop;
  vm.heapTop = vm.toSpace;

  copyRoots();
  traceReferences();
  tableRemoveWhite(&vm.strings);
  sweep(fromTop);

  char* oldSpace = vm.fromSpace;
  vm.fromSpace = vm.toSpace;
  vm.toSpace = oldSpace;

  vm.nextGC = vm.bytesAllocated * GC_HEAP_GROW_FACTOR;

#ifdef DEBUG_LOG_GC
  printf("-- gc end\n");
  printf("   collected %zu bytes (from %zu to %zu) next at %zu\n", before - vm.bytesAllocated, before, vm.bytesAllocated, vm.nextGC);
#endif
}

void freeObjects() {
  char* scan = vm.fromSpace;
  while (scan < vm.heapTop) {
    Obj* object = (Obj*)scan;
    scan += objectSize(object);
    freeObject(object);
  }

  free(vm.fromSpace);
  free(vm.toSpace);
}