---
name: Refactor to MVVM and UseCases
description: A guide and set of rules for migrating legacy code into the new MVVM + UseCases architecture.
---

# Refactor to MVVM and UseCases

This skill provides the architectural rules when migrating existing screens to the MVVM + UseCases approach in GainzAssist.

## Architecture Rules

1. **MVVM:**
   - Activities handle Android lifecycle and navigation only.
   - ViewModels own screen state and user actions.
   - UseCases own business rules.
   - Repositories own data access abstraction.
   - Compose screens receive `UiState` and callbacks only.
   - No direct repository/database/preference calls from composables.

2. **Koin:**
   - Use Koin DSL modules for now.
   - Do not use annotations yet.
   - Keep modules split by core/data/domain/presentation.
   - Bind interfaces to implementations.
   - Avoid injecting Android Context into domain/usecases.
   - If platform-specific APIs are needed, keep them in data/platform layer.

3. **Coroutines:**
   - Use `suspend` functions for one-shot work.
   - Use `Flow` for streams.
   - Use `StateFlow` for UI state.
   - Use `SharedFlow` or `Channel` for one-off events.
   - Inject `DispatcherProvider`.
   - Do not hardcode `Dispatchers.IO` in new repository/usecase code.
   - No RxJava.
   - No callback-based async in new code.

4. **KMP-ready boundaries:**
   - Domain models/usecases/repository interfaces should avoid Android imports.
   - Android-specific code stays in `app/data` implementation for now.
   - Do not move to KMP module yet.
   - Prepare names and dependencies so future shared module extraction is easy.

When refactoring, ensure that old callback-based patterns are eliminated in favor of coroutines, and view-heavy classes are strictly separated from business and data logic.
