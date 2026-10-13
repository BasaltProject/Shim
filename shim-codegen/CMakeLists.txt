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

add_library (shim_codegen
  src/compiler.cpp
  src/runtime.cpp
  src/targets.cpp

  src/compiler/context.cpp
  src/compiler/event.cpp
  src/compiler/frame.cpp
  src/compiler/ir.cpp
  src/compiler/promise.cpp
  src/compiler/read.cpp
  src/compiler/regalloc.cpp
  src/compiler/resource.cpp
  src/compiler/site.cpp
  src/compiler/value.cpp
)

add_subdirectory(src/target)
