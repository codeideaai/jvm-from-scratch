# Examples

**English** · [中文](README.zh-CN.md)

Demo01–Demo18 match the complete code block in both article editions.

```bash
bash examples/run.sh 01
python3 tools/verify_article_code.py
```

The runner uses temporary compilation directories and cleans them up. Chapter 17 automatically packages an agent. Follow the article commands in a working directory if you want to retain class files for javap or observation logs.

The optional [JMH project](jmh/README.md) requires Maven and dependency downloads. The basic examples do not.

## Foundation lessons

The beginner route adds F01–F06 without renumbering the main course. Each has English and Chinese prose, one shared Java example, and a record in tools/foundations.json. Run `python3 tools/verify_foundations.py` or select `--lesson F05`. This check always uses F05's bounded heap flags and verifies rejection of an out-of-scope heap. Do not remove that guard while editing.

Before release, run both verify_article_code.py and verify_foundations.py, then check_links.py and check_bilingual.py --site. Adding lessons also requires language-map.json, the matching sidebar group, and a regenerated language-switch.js. Prefer adding worked steps and answered self-checks over introducing unexplained new terms.
