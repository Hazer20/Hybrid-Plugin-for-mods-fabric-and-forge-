cmake_minimum_required(VERSION 3.24)
project(AVXEmulator LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

option(AVX_EMULATOR_BUILD_TESTS "Build local tests" ON)

if(MSVC)
  add_compile_options(/W4 /permissive- /MP)
  add_compile_definitions(_CRT_SECURE_NO_WARNINGS WIN32_LEAN_AND_MEAN NOMINMAX)
else()
  add_compile_options(-Wall -Wextra -Wpedantic)
endif()

add_subdirectory(avx_emulator)
