#pragma once

#include "avx_emulator/common.h"

#include <Zydis/Zydis.h>

namespace avxemu {

struct DecodedInstruction {
  ZydisDecodedInstruction instruction{};
  ZydisDecodedOperand operands[ZYDIS_MAX_OPERAND_COUNT]{};
  Address rip = 0;
  std::size_t length = 0;
};

class InstructionDecoder {
 public:
  InstructionDecoder();

  std::optional<DecodedInstruction> Decode(Address rip);

 private:
  ZydisDecoder decoder_{};
};

}  // namespace avxemu
