#pragma once

#include "avx_emulator/avx_to_sse_translator.h"
#include "avx_emulator/code_cache.h"
#include "avx_emulator/instruction_decoder.h"
#include "avx_emulator/jit_codegen.h"
#include "avx_emulator/memory_manager.h"
#include "avx_emulator/thread_manager.h"

namespace avxemu {

class TranslationEngine {
 public:
  static TranslationEngine& Instance();

  bool Initialize();
  bool HandleIllegalInstruction(CONTEXT* contextRecord);

 private:
  TranslationEngine();

  std::vector<DecodedInstruction> BuildBlock(Address rip);

  MemoryManager memoryManager_;
  CodeCache codeCache_;
  InstructionDecoder decoder_;
  AvxToSseTranslator translator_;
  JitCodegen codegen_;
  ThreadManager threadManager_;
};

}  // namespace avxemu
