#include "avx_emulator/register_mapper.h"

namespace avxemu {

YmmMapping RegisterMapper::MapYmmToXmmPair(std::uint32_t ymmIndex) {
  const std::uint32_t low = ymmIndex & 0x7u;
  const std::uint32_t high = low + 8;
  return {asmjit::x86::xmm(low), asmjit::x86::xmm(high)};
}

}  // namespace avxemu
