# Legacy DB Usage Audit

This document tracks the remaining usages of legacy `WorkoutViewModel` and `WorkoutRepo` components.

## After PR #93
- **Completed**: `WorkoutScreen` completed-session insert was successfully migrated to `WorkoutScreenViewModel` and its new use case. `WorkoutScreen` no longer uses `WorkoutViewModel`.
- **Remaining**: Remove or migrate legacy `MainTab` fragments (`Resume.kt`, `Workouts.kt`, `Settings.kt`) and `Authentication.kt` signOut path, which still use `WorkoutViewModel`.
- **Remaining**: Migrate `FirebaseService` off `WorkoutRepo` or replace with coroutine repository path.

## WorkoutViewModel
`WorkoutViewModel` is still actively referenced in the following files:
- `app/src/main/java/ca/gainzassist/activities/main/fragments/Resume.kt`
- `app/src/main/java/ca/gainzassist/activities/main/fragments/Workouts.kt`
- `app/src/main/java/ca/gainzassist/activities/main/fragments/Settings.kt`
- `app/src/main/java/ca/gainzassist/util/firebase/Authentication.kt`

**Status**: NOT safe to remove yet. Remaining Main fragments and authentication flows still rely on it.

## WorkoutRepo
`WorkoutRepo` is still actively referenced in the following files:
- `app/src/main/java/ca/gainzassist/models/db/WorkoutViewModel.kt`
- `app/src/main/java/ca/gainzassist/background/FirebaseService.kt`

`WorkoutRepo` still heavily uses `Thread { ... }` for background operations.

**Status**: NOT safe to remove yet. `FirebaseService` and `WorkoutViewModel` depend on it.
