cmake_minimum_required(VERSION 3.22.1)
project("bergamot_jni")

set(CMAKE_CXX_STANDARD 17)

add_library(bergamot_jni SHARED bergamot_jni.cpp)

find_library(log-lib log)

target_link_libraries(bergamot_jni ${log-lib})

if(DEFINED BERGAMOT_INCLUDE_DIR AND DEFINED BERGAMOT_LIB_DIR)
    target_include_directories(bergamot_jni PRIVATE ${BERGAMOT_INCLUDE_DIR})
    find_library(bergamot-lib bergamot-translator PATHS ${BERGAMOT_LIB_DIR} NO_DEFAULT_PATH)
    if(bergamot-lib)
        target_link_libraries(bergamot_jni ${bergamot-lib})
        target_compile_definitions(bergamot_jni PRIVATE BERGAMOT_AVAILABLE=1)
    endif()
endif()
