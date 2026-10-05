---
pagetitle: "F03 Trace Frames and Read a Class File"
---

# F03 Trace Frames and Read a Class File

[English](F03-read-bytecode.md) · [中文](../../foundations/F03-亲手推演栈帧与class文件.md)

## Do not read all of javap at once

Focus on calculate. The file-reading code in main only checks the class header; mastering every I/O API is not a prerequisite. x and y are local integers for this invocation. The operand stack temporarily holds values used in calculations. It cooperates with the local-variable table but is not the same structure.

For calculate(4), slot 0 initially holds x=4, slot 1 has not yet received y, and the operand stack is empty. A static method has no extra this parameter. Stacks below are written from bottom on the left to top on the right.

| Instruction | Operand stack before | Operand stack after | Local-variable effect |
| --- | --- | --- | --- |
| iload_0 | [] | [4] | Read slot 0 without deleting its value |
| iconst_2 | [4] | [4, 2] | None |
| iadd | [4, 2] | [6] | Consume two values and produce their sum |
| istore_1 | [6] | [] | Store 6 in slot 1, making y=6 |
| iload_1 | [] | [6] | Read y again |
| iconst_3 | [6] | [6, 3] | None |
| imul | [6, 3] | [18] | Replace operands with their product |
| ireturn | [18] | Invocation ends | Return 18 to the caller |

Load moves a local value onto the operand stack; store consumes a stack value and writes a local slot. These words do not mean loading or saving disk files. Calls establish separate invocation state rather than sharing one local-variable table across all methods.

## Four class-file terms

A **magic number** identifies the format. A **version** identifies the class-file format level. A **constant pool** is a table of numbered entries including names, descriptors, and symbolic references. A **Code attribute** holds a concrete method's bytecode and related information.

A constant-pool index such as `#12` is not an object address. The string intern pool and each class file's constant pool are also not interchangeable diagrams. The word constant appears at several abstraction levels.

## Complete code

Save as `Foundation03.java`.

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

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Foundation03.java
java -cp . Foundation03
```

Expected output:

```text
result=18
magic=CAFEBABE
major=61
```

## Compare the trace with a real class file

Run `javap -c -p Foundation03`, find calculate, and match the instructions to the table. In `javap -v -p Foundation03`, its descriptor is `(I)I`: the I inside parentheses is one int parameter, and the I outside is the int return type. It does not describe two parameters. An instance method's descriptor does not list this as an ordinary explicit parameter either.

For this compilation, `stack=2, locals=2` describes maximum operand-stack depth and local-variable slots. It does not mean two heap bytes are currently used. This calculation needs at most two int operands at once.

The header reader consumes four magic bytes, followed by minor and major versions. It ignores minor and checks major=61 because compilation explicitly uses `--release 17`. It is a header check, **not a complete class-file parser or bytecode verifier**.

## Loading, linking, and execution are separate

A disk file contains bytes and names. A loader finds the bytes, and the JVM establishes a runtime class representation. Linking includes checking structural and instruction constraints and connecting symbolic references to runtime targets. A portable class format does not permit arbitrary invalid bytecode to bypass verification.

Main Chapter 03 addresses initialization timing, and Chapter 04 explains loader-dependent identity. For now, distinguish records in a file from runtime classes and objects.

**Self-check and answers:** changing the return expression to `y + 3` should replace imul with iadd and change the result to 9; it does not change the class version. Making x an instance field introduces the receiver and getfield, so the original interpretation of iload_0 no longer applies. When experimenting, update the business assertion too; the repository verifier continues to check the unmodified example.

---

[Previous: Draw Memory Areas and Object References](F02-memory-and-references.md) · [Contents](../../README.md) · [Next: Understand GC through Reference Graphs](F04-gc-from-graphs.md)
