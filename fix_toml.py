import re

with open("gradle/libs.versions.toml", "r") as f:
    text = f.read()

if "desugar_jdk_libs" not in text:
    # insert version
    text = re.sub(r'(\[versions\]\n)', r'\1desugar_jdk_libs = "2.0.4"\n', text)
    # insert library
    text = re.sub(r'(\[libraries\]\n)', r'\1desugar-jdk-libs = { group = "com.android.tools", name = "desugar_jdk_libs", version.ref = "desugar_jdk_libs" }\n', text)

with open("gradle/libs.versions.toml", "w") as f:
    f.write(text)

with open("app/build.gradle.kts", "r") as f:
    build = f.read()

if "isCoreLibraryDesugaringEnabled = true" not in build:
    build = build.replace("compileOptions {", "compileOptions {\n    isCoreLibraryDesugaringEnabled = true")
    build = build.replace("dependencies {", "dependencies {\n  coreLibraryDesugaring(libs.desugar.jdk.libs)")

with open("app/build.gradle.kts", "w") as f:
    f.write(build)
