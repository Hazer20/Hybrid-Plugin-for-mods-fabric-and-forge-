#include <Windows.h>

#include <filesystem>
#include <iostream>
#include <string>

namespace {

bool InjectDll(HANDLE process, const std::wstring& dllPath) {
  const auto bytes = (dllPath.size() + 1) * sizeof(wchar_t);
  void* remoteStr = VirtualAllocEx(process, nullptr, bytes, MEM_COMMIT | MEM_RESERVE, PAGE_READWRITE);
  if (!remoteStr) {
    return false;
  }

  if (!WriteProcessMemory(process, remoteStr, dllPath.c_str(), bytes, nullptr)) {
    VirtualFreeEx(process, remoteStr, 0, MEM_RELEASE);
    return false;
  }

  HMODULE kernel32 = GetModuleHandleW(L"kernel32.dll");
  auto loadLibrary = reinterpret_cast<LPTHREAD_START_ROUTINE>(GetProcAddress(kernel32, "LoadLibraryW"));
  HANDLE remoteThread = CreateRemoteThread(process, nullptr, 0, loadLibrary, remoteStr, 0, nullptr);
  if (!remoteThread) {
    VirtualFreeEx(process, remoteStr, 0, MEM_RELEASE);
    return false;
  }

  WaitForSingleObject(remoteThread, INFINITE);
  CloseHandle(remoteThread);
  VirtualFreeEx(process, remoteStr, 0, MEM_RELEASE);
  return true;
}

}  // namespace

int wmain(int argc, wchar_t** argv) {
  if (argc < 3) {
    std::wcout << L"Usage: loader.exe <path-to-game.exe> <path-to-avx_emulator.dll> [args...]\n";
    return 1;
  }

  std::wstring gamePath = argv[1];
  std::wstring dllPath = argv[2];

  std::wstring cmdLine = L"\"" + gamePath + L"\"";
  for (int i = 3; i < argc; ++i) {
    cmdLine += L" \"" + std::wstring(argv[i]) + L"\"";
  }

  STARTUPINFOW si{};
  si.cb = sizeof(si);
  PROCESS_INFORMATION pi{};

  if (!CreateProcessW(gamePath.c_str(), cmdLine.data(), nullptr, nullptr, FALSE, CREATE_SUSPENDED,
                      nullptr, std::filesystem::path(gamePath).parent_path().c_str(), &si, &pi)) {
    std::wcerr << L"CreateProcessW failed: " << GetLastError() << L"\n";
    return 2;
  }

  if (!InjectDll(pi.hProcess, dllPath)) {
    std::wcerr << L"DLL injection failed\n";
    TerminateProcess(pi.hProcess, 3);
    CloseHandle(pi.hThread);
    CloseHandle(pi.hProcess);
    return 3;
  }

  ResumeThread(pi.hThread);
  CloseHandle(pi.hThread);
  CloseHandle(pi.hProcess);

  std::wcout << L"Game launched through AVX emulator loader.\n";
  return 0;
}
