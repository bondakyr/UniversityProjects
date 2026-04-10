cmake_minimum_required(VERSION 3.20)
project(ZbieranieFigur)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -static-libgcc -static-libstdc++ -static")

add_executable(ZbieranieFigur main.cpp)
