# Build

## Prerequisites

* JDK 17+
* Gradle 8.3.0+

## Build Degbug package

```shell
./gradlew assembleDebug
```

## Build Release APK

Requires the following environment variables set:

* `SIGNING_KEY_STORE_PATH` path to the keystore file
* `SIGNING_STORE_PASSWORD` password for the keystore file
* `SIGNING_KEY_ALIAS` alias for the key in the keystore file
* `SIGNING_KEY_PASSWORD` password for the key in the keystore file

```shell
./gradlew assembleRelease
```

## Bump version

remove `--dryrun` to update `app/build.gradle.kts` by a patch version, commit, and push the version change to origin

bump version by choosing major, minor, or patch (defaults to patch)

```shell
python tag.py --dryrun 
```
