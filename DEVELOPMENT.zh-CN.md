# 开发与维护

[English](DEVELOPMENT.md) · **中文**

## 目录约定

- tutorial：按序正文与附录，文章是实验代码的主来源。
- examples：正文中的可运行代码快照；jmh 为可选 Maven 工程。
- tools：从正文提取并验证代码，以及检查本地 Markdown 链接。
- _quarto.yml、index.qmd、styles.css、filters：阅读站点配置。

## 修改顺序

先同步修改英文 en/tutorial 和中文 tutorial 的完整 Java 代码块及预期输出，再同步 examples 中同名文件。执行 `python3 tools/verify_article_code.py` 与 `python3 tools/check_links.py`。需要重新生成快照时可用 `python3 tools/verify_article_code.py --sync`，该选项以英文为源同步 examples，仍会检查两版一致性，不会自动覆盖中文。

验证使用临时目录，不污染源码；从文中提取预期结果，不维护第二份手写输出快照。通过 `--chapter 03` 单独检查一篇。验证失败应修复代码或明确修正不成立的预期，不能直接将失败输出批量覆盖为“正确结果”。

## 站点

安装 Quarto 后，执行 `quarto preview` 或 `quarto render`。HTML 入口为 `_site/index.html`。发布目标未配置，需要有实际托管地址后再加入 site-url、repo-url 与发布工作流。

Markdown 保留仓库内导航。Lua 过滤器在 HTML 渲染时转换文章链接和返回目录链接；附属文档以项目资源提供。发布前应实际检查首页、文章跳转、搜索、移动宽度和代码块。

## 验证分层

基础检查只依赖 JDK 17 与 Python 3.9+。GC/JIT 补充实验是实现观察，不把日志时间或回收次数写成固定断言。JMH 构建与 smoke 单独记录；正式性能报告还需独立 fork、多次测量和环境信息。不要把 smoke 数字发布成比较结论。

## 双语站点

英文首页为 index.qmd，中文首页为 zh/index.qmd。每篇均可切换到另一语言的对应章节，原中文路径不变。language-map.json 管理配对；修改后执行 `python3 tools/build_language_switch.py`，再执行 `python3 tools/check_bilingual.py --site` 检查已渲染站点。

## 先修课维护

F01—F06 不改变主线编号。两种语言共用 examples/foundations 中的程序，并在 tools/foundations.json 记录运行方式。执行 `python3 tools/verify_foundations.py`；可用 `--lesson F05` 单独验证。F05 始终使用受限堆参数，且会检查错误配置被拒绝，修改时不要去掉保护条件。

发布前同时运行原主线与先修课验证器，再检查链接及双语渲染。新增篇目要同步语言配对与侧栏。内容维护优先补推导步骤和带答案的自测，避免继续引入没有解释的术语。
