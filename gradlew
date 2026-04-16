#!/bin/sh
##############################################################################
# Self-bootstrapping Gradle wrapper (no pre-built gradle-wrapper.jar needed)
# Works on: AndroidIDE on Android, Linux, macOS
# Requires: java, python3 (used for download + unzip if curl/unzip unavailable)
##############################################################################
set -e

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
PROPS="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

# ── Read distributionUrl from properties ─────────────────────────────────
if [ ! -f "$PROPS" ]; then
  echo "ERROR: $PROPS not found" >&2; exit 1
fi
DIST_URL=$(grep '^distributionUrl=' "$PROPS" | head -1 | cut -d= -f2- | tr -d '\r' | sed 's/\\:/:/g')
if [ -z "$DIST_URL" ]; then
  echo "ERROR: distributionUrl not found in $PROPS" >&2; exit 1
fi

# ── Compute cache directory ───────────────────────────────────────────────
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DISTS_DIR="$GRADLE_USER_HOME/wrapper/dists"
# dist name = filename without extension
DIST_FILENAME=$(basename "$DIST_URL")
DIST_NAME="${DIST_FILENAME%.zip}"
DIST_DIR="$DISTS_DIR/$DIST_NAME"

# ── Download & extract if not cached ─────────────────────────────────────
if [ ! -d "$DIST_DIR/$DIST_NAME" ] && [ ! -f "$DIST_DIR/gradle" ] && \
   [ -z "$(find "$DIST_DIR" -name 'gradle' -type f 2>/dev/null | head -1)" ]; then

  echo "Downloading Gradle: $DIST_URL"
  mkdir -p "$DIST_DIR"
  ZIP="$DIST_DIR/$DIST_FILENAME"

  # Download using best available tool
  if command -v curl >/dev/null 2>&1; then
    curl -fL --progress-bar -o "$ZIP" "$DIST_URL" || { echo "curl failed" >&2; exit 1; }
  elif command -v wget >/dev/null 2>&1; then
    wget -q --show-progress -O "$ZIP" "$DIST_URL" || { echo "wget failed" >&2; exit 1; }
  else
    python3 - "$DIST_URL" "$ZIP" << 'PYEOF'
import sys, urllib.request, os
url, out = sys.argv[1], sys.argv[2]
print("Downloading (python3):", url)
def progress(count, block, total):
    pct = min(100, count*block*100//total) if total>0 else 0
    print(f"\r  {pct}%", end="", flush=True)
urllib.request.urlretrieve(url, out, reporthook=progress)
print()
PYEOF
  fi

  echo "Extracting $DIST_FILENAME..."
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$ZIP" -d "$DIST_DIR"
  else
    python3 -c "
import zipfile, sys
with zipfile.ZipFile('$ZIP') as z: z.extractall('$DIST_DIR')
print('Extracted.')
"
  fi
  rm -f "$ZIP"
  echo "Gradle ready."
fi

# ── Find gradle-launcher jar ──────────────────────────────────────────────
GRADLE_HOME=$(find "$DIST_DIR" -maxdepth 2 -name "gradle-launcher-*.jar" 2>/dev/null | head -1 | xargs dirname | xargs dirname 2>/dev/null)
if [ -z "$GRADLE_HOME" ]; then
  GRADLE_HOME=$(find "$DIST_DIR" -maxdepth 2 -type d -name "lib" 2>/dev/null | head -1 | xargs dirname 2>/dev/null)
fi
if [ -z "$GRADLE_HOME" ]; then
  echo "ERROR: Could not find Gradle installation in $DIST_DIR" >&2; exit 1
fi

LAUNCHER_JAR=$(find "$GRADLE_HOME/lib" -name "gradle-launcher-*.jar" 2>/dev/null | head -1)
if [ -z "$LAUNCHER_JAR" ]; then
  echo "ERROR: gradle-launcher jar not found in $GRADLE_HOME/lib" >&2; exit 1
fi

# ── Find JAVA_HOME / java ─────────────────────────────────────────────────
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
  JAVA_CMD="java"
else
  echo "ERROR: java not found. Set JAVA_HOME or add java to PATH." >&2; exit 1
fi

# ── Build classpath: launcher + all lib jars ──────────────────────────────
CP="$LAUNCHER_JAR"
for j in "$GRADLE_HOME/lib"/*.jar; do
  [ "$j" = "$LAUNCHER_JAR" ] && continue
  CP="$CP:$j"
done

# ── Set GRADLE_OPTS defaults ──────────────────────────────────────────────
DEFAULT_JVM_OPTS="-Xmx1536m -Xms256m -Dfile.encoding=UTF-8"
GRADLE_OPTS="${GRADLE_OPTS:-$DEFAULT_JVM_OPTS}"

# ── Launch ────────────────────────────────────────────────────────────────
exec "$JAVA_CMD" $GRADLE_OPTS \
  "-Dorg.gradle.appname=$(basename "$0")" \
  "-Dgradle.user.home=$GRADLE_USER_HOME" \
  "-classpath" "$CP" \
  org.gradle.launcher.GradleMain "$@"
