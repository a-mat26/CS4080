#include <stdio.h>
#include <time.h>

#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

int main(int argc, const char* argv[]) {
  initVM();

  Chunk chunk;
  initChunk(&chunk);

  int constant = addConstant(&chunk, 1.2);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, constant, 123);

  for (int i = 0; i < 10000000; i++) {
    writeChunk(&chunk, OP_NEGATE, 123);
  }

  writeChunk(&chunk, OP_RETURN, 123);

  clock_t start = clock();
  interpret(&chunk);
  printf("%.3f seconds\n", (double)(clock() - start) / CLOCKS_PER_SEC);
  freeVM();
  freeChunk(&chunk);
  return 0;
}
