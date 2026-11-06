# Based on Avian (https://github.com/ReadyTalk/avian).
# Original work Copyright (c) 2008-2015, Avian Contributors.
# Original work licensed under ISC; see LICENSE.txt:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
#
# Modifications Copyright (c) 2026 BasaltMC.
# Modifications licensed under MIT; see LICENSE:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE
#
# SPDX-License-Identifier: ISC AND MIT

cmake_minimum_required(VERSION 3.28)
project(shim VERSION 1.3.0 LANGUAGES C CXX ASM)

set(SHIM_VERSION_STRING "1.3.0-SNAPSHOT")

set(SHIM_MODE "fast" CACHE STRING "Optimization mode: debug, debug-fast, fast, stress, stress-major or small")
set_property(CACHE SHIM_MODE PROPERTY STRINGS debug debug-fast fast stress stress-major small)
option(SHIM_WERROR "Treat compiler warnings as errors" OFF)
option(SHIM_TAILS "Enable tail call support" OFF)
option(SHIM_CONTINUATIONS "Enable continuation support (x86 only)" OFF)
option(SHIM_FRAME_POINTER "Keep frame pointers so native stacks can be walked" OFF)
option(SHIM_LTO "Link time optimization" OFF)

# Apple Silicon forbids the writable and executable mappings the JIT needs, so it defaults to the interpreter.
if(APPLE AND CMAKE_SYSTEM_PROCESSOR MATCHES "^(arm64|aarch64)$")
    set(_shim_process_default interpret)
else()
    set(_shim_process_default compile)
endif()
set(SHIM_PROCESS "${_shim_process_default}" CACHE STRING "Execution engine: compile (JIT) or interpret")
set_property(CACHE SHIM_PROCESS PROPERTY STRINGS compile interpret)

set(SHIM_JAVA_HOME "" CACHE PATH "JDK used to compile the class library (defaults to JAVA_HOME, then the javac on PATH)")

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}")
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}")

include(cmake/Platform.cmake)
include(cmake/Classpath.cmake)
include(cmake/Vm.cmake)

include(CTest)
if(BUILD_TESTING)
    include(cmake/Tests.cmake)
endif()
