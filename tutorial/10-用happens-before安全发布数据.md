---
pagetitle: "10 用happens-before安全发布数据"
---

# 10 用happens-before安全发布数据

[English](../en/tutorial/10-happens-before.md) · [中文](10-用happens-before安全发布数据.md)

工作线程写好了配置，另一个线程却未必能按预期看到它。Java 内存模型讨论的是跨线程读写允许呈现什么行为，不是简单地把每个变量分配给“主内存”和“工作内存”两个实体区域。

## 先建立机制模型

可见性、顺序性和原子性是不同问题。volatile 可以为特定读写建立同步关系，但不会自动把读取、加一、写回变成不可分割操作。需要原子计数时，可以使用 AtomicInteger 或锁。

本例使用一次性发布：生产者先写普通字段 payload，再写 volatile ready；消费者读到 ready 为 true 后再读 payload。这个协议建立发布之前的写入与后续读取之间的 happens-before 关系。它不是通用的可反复复用消息队列协议。

Thread.start 和 Thread.join 也有同步语义。做并发实验时，过早 join 生产者再读数据可能已消除了本来想研究的数据竞争；需要明确每个同步点到底在证明什么。

## 完整代码

将以下内容保存为 `Demo10.java`。仅依赖 JDK 17 标准库，可独立编译。

```java
public class Demo10 {
    static int payload;
    static volatile boolean ready;
    static volatile int observed;
    public static void main(String[] args) throws Exception {
        Thread reader = new Thread(() -> {
            long deadline = System.nanoTime() + 5_000_000_000L;
            while (!ready) {
                if (System.nanoTime() - deadline >= 0) return;
                Thread.onSpinWait();
            }
            observed = payload;
        });
        reader.start();
        payload = 42;
        ready = true;
        reader.join();
        if (observed != 42) throw new AssertionError("publication timed out or failed");
        System.out.println("observed=" + observed);
    }
}
```

## 编译与运行

```bash
javac --release 17 -encoding UTF-8 Demo10.java
java Demo10
```

预期输出（普通验证模式）：

```text
observed=42
```

## 解释结果与证据边界

消费者读取 payload 的时刻在其观察到 ready 之后。主线程在 join 之后检查 observed，则利用线程终止同步保证接收最终结果。这里 ready 的 volatile 负责业务发布，join 负责测试收尾，二者用途不同。

循环有截止时间，避免修改实验后无限挂起。但五秒截止不是操作系统调度的形式保证，极端机器负载可能让实验超时。并发正确性应由同步规则论证，运行成功只能说明本次执行符合预期。

删除 volatile 后，即使连续一万次成功，也不能证明数据竞争程序正确。允许出现的问题不一定在当前硬件与优化条件下显现。同步规则参见 [JLS 17.4](https://docs.oracle.com/javase/specs/jls/se17/html/jls-17.html#jls-17.4)。

## 把发布规则连成四个动作

先读 [F06 线程时序](../foundations/F06-把线程时序画出来再谈可见性.md)。下面 A、B 是发布者的动作，C、D 是读取者的动作：

| 动作 | 内容 | 和下一步的关系 |
| --- | --- | --- |
| A | 写 payload=42 | 同线程程序顺序，在 B 前 |
| B | 写 volatile ready=true | 与后续对 ready 的相应读取建立同步关系 |
| C | 读取 ready 并观察到 true | 同线程程序顺序，在 D 前 |
| D | 读取 payload | 可以沿 A→B→C→D 推导发布结果 |

happens-before 具有传递性，因此这条链条比“线程二大约晚了一点”更有力。本例只发布一次，ready 初值是 false，消费者退出循环时读到这次 true。若循环复用同一个标志而缺少确认、序号等协议，不能机械照抄这份推导。

observed 的检查又属于另一条链：读取线程完成后，main 的 join 返回，main 才检验结果。join 不会倒流时间去修复读取线程此前的非法访问；它只是这次收尾同步的一部分。

**自测与答案：**如果把 A 放到 B 之后，ready 的发布不再为那次后写入的 payload 建立原来的保证。即使当前机器上经常看到 42，也不能由原链条证明。把 volatile 改成 sleep 同样没有补回这条同步边。

## 动手修改与自测

试着写两个线程各增加十万次的 volatile int 计数器，再改为 AtomicInteger.incrementAndGet。不要断言错误版本每次必然出错；可靠的检查是正确版本在结束后必须等于二十万。解释 ready 置回 false 为什么不足以直接获得可靠的多轮通信。

---

[上一篇：用GC日志观察分配压力](09-用GC日志观察分配压力.md) · [目录](../README.zh-CN.md) · [下一篇：理解监视器与复合操作](11-理解监视器与复合操作.md)
