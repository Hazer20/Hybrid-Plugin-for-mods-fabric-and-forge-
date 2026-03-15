#pragma once

#include "avx_emulator/common.h"

namespace avxemu {

struct CachedBlock {
  Address originalRip = 0;
  void* translatedEntry = nullptr;
  std::uint32_t hitCount = 0;
};

class CodeCache {
 public:
  std::optional<CachedBlock> Find(Address rip);
  void Store(Address rip, void* translatedEntry);
  std::uint32_t RegisterHit(Address rip);

 private:
  std::unordered_map<Address, CachedBlock> blocks_;
  mutable std::shared_mutex mutex_;
};

}  // namespace avxemu
