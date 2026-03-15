#pragma once

#include "avx_emulator/common.h"

#include <fstream>

namespace avxemu {

class Logger {
 public:
  static Logger& Instance();

  void Initialize(std::wstring_view filePath, bool debugMode);
  void Shutdown();
  void Log(LogLevel level, std::string_view message);

 private:
  Logger() = default;
  std::mutex mutex_;
  std::ofstream file_;
  bool debugMode_ = false;
};

#define AVXEMU_LOG_TRACE(msg) ::avxemu::Logger::Instance().Log(::avxemu::LogLevel::Trace, (msg))
#define AVXEMU_LOG_DEBUG(msg) ::avxemu::Logger::Instance().Log(::avxemu::LogLevel::Debug, (msg))
#define AVXEMU_LOG_INFO(msg) ::avxemu::Logger::Instance().Log(::avxemu::LogLevel::Info, (msg))
#define AVXEMU_LOG_WARN(msg) ::avxemu::Logger::Instance().Log(::avxemu::LogLevel::Warn, (msg))
#define AVXEMU_LOG_ERROR(msg) ::avxemu::Logger::Instance().Log(::avxemu::LogLevel::Error, (msg))

}  // namespace avxemu
