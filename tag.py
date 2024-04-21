#!/usr/bin/env python3
import argparse
import os
import re
import subprocess
import fileinput
import sys

FILE_TO_UPDATE = "app/build.gradle.kts"


class updateTag:
    def __init__(self, version: str, remote: str, force: bool, message: str, dryrun: bool):
        self.version = version
        self.remote = remote
        self.force = force
        self.message = message,
        self.dryrun = dryrun
        self.newVersion = ""

    def check_version_format(self):
        pattern = re.compile(r"major|minor|patch")
        return pattern.match(self.version) is not None

    def check_tag_exists(self):
        try:
            subprocess.check_output(
                ["git", "rev-parse", self.newVersion], stderr=subprocess.STDOUT
            )
            return True
        except subprocess.CalledProcessError:
            return False

    def write_version(self):
        print(f"--- Updating version in {FILE_TO_UPDATE} to {self.newVersion}")
        version_name_line = re.compile(r'(?P<ws>\s+)versionName\s*=\s*"(?P<version>.*)"')
        version_code_line = re.compile(r"(?P<ws>\s+)versionCode\s*=\s*(?P<code>\d+)")
        with fileinput.FileInput(FILE_TO_UPDATE, inplace=not self.dryrun) as f:
            for line in f:
                name_match = version_name_line.match(line)
                code_match = version_code_line.match(line)
                if name_match:
                    vnws = name_match.group("ws")
                    self.newVersion = self.incrementVersion(name_match.group("version"))
                    print(
                        version_name_line.sub(
                            f'{vnws}versionName = "{self.newVersion}"', line
                        ),
                        end="",
                    )
                elif code_match:
                    version_code = code_match.group("code")
                    vcws = code_match.group("ws")
                    print(
                        version_code_line.sub(
                            f"{vcws}versionCode = {int(version_code)+1}", line
                        ),
                        end="",
                    )
                elif not self.dryrun:
                    print(line, end="")

    def run_command(self, args):
        print(f'--- Running command: {" ".join(args)}')
        if self.dryrun:
            return True
        else:
            subprocess.check_call(args)

    def check_version_to_commit(self):
        cmd = ["git", "diff", "--cached", FILE_TO_UPDATE]
        return subprocess.check_output(cmd) != b""

    def commit_change(self):
        self.run_command(["git", "add", FILE_TO_UPDATE])
        if not self.dryrun and self.check_version_to_commit():
            print("--- Version did not change, nothing to commit")
            return
        self.run_command(
            ["git", "commit", f'--message="{self.commit_message()}"']
        )

    def create_tag(self):
        cmd = ["git", "tag"]
        if self.force:
            cmd.append("--force")
        cmd.append(self.tag())
        self.run_command(cmd)

    def push_change(self):
        cmd = ["git", "push"]
        cmd.append(self.remote)
        if self.force:
            cmd.append("--force")
        cmd.append("--follow-tags")
        cmd.append(self.tag())
        self.run_command(cmd)

    def incrementVersion(self, version):
        major, minor, patch = map(int, version.split('.'))
        if self.version == "major":
            major += 1
            minor = 0
            patch = 0
        if self.version == "minor":
            minor += 1
            patch = 0
        if self.version == "patch":
            patch += 1
        return f"{major}.{minor}.{patch}"

    def tag(self):
        return f"v{self.newVersion}"

    def commit_message(self):
        return f"{''.join(self.message)} {self.newVersion}"

def main():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))

    parser = argparse.ArgumentParser(description="Create and push tag for new release")
    parser.add_argument(
        "--push",
        metavar="REMOTE",
        required=False,
        default="origin",
        help='remote to push (e.g. "origin")',
    )
    parser.add_argument(
        "-m",
        "--message",
        metavar="MESSAGE",
        required=False,
        default='chore: Version bump release',
        help='message for commit (default: "chore: Version bump release")',
    )
    parser.add_argument(
        "-f",
        "--force",
        action="store_true",
        required=False,
        help="force creation of tag, even when already exists",
    )
    parser.add_argument(
        "--version",
        "-v",
        type=str,
        default="Patch",
        metavar="VERSION",
        help="version bump type (Major, Minor, Patch)",
    )
    parser.add_argument(
        "-n",
        "--dryrun",
        action="store_true",
        required=False,
        help="run with no changes",
    )
    args = parser.parse_args()

    version = str.lower(args.version)

    remote = args.push
    force = args.force
    message = args.message
    dryrun = args.dryrun
    tag = updateTag(version, remote, force, message, dryrun)
    if not tag.check_version_format():
        print(
            f'expected version bump type "Major", "Minor", or "Patch")',
            file=sys.stderr,
        )
        sys.exit(1)

    tag.write_version()
    tag.check_tag_exists()
    tag.commit_change()
    tag.create_tag()
    if remote:
        tag.push_change()
    print(f"--- Successfully released {tag.tag()}")

if __name__ == "__main__":
    main()
