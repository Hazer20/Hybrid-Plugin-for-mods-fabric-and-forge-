#include "avx_emulator/sse_runtime.h"

namespace avxemu {

void SseRuntime::ExecuteBlock(void* fn, CONTEXT* contextRecord) {
  auto translated = reinterpret_cast<TranslatedFn>(fn);
  translated(contextRecord);
}

}  // namespace avxemu
