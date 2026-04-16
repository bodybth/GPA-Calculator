# GPA Calculator

**Package:** `com.bodybth.gpacalculator`

A clean, live-updating Android GPA calculator. Supports multiple semesters, per-course grade/credit/weight editing, delete with confirmation, and auto-saves data on exit.

---

## Features
- Live cumulative GPA gauge (no calculate button needed)
- Add / Edit / Delete semesters (with confirmation)
- Add / Edit / Delete courses per semester (with confirmation)
- Grade selector: A+, A, A−, B+, B, B−, C+, C, C−, D+, D, D−, F
- Weight selector: Regular / Honors / AP
- Data persists automatically via SharedPreferences + Gson
- Material Design 3 UI inspired by the reference screenshot

---

## ZIP Contents

| ZIP | Purpose |
|-----|---------|
| `GpaCalculator-AndroidIDE.zip` | Open directly in **AndroidIDE** on Android |
| `GpaCalculator-GitHubActions.zip` | Push to GitHub → auto-build via Actions |

---

## Building with AndroidIDE

1. Extract `GpaCalculator-AndroidIDE.zip` to internal storage  
   (e.g. `/sdcard/AndroidIDEProjects/GpaCalculator`)
2. Open **AndroidIDE** → Open Project → select that folder
3. AndroidIDE will prompt to download the Gradle wrapper JAR automatically
4. Click **Build → Assemble Debug**
5. APK will appear at `app/build/outputs/apk/debug/app-debug.apk`

> **Note:** AndroidIDE requires Android 8.0+ and at least 2 GB free storage.

---

## Building with GitHub Actions

1. Extract `GpaCalculator-GitHubActions.zip`
2. Create a new GitHub repository: `bodybth/GpaCalculator`
3. Push the extracted contents to the `main` branch:
   ```bash
   git init && git add . && git commit -m "Initial commit"
   git remote add origin https://github.com/bodybth/GpaCalculator.git
   git push -u origin main
   ```
4. GitHub Actions will automatically build a **debug APK**
5. Download the APK from the **Actions → Artifacts** tab

### Signed Release APK

Add these four **Repository Secrets** (Settings → Secrets → Actions):

| Secret | Value |
|--------|-------|
| `KEYSTORE_BASE64` | `base64 -w0 your_release.keystore` |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | Your key alias |
| `KEY_PASSWORD` | Your key password |

Then push a tag to trigger the signed release:
```bash
git tag v1.0.0 && git push origin v1.0.0
```

---

## Getting the Gradle Wrapper JAR

Both ZIPs **do not include** the `gradle-wrapper.jar` binary (it's a binary blob ~60 KB).

**AndroidIDE** downloads it automatically on first sync.

**For GitHub Actions**, the `setup-java` action + Gradle caching handles it automatically.

If you need it manually:
```bash
# From the project root, run once on a machine with Gradle installed:
gradle wrapper --gradle-version 8.6
```
Or download directly:
```
https://github.com/gradle/gradle/raw/v8.6.0/gradle/wrapper/gradle-wrapper.jar
```
Place it at `gradle/wrapper/gradle-wrapper.jar`.
# GPA-Calculator
