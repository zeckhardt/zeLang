#ifndef CVM_COMPILER_H
#define CVM_COMPILER_H

#include "vm.h"
#include "object.h"

bool compile(const char* source, Chunk* chunk);

#endif //CVM_COMPILER_H
