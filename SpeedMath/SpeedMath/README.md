# ⚡ SpeedMath — Offline Android Mental Math App

A complete, 100% offline Android application for practising speed mathematics.
Built with Kotlin, MVVM architecture, Room Database, and MPAndroidChart.

---

## 📁 Project Structure

```
SpeedMath/
├── app/
│   ├── build.gradle                    ← All dependencies declared here
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml         ← No internet permission
│       ├── java/com/ujjwal/speedmath/
│       │   ├── model/
│       │   │   ├── PracticeMode.kt     ← 9 modes enum
│       │   │   ├── PracticeSettings.kt ← Session config (Serializable)
│       │   │   └── Question.kt         ← Generated question data class
│       │   ├── data/
│       │   │   ├── entity/
│       │   │   │   ├── Session.kt      ← Room entity: one session
│       │   │   │   └── QuestionRecord.kt ← Room entity: one question result
│       │   │   ├── dao/
│       │   │   │   ├── SessionDao.kt
│       │   │   │   └── QuestionRecordDao.kt
│       │   │   ├── db/
│       │   │   │   └── AppDatabase.kt  ← Singleton Room DB
│       │   │   └── repository/
│       │   │       └── MathRepository.kt
│       │   ├── engine/
│       │   │   └── QuestionEngine.kt   ← All question generation logic
│       │   ├── ui/
│       │   │   ├── home/
│       │   │   │   ├── MainActivity.kt
│       │   │   │   └── HomeViewModel.kt
│       │   │   ├── settings/
│       │   │   │   └── SettingsActivity.kt
│       │   │   ├── practice/
│       │   │   │   ├── PracticeActivity.kt
│       │   │   │   └── PracticeViewModel.kt
│       │   │   ├── result/
│       │   │   │   └── ResultActivity.kt
│       │   │   └── progress/
│       │   │       ├── ProgressActivity.kt
│       │   │       └── ProgressViewModel.kt
│       │   └── utils/
│       │       ├── FormatUtils.kt
│       │       ├── SoundUtils.kt
│       │       ├── StreakManager.kt
│       │       └── DailyChallengeManager.kt
│       └── res/
│           ├── layout/                 ← 6 XML layout files
│           ├── values/
│           │   ├── colors.xml
│           │   ├── strings.xml
│           │   └── themes.xml
│           └── drawable/
│               └── spinner_bg.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## 🚀 How to Build & Run

### Prerequisites
| Tool | Version |
|------|---------|
| Android Studio | Hedgehog (2023.1.1) or newer |
| Android Gradle Plugin | 8.1.0 |
| Kotlin | 1.9.0 |
| Gradle | 8.0 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |

### Steps

1. **Open in Android Studio**
   ```
   File → Open → Select the SpeedMath/ folder
   ```

2. **Let Gradle sync** (it will download Room, MPAndroidChart, etc.)
   - Requires internet on FIRST build only; after that the app itself is 100% offline.

3. **Run**
   - Select a device or emulator (API 24+)
   - Click ▶ Run or `Shift+F10`

4. **Build APK for sideloading**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 App Screens & Flow

```
MainActivity (Home)
    ↓ tap mode card
SettingsActivity (Configure)
    ↓ Start Practice
PracticeActivity (Questions + Timer)
    ↓ session complete
ResultActivity (Summary)
    ↓ View Progress button
ProgressActivity (Analytics + Charts)
```

---

## 🔢 Practice Modes

| Mode | Description |
|------|-------------|
| Addition | a + b |
| Subtraction | a − b (always ≥ 0 result) |
| Multiplication | a × b |
| Division | a ÷ b (always integer quotient) |
| Mixed | Random of the 4 above |
| Squares | a² |
| Square Roots | √n (perfect roots by default) |
| Cubes | a³ |
| Cube Roots | ∛n (perfect cube roots) |

---

## ⚙️ Configurable Settings

- **Question count**: 10 / 20 / 30 / 50 / custom
- **Number ranges**: 1-digit (1–9), 2-digit (10–99), 3-digit (100–999), or custom min/max
- **Operation ranges**: Independent first and second operand ranges
- **Perfect roots only**: Toggle for √ and ∛ modes
- **Countdown timer**: Per-question countdown (5–300s); auto-submits blank on expiry

---

## 📊 Analytics

All data is stored locally in a Room (SQLite) database at:
`/data/data/com.ujjwal.speedmath/databases/speedmath.db`

Tracked data:
- Per-session: mode, total Q, correct, accuracy %, total time, avg time/Q
- Per-question: question text, user answer, correct answer, time taken
- Lifetime: overall accuracy, average speed, best session
- Streak: current & best consecutive-day streak (SharedPreferences)

---

## 🏗️ Architecture

```
UI Layer          ViewModel Layer       Data Layer
─────────────     ───────────────       ──────────────────────
Activity ────────→ ViewModel ──────────→ Repository
(XML layout)      (LiveData)            (Room DAO → SQLite)
                  (Coroutines)
```

Pattern: **MVVM** (Model-View-ViewModel)  
Async: **Kotlin Coroutines** + **LiveData**  
DI: Manual (no Hilt/Dagger – keeps it simple)

---

## 🔌 Dependencies

| Library | Purpose |
|---------|---------|
| `androidx.room` 2.6.1 | Local database (SQLite ORM) |
| `androidx.lifecycle` 2.7.0 | ViewModel + LiveData |
| `kotlinx.coroutines` 1.7.3 | Async DB operations & timer |
| `MPAndroidChart` v3.1.0 | Accuracy & speed line charts |
| `material` 1.11.0 | UI components (TextInputLayout, SwitchMaterial) |

---

## 🎯 Edge Cases Handled

| Case | Handling |
|------|---------|
| Division by zero | Divisor always ≥ 1 |
| Non-integer division | Quotient × divisor = dividend strategy |
| Empty answer submitted | Error shown, not recorded |
| Countdown expiry | Blank answer auto-submitted |
| Range min > max | Validation toast + block start |
| Question pool exhausted | Graceful repeat after 200-attempt fallback |
| Rotation during practice | ViewModel survives config change |

---

## 🔇 Permissions

```xml
<!-- NONE -->
```
No internet, no storage, no camera, no microphone.  
Vibration does NOT require permission on Android.

---

## 💡 Customisation Tips

**Add a new mode** → Add entry to `PracticeMode.kt`, add case in `QuestionEngine.kt`, add card in `activity_main.xml`.

**Change chart colours** → Edit `R.color.primary_blue` / `R.color.accent_orange` in `colors.xml`.

**Increase max question count** → Change validation in `SettingsActivity.validateAndStart()`.

**Export data** → Query `AppDatabase` and write to a CSV in external storage (add WRITE_EXTERNAL_STORAGE permission for API < 29).
