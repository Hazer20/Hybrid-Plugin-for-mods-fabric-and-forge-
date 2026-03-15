#pragma once

#include "avx_emulator/common.h"

namespace avxemu {

class MemoryManager {
 public:
  explicit MemoryManager(std::size_t reserveSize);
  ~MemoryManager();

  void* AllocateExecutable(std::size_t size, std::size_t alignment = 16);
  void Reset();

 private:
  std::byte* base_ = nullptr;
  std::size_t reserveSize_ = 0;
  std::atomic<std::size_t> offset_{0};
};

}  // namespace avxemu
