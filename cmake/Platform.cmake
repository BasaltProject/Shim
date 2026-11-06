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

# Target architecture, word size and object format.
if(WIN32)
    message(FATAL_ERROR "Shim does not build on Windows")
endif()
if(NOT (APPLE OR CMAKE_SYSTEM_NAME MATCHES "^(Linux|FreeBSD)$"))
    message(FATAL_ERROR "Unsupported platform ${CMAKE_SYSTEM_NAME}; Shim builds on Linux, macOS and FreeBSD")
endif()

set(_processor "${CMAKE_SYSTEM_PROCESSOR}")
if(APPLE AND CMAKE_OSX_ARCHITECTURES)
    list(GET CMAKE_OSX_ARCHITECTURES 0 _processor)
endif()
if(_processor MATCHES "^(x86_64|AMD64|amd64)$")
    set(SHIM_ARCH x86_64)
    set(SHIM_ARCH_ENUM SHIM_ARCH_X86_64)
    set(SHIM_ASSEMBLER x86)
elseif(_processor MATCHES "^(i[3-6]86|x86)$")
    set(SHIM_ARCH i386)
    set(SHIM_ARCH_ENUM SHIM_ARCH_X86)
    set(SHIM_ASSEMBLER x86)
elseif(_processor MATCHES "^(arm64|aarch64|ARM64)$")
    set(SHIM_ARCH arm64)
    set(SHIM_ARCH_ENUM SHIM_ARCH_ARM64)
    set(SHIM_ASSEMBLER arm)
elseif(_processor MATCHES "^arm")
    set(SHIM_ARCH arm)
    set(SHIM_ARCH_ENUM SHIM_ARCH_ARM)
    set(SHIM_ASSEMBLER arm)
else()
    message(FATAL_ERROR "Unsupported architecture ${_processor}; Shim supports x86_64, i386, arm and arm64")
endif()

if(SHIM_ARCH STREQUAL "i386" OR SHIM_ARCH STREQUAL "arm")
    set(SHIM_POINTER_SIZE 4)
else()
    set(SHIM_POINTER_SIZE 8)
endif()

if(APPLE)
    set(SHIM_TARGET_FORMAT macho)
    set(SHIM_FORMAT_ENUM SHIM_FORMAT_MACHO)
    set(JNI_PLATFORM_DIR darwin)
elseif(CMAKE_SYSTEM_NAME STREQUAL "FreeBSD")
    set(SHIM_TARGET_FORMAT elf)
    set(SHIM_FORMAT_ENUM SHIM_FORMAT_ELF)
    set(JNI_PLATFORM_DIR freebsd)
else()
    set(SHIM_TARGET_FORMAT elf)
    set(SHIM_FORMAT_ENUM SHIM_FORMAT_ELF)
    set(JNI_PLATFORM_DIR linux)
endif()

if(NOT SHIM_MODE MATCHES "^(debug|debug-fast|fast|stress|stress-major|small)$")
    message(FATAL_ERROR "SHIM_MODE '${SHIM_MODE}' is not one of debug, debug-fast, fast, stress, stress-major, small")
endif()
if(NOT SHIM_PROCESS MATCHES "^(compile|interpret)$")
    message(FATAL_ERROR "SHIM_PROCESS '${SHIM_PROCESS}' is not compile or interpret")
endif()
if(SHIM_CONTINUATIONS AND NOT SHIM_ASSEMBLER STREQUAL "x86")
    message(FATAL_ERROR "SHIM_CONTINUATIONS is only available on x86")
endif()

# The JDK supplies javac, jar and the JNI headers.
if(SHIM_JAVA_HOME)
    set(_java_home "${SHIM_JAVA_HOME}")
elseif(DEFINED ENV{JAVA_HOME} AND NOT "$ENV{JAVA_HOME}" STREQUAL "")
    set(_java_home "$ENV{JAVA_HOME}")
else()
    find_program(_javac_on_path javac)
    if(NOT _javac_on_path)
        message(FATAL_ERROR "No JDK found: set JAVA_HOME or -DSHIM_JAVA_HOME=<jdk>")
    endif()
    get_filename_component(_javac_real "${_javac_on_path}" REALPATH)
    get_filename_component(_java_home "${_javac_real}" DIRECTORY)
    get_filename_component(_java_home "${_java_home}" DIRECTORY)
endif()
find_program(SHIM_JAVAC javac PATHS "${_java_home}/bin" NO_DEFAULT_PATH REQUIRED)
find_program(SHIM_JAR jar PATHS "${_java_home}/bin" NO_DEFAULT_PATH REQUIRED)
if(NOT EXISTS "${_java_home}/include/jni.h")
    message(FATAL_ERROR "${_java_home}/include/jni.h is missing; Shim needs a full JDK")
endif()
message(STATUS "Shim: JDK ${_java_home}, ${SHIM_ARCH} ${SHIM_PROCESS} ${SHIM_MODE}")

find_package(ZLIB REQUIRED)
find_package(Threads REQUIRED)

set(SHIM_MODULES shim-runtime shim-codegen shim-system shim-heap shim-util shim-tools)
set(SHIM_INCLUDES "${CMAKE_SOURCE_DIR}" "${CMAKE_SOURCE_DIR}/shim-classpath/src"
    "${_java_home}/include" "${_java_home}/include/${JNI_PLATFORM_DIR}" "${CMAKE_BINARY_DIR}/generated")
foreach(_module ${SHIM_MODULES})
    list(APPEND SHIM_INCLUDES "${CMAKE_SOURCE_DIR}/${_module}/include" "${CMAKE_SOURCE_DIR}/${_module}/src")
endforeach()

# Settings every native target shares, the generator and the VM alike.
add_library(shim_common INTERFACE)
target_include_directories(shim_common INTERFACE ${SHIM_INCLUDES})
target_compile_definitions(shim_common INTERFACE
    __STDC_LIMIT_MACROS
    _JNI_IMPLEMENTATION_
    SHIM_VERSION="${SHIM_VERSION_STRING}"
    SHIM_INFO=""
    USE_ATOMIC_OPERATIONS
    SHIM_JAVA_HOME=""
    SHIM_EMBED_PREFIX="/shim-embedded"
    TARGET_BYTES_PER_WORD=${SHIM_POINTER_SIZE}
)
target_compile_options(shim_common INTERFACE
    # The VM never deletes through a base pointer, so it has no virtual destructors
    $<$<COMPILE_LANGUAGE:CXX>:-Wall;-Wextra;-Wunused-parameter;-Winit-self;-Wno-non-virtual-dtor;-fno-rtti;-fno-exceptions>
    $<$<COMPILE_LANGUAGE:CXX>:-fvisibility=hidden>
    $<$<BOOL:${SHIM_WERROR}>:$<$<COMPILE_LANGUAGE:CXX>:-Werror>>
    $<$<BOOL:${SHIM_FRAME_POINTER}>:-fno-omit-frame-pointer>
)
target_link_libraries(shim_common INTERFACE Threads::Threads)

# Settings for the code that ends up in the VM.
add_library(shim_target INTERFACE)
target_link_libraries(shim_target INTERFACE shim_common)
target_compile_definitions(shim_target INTERFACE
    SHIM_TARGET_ARCH=${SHIM_ARCH_ENUM}
    SHIM_TARGET_FORMAT=${SHIM_FORMAT_ENUM}
    BOOT_CLASSPATH="[classpathJar]"
    SHIM_CLASSPATH="[classpathJar]"
    $<$<BOOL:${SHIM_TAILS}>:SHIM_TAILS>
    $<$<BOOL:${SHIM_CONTINUATIONS}>:SHIM_CONTINUATIONS>
    $<$<AND:$<BOOL:${SHIM_FRAME_POINTER}>,$<STREQUAL:${SHIM_ASSEMBLER},x86>>:SHIM_USE_FRAME_POINTER>
)

if(SHIM_MODE STREQUAL "debug")
    set(_opt -O0 -g3)
elseif(SHIM_MODE STREQUAL "debug-fast")
    set(_opt -O0 -g3 -DNDEBUG)
elseif(SHIM_MODE STREQUAL "stress")
    set(_opt -O0 -g3 -DVM_STRESS)
elseif(SHIM_MODE STREQUAL "stress-major")
    set(_opt -O0 -g3 -DVM_STRESS -DVM_STRESS_MAJOR)
elseif(SHIM_MODE STREQUAL "small")
    if(CMAKE_CXX_COMPILER_ID MATCHES "Clang")
        set(_opt -Oz -g3 -DNDEBUG)
    else()
        set(_opt -Os -g3 -DNDEBUG)
    endif()
else()
    set(_opt -O3 -g3 -DNDEBUG)
endif()
target_compile_options(shim_target INTERFACE ${_opt})
if(SHIM_LTO)
    target_compile_options(shim_target INTERFACE -flto)
    target_link_options(shim_target INTERFACE -flto)
endif()

if(APPLE)
    target_compile_options(shim_target INTERFACE $<$<COMPILE_LANGUAGE:CXX>:-Wno-deprecated-declarations>)
endif()
if(SHIM_ARCH STREQUAL "arm")
    # ARMv6 is assumed because the CPU cannot be probed at run time
    target_compile_options(shim_target INTERFACE $<$<COMPILE_LANGUAGE:CXX>:-marm;-Wno-psabi>)
    target_compile_definitions(shim_target INTERFACE SHIM_ASSUME_ARMV6)
endif()
if(SHIM_ARCH STREQUAL "i386" AND NOT APPLE)
    # needed for __sync_bool_compare_and_swap
    target_compile_options(shim_target INTERFACE -m32 -march=i586)
    target_link_options(shim_target INTERFACE -m32 -march=i586)
endif()

# Libraries and linker flags for anything that embeds the VM.
add_library(shim_link INTERFACE)
target_link_libraries(shim_link INTERFACE ZLIB::ZLIB m Threads::Threads ${CMAKE_DL_LIBS})
if(APPLE)
    target_link_libraries(shim_link INTERFACE
        "-framework CoreFoundation" "-framework Foundation" "-framework CoreServices"
        "-framework SystemConfiguration" "-framework Security")
elseif(NOT CMAKE_SYSTEM_NAME STREQUAL "FreeBSD")
    target_link_options(shim_link INTERFACE -Wl,-z,noexecstack)
endif()

# Strips a release binary the way the old build did.
function(shim_strip target)
    if(NOT SHIM_MODE MATCHES "^(fast|small)$" OR NOT CMAKE_STRIP)
        return()
    endif()
    if(APPLE)
        set(_flags -S -x)
    else()
        set(_flags --strip-all)
    endif()
    add_custom_command(TARGET ${target} POST_BUILD COMMAND ${CMAKE_STRIP} ${_flags} $<TARGET_FILE:${target}>)
endfunction()
