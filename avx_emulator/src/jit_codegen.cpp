#include "avx_emulator/jit_codegen.h"

#include "avx_emulator/avx_to_sse_translator.h"
#include "avx_emulator/logger.h"

#include <asmjit/x86.h>

namespace avxemu {

JitCodegen::JitCodegen(MemoryManager& memoryManager) : memoryManager_(memoryManager) {}

JitCodegen::~JitCodegen() = default;

void* JitCodegen::GenerateBlock(std::span<const DecodedInstruction> block,
                                const AvxToSseTranslator& translator) {
  asmjit::CodeHolder code;
  code.init(runtime_.environment());
  asmjit::x86::Assembler a(&code);

  for (const auto& inst : block) {
    if (!translator.TranslateInstruction(inst, a)) {
      AVXEMU_LOG_WARN("Unsupported AVX instruction in block, abort JIT generation");
      return nullptr;
    }
  }

  a.ret();

  void* fn = nullptr;
  if (runtime_.add(&fn, &code) != asmjit::kErrorOk) {
    AVXEMU_LOG_ERROR("asmjit runtime failed to materialize generated code");
    return nullptr;
  }
  return fn;
}

}  // namespace avxemu
