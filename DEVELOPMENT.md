# Development

**English** · [中文](DEVELOPMENT.zh-CN.md)

## Structure and language routing

- index.qmd and README.md are the English entry points.
- en/tutorial contains English articles and appendices.
- zh/index.qmd and README.zh-CN.md are Chinese entry points.
- tutorial retains the original Chinese paths for backward compatibility.
- language-map.json pairs rendered pages; the header switch keeps the current chapter.
- examples contains code shared by both editions; examples/jmh is the optional Maven benchmark.
- tools contains executable checks. filters contains rendering and language-navigation code.

Each language has its own sidebar and document language. The homepage stays English regardless of browser preference. Every article also has ordinary counterpart links, so switching still works without JavaScript. The navbar links are enhanced to point to the current chapter. All URLs are relative so the site can be hosted under a repository subpath.

## Editing and verification

Update both article editions and their expected output. Match the complete code block in examples. Run:

```bash
python3 tools/verify_article_code.py
python3 tools/check_links.py
python3 tools/check_bilingual.py
```

The verifier compiles and executes both language editions independently in temporary directories. `--chapter 03` selects one experiment. `--sync` updates example snapshots from the English edition; it never silently overwrites the Chinese translation. Mismatched editions fail verification. JMH source and POM blocks are checked separately from its optional Maven build.

To add a chapter, update both sidebars and language-map.json, and regenerate filters/language-switch.js with `python3 tools/build_language_switch.py`. Run the bilingual checker after regeneration.

## Site preview

With Quarto installed:

```bash
quarto preview
quarto render
python3 tools/check_bilingual.py --site
```

The output starts at _site/index.html. Check English and Chinese landing pages, a paired article, previous/next navigation, mobile width, search, code copying, and diagrams. Site URL, repository URL, and deployment remain unconfigured until a real hosting target is chosen.

## Interpreting checks

GC and JIT logs are observations, not fixed timing assertions. JMH smoke verifies startup; meaningful performance reporting requires the full controlled measurement. Translation must not change numerical semantics or claim unperformed experiments.

## Foundation lessons

The beginner route adds F01–F06 without renumbering the main course. Each has English and Chinese prose, one shared Java example, and a record in tools/foundations.json. Run `python3 tools/verify_foundations.py` or select `--lesson F05`. This check always uses F05's bounded heap flags and verifies rejection of an out-of-scope heap. Do not remove that guard while editing.

Before release, run both verify_article_code.py and verify_foundations.py, then check_links.py and check_bilingual.py --site. Adding lessons also requires language-map.json, the matching sidebar group, and a regenerated language-switch.js. Prefer adding worked steps and answered self-checks over introducing unexplained new terms.
