# The Grind

This repository contains two distinct projects kept together for convenience:

1. The Grind — a responsive, productivity-focused website built with HTML, CSS, and JavaScript.
2. PennyWise — an Android personal-finance application (Kotlin) bundled in this repository as a separate Android project/module.

This README provides a brief, separated overview and quick-start instructions for each project. For full, project-specific documentation, please check for a README inside each project's directory (for example `PennyWise2/README.md`, `app/README.md`, or a README inside a subdirectory). If you can't find a separate README for the Android app, the summary below includes the main steps to build and run it.

---

## Project A — The Grind (Website)

A modern, responsive website to help users stay focused and productive.

Overview
- Purpose: Task management, progress tracking, and focused productivity through a simple web interface.
- Tech: HTML, CSS, JavaScript (static site)

Project structure (website files at repository root):
```
The-Grind/
├── README.md          # This file (repository-level)
├── index.html         # Main landing page for The Grind website
├── styles/
│   └── style.css      # Main stylesheet
├── scripts/
│   └── script.js      # JavaScript functionality
└── assets/            # Images and other assets
```

Quick start (website)
1. Open `index.html` in a web browser.
2. Or run a local server from the repository root:
```bash
python -m http.server 8000
# then visit http://localhost:8000
```

Notes
- The website is a static front-end project — no build tooling is required unless added later.
- Use a modern browser (Chrome, Firefox, Safari, Edge) when testing responsive behavior.

---

## Project B — PennyWise (Android App)

A gamified personal-finance Android application written in Kotlin. The Android project lives inside this repository as a separate module or subfolder — look for directories such as `PennyWise2/`, `PennyWise/`, or an `app/` module containing `build.gradle` and `AndroidManifest.xml`.

Summary of features
- Automated and manual expense recording (notification parsing for supported banks).
- Budget configuration and validation (monthly income, min/max spending goals).
- Local storage via SQLite and optional Firebase Firestore sync for selected progress data.
- Reports and charts (category totals, date-range filtering, downloadable statements).
- Gamified progression with XP and milestones, receipts support, and dark mode.

Quick start (Android)
1. Open the Android project in Android Studio.
2. Ensure you have JDK 11 and the Android SDK installed.
3. If Firebase is used, create a Firebase project and place `google-services.json` into the Android app module (usually `app/`).
4. Build and run on an emulator or physical device.

Common Gradle commands
```bash
# From the Android project root (if a Gradle wrapper is present):
./gradlew assembleDebug
./gradlew test
# For instrumentation tests (requires device/emulator):
./gradlew connectedAndroidTest
```

Privacy & security note
- Do not commit production Firebase credentials, signing keys, or other secrets to the repository.
- Ensure notification access is explained to users before enabling any bank-notification parsing features.
- Sensitive data should be handled and stored securely; consider encrypted storage for preferences and migrate plain-text passwords to Firebase Authentication.

---

## How to locate each project in this repository
- Website: look for `index.html`, `styles/`, and `scripts/` at the repository root.
- Android app: search for `AndroidManifest.xml`, `build.gradle(.kts)`, or an `app/src/main/` directory. If you prefer, run a quick search in your local clone:

```bash
# From the repo root (Unix-like systems)
find . -name AndroidManifest.xml -o -name build.gradle -o -name settings.gradle
```

If you're still unsure where the Android module is located, tell me and I will search the repository and point to the exact path.

---

## Contributing
Contributions are welcome — please indicate in your issue and pull request whether the change targets the website, the Android app, or both.

Suggested workflow
1. Create an issue describing the feature or bug.
2. Create a branch (e.g., `feature/...`, `bugfix/...`, `docs/...`).
3. Make changes and run project-specific checks (open `index.html` or run Gradle for Android).
4. Commit, push, and open a pull request describing which project the change affects.

---

## License
This repository is licensed under the MIT License. See the `LICENSE` file for details.

---

## Support
If you encounter any issues or have questions, please open an issue on the repository: https://github.com/Amogelang-Dev/The-Grind/issues

---

If you'd like, I can also:
- Move the Android app into a dedicated top-level folder if files are mixed between the website and Android project.
- Create or expand a dedicated README inside the Android project's directory with the full PennyWise README content.

Happy coding and happy grinding!