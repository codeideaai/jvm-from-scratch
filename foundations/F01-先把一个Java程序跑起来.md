---
pagetitle: "F01 先把一个Java程序跑起来"
---

# F01 先把一个Java程序跑起来

[English](../en/foundations/F01-first-program.md) · [中文](F01-先把一个Java程序跑起来.md)

## 你现在需要会什么

会新建文本文件、保存文件、打开终端即可。先不要背 JVM 参数。本篇只解决三个问题：源码交给谁、生成的文件是什么、执行命令从哪里找它。若还不熟悉 Java 语法，先看下面的逐行解释，再复制程序运行。

## JDK、JVM、java、javac 分别是什么

JDK 是开发工具与运行环境的发行包。本教程用到其中的 javac 编译器、java 启动器和 javap 查看工具。JVM 是执行 class 中程序语义的虚拟机；HotSpot 是一种 JVM 实现。JRE 常被用来指运行环境这一组合，但不能因此假设每个现代 JDK 都带一个独立 jre 子目录。

源码文件是你编辑的 `.java` 文本；class 文件是编译器写出的二进制结构；一个正在运行的 Java 应用通常是操作系统中的一个进程。文件留在磁盘上，不代表程序仍在运行；进程结束也不会自动删除 class 文件。

先执行 `java -version` 和 `javac -version`。本系列基线是 JDK 17，两条命令都应可用。若 java 存在而 javac 不存在，先检查是否安装了完整 JDK，以及 PATH 指向哪里。macOS/Linux 可用 `command -v java`，Windows 的命令提示符可用 `where java`。不要在没有确定机器系统和安装位置之前照抄别人的 JAVA_HOME 路径。

## 用一个工作目录减少变量

建立一个空目录，把下面代码存成 `Foundation01.java`，终端切换到这个目录。文件名与 public 类名大小写一致；不要把它存成 `Foundation01.java.txt`。代码中没有 package，因此现在不必先理解包目录。

`main` 是这次启动的入口；`String[] args` 是传给应用的命令行参数数组；`int` 保存整数；`System.out.println` 输出一行。每条赋值语句末尾有分号，花括号围出类和方法的范围。乘法先计算，字符串拼接再把结果放到输出中。

## 完整代码

保存为 `Foundation01.java`.

```java
public class Foundation01 {
    public static void main(String[] args) {
        int count = 3;
        int price = 12;
        System.out.println("total=" + count * price);
        System.out.println("arguments=" + args.length);
    }
}
```

## 编译与运行

```bash
javac --release 17 -encoding UTF-8 Foundation01.java
java -cp . Foundation01
```

预期输出：

```text
total=36
arguments=0
```

## 把编译和运行分成两个动作

执行编译命令后，目录中应出现 `Foundation01.class`。编译通常没有成功提示，不能把“没有文字”当成失败。若终端出现错误，先读第一条编译错误的文件名、行号和消息，再检查对应代码。

运行命令中的 `-cp .` 表示从当前目录这一 classpath 根开始查找类。`Foundation01` 是类名，不带 `.class` 后缀。这里刻意使用“先 javac 再 java”的方式，虽然 JDK 支持某些源码直接启动形式，但分开更便于理解两步职责。

| 看到的现象 | 所处阶段 | 先检查 |
| --- | --- | --- |
| javac: command not found | 工具尚未启动 | JDK 与 PATH |
| cannot find symbol | 源码编译 | 拼写、变量声明、导入 |
| Could not find or load main class | 启动定位类 | 当前目录、-cp、类名 |
| UnsupportedClassVersionError | 类文件版本不兼容 | 编译与运行使用的 JDK |
| 程序进入 main 后抛异常 | 应用执行 | 首个相关业务栈帧与异常类型 |

**操作练习：**运行 `java -cp . Foundation01 apple pear`，第二行应该是 `arguments=2`。改动源码中的 price 但不重新编译，再运行相同 class 命令，结果仍来自旧 class；重新 javac 后才改变。这能证明你究竟在运行哪个产物。

**答案与出口检查：**为什么不能输入 `java Foundation01.class`？这里要求二进制类名，而不是 class 文件名。为什么 IDE 中能运行而终端不行？IDE 可能使用另一个 JDK、工作目录或 classpath。能够说明这三项差异之后，再进入主线第 01 篇。

---

[目录](../README.zh-CN.md) · [下一篇: 画清内存区域与对象引用](F02-画清内存区域与对象引用.md)
