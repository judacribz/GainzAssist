# MVVM + Clean Architecture Guide

1. **Domain Layer (`ca.gainzassist.domain.model`):** Pure business entities like `Workout`, `Exercise`, `Session`. No Android or data dependencies.
2. **Data Layer (`ca.gainzassist.data`):** Repository implementations, Room DAOs (`local.database`), Firebase syncing (`remote.firebase`), and background `service`.
3. **Core Layer (`ca.gainzassist.core`):** Utilities, constants, and extensions used globally.
4. **UI Layer (`ca.gainzassist.activities`):** Follows a feature-based structure where each screen gets its own directory.
   - `<feature_name>/[<screen_name>]/`: Contains the screen's ViewModel at the root.
   - `<feature_name>/[<screen_name>]/view/`: Contains the screen's Activity, Fragment, Compose `Screen`, `UiState`, `Event`, `Actions`, and any UI models related to that screen only.
5. **ViewModels:** Own screen state and user actions. They map user interactions to UseCases and expose `StateFlow` for UI state.
6. **UseCases:** Own business rules. A UseCase typically exposes a single `operator fun invoke()` which handles a specific piece of business logic.
7. **Compose Screens:** Receive `UiState` and callbacks only. They do not access `ViewModel` directly inside the screen if possible, and definitely no direct repository, database, or preference calls from composables.
