# Compose Migration Strategy

- Keep changes incremental.
- Replace views in existing Activities/Fragments using `ComposeView`.
- Create UI state classes (e.g. `LoginUiState`) and action callbacks.
- Do not refactor core logic, viewmodels, or persistence layers simultaneously.
