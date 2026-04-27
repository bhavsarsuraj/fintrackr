# FinTrackr

> An offline-first personal finance & expense tracker for Android, built natively with Jetpack Compose, Clean Architecture, and MVVM.

FinTrackr lets you track expenses, set per-category monthly budgets, automate recurring bills, and visualise spending trends — all working seamlessly offline with transparent cloud sync to Firebase.

---

## Highlights

- **Offline-first** — Room is the source of truth; the app is fully usable without network. Firebase Firestore syncs in the background when connected.
- **Reactive UI** — Compose + StateFlow + Kotlin Flow throughout; UI re-renders on data changes without explicit refresh.
- **Clean Architecture + MVVM** — `data/`, `domain/`, `feature/` layers with strict dependency direction.
- **Email/password auth** — Firebase Auth gates the app via an `AuthGate` composable; per-user Firestore namespaces.
- **Background automation** — WorkManager materialises recurring expenses on schedule and runs network-constrained sync.
- **Polished design system** — bespoke palette, per-category icon/colour, animated progress bars, refined typography, light/dark/system theme.
- **Well-tested** — 97 unit tests across use cases, repositories, ViewModels, mappers, and helpers.

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose · Material 3 · Compose Navigation |
| State | StateFlow · `UiState<T>` sealed types · `collectAsStateWithLifecycle` |
| DI | Hilt (`SingletonComponent`, `@HiltViewModel`, `@HiltWorker`) |
| Local persistence | Room 2.6 (KSP) — single source of truth |
| Remote backend | Firebase Firestore (with offline persistence) + Firebase Auth (email/password) |
| Preferences | DataStore Preferences |
| Background work | WorkManager (`PeriodicWorkRequest` + `OneTimeWorkRequest`) |
| Concurrency | Kotlin Coroutines · Flow · `combine` / `flatMapLatest` |
| Build | Gradle 8.7 · AGP 8.7 · Kotlin 2.0 · KSP · Version Catalog |
| Testing | JUnit 4 · MockK · Turbine · `kotlinx-coroutines-test` |

---

## Project Structure

```
app/src/main/java/com/surajbhavsar/fintrack/
├── core/
│   ├── common/          # Result, dispatchers, money/date helpers
│   ├── designsystem/    # Theme, palette, typography, reusable composables
│   └── ui/              # UiState, UiEvent
├── data/
│   ├── local/           # Room: entities, DAOs, FinTrackrDatabase
│   ├── remote/          # Firestore data sources
│   ├── preferences/     # DataStore implementation
│   ├── repository/      # Repository implementations (offline-first)
│   └── mapper/          # Entity ↔ Domain mappers
├── domain/
│   ├── model/           # Pure-Kotlin: Expense, Budget, Category, RecurringRule, MonthlyInsight
│   ├── repository/      # Repository interfaces
│   └── usecase/         # AddExpense, GenerateRecurringExpenses, GetMonthlyInsights, …
├── feature/
│   ├── expenses/        # List, edit screen + ViewModels
│   ├── budgets/         # CRUD with editor dialog
│   ├── insights/        # Stat cards, breakdowns, budget usage
│   ├── recurring/       # Recurring rules CRUD
│   ├── settings/        # Theme, currency, sync, recurring entry
│   ├── theme/           # ThemeViewModel (System/Light/Dark)
│   └── auth/            # Email/password sign-in & sign-up flow + AuthGate
├── work/                # RecurringExpenseWorker, SyncWorker, schedulers
├── di/                  # Hilt modules
└── navigation/          # NavHost, routes, bottom bar
```

---

## Build & Run

### Prerequisites
- Android Studio Giraffe (or newer)
- JDK 17
- Android SDK with API 35
- A connected device or emulator on API 24+

### Steps

1. Clone and open the project in Android Studio.
2. Create a Firebase project, register an Android app with package name `com.surajbhavsar.fintrack`, and place the downloaded `google-services.json` in `app/` (it is gitignored).
3. In the Firebase console for that project, ensure:
   - **Authentication → Email/Password** is enabled.
   - **Firestore → Native mode** is initialised, with rules restricting access to each user's own `users/{uid}/...` namespace.
4. Sync Gradle and run the `app` configuration.

### Tests

```bash
./gradlew :app:testDebugUnitTest
```

**25 test files · 97 individual tests** covering use cases, repository orchestration (push/pull/conflict resolution), ViewModel state transitions, mappers, and helpers. Highlights:

- `ExpenseRepositoryImplTest` — offline-first push, soft-delete sync, last-writer-wins merge logic
- `GetMonthlyInsightsUseCaseTest` — multi-flow `combine` correctness
- `AuthViewModelTest` — Firebase error humanisation + state transitions
- `SyncExpensesUseCaseTest` — push-then-pull ordering with `coVerifyOrder`

