#include "avx_emulator/thread_manager.h"

namespace avxemu {

thread_local ThreadState ThreadManager::tlsState_{};

ThreadState& ThreadManager::GetCurrentThreadState() { return tlsState_; }

}  // namespace avxemu
