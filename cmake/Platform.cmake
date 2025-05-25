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

IF (APPLE)
   INCLUDE_DIRECTORIES ( /Developer/Headers/FlatCarbon )
   FIND_LIBRARY(CORE_FOUNDATION_LIBRARY CoreFoundation)

   MARK_AS_ADVANCED (CORE_FOUNDATION_LIBRARY)

   SET(PLATFORM_LIBS ${CORE_FOUNDATION_LIBRARY})
ENDIF()

IF (MSVC)
	SET(PLATFORM_CXX_FLAGS "/Wall")
ELSE()
	SET(PLATFORM_CXX_FLAGS "-Wall -Werror -fno-exceptions -std=c++0x")
  SET(PLATFORM_LIBS ${PLATFORM_LIBS} pthread dl)
ENDIF()

find_package(ZLIB REQUIRED)
include_directories(${ZLIB_INCLUDE_DIRS})
