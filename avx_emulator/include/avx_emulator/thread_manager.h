#pragma once

#include "avx_emulator/common.h"

namespace avxemu {

struct ThreadState {
  std::uint32_t translatedInstructions = 0;
  std::uint32_t exceptionsHandled = 0;
};

class ThreadManager {
 public:
  ThreadState& GetCurrentThreadState();

 private:
  static thread_local ThreadState tlsState_;
};

}  // namespace avxemu
