# Legacy DB Usage Audit

This document tracks the remaining usages of legacy `WorkoutViewModel` and `WorkoutRepo` components.

## After PR #93
- **Completed**: `WorkoutScreen` completed-session insert was successfully migrated to `WorkoutScreenViewModel` and its new use case. `WorkoutScreen` no longer uses `WorkoutViewModel`.
- **Completed**: Removed legacy `MainTab` fragments (`Resume.kt`, `Workouts.kt`, `Settings.kt`).
- **Completed**: Migrated `Authentication.kt` signOut path to no longer use `WorkoutViewModel`.
- **Remaining**: Migrate `FirebaseService` off `WorkoutRepo` or replace with coroutine repository path.

## WorkoutViewModel
`WorkoutViewModel` is now completely unused across the production application architecture!
The class definition itself (`WorkoutViewModel.kt`) remains as the final artifact.

**Status**: READY TO BE DELETED. The entire ViewModel logic has been successfully migrated to Compose ViewModels and UseCases!

## WorkoutRepo
`WorkoutRepo` is still actively referenced in the following files:
- `app/src/main/java/ca/gainzassist/models/db/WorkoutViewModel.kt`
- `app/src/main/java/ca/gainzassist/background/FirebaseService.kt`

`WorkoutRepo` still heavily uses `Thread { ... }` for background operations.

**Status**: NOT safe to remove yet. `FirebaseService` and `WorkoutViewModel` depend on it.
