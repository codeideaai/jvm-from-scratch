---
pagetitle: "17 编写最小JavaAgent观察对象尺寸"
---

# 17 编写最小JavaAgent观察对象尺寸

[English](../en/tutorial/17-java-agent.md) · [中文](17-编写最小JavaAgent观察对象尺寸.md)

监控工具可以在应用 main 之前获得 Instrumentation。我们先做一个只观察、不改字节码的 agent：确认启动顺序，并获取对象浅大小。这样可以把 agent 的加载机制与字节码重写的复杂性分开。

## 先建立机制模型

启动型 Java agent 通过 JAR 清单中的 Premain-Class 指定入口，命令行使用 -javaagent 加载。入口可接收 Instrumentation，用于安装转换器、查询对象大小等。动态附加使用另一套入口及能力条件，本篇不涉及。

getObjectSize 返回实现相关的近似浅大小：数组本身的空间属于它，数组元素引用的对象通常不包含在结果里。把一个对象及其所有可达对象去重后累计得到的图大小，是另一种分析任务。

真正的 ClassFileTransformer 必须处理类名、加载器、重转换条件与字节码验证。改变方法体与任意改变已加载类结构不是同一能力；不能假定 attach 一个 agent 就能随意增删字段。

## 完整代码

将以下内容保存为 `Demo17.java`。仅依赖 JDK 17 标准库，可独立编译。

```java
import java.lang.instrument.Instrumentation;
public class Demo17 {
    private static volatile Instrumentation instrumentation;
    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        System.out.println("agent ready");
    }
    public static void main(String[] args) {
        Instrumentation inst = instrumentation;
        if (inst == null) throw new IllegalStateException("run with -javaagent");
        long small = inst.getObjectSize(new byte[0]);
        long large = inst.getObjectSize(new byte[1024]);
        if (small <= 0 || large <= small) throw new AssertionError("unexpected sizes");
        System.out.println("positive shallow size=" + (small > 0));
        System.out.println("larger array=" + (large > small));
    }
}
```

## 编译与运行

```bash
javac --release 17 -encoding UTF-8 Demo17.java
printf 'Premain-Class: Demo17\n\n' > manifest.mf
jar --create --file demo17-agent.jar --manifest manifest.mf Demo17.class
java -javaagent:demo17-agent.jar Demo17
```

预期输出（普通验证模式）：

```text
agent ready
positive shallow size=true
larger array=true
```

## 解释结果与证据边界

程序不把对象大小硬编码为 16 或 24，因此不会把当前压缩指针和对齐设置误写成普适结论。你可以临时打印 small 与 large，再连同 JVM 选项记录观测值。

agent 与应用入口使用同一个类，是为了保持单文件可复制。实际工程往往拆成独立模块，并减少 premain 中的依赖，避免过早触发大量业务类加载。

这一章验证的是 premain 和尺寸查询，并未实现插桩。要把计时注入所有方法，还需要处理异常出口、递归调用、转换器自身依赖和重复转换。添加探针本身也可能改变内联和性能，诊断结果需要考虑观察开销。

## 动手修改与自测

去掉 -javaagent 再运行，应该明确失败，而不是返回虚假的零字节。随后比较 Object[] 的浅大小与其元素所指 byte[] 的大小，解释为什么仅增大元素数组不会改变外层引用数组长度。

---

[上一篇：把线程堆与JFR串成诊断流程](16-把线程堆与JFR串成诊断流程.md) · [目录](../README.zh-CN.md) · [下一篇：综合实战定位无界缓存增长](18-综合实战定位无界缓存增长.md)
