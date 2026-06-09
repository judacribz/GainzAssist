# Compose Screen State Guide

- Compose screens should receive `UiState` objects containing the data needed to render the UI.
- Use `StateFlow` from ViewModels to manage and emit this `UiState`.
- Screen components (Composables) should be stateless where possible. Pass data down and hoist events up via lambdas (e.g., `onBackClick: () -> Unit`).
- Avoid passing the ViewModel itself into deep Composables. Extract the state and callbacks at the screen-level Composable and pass them down to UI components.
- Do not mix business logic inside Composables. Handle all complex data operations in the ViewModel or UseCases.
