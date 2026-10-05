---
pagetitle: "02 Primitive Types and Numeric Boundaries"
---

# 02 Primitive Types and Numeric Boundaries

[English](02-primitive-types.md) · [中文](../../tutorial/02-理解基本类型与数值边界.md)

Money, counters, and flags all involve primitive values. Successful compilation does not establish that the result lies within the business domain. This experiment combines integer overflow, narrowing conversions, and floating-point error to distinguish specified language behavior from the behavior an application actually needs.

## Build the mechanism model

An int is a signed 32-bit integer. Ordinary integer addition does not throw when it overflows. Use an explicitly checked operation such as Math.addExact when overflow must fail. Casting int to byte discards higher bits and interprets the remaining bits in the signed byte range.

The type of an expression matters before assignment. Multiplying two ints performs int arithmetic even when the result is subsequently assigned to a long. The wider destination cannot repair overflow that has already happened. Widen at least one operand before multiplication.

Floating-point values have finite precision. Many decimal fractions have no exact binary floating-point representation. Decimal money calculations can use BigDecimal with an explicit rounding policy. Construct it from a decimal string instead of first introducing error through a double.

## Complete code

Save this as `Demo02.java`. It uses only the JDK 17 standard library and compiles independently.

```java
import java.math.BigDecimal;
public class Demo02 {
    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE + 1);
        System.out.println((byte) 130);
        System.out.println(50_000 * 50_000);
        System.out.println(50_000L * 50_000);
        System.out.println(0.1 + 0.2 == 0.3);
        BigDecimal sum = new BigDecimal("0.1").add(new BigDecimal("0.2"));
        if (sum.compareTo(new BigDecimal("0.3")) != 0) throw new AssertionError(sum);
        System.out.println(sum);
        try {
            Math.addExact(Integer.MAX_VALUE, 1);
            throw new AssertionError("overflow accepted");
        } catch (ArithmeticException expected) {
            System.out.println("overflow rejected");
        }
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo02.java
java Demo02
```

Expected output (default verification mode):

```text
-2147483648
-126
-1794967296
2500000000
false
0.3
overflow rejected
```

## Interpreting the evidence

The third line overflows during int multiplication. The fourth uses long arithmetic because the left operand has an L suffix. This distinction arises before assignment to a destination variable. Disassembly shows imul and lmul respectively.

BigDecimal.compareTo compares numeric values, whereas equals also considers scale. For example, 1.0 and 1.00 are not equal according to equals. Decide whether representation precision is significant before using these values as Map keys.

The language values of boolean are true and false. The use of integer instructions for some boolean operations does not establish that every boolean field occupies four bytes. Field and array layouts are implementation questions.

## Exercises and self-checks

Compare `(long) (50_000 * 50_000)` with `(long) 50_000 * 50_000`, identifying where each conversion occurs. Then divide BigDecimal 1 by 3 and explain why omitting a rounding policy can cause ArithmeticException.

---

[Previous: From Source Code to Bytecode](01-source-to-bytecode.md) · [Contents](../../README.md) · [Next: Separating Class Loading from Initialization](03-class-initialization.md)
