#include "avx_emulator/config.h"

#include <cstdlib>

namespace avxemu {

Config& Config::Instance() {
  static Config instance;
  return instance;
}

const EmulatorConfig& Config::Get() const { return cfg_; }

void Config::ReloadFromEnvironment() {
  cfg_.enableDebugLog = std::getenv("AVXEMU_DEBUG") != nullptr;
  if (const char* hot = std::getenv("AVXEMU_HOT_THRESHOLD")) {
    cfg_.hotPathThreshold = static_cast<std::uint32_t>(std::strtoul(hot, nullptr, 10));
  }
  if (const char* block = std::getenv("AVXEMU_BLOCK_LEN")) {
    cfg_.translationBlockMaxInstructions = static_cast<std::size_t>(std::strtoull(block, nullptr, 10));
  }
}

}  // namespace avxemu
