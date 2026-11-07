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

set(_unit "${CMAKE_SOURCE_DIR}/shim-tests/native")
set(_java_tests "${CMAKE_SOURCE_DIR}/shim-tests/java")

# Native unit tests. The interpreter VM omits the JIT, which the assembler tests exercise.
file(GLOB_RECURSE _unit_sources CONFIGURE_DEPENDS "${_unit}/*.cpp")
add_executable(shim_unittest ${_unit_sources} $<TARGET_OBJECTS:shim_vm> "${CMAKE_SOURCE_DIR}/shim-util/src/arg-parser.cpp")
set_target_properties(shim_unittest PROPERTIES OUTPUT_NAME shim-unittest ENABLE_EXPORTS ON)
target_include_directories(shim_unittest PRIVATE "${_unit}")
target_link_libraries(shim_unittest PRIVATE shim_target shim_link)
add_dependencies(shim_unittest shim_generated)
if(SHIM_PROCESS STREQUAL "interpret")
    target_sources(shim_unittest PRIVATE ${_codegen_sources})
endif()
add_test(NAME shim_unittest COMMAND shim_unittest)

# Java test classes and the native library some of them load.
file(GLOB _test_classes CONFIGURE_DEPENDS "${_java_tests}/*.java")
file(GLOB_RECURSE _test_support CONFIGURE_DEPENDS "${_java_tests}/shim/*.java")
file(GLOB _test_extra CONFIGURE_DEPENDS "${_java_tests}/extra/*.java")
file(GLOB _test_native CONFIGURE_DEPENDS "${_java_tests}/*.cpp")
set(_test_all_sources ${_test_classes} ${_test_support})
set(SHIM_TEST_CLASSES_DIR "${CMAKE_BINARY_DIR}/test")

add_library(shim_test_library SHARED ${_test_native})
set_target_properties(shim_test_library PROPERTIES OUTPUT_NAME test)
target_link_libraries(shim_test_library PRIVATE shim_target shim_link)

# javac reads one quoted path per line, so folders with spaces survive
foreach(_list _test_all_sources _test_extra)
    set(_quoted ${${_list}})
    list(TRANSFORM _quoted REPLACE "^(.+)$" "\"\\1\"")
    list(JOIN _quoted "\n" ${_list}_args)
endforeach()
file(GENERATE OUTPUT "${CMAKE_BINARY_DIR}/test-sources.args" CONTENT "${_test_all_sources_args}\n")
file(GENERATE OUTPUT "${CMAKE_BINARY_DIR}/test-extra-sources.args" CONTENT "${_test_extra_args}\n")
add_custom_command(
    OUTPUT "${CMAKE_BINARY_DIR}/test.stamp"
    COMMAND ${CMAKE_COMMAND} -E make_directory "${SHIM_TEST_CLASSES_DIR}"
    COMMAND "${SHIM_JAVAC}" -encoding UTF-8 -source 8 -target 8 -Xlint:-options
            -classpath "${SHIM_TEST_CLASSES_DIR}" -d "${SHIM_TEST_CLASSES_DIR}"
            -bootclasspath "${SHIM_CLASSES_DIR}" "@${CMAKE_BINARY_DIR}/test-sources.args"
    COMMAND "${SHIM_JAVAC}" -encoding UTF-8 -source 8 -target 8 -Xlint:-options
            -d "${SHIM_TEST_CLASSES_DIR}" -bootclasspath "${SHIM_CLASSES_DIR}"
            "@${CMAKE_BINARY_DIR}/test-extra-sources.args"
    COMMAND ${CMAKE_COMMAND} -E touch "${CMAKE_BINARY_DIR}/test.stamp"
    DEPENDS "${SHIM_CLASSPATH_STAMP}" ${_test_classes} ${_test_support} ${_test_extra}
            "${CMAKE_BINARY_DIR}/test-sources.args" "${CMAKE_BINARY_DIR}/test-extra-sources.args"
    COMMENT "Compiling the Java test classes"
    VERBATIM)
add_custom_target(shim_test_classes ALL DEPENDS "${CMAKE_BINARY_DIR}/test.stamp")

# Java programs that run to completion on the built VM and exit 0
foreach(_name Hello ArraysTest Strings Exceptions Collections Floats Integers Longs Misc Enums FormatStrings
        Dates Files Proxies Reflection Zip Threads GC InvokeDynamic Initializers Subroutine Trace Widening)
    add_test(NAME shim_java_${_name} COMMAND shim_exe -cp "${SHIM_TEST_CLASSES_DIR}" ${_name})
    set_tests_properties(shim_java_${_name} PROPERTIES TIMEOUT 120 WORKING_DIRECTORY "${CMAKE_BINARY_DIR}")
endforeach()

add_custom_target(check COMMAND ${CMAKE_CTEST_COMMAND} --output-on-failure DEPENDS shim_unittest shim_exe shim_test_classes)
