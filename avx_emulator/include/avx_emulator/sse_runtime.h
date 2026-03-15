#pragma once

#include "avx_emulator/common.h"

namespace avxemu {

using TranslatedFn = void (*)(CONTEXT*);

class SseRuntime {
 public:
  static void ExecuteBlock(void* fn, CONTEXT* contextRecord);
};

}  // namespace avxemu
