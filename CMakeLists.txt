# ##
# Requirements
# ~~~~~~~~~~~~
# - CMAKE v3.19 or higher
# - gcc, g++ >=8.1
#
# Build
# ~~~~~
# cmake -B build/release -DCMAKE_BUILD_TYPE=Release
# cmake --build build/release --config Release
#
# (will update dynamic link libraries under
# `core/src/main/resources/com/hivemq/bin/**`
# which will in turn be loaded by Java through JNI).

cmake_minimum_required(VERSION 3.5)
project(hivemq)

include(ExternalProject)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_CXX_VISIBILITY_PRESET hidden)
set(CMAKE_C_VISIBILITY_PRESET hidden)
set(CMAKE_VERBOSE_MAKEFILE ON)

if (CMAKE_SYSTEM_PROCESSOR MATCHES "^arm64" OR CMAKE_SYSTEM_PROCESSOR MATCHES "^aarch64")
    set(ARCH_AARCH64 1)
else ()
    set(ARCH_AMD64 1)
endif ()

if (APPLE)
    set(OS_DARWIN 1)
elseif (UNIX)
    if (CMAKE_SYSTEM_NAME MATCHES "Linux")
        set(OS_LINUX 1)
    endif ()
endif (APPLE)

set(
    SOURCE_FILES
    src/main/c/share/files.h
)

include_directories($ENV{JAVA_HOME}/include/)
if (OS_DARWIN)
    include_directories($ENV{JAVA_HOME}/include/darwin/)
    set(
        SOURCE_FILES ${SOURCE_FILES}
        src/main/c/freebsd/files.c
    )
elseif (OS_LINUX)
    include_directories($ENV{JAVA_HOME}/include/linux/)
    set(
        SOURCE_FILES ${SOURCE_FILES}
        src/main/c/linux/files.c
    )
endif ()

set(
    SOURCE_FILES ${SOURCE_FILES}
    src/main/c/share/files.c
)

set(OUTPUT ${CMAKE_CURRENT_SOURCE_DIR}/target/classes/com/hivemq/bin-local/)

set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${OUTPUT})
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${OUTPUT})
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${OUTPUT})

set(COMMON_OPTIONS "-Wno-gnu-anonymous-struct;-Wno-nested-anon-types;-Wno-unused-parameter;-fPIC;-fno-rtti;-fno-exceptions")
set(DEBUG_OPTIONS "-Wall;-pedantic;-Wextra;-g;-O0")
set(RELEASE_OPTIONS "-O3")

if (OS_LINUX)
    target_link_libraries(hivemq liburing)
    add_dependencies(hivemq liburing)
endif ()

if (ARCH_AARCH64)
    add_compile_definitions(__AARCH64__)
    add_library(hivemq-aarch64 OBJECT ${AARCH64_FILES})
    target_compile_options(hivemq-aarch64 PRIVATE "-fPIC"
            "$<$<CONFIG:DEBUG>:${DEBUG_OPTIONS}>"
            "$<$<CONFIG:RELEASE>:${RELEASE_OPTIONS}>")
    target_link_libraries(
            hivemq
            hivemq-aarch64
    )
    target_compile_options(hivemq PRIVATE
            "$<$<CONFIG:DEBUG>:${DEBUG_OPTIONS}>"
            "$<$<CONFIG:RELEASE>:${RELEASE_OPTIONS}>")
else ()
    target_compile_options(hivemq PRIVATE "-m64"
            "$<$<CONFIG:DEBUG>:${DEBUG_OPTIONS}>"
            "$<$<CONFIG:RELEASE>:${RELEASE_OPTIONS}>")
    target_link_libraries(
            hivemq
    )
endif (ARCH_AARCH64)


add_custom_command(TARGET hivemq POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_directory ${OUTPUT} ${SECONDARY_OUTPUT}
        COMMENT "Copying all build outputs from ${OUTPUT} to ${SECONDARY_OUTPUT}"
)
