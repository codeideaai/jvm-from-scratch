#!/usr/bin/env python3
"""Compile both article editions independently and compare documented output."""
from pathlib import Path
import argparse
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]

def run(command, cwd):
    result = subprocess.run(command, cwd=cwd, text=True, capture_output=True, timeout=60)
    if result.returncode:
        raise RuntimeError(f'{command}\n{result.stdout}\n{result.stderr}')
    return result.stdout.replace('\r\n', '\n')

def blocks(article, language):
    text = article.read_text(encoding='utf-8')
    return re.findall(r'^```' + language + r'\n(.*?)^```', text, re.M | re.S)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--chapter', type=int, choices=range(1, 19))
    parser.add_argument('--sync', action='store_true', help='Sync examples from English; still check both editions')
    args = parser.parse_args()
    if args.chapter in (None, 15):
        english15 = next((ROOT / 'en/tutorial').glob('15-*.md'))
        for target, language, index in [
            ('examples/jmh/src/main/java/learning/SumBenchmark.java', 'java', 1),
            ('examples/jmh/pom.xml', 'xml', 0),
        ]:
            content = blocks(english15, language)[index]
            if args.sync:
                (ROOT / target).write_text(content, encoding='utf-8')
            for folder in ['en/tutorial', 'tutorial']:
                article = next((ROOT / folder).glob('15-*.md'))
                if blocks(article, language)[index] != (ROOT / target).read_text(encoding='utf-8'):
                    raise AssertionError(f'{article}: {target} differs')
    numbers = [args.chapter] if args.chapter else range(1, 19)
    count = 0
    for number in numbers:
        for language, folder in [('en', 'en/tutorial'), ('zh', 'tutorial')]:
            article = next((ROOT / folder).glob(f'{number:02d}-*.md'))
            code = blocks(article, 'java')[0]
            expected = blocks(article, 'text')[0]
            name = f'Demo{number:02d}'
            snapshot = ROOT / 'examples' / f'{name}.java'
            if args.sync and language == 'en':
                snapshot.write_text(code, encoding='utf-8')
            if snapshot.read_text(encoding='utf-8') != code:
                raise AssertionError(f'{article}: code differs from shared example')
            with tempfile.TemporaryDirectory(prefix=f'jvm-{language}-{number:02d}-') as directory:
                work = Path(directory)
                (work / f'{name}.java').write_text(code, encoding='utf-8')
                run(['javac', '--release', '17', '-encoding', 'UTF-8', f'{name}.java'], work)
                command = ['java']
                if number == 17:
                    (work / 'manifest.mf').write_text('Premain-Class: Demo17\n\n', encoding='utf-8')
                    run(['jar', '--create', '--file', 'agent.jar', '--manifest', 'manifest.mf', 'Demo17.class'], work)
                    command += ['-javaagent:agent.jar']
                actual = run(command + [name], work)
                if actual != expected:
                    raise AssertionError(f'{article}\nexpected: {expected!r}\nactual: {actual!r}')
            count += 1
            print(f'PASS {language} {number:02d}')
    print(f'All {count} selected article editions passed.')

if __name__ == '__main__':
    main()
