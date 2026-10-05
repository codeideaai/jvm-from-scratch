# 一步步理解 Java 虚拟机

[English](README.md) · **中文**

从字节码到 GC、JIT 与诊断，用可运行实验建立证据。

面向有 Java 基础的读者，现共 26 篇：6 篇先修课、导读、18 篇主线实验、1 篇扩展。以 JDK 17 HotSpot 为基线。文章包含机制解释、完整代码、运行命令、预期输出、观察方法和自测题。

## JVM 新手从这里开始

先看[新手路线与术语表](tutorial/新手学习路线与补齐说明.md)。先修课补齐主线所需的前置概念，每篇都有逐步推演、完整实验和带答案的自测。

| 篇号 | 先修内容 |
| --- | --- |
| F01 | [先把一个Java程序跑起来](foundations/F01-先把一个Java程序跑起来.md) |
| F02 | [画清内存区域与对象引用](foundations/F02-画清内存区域与对象引用.md) |
| F03 | [亲手推演栈帧与class文件](foundations/F03-亲手推演栈帧与class文件.md) |
| F04 | [先看引用图再理解垃圾回收](foundations/F04-先看引用图再理解垃圾回收.md) |
| F05 | [分清内存溢出泄漏与GC压力](foundations/F05-分清内存溢出泄漏与GC压力.md) |
| F06 | [把线程时序画出来再谈可见性](foundations/F06-把线程时序画出来再谈可见性.md) |

## 开始阅读

| 篇号 | 内容 |
| --- | --- |
| 00 | [导读与学习路线](tutorial/00-导读与学习路线.md) |
| 01 | [从源码追到字节码](tutorial/01-从源码追到字节码.md) |
| 02 | [理解基本类型与数值边界](tutorial/02-理解基本类型与数值边界.md) |
| 03 | [分清类加载与初始化](tutorial/03-分清类加载与初始化.md) |
| 04 | [用类加载器隔离同名类型](tutorial/04-用类加载器隔离同名类型.md) |
| 05 | [区分重载与动态分派](tutorial/05-区分重载与动态分派.md) |
| 06 | [沿异常表理解资源关闭](tutorial/06-沿异常表理解资源关闭.md) |
| 07 | [连接反射方法句柄与Lambda](tutorial/07-连接反射方法句柄与Lambda.md) |
| 08 | [从对象引用走到垃圾回收根](tutorial/08-从对象引用走到垃圾回收根.md) |
| 09 | [用GC日志观察分配压力](tutorial/09-用GC日志观察分配压力.md) |
| 10 | [用happens-before安全发布数据](tutorial/10-用happens-before安全发布数据.md) |
| 11 | [理解监视器与复合操作](tutorial/11-理解监视器与复合操作.md) |
| 12 | [拆开泛型与编译器语法糖](tutorial/12-拆开泛型与编译器语法糖.md) |
| 13 | [观察即时编译与方法内联](tutorial/13-观察即时编译与方法内联.md) |
| 14 | [理解逃逸分析循环优化与向量化](tutorial/14-理解逃逸分析循环优化与向量化.md) |
| 15 | [建立可信的性能实验](tutorial/15-建立可信的性能实验.md) |
| 16 | [把线程堆与JFR串成诊断流程](tutorial/16-把线程堆与JFR串成诊断流程.md) |
| 17 | [编写最小JavaAgent观察对象尺寸](tutorial/17-编写最小JavaAgent观察对象尺寸.md) |
| 18 | [综合实战定位无界缓存增长](tutorial/18-综合实战定位无界缓存增长.md) |
| 19 | [扩展篇：编译期生成与本地执行](tutorial/19-扩展篇编译期生成与本地执行.md) |

## 直接运行与验证

在项目根目录执行，要求 JDK 17 和 Python 3.9+：

```bash
python3 tools/verify_article_code.py
python3 tools/verify_foundations.py
```

验证器直接提取中英文两版正文代码，在独立临时目录逐篇编译运行，并比对文中输出；同时检查 examples 源码是否与正文一致。第 17 篇会生成临时 agent JAR。它不依赖 Maven 或其他源码项目。

```bash
bash examples/run.sh 03
```

这条命令独立运行第 03 篇。可选的 JMH Maven 工程位于 [examples/jmh](examples/jmh/README.zh-CN.md)，不计入基础验证。

## 本地阅读站点

站点使用 Quarto 构建，支持左侧目录、页内目录、搜索和代码复制。安装 Quarto 后执行：

```bash
quarto preview
# 输出静态文件到 _site
quarto render
```

此项目尚未发布为在线网站，不预设远程仓库地址。[开发说明](DEVELOPMENT.zh-CN.md)记录目录、修改与验证方式。

## 资料与验证范围

- [验证记录](tutorial/验证记录.md)：真实运行环境、检查结果和未验证项目。
- [技术文档与版本说明](tutorial/技术文档与版本说明.md)：技术规范、版本边界与官方文档链接。

GC 时序、JIT 决策与性能数值按观测处理，不伪造跨平台固定结果。
