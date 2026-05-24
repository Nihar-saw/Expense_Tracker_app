# 💰 SpendWise - Android Expense Tracker

SpendWise is a modern Android expense tracking application built using **Kotlin** and **Jetpack Compose**.

The app helps users:
- track daily expenses
- manage monthly/yearly budgets
- visualize spending analytics
- monitor savings goals
- maintain better financial habits

SpendWise focuses on a clean fintech-inspired UI, smooth user experience, and scalable Android architecture.

---

# ✨ Features

## 📊 Expense Management
- Add expenses
- Edit/Delete transactions
- Category-based expense tracking
- Search and filter transactions
- Expense history management

---

## 📈 Analytics Dashboard
- Monthly spending overview
- Yearly spending analysis
- Interactive charts & graphs
- Category-wise breakdown
- Spending trends visualization

---

## 🎯 Budget Management
- Monthly budget setup
- Spending limit alerts
- Savings goal tracking
- Budget progress indicators

---

## 🌙 Modern UI/UX
- Material 3 Design
- Dark & Light Theme
- Smooth animations
- Responsive layouts
- Minimal fintech-style interface

---

## ☁️ Cloud Integration
- Firebase Authentication
- Cloud database sync
- Secure user sessions
- Real-time data updates

---

# 🛠️ Tech Stack

| Technology | Usage |
|---|---|
| Kotlin | Android Development |
| Jetpack Compose | Modern UI Toolkit |
| MVVM Architecture | Clean Architecture |
| Firebase | Authentication & Cloud Database |
| Room Database | Local Offline Storage |
| Hilt | Dependency Injection |
| Retrofit | Networking |
| Coroutines & Flow | Async Programming |
| Material 3 | UI Design |

---

# 📂 Project Structure

```bash
.
├── app
│   ├── src
│   │   ├── androidTest
│   │   │   └── java/com/example
│   │   │
│   │   ├── main
│   │   │   ├── java/com/example
│   │   │   │
│   │   │   │   ├── data
│   │   │   │   │   ├── local
│   │   │   │   │   ├── remote
│   │   │   │   │   ├── model
│   │   │   │   │   └── repository
│   │   │   │   │
│   │   │   │   ├── di
│   │   │   │   │   └── AppModule.kt
│   │   │   │   │
│   │   │   │   ├── domain
│   │   │   │   │   ├── model
│   │   │   │   │   ├── repository
│   │   │   │   │   └── usecase
│   │   │   │   │
│   │   │   │   ├── navigation
│   │   │   │   │   └── NavGraph.kt
│   │   │   │   │
│   │   │   │   ├── ui
│   │   │   │   │   ├── components
│   │   │   │   │   ├── screens
│   │   │   │   │   │   ├── auth
│   │   │   │   │   │   ├── dashboard
│   │   │   │   │   │   ├── analytics
│   │   │   │   │   │   ├── budget
│   │   │   │   │   │   ├── addexpense
│   │   │   │   │   │   └── settings
│   │   │   │   │   │
│   │   │   │   │   └── theme
│   │   │   │   │
│   │   │   │   ├── utils
│   │   │   │   │   ├── Constants.kt
│   │   │   │   │   ├── Extensions.kt
│   │   │   │   │   └── Resource.kt
│   │   │   │   │
│   │   │   │   ├── viewmodel
│   │   │   │   │   ├── AuthViewModel.kt
│   │   │   │   │   ├── ExpenseViewModel.kt
│   │   │   │   │   └── BudgetViewModel.kt
│   │   │   │   │
│   │   │   │   └── MainActivity.kt
│   │   │   │
│   │   │   ├── res
│   │   │   │   ├── drawable
│   │   │   │   ├── mipmap
│   │   │   │   ├── values
│   │   │   │   └── xml
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test
│   │       ├── java/com/example
│   │       │   ├── ExampleRobolectricTest.kt
│   │       │   ├── ExampleUnitTest.kt
│   │       │   └── GreetingScreenshotTest.kt
│   │       │
│   │       └── screenshots
│   │           └── greeting.png
│   │
│   ├── .gitignore
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── gradle
│   └── libs.versions.toml
│
├── .env.example
├── .gitignore
├── README.md
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── metadata.json
```

---

# 🚀 Getting Started

## 📋 Prerequisites

Before running the project, make sure you have:

- Android Studio
- Android SDK
- Kotlin
- Firebase Project Setup
- Emulator or Android Device

---

# ⚙️ Installation

## 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/spendwise.git
```

---

## 2️⃣ Open Project

Open the project in Android Studio.

---

## 3️⃣ Configure Firebase

1. Create a Firebase project
2. Add an Android app
3. Download `google-services.json`
4. Place it inside:

```bash
app/google-services.json
```

---

## 4️⃣ Sync Dependencies

Allow Gradle to sync dependencies automatically.

---

## 5️⃣ Run the App

Run the application using:

```bash
Shift + F10
```

Or launch it directly from Android Studio.

---

# 📱 App Screens

- Splash Screen
- Login/Register
- Dashboard
- Add Expense
- Expense History
- Analytics
- Budget Planner
- Settings/Profile

---

# 📊 Future Improvements

Planned features:
- AI financial insights
- Voice expense input
- Receipt scanner (OCR)
- Export reports as PDF/CSV
- Multi-currency support
- Financial health score
- Smart notifications
- Recurring expenses

---

# 📸 Screenshots

Add your application screenshots here.

```markdown
![Dashboard](screenshots/dashboard.png)
![Analytics](screenshots/analytics.png)
```

---

# 🔒 Security

- Secure Firebase Authentication
- Local encrypted storage
- Protected API handling
- Secure user sessions

---

# 🧪 Testing

The project includes:
- Unit Tests
- Robolectric Tests
- Screenshot Tests

Run tests using:

```bash
./gradlew test
```

---

# 🤝 Contributing

Contributions are welcome.

## Steps:
1. Fork the repository
2. Create a new feature branch
3. Commit your changes
4. Push your branch
5. Open a Pull Request

---

# 📄 License

This project is licensed under the MIT License.

---

# 👨‍💻 Developer

Developed by **Nihar Sawant**

---

# ⭐ Support

If you like this project, consider giving it a ⭐ on GitHub.
```
