# 数组求和 JMH 实验

需要 JDK 17、Maven 3.9+，首次构建需要联网下载依赖。使用固定 JMH 1.37 便于复现，不表示它是最新版本。

```bash
cd examples/jmh
mvn clean package
# 只确认框架能启动，不用其数字得出性能结论
java -jar target/benchmarks.jar -f 1 -wi 1 -i 1 -w 100ms -r 100ms -p size=1024
# 正式实验，默认 3 fork、3 次预热、5 次测量
java -jar target/benchmarks.jar -prof gc -rf json -rff results.json
```

每个 invocation 对整个数组求和，单位 ns/op；不是单个元素成本。返回值由 JMH 消费。Setup 负责创建输入并验证两种结果一致。

记录 JDK、CPU、操作系统、JVM 参数、负载和全部 fork 的结果。默认运行需要数分钟。当前实际验证状态见 [验证记录](../../tutorial/验证记录.md)。
