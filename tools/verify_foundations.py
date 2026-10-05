#!/usr/bin/env python3
"""Verify beginner examples, including the bounded heap-failure experiment."""
from pathlib import Path
import argparse
import json
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]

def run(command, cwd, success=True):
    result = subprocess.run(command, cwd=cwd, text=True, capture_output=True, timeout=30)
    if success and result.returncode != 0:
        raise RuntimeError(f'{command}\n{result.stdout}\n{result.stderr}')
    return result

def block(path, language):
    return re.search(r'^```' + language + r'\n(.*?)^```', path.read_text(encoding='utf-8'), re.M | re.S).group(1)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--lesson', choices=[f'F{i:02d}' for i in range(1, 7)])
    args = parser.parse_args()
    lessons = json.loads((ROOT / 'tools/foundations.json').read_text(encoding='utf-8'))
    count = 0
    for lesson in lessons:
        if args.lesson and lesson['id'] != args.lesson:
            continue
        for language in ['en', 'zh']:
            article = ROOT / lesson[language]
            code = block(article, 'java')
            assert code == (ROOT / lesson['source']).read_text(encoding='utf-8'), article
            expected = block(article, 'text')
            name = lesson['class']
            with tempfile.TemporaryDirectory(prefix='jvm-foundation-') as directory:
                work = Path(directory)
                (work / (name + '.java')).write_text(code, encoding='utf-8')
                run(['javac', '--release', '17', '-encoding', 'UTF-8', name + '.java'], work)
                result = run(['java'] + lesson['vm_args'] + ['-cp', '.', name], work)
                assert result.stdout.replace('\r\n', '\n') == expected, (article, result.stdout)
                if lesson['id'] == 'F01':
                    result = run(['java', '-cp', '.', name, 'apple', 'pear'], work)
                    assert 'arguments=2' in result.stdout
                if lesson['id'] == 'F03':
                    bytecode = run(['javap', '-c', '-p', name], work).stdout
                    calculate = bytecode.split('static int calculate(int);', 1)[1].split('public static void main', 1)[0]
                    instructions = re.findall(r'^\s*\d+:\s+(\w+)', calculate, re.M)
                    assert instructions == ['iload_0', 'iconst_2', 'iadd', 'istore_1', 'iload_1', 'iconst_3', 'imul', 'ireturn']
                if lesson['id'] == 'F05':
                    # The guard must reject an out-of-scope heap before payload allocation.
                    rejected = run(['java', '-Xmx64m', '-cp', '.', name], work, success=False)
                    assert rejected.returncode != 0 and 'use -Xms16m -Xmx32m' in rejected.stderr
                if lesson['id'] == 'F06':
                    # Check the explicitly arranged schedule, not a probabilistic racy outcome.
                    for _ in range(2):
                        again = run(['java', '-cp', '.', name], work)
                        assert again.stdout == expected
            print(f'PASS {language} {lesson["id"]}')
            count += 1
    print(f'All {count} foundation editions passed, including bounded failure and bytecode checks.')

if __name__ == '__main__':
    main()
