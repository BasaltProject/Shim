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

# GNU Make builds the complete VM; CMake builds native libraries, tools and tests.
cmake_minimum_required(VERSION 3.28)
project(shim LANGUAGES C CXX)

option(SHIM_WERROR "Treat compiler warnings as errors" OFF)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

foreach(module shim-runtime shim-codegen shim-system shim-heap shim-util shim-tools)
    include_directories(${module}/include ${module}/src)
endforeach()
include_directories(shim-classpath/src)

add_definitions(
    -DSHIM_TARGET_FORMAT=SHIM_FORMAT_MACHO
    -DSHIM_TARGET_ARCH=SHIM_ARCH_X86_64
    -DTARGET_BYTES_PER_WORD=8
    -D__STDC_LIMIT_MACROS
    -D__STDC_CONSTANT_MACROS
)

include(cmake/Platform.cmake)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${PLATFORM_CXX_FLAGS}")
include(CTest)
add_custom_target(check COMMAND ${CMAKE_CTEST_COMMAND} --output-on-failure)

add_subdirectory(shim-util)
add_subdirectory(shim-system)
add_subdirectory(shim-heap)
add_subdirectory(shim-codegen)
add_subdirectory(shim-runtime)
add_subdirectory(shim-tools)
add_subdirectory(shim-tests)
