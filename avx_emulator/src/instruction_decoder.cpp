#include "avx_emulator/instruction_decoder.h"

#include "avx_emulator/logger.h"

namespace avxemu {

InstructionDecoder::InstructionDecoder() {
  ZydisDecoderInit(&decoder_, ZYDIS_MACHINE_MODE_LONG_64, ZYDIS_STACK_WIDTH_64);
}

std::optional<DecodedInstruction> InstructionDecoder::Decode(Address rip) {
  DecodedInstruction decoded{};
  decoded.rip = rip;

  auto* bytes = reinterpret_cast<const std::uint8_t*>(rip);
  const ZyanStatus status = ZydisDecoderDecodeFull(&decoder_, bytes, ZYDIS_MAX_INSTRUCTION_LENGTH,
                                                    &decoded.instruction, decoded.operands);
  if (!ZYAN_SUCCESS(status)) {
    AVXEMU_LOG_WARN("Zydis failed to decode instruction");
    return std::nullopt;
  }

  decoded.length = decoded.instruction.length;
  return decoded;
}

}  // namespace avxemu
