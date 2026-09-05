#!/usr/bin/env python3
import argparse
import collections
import hashlib
import os
import sys
import zipfile


def human(n):
    units = ['B', 'KiB', 'MiB', 'GiB']
    value = float(n)
    for unit in units:
        if value < 1024 or unit == units[-1]:
            return f"{value:.2f} {unit}"
        value /= 1024


def category(name):
    if name.startswith('lib/'):
        return 'lib'
    if name.startswith('res/'):
        return 'res'
    if name.startswith('assets/'):
        return 'assets'
    if name.startswith('META-INF/'):
        return 'META-INF'
    if name.endswith('.dex'):
        return 'dex'
    if name == 'resources.arsc':
        return 'resources.arsc'
    if name == 'AndroidManifest.xml':
        return 'manifest'
    return 'other'


def main():
    p = argparse.ArgumentParser(description='Read-only APK size audit')
    p.add_argument('apk')
    p.add_argument('--top', type=int, default=40)
    args = p.parse_args()

    if not os.path.isfile(args.apk):
        print(f"APK not found: {args.apk}", file=sys.stderr)
        return 2

    total_disk = os.path.getsize(args.apk)
    sha = hashlib.sha256()
    with open(args.apk, 'rb') as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b''):
            sha.update(chunk)

    by_cat = collections.Counter()
    by_abi = collections.Counter()
    by_ext = collections.Counter()
    rows = []

    with zipfile.ZipFile(args.apk, 'r') as z:
        for info in z.infolist():
            if info.is_dir():
                continue
            name = info.filename
            by_cat[category(name)] += info.compress_size
            if name.startswith('lib/'):
                parts = name.split('/')
                if len(parts) >= 3:
                    by_abi[parts[1]] += info.compress_size
            ext = os.path.splitext(name)[1].lower() or '<none>'
            by_ext[ext] += info.compress_size
            rows.append((info.compress_size, info.file_size, name))

    print('APO Slim APK Audit')
    print('==================')
    print(f'File: {args.apk}')
    print(f'APK size: {total_disk} bytes ({human(total_disk)})')
    print(f'SHA256: {sha.hexdigest()}')

    print('\nCompressed size by category:')
    for key, size in by_cat.most_common():
        print(f'  {key:16} {human(size):>12}')

    print('\nNative libraries by ABI:')
    if by_abi:
        for key, size in by_abi.most_common():
            print(f'  {key:16} {human(size):>12}')
    else:
        print('  <none>')

    print('\nLargest extensions:')
    for key, size in by_ext.most_common(20):
        print(f'  {key:16} {human(size):>12}')

    print(f'\nTop {args.top} largest entries (compressed):')
    for compressed, raw, name in sorted(rows, reverse=True)[:args.top]:
        print(f'  {human(compressed):>12}  raw={human(raw):>12}  {name}')

    return 0


if __name__ == '__main__':
    raise SystemExit(main())
