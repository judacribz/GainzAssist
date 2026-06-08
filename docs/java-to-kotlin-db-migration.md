# Java-to-Kotlin DB Migration Plan

This document outlines the migration strategy for remaining legacy Java database components to Kotlin.

## Remaining Java Files
- `WorkoutDatabase.java`
- `WorkoutRepo.java`
- `WorkoutViewModel.java`
- Various DAO interfaces (e.g., `WorkoutDao.java`, `ExerciseDao.java`, etc.)

## Conversion Order
To minimize breakage and properly adopt coroutines and Flow, the conversion must follow this bottom-up order:
1. **DAOs**
2. **WorkoutDatabase**
3. **WorkoutRepo**
4. **WorkoutViewModel** or remove after callers migrate

## Risk Areas
- **Room annotation parity**: Ensure annotations translate correctly to Kotlin (e.g., `@get:Query` vs `@Query`, nullability of return types).
- **Java/Kotlin nullability**: Java's implicit nullability might lead to strict NPEs in Kotlin if DB fields were unexpectedly null.
- **legacy WorkoutRepo background threads**: Transitioning from Executor patterns to Coroutines can introduce threading issues if not carefully converted.
- **insertSession progression persistence from PR #79**: Ensure the new progression persistence continues to work correctly.
- **Firebase side effects**: Some DB operations might trigger or rely on Firebase syncs.
- **WorkoutScreen still using legacy WorkoutViewModel**: Requires careful coordination so as not to break its existing lifecycle.
