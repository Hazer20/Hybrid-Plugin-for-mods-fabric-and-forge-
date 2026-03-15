#include "avx_emulator/avx_to_sse_translator.h"

#include "avx_emulator/logger.h"
#include "avx_emulator/register_mapper.h"

namespace avxemu {
namespace {

std::optional<YmmMapping> MapOperandToPair(const ZydisDecodedOperand& op) {
  if (op.type != ZYDIS_OPERAND_TYPE_REGISTER) {
    return std::nullopt;
  }

  const auto regClass = ZydisRegisterGetClass(op.reg.value);
  if (regClass != ZYDIS_REGCLASS_XMM && regClass != ZYDIS_REGCLASS_YMM) {
    return std::nullopt;
  }
  return RegisterMapper::MapYmmToXmmPair(ZydisRegisterGetId(op.reg.value));
}

using BinarySseOp = void (asmjit::x86::Assembler::*)(const asmjit::x86::Xmm&, const asmjit::x86::Xmm&);

bool EmitSplitBinary(const DecodedInstruction& decoded, asmjit::x86::Assembler& a, BinarySseOp op) {
  auto dst = MapOperandToPair(decoded.operands[0]);
  auto src1 = MapOperandToPair(decoded.operands[1]);
  auto src2 = MapOperandToPair(decoded.operands[2]);
  if (!dst || !src1 || !src2) {
    return false;
  }

  a.movaps(dst->low, src1->low);
  a.movaps(dst->high, src1->high);
  (a.*op)(dst->low, src2->low);
  (a.*op)(dst->high, src2->high);
  return true;
}

}  // namespace

bool AvxToSseTranslator::TranslateInstruction(const DecodedInstruction& decoded,
                                              asmjit::x86::Assembler& a) const {
  return TranslateFloatOp(decoded, a) || TranslateLogicOp(decoded, a) ||
         TranslateMemoryOp(decoded, a) || TranslateIntegerOp(decoded, a);
}

bool AvxToSseTranslator::TranslateFloatOp(const DecodedInstruction& decoded,
                                          asmjit::x86::Assembler& a) const {
  switch (decoded.instruction.mnemonic) {
    case ZYDIS_MNEMONIC_VADDPS:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::addps);
    case ZYDIS_MNEMONIC_VSUBPS:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::subps);
    case ZYDIS_MNEMONIC_VMULPS:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::mulps);
    case ZYDIS_MNEMONIC_VDIVPS:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::divps);
    case ZYDIS_MNEMONIC_VMAXPS:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::maxps);
    case ZYDIS_MNEMONIC_VMINPS:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::minps);
    default:
      return false;
  }
}

bool AvxToSseTranslator::TranslateLogicOp(const DecodedInstruction& decoded,
                                          asmjit::x86::Assembler& a) const {
  switch (decoded.instruction.mnemonic) {
    case ZYDIS_MNEMONIC_VANDPS:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::andps);
    case ZYDIS_MNEMONIC_VORPS:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::orps);
    case ZYDIS_MNEMONIC_VXORPS:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::xorps);
    default:
      return false;
  }
}

bool AvxToSseTranslator::TranslateMemoryOp(const DecodedInstruction& decoded,
                                           asmjit::x86::Assembler& a) const {
  const auto mnemonic = decoded.instruction.mnemonic;
  if (mnemonic != ZYDIS_MNEMONIC_VMOVAPS && mnemonic != ZYDIS_MNEMONIC_VMOVUPS &&
      mnemonic != ZYDIS_MNEMONIC_VMOVDQA && mnemonic != ZYDIS_MNEMONIC_VMOVDQU) {
    return false;
  }

  // Production note: full memory operand translation should rebuild effective address and split
  // 256-bit transfer into two 128-bit transfers. For safety this minimal version supports only
  // register-to-register moves.
  auto dst = MapOperandToPair(decoded.operands[0]);
  auto src = MapOperandToPair(decoded.operands[1]);
  if (!dst || !src) {
    return false;
  }

  a.movaps(dst->low, src->low);
  a.movaps(dst->high, src->high);
  return true;
}

bool AvxToSseTranslator::TranslateIntegerOp(const DecodedInstruction& decoded,
                                            asmjit::x86::Assembler& a) const {
  switch (decoded.instruction.mnemonic) {
    case ZYDIS_MNEMONIC_VPADDD:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::paddd);
    case ZYDIS_MNEMONIC_VPSUBD:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::psubd);
    case ZYDIS_MNEMONIC_VPMULLD:
      return EmitSplitBinary(decoded, a, &asmjit::x86::Assembler::pmulld);
    default:
      return false;
  }
}

}  // namespace avxemu
