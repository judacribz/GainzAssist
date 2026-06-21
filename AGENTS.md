# GainzAssist Agent Rules

## Architecture Direction

The app must move toward proper MVVM + Clean Architecture.

Use this structure where possible:

```text
app/src/main/java/ca/gainzassist/
  core/
    constants/
    resources/
    util/
  data/
    local/
    remote/
    repository/
  domain/
    model/
    repository/
    usecase/
  feature/
    <feature-name>/
      data/
      domain/
      presentation/
        screen/
        component/
        viewmodel/
        state/
        event/
```

Each screen should live inside its own feature folder.

Examples:

```text
feature/login/presentation/screen/LoginScreen.kt
feature/login/presentation/viewmodel/LoginViewModel.kt
feature/login/presentation/state/LoginUiState.kt
feature/workout_entry/presentation/screen/WorkoutEntryScreen.kt
feature/start_workout/presentation/viewmodel/StartWorkoutViewModel.kt
```

Do not keep growing large activity/view packages.

## MVVM Rules

Activities should be thin.

Activities may:

* read intent extras
* call `setContent`
* connect lifecycle callbacks
* navigate to another activity when needed
* delegate UI state/events to ViewModel

Activities should not:

* hold screen business state unless unavoidable
* perform validation
* perform database work
* perform Firebase work
* contain formatting/business rules
* contain large lists of mutable global variables

Move state into ViewModels where possible.

Use:

* `UiState`
* `UiEvent`
* `UiAction` or screen action interfaces
* use cases for business operations

## Clean Architecture Rules

Use cases should be added where logic is business-related or reusable.

Examples:

* `ValidateLoginUseCase`
* `ValidateWorkoutInputUseCase`
* `GetWorkoutForSummaryUseCase`
* `SaveWorkoutUseCase`
* `StartWorkoutSessionUseCase`
* `FinishWorkoutSessionUseCase`
* `GetHowToVideosUseCase`
* `SignOutUseCase`

Domain layer should not depend on Android framework classes where possible.

Do not introduce new Android dependencies into domain models if avoidable.

Because the project is preparing for Kotlin Multiplatform:

* Keep domain models as pure Kotlin where possible.
* Avoid `Context`, `Intent`, `Bundle`, `Parcelable`, `Activity`, Android resources, Room annotations, and Firebase types in domain layer when creating new code.
* If existing models still use Android/Room types, do not expand that pattern.
* Prefer future separation between pure domain models and Room/Firebase DTO/entities.

## SOLID Rules

Follow SOLID principles.

Single Responsibility:

* One class should have one clear reason to change.
* Do not mix UI, validation, persistence, and networking in one class.

Open/Closed:

* Prefer small interfaces and sealed events over large conditional blocks.

Liskov:

* Do not create abstractions that force unrelated implementations.

Interface Segregation:

* Keep contracts small.
* Do not create large generic managers.

Dependency Inversion:

* ViewModels should depend on use cases or repository interfaces, not concrete data sources.

## Clean Code Rules

No hardcoded user-facing text in Kotlin.
Use `strings.xml`.

No hardcoded reusable UI dimensions in screen code.
Use `dimens.xml` where practical.

No hardcoded colors in Kotlin.
Use color resources or Material theme values.

No repeated primitive values or magic numbers.
Use named constants or resources.

Allowed:

* local obvious values where extracting them makes code worse
* framework-required values
* one-off Compose modifier values only if clearly local and not reused

Prefer named constants for:

* request keys
* intent extras
* route names
* animation durations
* limits
* validation thresholds
* delay values
* API fields
* repeated UI measurements

Private functions should be placed near the bottom of the file, after public/override lifecycle functions.

Companion objects must be the absolute bottom-most section of the class.

Override functions should be near the top of the class, directly under properties/global variables.

Preview composables must be private.

Composable files should be organized like this:

```text
Public screen composable
Private section composables
Private small components
Private preview composables at bottom
Constants at top or bottom consistently
```

## Resources Rules

User-facing strings must be in `strings.xml`.

Examples:

* button labels
* error messages
* toast messages
* snackbar messages
* content descriptions
* screen titles

Reusable UI values should be resources when practical:

* dimensions
* colors
* animation durations if reused
* alpha values if reused

Do not hardcode API keys, URLs, secrets, or config values.

## Gradle Rules

Use `gradle/libs.versions.toml` for dependency and plugin versions.

Do not hardcode dependency versions in Gradle files.

Do not change SDK versions unless explicitly requested.

Preserve unless instructed otherwise:

* `compileSdk`
* `targetSdk`
* `minSdk`
* `applicationId`
* `versionCode`
* `versionName`

Keep dependencies organized in TOML bundles where it improves readability.

## PR Rules

This should be one PR, but use clean commits.

Recommended commit order:

1. `docs: add architecture and clean code agent rules`
2. `refactor: organize feature package structure`
3. `refactor: move screen state into viewmodels`
4. `refactor: add use cases for business logic`
5. `refactor: move hardcoded strings and values to resources`
6. `refactor: clean composable structure and previews`
7. `chore: clean gradle catalog usage`
8. `test: verify architecture refactor`

Do not mix unrelated feature behavior changes.

Do not change UI behavior unless required by the refactor.

Do not change database schema unless explicitly required.

Do not change Firebase data format unless explicitly required.

Do not change release signing, app id, version code, or version name.

## Required Validation

Run:

```bash
./gradlew clean :app:assembleDebug
./gradlew testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

Manual smoke test:

* app launch
* login
* logout
* add workout
* add exercises
* save workout
* edit workout
* start workout
* finish workout
* resume incomplete workout
* delete workout
* how-to videos
* settings screen
* back button behavior
* rotate/portrait behavior if locked

If behavior changes, document it in the PR.

## Fragment Communication Rules

Avoid Fragment-to-Activity listener interfaces.

Do not make a Fragment require its hosting Activity to implement an interface through `onAttach()`.

Avoid patterns like:

- `context as SomeListener`
- `activity as SomeListener`
- `requireActivity() as SomeListener`
- `parentFragment as SomeListener`
- listener interfaces inside Fragment classes
- Activity directly calling public Fragment mutation methods for screen state
- Fragment calling Activity methods for validation, business logic, add/update/delete actions, or navigation decisions

Preferred communication:

1. For fragments/screens inside the same feature, use a shared feature ViewModel.
2. Fragments/composables dispatch actions to the ViewModel.
3. ViewModel owns screen state and business decisions.
4. Activity observes one-time navigation/events from the ViewModel.
5. Activity should only handle platform work such as navigation, ActivityResult APIs, lifecycle setup, and Android SDK callbacks.
6. For truly independent fragments where a shared feature ViewModel is not appropriate, use Fragment Result API, not direct Activity listener interfaces.
7. Long-term, Compose screens should avoid Fragment wrappers where possible.

Fragments should not own business state if it can live in a ViewModel.

Activities should not act as business-logic mediators between fragments.

Use cases should be introduced for reusable business rules such as validation, duplicate checks, building models, saving, deleting, and navigation decisions.
