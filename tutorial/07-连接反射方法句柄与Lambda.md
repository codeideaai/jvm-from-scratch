---
pagetitle: "07 连接反射方法句柄与Lambda"
---

# 07 连接反射方法句柄与Lambda

[English](../en/tutorial/07-reflection-method-handles-lambdas.md) · [中文](07-连接反射方法句柄与Lambda.md)

框架根据配置调用方法，Lambda 则把行为作为值传递。它们都涉及间接调用，却不是同一个实现层次。本篇对同一个加法函数使用反射、方法句柄和 Lambda，先建立功能对应关系，再讨论哪些现象不能由这段程序证明。

## 先建立机制模型

反射 Method 描述方法元数据，invoke 使用 Object 风格的参数和返回值，基本类型需要适配。MethodHandle 带有具体 MethodType，invokeExact 要求调用点类型精确匹配；invoke 可以进行受规则约束的适配。

javac 通常使用 invokedynamic 表达 Lambda 的创建调用点，BootstrapMethods 指明链接方式。invokedynamic 是可编程链接机制，不等于“每次调用都重新反射查找”。同样，Lambda 不保证每次求值生成新对象，也不承诺是一个固定命名的匿名内部类。

本篇只比较行为，不比较速度。反射对象是否被缓存、调用点能否内联、参数形态以及 JDK 版本都可能影响性能。旧版本的调用次数阈值不是 Java 语言规范保证。

## 完整代码

将以下内容保存为 `Demo07.java`。仅依赖 JDK 17 标准库，可独立编译。

```java
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.IntBinaryOperator;
public class Demo07 {
    public static int add(int a, int b) { return a + b; }
    public static int fail() { throw new IllegalArgumentException("business"); }
    public static void main(String[] args) throws Throwable {
        Method method = Demo07.class.getMethod("add", int.class, int.class);
        MethodHandle handle = MethodHandles.lookup().findStatic(
            Demo07.class, "add", MethodType.methodType(int.class, int.class, int.class));
        IntBinaryOperator lambda = Demo07::add;
        System.out.println("reflection=" + method.invoke(null, 2, 3));
        System.out.println("handle=" + (int) handle.invokeExact(2, 3));
        System.out.println("lambda=" + lambda.applyAsInt(2, 3));
        try {
            Demo07.class.getMethod("fail").invoke(null);
            throw new AssertionError("exception lost");
        } catch (InvocationTargetException e) {
            if (!(e.getCause() instanceof IllegalArgumentException)) throw e;
            System.out.println("cause=" + e.getCause().getMessage());
        }
    }
}
```

## 编译与运行

```bash
javac --release 17 -encoding UTF-8 Demo07.java
java Demo07
```

预期输出（普通验证模式）：

```text
reflection=5
handle=5
lambda=5
cause=business
```

## 解释结果与证据边界

invokeExact 前的 int 转换参与确定调用点返回类型，不能随意删掉再期待 JVM 自动把所有类型补齐。错误的签名会导致 WrongMethodTypeException，这与目标函数本身抛出的业务异常不同。

反射调用把目标异常包在 InvocationTargetException 中；框架如果统一包装异常，应该保留 cause。方法句柄调用不会用这一反射包装类型包住目标异常。

执行 `javap -v -p Demo07` 搜索 InvokeDynamic 和 BootstrapMethods，区分 LambdaMetafactory 与字符串拼接所用的引导项。看到 invokedynamic 并不足以断定该位置一定来自 Lambda。反射机制在 JDK 18 有实现调整，见 [JEP 416](https://openjdk.org/jeps/416)。

补充观察命令，在本篇 class 文件所在目录执行：

```bash
javap -v -p Demo07
```

## 第一次读，先把三个层次分开

先用普通静态调用理解 `add(2, 3)` 会得到 5。反射的 getMethod 是按“方法名 + 参数类型”查找一个描述对象，invoke 才是调用；lookup/findStatic 建立有精确类型的句柄，invokeExact 再调用；`Demo07::add` 用来生成符合 IntBinaryOperator 接口的行为值。这三条路径的共同点是业务函数，不同点是如何表示和连接调用。

| 容易混淆的两步 | 第一步 | 第二步 |
| --- | --- | --- |
| 反射 | 查找、保存 Method | 使用 Method 调用目标 |
| 方法句柄 | 查找并确定 MethodType | 按调用点类型调用 |
| Lambda/方法引用 | 建立函数式接口值 | 调用接口方法执行行为 |

为什么静态反射 invoke 的接收者参数是 null？静态 add 不需要一个 Demo07 实例接收者。为什么返回 5 不能证明反射和普通调用一样快？这里只验证了功能；元数据查找、适配和优化机会尚未做受控测量。

建议第一遍只掌握反射查找与异常包装，把 invokedynamic 和 BootstrapMethods 当作第二遍内容。这样不会因为一个术语不懂而卡住后面的对象和 GC 主线。

## 动手修改与自测

把 invokeExact 的第一个参数改为 2L，观察精确类型检查。再改用 handle.asType 显式声明适配目标，分析哪些转换允许、哪些不允许。不要把一次运行耗时用作反射与 Lambda 的性能排名。

---

[上一篇：沿异常表理解资源关闭](06-沿异常表理解资源关闭.md) · [目录](../README.zh-CN.md) · [下一篇：从对象引用走到垃圾回收根](08-从对象引用走到垃圾回收根.md)
