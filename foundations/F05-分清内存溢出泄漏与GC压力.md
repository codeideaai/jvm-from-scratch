---
pagetitle: "F05 分清内存溢出泄漏与GC压力"
---

# F05 分清内存溢出泄漏与GC压力

[English](../en/foundations/F05-memory-failures.md) · [中文](F05-分清内存溢出泄漏与GC压力.md)

## 三种现象先不要混叫“内存泄漏”

**内存不足/溢出**是一次资源申请无法满足的结果。**泄漏**是程序继续持有已经不需要的数据，使它无法及时成为回收候选的一类原因。**GC 压力**是大量分配与回收工作消耗时间或资源的现象。三者有关系，但不能相互替换。

程序真的需要保存一份大数据，而配置的堆过小，可能 OOM 却没有泄漏；无界缓存可能先泄漏很久，尚未达到 OOM；大量短命对象可能频繁 GC，但回收后存活量仍稳定。

## 在小进程里观察一次堆申请失败

本篇与普通实验不同，**必须使用下方带堆上限的命令**。它只在新启动的 Java 进程中运行，最大 Java 堆 32 MiB；代码还检查堆上限，拒绝在默认大堆下直接执行。32 MiB 不包含进程全部原生内存，但不会为业务对象请求数 GB 的 Java 堆。

程序尝试保留至多 128 个 1 MiB 数组，申请次数有界。预先创建的列表持续持有已分配数组，所以回收器不能随意丢掉这些仍可达的值。在到达尝试上限前，堆应无法继续满足申请。本例显式选 Serial GC，减少巨型对象路径等与核心问题无关的变量。

catch 中清空列表，是为了让这个短实验能输出观察结果并结束，**不是建议生产程序在 OOM 后捕获错误继续提供完整服务**。真实应用可能已经处于无法可靠分配日志对象、响应对象或清理资源的状态。

## 完整代码

保存为 `Foundation05.java`.

```java
import java.util.ArrayList;
import java.util.List;
public class Foundation05 {
    public static void main(String[] args) {
        long max = Runtime.getRuntime().maxMemory();
        if (max < 16L * 1024 * 1024 || max > 40L * 1024 * 1024) {
            throw new IllegalStateException("use -Xms16m -Xmx32m -XX:+UseSerialGC");
        }
        List<byte[]> retained = new ArrayList<>(128);
        System.out.println("heap guard passed=true");
        try {
            for (int i = 0; i < 128; i++) retained.add(new byte[1024 * 1024]);
            throw new AssertionError("small heap unexpectedly held all payloads");
        } catch (OutOfMemoryError expected) {
            retained.clear();
            System.out.println("failure=OutOfMemoryError");
            System.out.println("references cleared=" + retained.isEmpty());
        }
    }
}
```

## 编译与运行

```bash
javac --release 17 -encoding UTF-8 Foundation05.java
java -Xms16m -Xmx32m -XX:+UseSerialGC -cp . Foundation05
```

预期输出：

```text
heap guard passed=true
failure=OutOfMemoryError
references cleared=true
```

## 逐个解释参数与输出

`-Xms16m` 指定初始堆大小配置，`-Xmx32m` 限制最大堆。小写 m 在这些参数中使用二进制数量级：1 MiB=1024×1024 字节。线程栈用的是另一类参数，例如 -Xss；增加 Xmx 不会把无限递归自动修好。

第一行表示代码通过了上限检查；第二行表示尝试捕获到了 OutOfMemoryError；第三行只表示列表已经不持有元素，**不表示堆占用已经立刻归零**。不要断言恰好第几个数组失败：对象头、列表存储、可用堆与实现配置都会影响边界。

| 症状或消息 | 初步范围 | 下一步证据 |
| --- | --- | --- |
| OutOfMemoryError: Java heap space | Java 堆分配不足 | 堆参数、回收后存活趋势、持有关系 |
| OutOfMemoryError: Metaspace | 类元数据相关空间 | 类加载数量、类加载器是否被保留、相应限制 |
| StackOverflowError | 当前线程调用深度/栈空间 | 重复调用栈、递归退出条件 |
| unable to create native thread | 线程创建与本地资源限制 | 线程数、操作系统限制、进程本地内存 |
| 容器直接杀死进程，未见 Java 异常 | 可能为系统或容器限额等外部原因 | 退出原因、容器事件、系统记录 |

这些是排查入口，不是看到一段字符串就完成诊断。不要将所有错误都归结为“调大堆”。

## 读 GC 日志时先做一道算术题

主线 09 的已有实测日志包含 `14M->1M(32M) 0.784ms`。14M 是该行汇总的回收前堆使用量，1M 是之后的使用量，32M 是该时刻报告的堆容量，0.784ms 是这次暂停时长。约 13M 的占用下降不等于程序从启动起只分配过 13M，也不代表每次都能下降这么多。

连续多次回收后基线持续增长，才值得继续调查保留对象；一张瞬时截图无法单独证明泄漏。主线 18 再用无界缓存与有界缓存做同负载对照。

**练习与答案：**将上限保持 32 MiB，只保留 4 个数组，应该改写为“正常完成”的检查，不能继续期待 OOM。保持原例却只把命令改成 -Xmx512m，会被 guard 拒绝，这是刻意的保护条件。泄漏定义依赖“是否仍有业务用途”，不能只靠对象有引用就宣判。

---

[上一篇: 先看引用图再理解垃圾回收](F04-先看引用图再理解垃圾回收.md) · [目录](../README.zh-CN.md) · [下一篇: 把线程时序画出来再谈可见性](F06-把线程时序画出来再谈可见性.md)
