# MVVM + UseCase Architecture Guide

1. **Activities:** Handle Android lifecycle and navigation only. They should observe UI state from the ViewModel and pass user intents back.
2. **ViewModels:** Own screen state and user actions. They map user interactions to UseCases and expose `StateFlow` for UI state.
3. **UseCases:** Own business rules. A UseCase typically exposes a single `operator fun invoke()` which handles a specific piece of business logic.
4. **Repositories:** Own data access abstraction. Interfaces sit in the `domain` layer while implementations sit in the `data` layer.
5. **Compose Screens:** Receive `UiState` and callbacks only. They do not access `ViewModel` directly inside the screen if possible, and definitely no direct repository, database, or preference calls from composables.
