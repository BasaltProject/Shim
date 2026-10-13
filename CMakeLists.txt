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

# NOTE that this CMake file doesn't current build all of shim.
# It only builds what's required for example/kaleidoscope.

cmake_minimum_required (VERSION 2.6)
project (shim)

include_directories (include src)

add_definitions (
  -DSHIM_TARGET_FORMAT=SHIM_FORMAT_MACHO

  -DSHIM_TARGET_ARCH=SHIM_ARCH_X86_64

  -DTARGET_BYTES_PER_WORD=8
  -D__STDC_LIMIT_MACROS
  -D__STDC_CONSTANT_MACROS
)

include ("cmake/Platform.cmake")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${PLATFORM_CXX_FLAGS}")

include (CTest)

# Sadly, we can't use the 'test' target, as that's coopted by ctest
add_custom_target(check ${CMAKE_CTEST_COMMAND} -V)

add_subdirectory (src)
add_subdirectory (unittest)
