#!/usr/bin/env python3
from pathlib import Path
import re
from urllib.parse import unquote
root = Path(__file__).resolve().parents[1]
errors = []
count = 0
for pattern in ('*.md', '*.qmd'):
    for page in root.rglob(pattern):
        if any(x in page.parts for x in ('target', '_site', '.quarto')):
            continue
        for target in re.findall(r'\[[^\]]*\]\(([^)]+)\)', page.read_text(encoding='utf-8')):
            if re.match(r'^[a-zA-Z]+:', target) or target.startswith('#'):
                continue
            path = unquote(target.split('#', 1)[0].split('?', 1)[0])
            count += 1
            if not (page.parent / path).exists():
                errors.append(f'{page.relative_to(root)} -> {target}')
if errors:
    raise SystemExit('\n'.join(errors))
print(f'PASS {count} local Markdown links')
