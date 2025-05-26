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

add_library(shim_codegen_arm
  assembler.cpp
  block.cpp
  context.cpp
  fixup.cpp
  multimethod.cpp
  operations32.cpp
  operations64.cpp
)
