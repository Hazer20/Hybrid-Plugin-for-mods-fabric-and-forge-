#pragma once

#include "avx_emulator/common.h"

#include <asmjit/x86.h>

namespace avxemu {

struct YmmMapping {
  asmjit::x86::Xmm low;
  asmjit::x86::Xmm high;
};

class RegisterMapper {
 public:
  static YmmMapping MapYmmToXmmPair(std::uint32_t ymmIndex);
};

}  // namespace avxemu
