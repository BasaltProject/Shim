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

set(_runtime "${CMAKE_SOURCE_DIR}/shim-runtime/src")
set(_codegen "${CMAKE_SOURCE_DIR}/shim-codegen/src")
set(_system "${CMAKE_SOURCE_DIR}/shim-system/src")
set(_classpath_native "${CMAKE_SOURCE_DIR}/shim-classpath/src")

file(GLOB _system_os CONFIGURE_DEPENDS "${_system}/${SHIM_SYSTEM}/*.cpp")
file(GLOB _jni_sources CONFIGURE_DEPENDS "${_classpath_native}/*.cpp")
file(GLOB _compiler_parts CONFIGURE_DEPENDS "${_codegen}/compiler/*.cpp")
file(GLOB _assembler_sources CONFIGURE_DEPENDS "${_codegen}/target/${SHIM_ASSEMBLER}/*.cpp")

set(_vm_sources
    "${_system}/${SHIM_SYSTEM}.cpp" ${_system_os}
    "${_runtime}/finder.cpp" "${_runtime}/machine.cpp" "${_runtime}/bootstrap.cpp"
    "${_runtime}/verify.cpp" "${_runtime}/verify-stack.cpp" "${_runtime}/verify-maps.cpp"
    "${_runtime}/util.cpp" "${CMAKE_SOURCE_DIR}/shim-heap/src/heap.cpp"
    "${_runtime}/${SHIM_PROCESS}.cpp" "${_runtime}/classpath-shim.cpp"
    "${_runtime}/builtin.cpp" "${_runtime}/foreign-call.cpp" "${_runtime}/npe-message.cpp"
    "${_runtime}/jnienv.cpp" "${_runtime}/process.cpp" "${_runtime}/heapdump.cpp"
    "${_runtime}/classpath-string.cpp" "${_runtime}/classpath-digest.cpp"
    "${_runtime}/heapwalk.cpp"
    ${_jni_sources}
)
set(_vm_asm "${_runtime}/${SHIM_ARCH}.S")

# The JIT and the assemblers it needs. The interpreter build leaves them out of the VM.
set(_codegen_sources
    "${_codegen}/compiler.cpp" ${_compiler_parts} "${_runtime}/debug-util.cpp"
    "${_codegen}/runtime.cpp" "${_codegen}/targets.cpp" "${CMAKE_SOURCE_DIR}/shim-util/src/fixed-allocator.cpp"
    ${_assembler_sources}
)
if(SHIM_PROCESS STREQUAL "compile")
    list(APPEND _vm_sources ${_codegen_sources})
    list(APPEND _vm_asm "${_runtime}/compile-${SHIM_ARCH}.S")
endif()

# Everything the static library, the dynamic library and the executables share.
add_library(shim_vm OBJECT ${_vm_sources} ${_vm_asm})
target_link_libraries(shim_vm PUBLIC shim_target)
set_target_properties(shim_vm PROPERTIES POSITION_INDEPENDENT_CODE ON)
add_dependencies(shim_vm shim_generated)

# Static library.
add_library(shim_static STATIC $<TARGET_OBJECTS:shim_vm>)
set_target_properties(shim_static PROPERTIES OUTPUT_NAME shim)
target_link_libraries(shim_static PUBLIC shim_target)

# The VM as a shared library, which is what an embedder loads.
add_library(shim_jvm SHARED $<TARGET_OBJECTS:shim_vm> "${_runtime}/boot.cpp" "${SHIM_CLASSPATH_OBJECT}")
set_target_properties(shim_jvm PROPERTIES OUTPUT_NAME jvm)
target_link_libraries(shim_jvm PUBLIC shim_target shim_link)
add_dependencies(shim_jvm shim_generated)
shim_strip(shim_jvm)

# Standalone executable with the VM linked in.
add_executable(shim_exe $<TARGET_OBJECTS:shim_vm> "${_runtime}/main.cpp" "${_runtime}/boot.cpp"
    "${SHIM_CLASSPATH_OBJECT}")
set_target_properties(shim_exe PROPERTIES OUTPUT_NAME shim ENABLE_EXPORTS ON)
target_link_libraries(shim_exe PRIVATE shim_target shim_link)
add_dependencies(shim_exe shim_generated)

# Executable that loads libjvm.
add_executable(shim_dynamic "${_runtime}/main.cpp")
set_target_properties(shim_dynamic PROPERTIES OUTPUT_NAME shim-dynamic)
target_compile_definitions(shim_dynamic PRIVATE BOOT_LIBRARY="${CMAKE_SHARED_LIBRARY_PREFIX}jvm${CMAKE_SHARED_LIBRARY_SUFFIX}")
target_link_libraries(shim_dynamic PRIVATE shim_target shim_link shim_jvm)
if(NOT APPLE AND NOT WIN32)
    set_target_properties(shim_dynamic PROPERTIES BUILD_RPATH "\$ORIGIN" INSTALL_RPATH "\$ORIGIN")
endif()
add_dependencies(shim_dynamic shim_generated)
shim_strip(shim_dynamic)
