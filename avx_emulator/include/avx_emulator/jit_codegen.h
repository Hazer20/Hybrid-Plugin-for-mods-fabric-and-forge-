#pragma once

#include "avx_emulator/common.h"
#include "avx_emulator/instruction_decoder.h"

#include <asmjit/core.h>

namespace avxemu {

class AvxToSseTranslator;
class MemoryManager;

class JitCodegen {
 public:
  explicit JitCodegen(MemoryManager& memoryManager);
  ~JitCodegen();

  void* GenerateBlock(std::span<const DecodedInstruction> block, const AvxToSseTranslator& translator);

 private:
  MemoryManager& memoryManager_;
  asmjit::JitRuntime runtime_;
};

}  // namespace avxemu
