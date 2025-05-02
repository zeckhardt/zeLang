cmake_minimum_required(VERSION 3.31)
project(cVM C)

set(CMAKE_C_STANDARD 11)

add_executable(cVM
        main.c
        chunk.c
        memory.c
        debug.c
        value.c
)
