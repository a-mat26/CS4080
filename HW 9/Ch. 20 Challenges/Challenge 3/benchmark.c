#include <stdio.h>
#include <time.h>

#include "common.h"
#include "object.h"
#include "table.h"
#include "vm.h"

#define KEY_COUNT 100000
#define BAD_COUNT 2000

static ObjString* keys[KEY_COUNT];
static ObjString* missing[KEY_COUNT];
static ObjString* goodKeys[BAD_COUNT];
static ObjString* badKeys[BAD_COUNT];

static uint32_t hashKey(const char* key, int length) {
  uint32_t hash = 2166136261u;
  for (int i = 0; i < length; i++) {
    hash ^= (uint8_t)key[i];
    hash *= 16777619;
  }
  return hash;
}

static void makeKeys(ObjString** list, int count, const char* prefix) {
  char buffer[32];
  for (int i = 0; i < count; i++) {
    int length = sprintf(buffer, "%s%d", prefix, i);
    list[i] = copyString(buffer, length);
  }
}

//keys whose hashes all land in a few buckets once the table has 4096 slots.
static void makeBadKeys() {
  char buffer[32];
  int found = 0;
  for (int i = 0; found < BAD_COUNT; i++) {
    int length = sprintf(buffer, "bad%d", i);
    if (hashKey(buffer, length) % 1024 == 0) {
      badKeys[found] = copyString(buffer, length);
      found++;
    }
  }
}

static void report(const char* name, clock_t start, int operations) {
  double seconds = (double)(clock() - start) / CLOCKS_PER_SEC;
  printf("%-34s %8.1f ns per operation\n", name, seconds * 1e9 / operations);
}

static void insertKeys() {
  clock_t start = clock();
  for (int round = 0; round < 10; round++) {
    Table table;
    initTable(&table);
    for (int i = 0; i < KEY_COUNT; i++) tableSet(&table, keys[i], NIL_VAL);
    freeTable(&table);
  }
  report("insert 100000 keys (with growing)", start, 10 * KEY_COUNT);
}

static void lookups(const char* name, Table* table, ObjString** list, int count) {
  Value value;
  int operations = 0;
  clock_t start = clock();
  while (operations < 5000000) {
    for (int i = 0; i < count; i++) tableGet(table, list[i], &value);
    operations += count;
  }
  report(name, start, operations);
}

static void hitsAndMisses() {
  Table table;
  initTable(&table);
  for (int i = 0; i < KEY_COUNT; i++) tableSet(&table, keys[i], NIL_VAL);
  lookups("lookups that hit", &table, keys, KEY_COUNT);
  lookups("lookups that miss", &table, missing, KEY_COUNT);
  freeTable(&table);
}

static void smallTable() {
  Table table;
  initTable(&table);
  for (int i = 0; i < 10; i++) tableSet(&table, keys[i], NIL_VAL);
  lookups("lookups in a 10 key table", &table, keys, 10);
  freeTable(&table);
}

static void afterDeletes() {
  Table table;
  initTable(&table);
  for (int i = 0; i < KEY_COUNT; i++) tableSet(&table, keys[i], NIL_VAL);
  for (int i = 0; i < KEY_COUNT; i++) tableDelete(&table, keys[i]);
  lookups("misses after deleting every key", &table, missing, KEY_COUNT);
  freeTable(&table);
}

static void collisions(const char* name, ObjString** list) {
  clock_t start = clock();
  for (int round = 0; round < 200; round++) {
    Table table;
    initTable(&table);
    for (int i = 0; i < BAD_COUNT; i++) tableSet(&table, list[i], NIL_VAL);
    Value value;
    for (int i = 0; i < BAD_COUNT; i++) tableGet(&table, list[i], &value);
    freeTable(&table);
  }
  report(name, start, 200 * 2 * BAD_COUNT);
}

int main() {
  initVM();
  makeKeys(keys, KEY_COUNT, "key");
  makeKeys(missing, KEY_COUNT, "missing");
  makeKeys(goodKeys, BAD_COUNT, "good");
  makeBadKeys();

  insertKeys();
  hitsAndMisses();
  smallTable();
  afterDeletes();
  collisions("2000 normal keys (set + get)", goodKeys);
  collisions("2000 colliding keys (set + get)", badKeys);

  freeVM();
  return 0;
}