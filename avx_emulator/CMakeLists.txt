include(FetchContent)

set(ASMJIT_STATIC ON CACHE BOOL "" FORCE)
FetchContent_Declare(
  asmjit
  GIT_REPOSITORY https://github.com/asmjit/asmjit.git
  GIT_TAG master
)
FetchContent_MakeAvailable(asmjit)

FetchContent_Declare(
  zydis
  GIT_REPOSITORY https://github.com/zyantific/zydis.git
  GIT_TAG master
)
set(ZYDIS_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(ZYDIS_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(ZYDIS_BUILD_TESTS OFF CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(zydis)

set(AVX_EMULATOR_HEADERS
  include/avx_emulator/common.h
  include/avx_emulator/config.h
  include/avx_emulator/logger.h
  include/avx_emulator/memory_manager.h
  include/avx_emulator/code_cache.h
  include/avx_emulator/instruction_decoder.h
  include/avx_emulator/register_mapper.h
  include/avx_emulator/avx_to_sse_translator.h
  include/avx_emulator/jit_codegen.h
  include/avx_emulator/translation_engine.h
  include/avx_emulator/sse_runtime.h
  include/avx_emulator/thread_manager.h
  include/avx_emulator/cpuid_hook.h
  include/avx_emulator/exception_handler.h
)

set(AVX_EMULATOR_SOURCES
  src/dllmain.cpp
  src/config.cpp
  src/logger.cpp
  src/memory_manager.cpp
  src/code_cache.cpp
  src/register_mapper.cpp
  src/instruction_decoder.cpp
  src/avx_to_sse_translator.cpp
  src/jit_codegen.cpp
  src/sse_runtime.cpp
  src/thread_manager.cpp
  src/translation_engine.cpp
  src/cpuid_hook.cpp
  src/exception_handler.cpp
)

add_library(avx_emulator SHARED ${AVX_EMULATOR_SOURCES} ${AVX_EMULATOR_HEADERS})
target_include_directories(avx_emulator PUBLIC include)
target_link_libraries(avx_emulator PRIVATE asmjit::asmjit Zydis::Zydis)

if(WIN32)
  target_link_libraries(avx_emulator PRIVATE dbghelp)
endif()

add_executable(loader src/loader.cpp)
target_link_libraries(loader PRIVATE)

if(WIN32)
  target_compile_definitions(loader PRIVATE UNICODE _UNICODE)
endif()
