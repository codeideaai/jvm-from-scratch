---
pagetitle: "F01 Run Your First Java Program"
---

# F01 Run Your First Java Program

[English](F01-first-program.md) · [中文](../../foundations/F01-先把一个Java程序跑起来.md)

## What you need first

You only need to create a text file and open a terminal. Do not memorize VM flags yet. We will answer three questions: what consumes the source, what file it produces, and where execution finds that file. If Java syntax is unfamiliar, read the line-by-line explanation before running the program.

## JDK, JVM, java, and javac

A JDK distribution provides development tools and a runtime. We use its javac compiler, java launcher, and javap inspection tool. The JVM executes the semantics represented by class files; HotSpot is one implementation. JRE commonly refers to the runtime combination, but not every modern JDK has a separate jre subdirectory.

A `.java` file is editable text. A class file is structured binary output from compilation. A running Java application is typically an operating-system process. A file remaining on disk does not mean the program is running, and process termination does not delete its class files.

Run `java -version` and `javac -version`. Both should work with the JDK 17 baseline. If java works but javac does not, check whether you installed a complete JDK and which executable PATH selects. Use `command -v java` on macOS/Linux or `where java` in Windows Command Prompt. Do not copy another machine's JAVA_HOME without checking your system and installation location.

## Use a clean working directory

Create an empty directory and save the program as `Foundation01.java`. Change the terminal's directory to that location. The public class name and filename must match, including case. Avoid accidentally saving a `.java.txt` file. There is no package declaration yet, so package directories need not be understood first.

main is the entry point for this launch. `String[] args` holds application arguments. int stores integers, and System.out.println prints a line. Semicolons end the assignment statements; braces delimit the class and method. Multiplication computes the number before concatenation places it in the output.

## Complete code

Save as `Foundation01.java`.

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

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Foundation01.java
java -cp . Foundation01
```

Expected output:

```text
total=36
arguments=0
```

## Separate compilation from execution

After compilation, the directory should contain `Foundation01.class`. Successful compilation is usually silent. If errors appear, read the first error's filename, line number, and message before changing unrelated settings.

In the run command, `-cp .` means the current directory is a classpath root. Foundation01 is the class name without a `.class` suffix. We intentionally compile and run separately even though the JDK supports some source-launch modes: the separation makes each responsibility visible.

| Observation | Stage | First checks |
| --- | --- | --- |
| javac: command not found | Tool discovery | JDK installation and PATH |
| cannot find symbol | Source compilation | Spelling, declarations, imports |
| Could not find or load main class | Launch-time lookup | Working directory, -cp, class name |
| UnsupportedClassVersionError | Class-version compatibility | Compiler and runtime JDK versions |
| Exception after entering main | Application execution | Exception type and first relevant application frame |

**Exercise:** run `java -cp . Foundation01 apple pear`. The second line becomes `arguments=2`. Change price in the source but run the same class command without recompiling. The old class still supplies the result; running javac again changes the artifact. This establishes which file is actually executed.

**Answers and exit check:** `java Foundation01.class` is wrong here because the launcher expects a binary class name. An IDE may use a different JDK, working directory, or classpath than your terminal. Be able to explain those three differences before moving to main Chapter 01.

---

[Contents](../../README.md) · [Next: Draw Memory Areas and Object References](F02-memory-and-references.md)
