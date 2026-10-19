# The Grind

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](./LICENSE)
[![Repo Size](https://img.shields.io/github/repo-size/Amogelang-Dev/The-Grind)](https://github.com/Amogelang-Dev/The-Grind)
[![Issues](https://img.shields.io/github/issues/Amogelang-Dev/The-Grind)](https://github.com/Amogelang-Dev/The-Grind/issues)


A combined repository containing a responsive website (The Grind) and a companion Android mobile application (Grind Coffee Co.). This README gives an at-a-glance overview and quick-start instructions for both projects.

---

<!-- HERO -->
<p align="center">
  <img src="images/house-blend.jpeg" alt="Grind Coffee" width="720" style="border-radius:8px;">
</p>

---

## Project summary

- Website: The Grind — a simple, responsive front-end for productivity and information.
- Mobile app: Grind Coffee Co. — an Android ordering & rewards app included in the `app/` module.


## Quick links

- Source: https://github.com/Amogelang-Dev/The-Grind
- Issues: https://github.com/Amogelang-Dev/The-Grind/issues

---

## Project A — The Grind (Website)

A modern, responsive website to help users stay focused and productive.

Tech stack
- HTML, CSS, JavaScript (static front-end)

Project structure (website files at repository root):

```
The-Grind/
├── README.md
├── index.html
├── style.css
├── images/       # images used by website and app documentation
└── scripts/      # optional JS functionality
```

Quick start (website)
1. Open `index.html` in a web browser.
2. Or run a simple local server from the repository root:

```bash
python -m http.server 8000
# then visit http://localhost:8000
```

Notes
- Static site — no build tooling required.
- Test responsive behaviour in a modern browser (Chrome, Firefox, Safari, Edge).

---

## Project B — Grind Coffee Co. (Android App)

Grind Coffee Co. is the Android app included under the `app/` directory. It provides mobile ordering, rewards, and profile management for a coffee shop experience.

Primary app assets (examples)

- App name: `Grind Coffee Co.`
- Entry activity: `LoginActivity` (manifest declares LAUNCHER intent)
- App strings and resources: `app/src/main/res/values/strings.xml`

App screenshots and icons

<p align="center">
  <img src="images/americano.jpeg" alt="Americano" width="280" style="margin-right:12px; border-radius:6px;">
  <img src="images/flat-white.jpeg" alt="Flat White" width="280" style="border-radius:6px;">
</p>

Observed features
- Menu browsing (coffee, food), add-to-cart and order placement.
- Pickup/delivery options and estimated pickup times.
- User accounts: login, register and profile management.
- Rewards, promotions and order history.
- Integration points: UCrop activity (image cropping), Gradle-based build.

Quick start (Android)
1. Open the repository in Android Studio (or open the `app/` module).
2. Ensure required SDKs/JDK are installed (project uses Gradle Kotlin DSL).
3. If needed, add `google-services.json` to `app/` for Firebase features.
4. Build and run using the Gradle wrapper:

```bash
# From repository root
./gradlew assembleDebug
./gradlew installDebug
```

Notes
- Do not commit production credentials or signing keys.
- Check `app/src/main/AndroidManifest.xml` and `app/src/main/res/values/strings.xml` for app configuration and labels.

---

## Contributing

Contributions are welcome. To contribute:
1. Create an issue describing the feature or bug.
2. Create a branch (e.g., `feature/...`, `bugfix/...`, `docs/...`).
3. Make changes and run project-specific checks (open `index.html` or run Gradle for Android).
4. Commit, push, and open a pull request describing which project the change affects.

Guidelines
- Prefix commit messages with `web:` for website changes and `android:` for app changes where appropriate.
- Include screenshots for UI changes and a short testing checklist in PR descriptions.

---

## License

This repository is licensed under the MIT License. See `LICENSE` for details.

---

## Contact / Support

If you run into issues or have questions, open an issue: https://github.com/Amogelang-Dev/The-Grind/issues

---

If you'd like, I can also:
- Add a dedicated `app/README.md` with package name, build variants, and developer notes (recommended).
- Create smaller icon files (app logo) and place them in `app/src/main/res/mipmap-*/` if you want a ready-to-use launcher icon.

