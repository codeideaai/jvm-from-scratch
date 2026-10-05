#!/usr/bin/env python3
"""Check language pairs, code equivalence, and optionally rendered HTML links."""
from pathlib import Path
from html.parser import HTMLParser
from urllib.parse import urlsplit, unquote
import argparse
import json
import re

root = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser()
p.add_argument('--site', action='store_true')
args = p.parse_args()
pairs = json.loads((root / 'language-map.json').read_text(encoding='utf-8'))
assert pairs and pairs[0] == {'en': 'index.html', 'zh': 'zh/index.html'}
for language in ['en', 'zh']:
    assert len({pair[language] for pair in pairs}) == len(pairs), 'Duplicate language target'
for lesson in json.loads((root / 'tools/foundations.json').read_text(encoding='utf-8')):
    assert {lang: lesson[lang].replace('.md', '.html') for lang in ['en', 'zh']} in pairs
assert 'lang: en' in (root / '_quarto.yml').read_text()
assert 'sidebar: english' in (root / 'en/_metadata.yml').read_text()
assert 'lang: zh-CN' in (root / 'tutorial/_metadata.yml').read_text()
for pair in pairs[1:]:
    en = root / pair['en'].replace('.html', '.md')
    zh = root / pair['zh'].replace('.html', '.md')
    a, b = en.read_text(encoding='utf-8'), zh.read_text(encoding='utf-8')
    assert zh.name in a and en.name in b, f'Missing counterpart link: {pair}'
    for language in ['java', 'xml', 'text']:
        pattern = r'^```' + language + r'\n(.*?)^```'
        assert re.findall(pattern, a, re.M | re.S) == re.findall(pattern, b, re.M | re.S), f'{pair}: {language} differs'
assert json.dumps(pairs, ensure_ascii=False, indent=2) in (root / 'filters/language-switch.js').read_text(), 'Regenerate language-switch.js'
print(f'PASS {len(pairs)} language pairs and shared Java/XML/output blocks')

if args.site:
    site = root / '_site'
    class Page(HTMLParser):
        def __init__(self):
            super().__init__(); self.lang = None; self.links = []
        def handle_starttag(self, tag, attrs):
            attrs = dict(attrs)
            if tag == 'html': self.lang = attrs.get('lang')
            if tag == 'a' and attrs.get('href'): self.links.append(attrs['href'])
    errors = []
    for pair in pairs:
        for language in ['en', 'zh']:
            file = site / pair[language]
            assert file.is_file(), f'Missing output: {file}'
            page = Page(); page.feed(file.read_text(encoding='utf-8'))
            expected = 'en' if language == 'en' else 'zh-CN'
            assert page.lang == expected, (file, page.lang, expected)
            counterpart = site / pair['zh' if language == 'en' else 'en']
            resolved = []
            for href in page.links:
                url = urlsplit(href)
                if url.scheme or url.netloc or not url.path: continue
                path = unquote(url.path)
                target = (site / path.lstrip('/') if path.startswith('/') else file.parent / path).resolve()
                if path.endswith('/'): target = target / 'index.html'
                resolved.append(target)
                if not target.exists(): errors.append(f'{file.relative_to(site)} -> {href}')
            assert counterpart.resolve() in resolved, f'Missing rendered counterpart: {file}'
    if errors: raise SystemExit('\n'.join(errors))
    print(f'PASS {2 * len(pairs)} rendered pages: languages, counterpart links, and local link targets')
