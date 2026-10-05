---
pagetitle: "F03 亲手推演栈帧与class文件"
---

# F03 亲手推演栈帧与class文件

[English](../en/foundations/F03-read-bytecode.md) · [中文](F03-亲手推演栈帧与class文件.md)

## 不要一次读完 javap 的全部输出

本篇只看 calculate 这个方法。main 中的文件读取是为了确认 class 的开头，不需要先学会所有输入输出 API。x、y 是本次方法调用中的两个局部整数；操作数栈则暂放计算时取出的值。两者配合，但不是同一张表。

设调用为 calculate(4)。进入方法时，局部变量槽 0 放 x=4，槽 1 尚未写入 y；操作数栈为空。这里是静态方法，没有额外的 this 参数。下面的“栈”均从左到右表示从底到顶。

| 指令 | 执行前操作数栈 | 执行后操作数栈 | 局部变量发生什么 |
| --- | --- | --- | --- |
| iload_0 | [] | [4] | 读取槽 0，不删除局部值 |
| iconst_2 | [4] | [4, 2] | 不变 |
| iadd | [4, 2] | [6] | 取出两个数，加完压回一个数 |
| istore_1 | [6] | [] | 把 6 存入槽 1，y=6 |
| iload_1 | [] | [6] | 再次读取 y |
| iconst_3 | [6] | [6, 3] | 不变 |
| imul | [6, 3] | [18] | 两个数换成一个乘积 |
| ireturn | [18] | 方法结束 | 把 18 返回调用者 |

这张表解释了 load 和 store 的方向：load 把局部值推入操作数栈，store 从操作数栈取值写入局部槽。它们不是从硬盘加载或保存文件。方法调用产生新的调用状态，并不让所有方法争用同一个局部变量表。

## class 文件先认识四个词

**魔数**是用于识别格式的固定标记；**版本号**说明 class 采用的格式版本；**常量池**是文件中的编号条目表，可以包含名称、描述符和符号引用等；**Code 属性**记录具体方法的字节码及相关信息。

看到 `#12` 之类的常量池索引，不要把它当成对象地址。看到字符串常量池，也不要把它与“每个 class 的常量池”画成完全相同的东西。相同的“常量”二字可能属于不同抽象层。

## 完整代码

保存为 `Foundation03.java`.

```java
import java.io.DataInputStream;
import java.io.InputStream;
public class Foundation03 {
    static int calculate(int x) {
        int y = x + 2;
        return y * 3;
    }
    public static void main(String[] args) throws Exception {
        int result = calculate(4);
        if (result != 18) throw new AssertionError(result);
        System.out.println("result=" + result);
        try (InputStream raw = Foundation03.class.getResourceAsStream("/Foundation03.class")) {
            if (raw == null) throw new IllegalStateException("class resource missing");
            DataInputStream in = new DataInputStream(raw);
            int magic = in.readInt();
            in.readUnsignedShort();
            int major = in.readUnsignedShort();
            if (magic != 0xCAFEBABE || major != 61) throw new AssertionError();
            System.out.printf("magic=%08X%n", magic);
            System.out.println("major=" + major);
        }
    }
}
```

## 编译与运行

```bash
javac --release 17 -encoding UTF-8 Foundation03.java
java -cp . Foundation03
```

预期输出：

```text
result=18
magic=CAFEBABE
major=61
```

## 从真实文件印证推演

运行 `javap -c -p Foundation03` 找到 calculate，按表格一条条对照。使用 `javap -v -p Foundation03` 可看到 descriptor 为 `(I)I`：括号内的 I 表示一个 int 参数，括号外 I 表示 int 返回值。它不是两个参数；实例方法描述符也不会把 this 写成普通显式参数。

输出中的 `stack=2, locals=2`（此方法在本编译方式下）分别表示最大操作数栈深度和局部变量槽数量，不是当前堆内存用了两字节。这个小计算最多同时需要两个 int 操作数。

文件头读取先取四字节魔数，再读 minor 和 major。程序跳过 minor，只检查 major=61，因为我们显式使用 `--release 17`。这段代码只检查文件开头，**不是完整 class 解析器，更不是字节码验证器**。

## 加载、链接、执行为什么还不是一件事

磁盘中只有字节与名称。加载器找到字节后，JVM 建立类的运行时表示；链接中的验证检查结构和指令是否符合要求，解析则把符号关系与运行时目标建立联系。不能因为 class 格式可跨平台，就认为错误字节码可以跳过校验随意执行。

主线 03 再讨论初始化何时执行，主线 04 再讨论加载器为何影响类型身份。现在只要能区分“文件里的记录”和“运行时中的对象/类”即可。

**自测与答案：**若把最后一行改成 `return y + 3`，期望 imul 变成 iadd，结果变成 9；不是改了 class 版本。若 x 改成实例字段，取值过程会涉及接收者与 getfield，不能继续照抄第一行 iload_0 的含义。自行修改后应同时修正业务断言，原验证器仍用于检查原始实验。

---

[上一篇: 画清内存区域与对象引用](F02-画清内存区域与对象引用.md) · [目录](../README.zh-CN.md) · [下一篇: 先看引用图再理解垃圾回收](F04-先看引用图再理解垃圾回收.md)
