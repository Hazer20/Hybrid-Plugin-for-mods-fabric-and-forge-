#!/usr/bin/env bash
# Профиль запуска Paper/Folia для ПК с 8 ГБ RAM (Linux)
# Важно: не ставить Xmx слишком большим, иначе упираемся в native memory.

JAVA_OPTS="-Xms2G -Xmx4G \
-XX:+UseG1GC -XX:MaxGCPauseMillis=150 \
-XX:+ParallelRefProcEnabled -XX:+DisableExplicitGC \
-XX:+AlwaysActAsServerClassMachine -XX:+PerfDisableSharedMem \
-XX:MaxMetaspaceSize=384M -XX:MaxDirectMemorySize=512M \
-XX:ReservedCodeCacheSize=256M -Xss512k -Dfile.encoding=UTF-8"

exec java ${JAVA_OPTS} -jar paper.jar --nogui
