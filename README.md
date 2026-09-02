# VoidEditor

Modern Android code editor app built with Kotlin and Jetpack Compose.

## Stack
- Kotlin 2.3.10 with AGP 9.1.0 built-in Kotlin support
- Jetpack Compose (Material 3, Compose Navigation)
- sora-editor 0.24.6 as a git submodule, compiled from source via includeBuild
- TextMate grammars for C, C++, Kotlin, Java, JSON and Markdown highlighting

## Features
- Code editor with syntax highlighting
- Git integration with graph view and blame
- Debugging support (GDB/LLDB)
- Remote development (SSH/SFTP)
- File watcher for external changes
- Command palette
- 38+ language support

## Build
APKs are built by GitHub Actions on every push to main. Download release and debug
APKs from the Actions tab artifacts.

Local build:

git submodule update --init --recursive
./gradlew assembleRelease

## Details
- Application id: com.voideditor
- Min SDK 28, Target SDK 36, Compile SDK 36
- Release builds are minified and resource-shrunk with R8
