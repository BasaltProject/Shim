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


include_directories (${CMAKE_CURRENT_SOURCE_DIR})

add_executable (avian_unittest
  test-harness.cpp

  codegen/assembler-test.cpp
  codegen/registers-test.cpp

  util/arg-parser-test.cpp
)

target_link_libraries (avian_unittest
  avian_codegen
  avian_codegen_x86
  avian_system
  avian_heap
  avian_util
  ${PLATFORM_LIBS}
)

add_test(NAME avian_unittest COMMAND avian_unittest)
add_dependencies(check avian_unittest)
