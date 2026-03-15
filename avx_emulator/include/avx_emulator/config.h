#pragma once

#include "avx_emulator/common.h"

namespace avxemu {

struct EmulatorConfig {
  bool enableDebugLog = false;
  bool enableHotPathDetection = true;
  bool enableCpuidSpoof = true;
  bool enableBlockTranslation = true;
  std::size_t codeCacheSize = 64 * 1024 * 1024;
  std::size_t translationBlockMaxInstructions = 64;
  std::uint32_t hotPathThreshold = 8;
  std::wstring logFile = L"avx_emulator.log";
};

class Config {
 public:
  static Config& Instance();

  const EmulatorConfig& Get() const;
  void ReloadFromEnvironment();

 private:
  Config() = default;
  EmulatorConfig cfg_{};
};

}  // namespace avxemu
