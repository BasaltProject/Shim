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

# Build tools: the object writer, the type generator, and the class library they feed.

set(SHIM_CLASSES_DIR "${CMAKE_BINARY_DIR}/classpath")
set(SHIM_CLASSPATH_STAMP "${CMAKE_BINARY_DIR}/classpath.stamp")
set(SHIM_CLASSPATH_JAR "${CMAKE_BINARY_DIR}/classpath.jar")
set(SHIM_GENERATED_DIR "${CMAKE_BINARY_DIR}/generated")
file(MAKE_DIRECTORY "${SHIM_GENERATED_DIR}")

# Turns a file into an object that exposes it between two symbols.
set(_tools "${CMAKE_SOURCE_DIR}/shim-tools/src")
file(GLOB _object_writer_sources CONFIGURE_DEPENDS "${_tools}/object-writer/*.cpp")
file(GLOB _binary_to_object_sources CONFIGURE_DEPENDS "${_tools}/binary-to-object/*.cpp")
add_executable(shim_binary_to_object ${_object_writer_sources} ${_binary_to_object_sources})
set_target_properties(shim_binary_to_object PROPERTIES OUTPUT_NAME binaryToObject)
target_include_directories(shim_binary_to_object PRIVATE ${SHIM_INCLUDES})
target_compile_definitions(shim_binary_to_object PRIVATE
    __STDC_CONSTANT_MACROS SHIM_TARGET_ARCH=SHIM_ARCH_UNKNOWN SHIM_TARGET_FORMAT=SHIM_FORMAT_UNKNOWN)
target_compile_options(shim_binary_to_object PRIVATE
    -Wall -Wextra -Wunused-parameter -Winit-self -Wno-non-virtual-dtor -fno-rtti -fno-exceptions -g
    $<$<BOOL:${SHIM_WERROR}>:-Werror>)

# Reads the class library and writes the C++ that describes the VM's own types.
set(_system_dir "${CMAKE_SOURCE_DIR}/shim-system/src")
file(GLOB _system_sources CONFIGURE_DEPENDS "${_system_dir}/posix/*.cpp")
add_executable(shim_type_generator
    "${_tools}/type-generator/main.cpp"
    "${_system_dir}/posix.cpp"
    ${_system_sources}
    "${CMAKE_SOURCE_DIR}/shim-runtime/src/finder.cpp"
    "${CMAKE_SOURCE_DIR}/shim-util/src/arg-parser.cpp")
set_target_properties(shim_type_generator PROPERTIES OUTPUT_NAME generator)
target_compile_definitions(shim_type_generator PRIVATE POINTER_SIZE=${SHIM_POINTER_SIZE})
target_compile_options(shim_type_generator PRIVATE -O0 -g3)
target_link_libraries(shim_type_generator PRIVATE shim_common shim_link)

# Class library.
file(GLOB_RECURSE _classpath_sources CONFIGURE_DEPENDS "${CMAKE_SOURCE_DIR}/shim-classpath/java/*.java")
# javac reads one quoted path per line, so folders with spaces survive
set(_quoted_sources ${_classpath_sources})
list(TRANSFORM _quoted_sources REPLACE "^(.*)$" "\"\\1\"")
list(JOIN _quoted_sources "\n" _classpath_args)
file(GENERATE OUTPUT "${CMAKE_BINARY_DIR}/classpath-sources.args" CONTENT "${_classpath_args}\n")
add_custom_command(
    OUTPUT "${SHIM_CLASSPATH_STAMP}"
    COMMAND ${CMAKE_COMMAND} -E make_directory "${SHIM_CLASSES_DIR}"
    # The bootstrap classes define java.lang themselves, so they compile in the Java 8 bootclasspath mode.
    COMMAND "${SHIM_JAVAC}" -encoding UTF-8 -source 8 -target 8 -Xlint:-options
            -d "${SHIM_CLASSES_DIR}" -bootclasspath "${SHIM_CLASSES_DIR}"
            "@${CMAKE_BINARY_DIR}/classpath-sources.args"
    COMMAND ${CMAKE_COMMAND} -E touch "${SHIM_CLASSPATH_STAMP}"
    DEPENDS ${_classpath_sources} "${CMAKE_BINARY_DIR}/classpath-sources.args"
    COMMENT "Compiling the class library"
    VERBATIM)

file(GLOB _classpath_licenses CONFIGURE_DEPENDS "${CMAKE_SOURCE_DIR}/shim-classpath/licenses/*")
set(_license_commands "")
if(_classpath_licenses)
    set(_license_commands
        COMMAND ${CMAKE_COMMAND} -E make_directory "${SHIM_CLASSES_DIR}/META-INF/licenses"
        COMMAND ${CMAKE_COMMAND} -E copy ${_classpath_licenses} "${SHIM_CLASSES_DIR}/META-INF/licenses/")
endif()
add_custom_command(
    OUTPUT "${SHIM_CLASSPATH_JAR}"
    ${_license_commands}
    COMMAND "${SHIM_JAR}" c0f "${SHIM_CLASSPATH_JAR}" -C "${SHIM_CLASSES_DIR}" .
    DEPENDS "${SHIM_CLASSPATH_STAMP}" ${_classpath_licenses}
    COMMENT "Creating classpath.jar"
    VERBATIM)

# Generated type tables.
set(SHIM_GENERATED_SOURCES "")
foreach(_kind enums declarations constructors initializations java-initializations name-initializations maps)
    set(_out "${SHIM_GENERATED_DIR}/type-${_kind}.cpp")
    add_custom_command(
        OUTPUT "${_out}"
        COMMAND shim_type_generator -cp "${SHIM_CLASSES_DIR}" -i "${CMAKE_SOURCE_DIR}/shim-runtime/src/types.def"
                -o "${_out}" -t ${_kind}
        DEPENDS shim_type_generator "${CMAKE_SOURCE_DIR}/shim-runtime/src/types.def" "${SHIM_CLASSPATH_STAMP}"
        COMMENT "Generating type-${_kind}.cpp"
        VERBATIM)
    list(APPEND SHIM_GENERATED_SOURCES "${_out}")
endforeach()

# The class library embedded in the VM.
set(SHIM_CLASSPATH_OBJECT "${CMAKE_BINARY_DIR}/classpath-jar.o")
add_custom_command(
    OUTPUT "${SHIM_CLASSPATH_OBJECT}"
    COMMAND shim_binary_to_object "${SHIM_CLASSPATH_JAR}" "${SHIM_CLASSPATH_OBJECT}"
            _binary_classpath_jar_start _binary_classpath_jar_end ${SHIM_TARGET_FORMAT} ${SHIM_ARCH}
    DEPENDS shim_binary_to_object "${SHIM_CLASSPATH_JAR}"
    COMMENT "Embedding classpath.jar"
    VERBATIM)
set_source_files_properties("${SHIM_CLASSPATH_OBJECT}" PROPERTIES EXTERNAL_OBJECT TRUE GENERATED TRUE)

add_custom_target(shim_generated DEPENDS ${SHIM_GENERATED_SOURCES})
add_custom_target(shim_classpath ALL DEPENDS "${SHIM_CLASSPATH_JAR}")
