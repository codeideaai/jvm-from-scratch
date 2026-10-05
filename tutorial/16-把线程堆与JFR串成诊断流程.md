---
pagetitle: "16 把线程堆与JFR串成诊断流程"
---

# 16 把线程堆与JFR串成诊断流程

[English](../en/tutorial/16-diagnostics-and-jfr.md) · [中文](16-把线程堆与JFR串成诊断流程.md)

服务变慢时直接扩大堆，可能完全没有碰到真正瓶颈。诊断的第一步是确定症状：CPU 忙、请求等待、分配压力大，还是本地内存增长。第二步才是选择能验证假设的证据。

## 先建立机制模型

线程栈回答线程正在执行或等待什么，堆信息回答对象和堆容量的问题，JFR 用事件把执行、分配、锁和时间联系起来。工具有不同开销与覆盖范围，不能用其中一张截图解释全部故障。

jcmd 连接目标 JVM 执行诊断命令，通常需要相同用户与适当的系统权限。容器 PID 命名空间和禁用 Attach 等配置会影响连接。本篇启动自己的进程，并用程序打印的 PID 作为目标，不扫描或操作其他业务服务。

类直方图展示类型聚合计数，无法直接解释每个对象由谁持有。需要进一步分析堆快照中的支配关系与引用链。堆转储可能带来较大暂停和磁盘占用，应先在受控实验中理解成本。

## 完整代码

将以下内容保存为 `Demo16.java`。仅依赖 JDK 17 标准库，可独立编译。

```java
import java.util.ArrayList;
import java.util.List;
public class Demo16 {
    static final List<byte[]> retained = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 16; i++) retained.add(new byte[64 * 1024]);
        System.out.println("blocks=" + retained.size());
        if (args.length > 0 && args[0].equals("--observe")) {
            System.out.println("pid=" + ProcessHandle.current().pid());
            Thread.sleep(60_000);
        }
        if (retained.size() != 16) throw new AssertionError();
    }
}
```

## 编译与运行

```bash
javac --release 17 -encoding UTF-8 Demo16.java
java Demo16
```

预期输出（普通验证模式）：

```text
blocks=16
```

## 解释结果与证据边界

默认运行立即结束，便于自动验证；观察模式保留约一分钟，输出 PID。打开第二个终端，把下面命令里的 PID 替换为该数字。先获取线程栈与堆概要，再启动十秒 JFR 记录。

```bash
java Demo16 --observe
# 在另一个终端执行；把 PID 替换成上述进程号
jcmd PID Thread.print
jcmd PID GC.heap_info
jcmd PID JFR.start name=learning settings=profile duration=10s filename=learning.jfr
# 等待记录结束后查看
jfr summary learning.jfr
```

本例主线程在 sleep，因此应看到定时等待。不要因为看到 TIMED_WAITING 就推断死锁。真实 CPU 问题需要在一段时间内多次采样，定位持续出现的热点调用；真实泄漏需要增长趋势与存活引用证据。

若研究本地内存，可在启动前加入 `-XX:NativeMemoryTracking=summary`，随后用 `jcmd PID VM.native_memory summary`。它跟踪的是 HotSpot 支持的本地分配类别，不是所有第三方本地库内存的完整审计。

## 拿到线程栈后，第一眼究竟看哪里

已有实验的关键片段如下，省略了会变化的地址、PID 与时长：

```log
"main"
   java.lang.Thread.State: TIMED_WAITING (sleeping)
    at java.lang.Thread.sleep(java.base@17.0.20/Native Method)
    at Demo16.main(Demo16.java:10)
```

先找线程名，再读状态，接着从顶部向下找第一处能与你的源码对应的位置。这里是 main 主动 sleep，符合观察窗口设计。`Native Method` 只说明这个栈帧的方法实现与本地代码有关，不等于发生了 JNI 崩溃。

| 原始问题 | 先收集什么 | 下一步怎样判断 |
| --- | --- | --- |
| CPU 持续很高 | 时间窗口内的多次采样、CPU 指标、JFR | 是否总在相同热点运行，是否符合请求量 |
| 请求一直不返回 | 多线程栈与持有/等待的资源 | 是否存在锁环，还是正常 I/O 或条件等待 |
| 回收后堆基线持续增长 | GC 趋势、对象分布，必要时引用链 | 谁持续保留对象，数据是否仍有业务用途 |
| 堆不大但进程内存涨 | 线程数、本地内存、系统数据 | 不要只盯着 Xmx 和 Java 对象数量 |

看到很多 RUNNABLE 也不能单独断言 CPU 已满，看到很多 WAITING 也不能单独断言死锁。状态是线索，还需和时间、负载、资源关系交叉验证。

## 一份能复查的诊断笔记

按“现象与时间窗口 → 假设 → 采集命令与参数 → 关键证据 → 一次只改一项 → 同负载复测”记录。假设为缓存保留时，应看到回收后仍存活的对象及其持有路径；如果证据指向计算热点，就要修正假设，而不是继续扩大堆。

**检查理解：**本例 main 睡眠，给它增加堆能让它提前醒来吗？不能，等待行为由代码决定。把采集过程运行成功当成故障诊断成功了吗？没有，工具成功只是拿到了证据，解释证据才是接下来的工作。

## 动手修改与自测

将观察窗口内的 sleep 改为有终止条件的计算循环，比较线程栈和 JFR 样本。然后保持计算不变，只增加 retained 的有界容量，判断哪些指标改变。不要同时改变负载、堆容量和采样配置，否则很难解释原因。

---

[上一篇：建立可信的性能实验](15-建立可信的性能实验.md) · [目录](../README.zh-CN.md) · [下一篇：编写最小JavaAgent观察对象尺寸](17-编写最小JavaAgent观察对象尺寸.md)
