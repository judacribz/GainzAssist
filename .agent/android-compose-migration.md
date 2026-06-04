# Compose Migration Strategy

- Keep changes incremental.
- Replace views in existing Activities/Fragments using `ComposeView`.
- Create UI state classes (e.g. `LoginUiState`) and action callbacks.
- Do not refactor core logic, viewmodels, or persistence layers simultaneously.

## Kotlin Style Guidelines
- Follow Kotlin lowerCamelCase naming.
- Do not use Java Math helpers when Kotlin stdlib equivalents exist.
- Do not revert intentional cleanup from the current branch unless it directly causes a bug.
