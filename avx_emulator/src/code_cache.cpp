#include "avx_emulator/code_cache.h"

namespace avxemu {

std::optional<CachedBlock> CodeCache::Find(Address rip) {
  std::shared_lock lock(mutex_);
  const auto it = blocks_.find(rip);
  if (it == blocks_.end()) {
    return std::nullopt;
  }
  return it->second;
}

void CodeCache::Store(Address rip, void* translatedEntry) {
  std::unique_lock lock(mutex_);
  blocks_[rip] = CachedBlock{rip, translatedEntry, 0};
}

std::uint32_t CodeCache::RegisterHit(Address rip) {
  std::unique_lock lock(mutex_);
  auto it = blocks_.find(rip);
  if (it == blocks_.end()) {
    return 0;
  }
  return ++it->second.hitCount;
}

}  // namespace avxemu
