#pragma once

#include "avx_emulator/common.h"
#include "avx_emulator/instruction_decoder.h"

#include <asmjit/x86.h>

namespace avxemu {

class AvxToSseTranslator {
 public:
  bool TranslateInstruction(const DecodedInstruction& decoded, asmjit::x86::Assembler& a) const;

 private:
  bool TranslateFloatOp(const DecodedInstruction& decoded, asmjit::x86::Assembler& a) const;
  bool TranslateLogicOp(const DecodedInstruction& decoded, asmjit::x86::Assembler& a) const;
  bool TranslateMemoryOp(const DecodedInstruction& decoded, asmjit::x86::Assembler& a) const;
  bool TranslateIntegerOp(const DecodedInstruction& decoded, asmjit::x86::Assembler& a) const;
};

}  // namespace avxemu
