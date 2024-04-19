#!/usr/bin/env python3
import argparse
import os
import re
import subprocess
import fileinput
import sys

FILE_TO_UPDATE="app/build.gradle.kts"

class updateTag:
    def __init__(self, version, remote, force, dryrun, tag):
        self.version = version
        self.remote = remote
        self.force = force
        self.dryrun = dryrun
        self.tag = tag

    def run(self):
        version = self.version

    def check_version_format(self):
        pattern = re.compile(r'^v?\d+\.\d+\.\d+$')
        return pattern.match(self.version) is not None

    def check_tag_exists(self):
        try:
            subprocess.check_output(['git', 'rev-parse', self.tag], stderr=subprocess.STDOUT)
            return True
        except subprocess.CalledProcessError:
            return False

    def write_version(self):
        print(f'--- Updating version in {FILE_TO_UPDATE} to {self.version}')
        version_name_line = re.compile(r'(?P<ws>\s+)versionName\s*=\s*".*"')
        version_code_line = re.compile(r'(?P<ws>\s+)versionCode\s*=\s*(?P<code>\d+)')
        with fileinput.FileInput(FILE_TO_UPDATE, inplace = not self.dryrun) as f:
            for line in f:
                n = version_name_line.match(line)
                c = version_code_line.match(line)
                if n:
                    vnws = n.group('ws')
                    print(version_name_line.sub(f'{vnws}versionName = "{self.version}"', line), end='')
                elif c:
                    version_code = c.group('code')
                    vcws = c.group('ws')
                    print(version_code_line.sub(f'{vcws}versionCode = {int(version_code)+1}', line), end='')
                elif not self.dryrun:                    
                    print(line, end='')

    def run_command(self, args):
        print(f'--- Running command: {" ".join(args)}')
        if self.dryrun:
            return True
        else:
            subprocess.check_call(args)

    def check_version_to_commit(self):
        cmd = ['git', 'diff', '--cached', FILE_TO_UPDATE]
        return subprocess.check_output(cmd) != b''

    def commit_change(self):
        self.run_command(['git', 'add', FILE_TO_UPDATE])
        if not self.dryrun and self.check_version_to_commit():
            print('--- Version did not change, nothing to commit')
            return
        self.run_command(['git', 'commit', f'--message="chore: Version bump release {self.version}"'])

    def create_tag(self):
        cmd=['git', 'tag']
        if self.force:
            cmd.append('--force')
        cmd.append(self.tag)
        self.run_command(cmd)


    def push_change(self):
        cmd=['git', 'push']
        cmd.append(self.remote)
        if self.force:
            cmd.append('--force')
        cmd.append('--follow-tags')
        cmd.append(self.tag)
        self.run_command(cmd)

def main():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))

    parser = argparse.ArgumentParser(description='Create and push tag for new release')
    parser.add_argument('--push', metavar='REMOTE', required=False,
                        help='remote to push (e.g. "origin")')
    parser.add_argument('-n','--dryrun', action='store_true', required=False,
                        help='run with no changes')
    parser.add_argument('-f', '--force', action='store_true', required=False,
                        help='force creation of tag, even when already exists')
    parser.add_argument('version', metavar='VERSION',
                        help='version to release ("N.N.N" or "vN.N.N")')
    args = parser.parse_args()

    version = args.version

    version = version.lstrip('v')
    remote = args.push
    force = args.force
    dryrun = args.dryrun
    tag = updateTag(version, remote, force, dryrun, f'v{version}')
    if not tag.check_version_format():
        print(f'Error: version "{version}" is not in correct format,'
              ' expected "N.N.N" or "vN.N.N"', file=sys.stderr)
        sys.exit(1)

    if not force and tag.check_tag_exists():
        print(f'Error: tag "{tag.tag}" already exists', file=sys.stderr)
        sys.exit(1)

    tag.write_version()
    tag.commit_change()
    tag.create_tag()
    if remote:
        tag.push_change()
    print(f'--- Successfully released {tag.tag}')

if __name__ == '__main__':
    main()
