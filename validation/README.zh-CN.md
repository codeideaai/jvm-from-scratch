# 实验原始记录

2026-10-05 在 macOS arm64、Homebrew OpenJDK 17.0.20 上执行。日志只记录本教程自行启动的进程。

- gc.txt：Demo09 的 G1 日志；时间和次数仅属于本次运行。
- jit.txt：Demo13 的编译与内联日志。
- ea-on.txt、ea-off.txt：Demo14 两种配置的 GC 日志，不能单凭这些日志证明具体机器码。
- bytecode-checks.txt：正文关键字节码特征及 agent 失败路径检查。
- Thread.print.txt、GC.heap_info.txt：Demo16 观察进程的数据。
- JFR.start.txt、jfr-summary.txt：1 秒 smoke 记录的启动和摘要；正文教学命令采用 10 秒。
- jmh-smoke.txt：JMH 启动检查；测量窗口过短，数据不能用于性能排名。

PID、路径和时间是本次环境中的观测值，不属于期望输出。JFR 二进制文件和构建产物不包含在交付源码中。
