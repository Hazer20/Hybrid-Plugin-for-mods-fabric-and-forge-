#include "avx_emulator/memory_manager.h"

#include "avx_emulator/logger.h"

namespace avxemu {

MemoryManager::MemoryManager(std::size_t reserveSize) : reserveSize_(reserveSize) {
  base_ = static_cast<std::byte*>(
      VirtualAlloc(nullptr, reserveSize_, MEM_RESERVE | MEM_COMMIT, PAGE_EXECUTE_READWRITE));
  if (!base_) {
    AVXEMU_LOG_ERROR("VirtualAlloc for JIT cache failed");
  }
}

MemoryManager::~MemoryManager() {
  if (base_) {
    VirtualFree(base_, 0, MEM_RELEASE);
  }
}

void* MemoryManager::AllocateExecutable(std::size_t size, std::size_t alignment) {
  if (!base_) {
    return nullptr;
  }

  auto current = offset_.load(std::memory_order_relaxed);
  const auto aligned = (current + alignment - 1) & ~(alignment - 1);
  if (aligned + size >= reserveSize_) {
    AVXEMU_LOG_WARN("JIT cache exhausted, reset requested");
    return nullptr;
  }

  offset_.store(aligned + size, std::memory_order_release);
  return base_ + aligned;
}

void MemoryManager::Reset() { offset_.store(0, std::memory_order_release); }

}  // namespace avxemu
