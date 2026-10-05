# 配套实验

Demo01—Demo18 与对应文章的第一个 Java 代码块一致，以文章为修改源。

```bash
bash examples/run.sh 01
python3 tools/verify_article_code.py
```

run.sh 使用临时目录编译，并自动清理。第 17 篇自动打包 agent。
想执行 javap 或观察日志时，请按文章命令在自己的实验目录编译，以便保留 class 文件。

[JMH 工程](jmh/README.md)是可选项，需要 Maven 和下载依赖。基础实验无需 Maven。

## 先修课维护

F01—F06 不改变主线编号。两种语言共用 examples/foundations 中的程序，并在 tools/foundations.json 记录运行方式。执行 `python3 tools/verify_foundations.py`；可用 `--lesson F05` 单独验证。F05 始终使用受限堆参数，且会检查错误配置被拒绝，修改时不要去掉保护条件。

发布前同时运行原主线与先修课验证器，再检查链接及双语渲染。新增篇目要同步语言配对与侧栏。内容维护优先补推导步骤和带答案的自测，避免继续引入没有解释的术语。
