#include <stdio.h>
#include <string.h>

#include "common.h"
#include "scanner.h"

/// Consumes source code, tracking the progress along the way.
typedef struct {
    const char* start; ///< Pointer that marks the beginning of the current lexeme.
    const char* current; ///< Points to the current character being looked at.
    int line; ///< Tracks what line the current lexeme is on.
} Scanner;

Scanner scanner;

void initScanner(const char* source) {
    scanner.start = source;
    scanner.current = source;
    scanner.line = 1;
}