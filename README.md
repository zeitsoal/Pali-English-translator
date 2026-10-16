# Pali Dhamma Translator — Gemini 4 Argon Edition (Android)

An Android application built with Jetpack Compose, Material 3, Room Database, and **Gemini 4 Argon (`gemini-4-argon`)** to translate Romanized Pali (IAST) into English across three distinct translation standards with line-by-line grammatical breakdowns, root etymology, and doctrinal commentary.

---

## Core Features (Argon Edition)

1. **Powered by Gemini 4 Argon (`gemini-4-argon`)**:
   - Uses `gemini-4-argon` by default for deep morphological decomposition (*dhātu*, *vibhatti*, *sandhi*, *samāsa*) and structured JSON outputs (`responseSchema`).
   - Includes an in-app model selector in Settings so you can switch between **Gemini 4 Argon**, **Gemini 1.5 Pro**, and **Gemini 1.5 Flash**.

2. **Adaptive Multi-Form Factor Layout**:
   - **Foldables & Tablets (`>= 600dp`)**: True 50/50 side-by-side split view (Pali input on the left, synchronized translations on the right).
   - **Smartphones (Portrait)**: Vertically stacked cards for clean reading and typing without horizontal cramping.

3. **Three Distinct Translation Standards + Notes Tab**:
   - **Tab 1 — Classical (PTS & Commentary)**: Orthodox Theravāda hermeneutics following the commentaries (*Aṭṭhakathā*, *Ṭīkā*, Buddhaghosa) and Pali Text Society lexicography.
   - **Tab 2 — Argon AI (Gemini 4 Argon Autonomous Reasoning)**: First-principles philological analysis relying solely on Gemini 4 Argon's own morphological and syntactical reasoning without external scholastic dogma.
   - **Tab 3 — Enlightened Thai Forest Monk**: Direct, uncompromising contemplative insight from the Kammatthana forest tradition focusing on the living mind (*citta*), fabrications (*saṅkhāra*), and practical liberation, completely independent of PTS or classical commentary conventions.
   - **Tab 4 — Notes & Study Scratchpad**: A dedicated notepad where you can tap the **Add to Notes** icon on any line or word to compile your favorite passages across the three standards.

4. **Pixel-Locked Synchronized Scrolling Across All 3 Tabs**:
   - Scrolling up or down in any of the three English translation tabs locks both the line index and pixel scroll offset across the other two tabs so you can switch between standards and immediately compare the exact same phrase.

5. **Local Room Database (History, Bi-Directional Search, & Bookmarks)**:
   - Every translation is automatically saved with a timestamp.
   - Search across Pali source text, all three English translations, and your personal notes.
   - Bookmark favorite passages for offline review and copy any line, word breakdown, or note with one tap.

---

## How to Get the Installable `.apk` File

### Method 1: Cloud Build via GitHub (No Android Studio or PC Installation Needed)
This project includes a pre-configured GitHub Actions workflow (`.github/workflows/build-apk.yml`) that compiles the `.apk` for you automatically in the cloud:

1. Create a free repository on [GitHub](https://github.com/new) and upload the contents of this extracted project folder.
2. Click the **Actions** tab in your GitHub repository and select **Build Pali Translator APK**.
3. Once the workflow finishes (~2 minutes), scroll to the **Artifacts** section at the bottom of the run page and download **`Pali-Dhamma-Translator-Argon-APK`** (`app-debug.apk`).
4. Open `app-debug.apk` on your Android phone or tablet to install it.

### Method 2: Local Build in Android Studio
1. Extract this archive and open the folder in **Android Studio** (Ladybug / Koala or newer).
2. Wait for Gradle sync to complete.
3. In the top menu bar, click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Click **locate** in the notification popup to grab `app/build/outputs/apk/debug/app-debug.apk` and transfer it to your phone.
