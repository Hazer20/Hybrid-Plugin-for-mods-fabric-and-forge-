#include "avx_emulator/translation_engine.h"

#include "avx_emulator/config.h"
#include "avx_emulator/logger.h"
#include "avx_emulator/sse_runtime.h"

#include <sstream>

namespace avxemu {

TranslationEngine& TranslationEngine::Instance() {
  static TranslationEngine engine;
  return engine;
}

TranslationEngine::TranslationEngine()
    : memoryManager_(Config::Instance().Get().codeCacheSize), codegen_(memoryManager_) {}

bool TranslationEngine::Initialize() {
  AVXEMU_LOG_INFO("Translation engine initialized");
  return true;
}

std::vector<DecodedInstruction> TranslationEngine::BuildBlock(Address rip) {
  std::vector<DecodedInstruction> block;
  block.reserve(Config::Instance().Get().translationBlockMaxInstructions);

  Address current = rip;
  for (std::size_t i = 0; i < Config::Instance().Get().translationBlockMaxInstructions; ++i) {
    auto decoded = decoder_.Decode(current);
    if (!decoded.has_value()) {
      break;
    }

    block.push_back(*decoded);
    current += decoded->length;

    // Conservative block boundary: end on branch/call/ret.
    if (decoded->instruction.meta.category == ZYDIS_CATEGORY_COND_BR ||
        decoded->instruction.meta.category == ZYDIS_CATEGORY_UNCOND_BR ||
        decoded->instruction.meta.category == ZYDIS_CATEGORY_RET ||
        decoded->instruction.meta.category == ZYDIS_CATEGORY_CALL) {
      break;
    }
  }

  return block;
}

bool TranslationEngine::HandleIllegalInstruction(CONTEXT* contextRecord) {
  const Address rip = static_cast<Address>(contextRecord->Rip);
  auto& threadState = threadManager_.GetCurrentThreadState();
  ++threadState.exceptionsHandled;

  if (auto cached = codeCache_.Find(rip); cached.has_value()) {
    codeCache_.RegisterHit(rip);
    SseRuntime::ExecuteBlock(cached->translatedEntry, contextRecord);
    return true;
  }

  auto block = BuildBlock(rip);
  if (block.empty()) {
    AVXEMU_LOG_WARN("Failed to build translation block");
    return false;
  }

  void* translated = codegen_.GenerateBlock(block, translator_);
  if (!translated) {
    std::ostringstream oss;
    oss << "Translation failed at RIP=0x" << std::hex << rip;
    AVXEMU_LOG_WARN(oss.str());
    return false;
  }

  codeCache_.Store(rip, translated);
  ++threadState.translatedInstructions;
  SseRuntime::ExecuteBlock(translated, contextRecord);

  // Continue execution after translated instruction block.
  contextRecord->Rip += block.front().length;
  return true;
}

}  // namespace avxemu
