# The Grind

This repository contains two distinct projects kept together for convenience:

1. The Grind — a responsive, productivity-focused website built with HTML, CSS, and JavaScript.
2. Grind Coffee Co. — an Android mobile application (Kotlin/Java) bundled in this repository as a separate Android project/module.

This README provides a brief, separated overview and quick-start instructions for each project. For full, project-specific documentation, please check for a README inside each project's directory (or open an issue if you can't find it).

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

## Project B — Grind Coffee Co. (Android App)

A mobile ordering and rewards Android application for a coffee shop brand (Grind Coffee Co.). The Android project lives inside this repository as a separate module (see the `app/` directory).

Summary of features (observed from the project resources)
- Place and manage coffee and food orders (menu, cart, order history).
- Pickup and delivery options with estimated pickup times.
- User account, login/registration, and profile management.
- Rewards and promotions, gamified elements (points/rewards tracking).
- Support for image cropping (UCrop activity integration) and various product/detail screens.
- Local data storage and standard Android app architecture (Gradle-based build).

Quick start (Android)
1. Open the Android project in Android Studio (open the repository or the `app/` module).
2. Ensure you have JDK 11 (or the version the project requires) and the Android SDK installed.
3. If Firebase or other services are used, add the appropriate configuration files (for example `google-services.json`) into the app module.
4. Build and run on an emulator or physical device.

Common Gradle commands
```bash
# From the repository root (Gradle wrapper is present):
./gradlew assembleDebug
./gradlew test
# For instrumentation tests (requires device/emulator):
./gradlew connectedAndroidTest
```

Privacy & security note
- Do not commit production Firebase credentials, signing keys, or other secrets to the repository.
- Explain notification access to users before enabling any parsing of notifications.
- Handle sensitive data securely; consider encrypted storage for preferences and use proper authentication for user accounts.

---

## How to locate each project in this repository
- Website: look for `index.html`, `styles/`, and `scripts/` at the repository root.
- Android app: open the `app/` directory — it contains `build.gradle.kts`, `src/main/AndroidManifest.xml`, and the `res/` resources (including `strings.xml` where the app name is defined).

If you're still unsure where the Android module is located, tell me and I will point to the exact path or open the app's README if you want one created.

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
- Create or expand a dedicated README inside the Android project's `app/` directory with the full Grind Coffee Co. README content (screenshots, build notes, package name, troubleshooting).

Happy coding and happy grinding!
