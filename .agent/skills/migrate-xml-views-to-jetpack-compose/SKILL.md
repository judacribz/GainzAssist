# Android XML-to-Compose Migration Skill

## Purpose
This skill defines the canonical workflow for migrating an existing Android screen, fragment, or custom view from XML layout to Jetpack Compose while strictly preserving visual parity and existing behavior.

## Core Rules
1. **Migration is UI-Only**: Do not refactor architecture, ViewModels, database schemas, network calls, or navigation logic during the migration.
2. **Preserve Visual/Functional Parity**: The new Compose UI must look and act identically to the legacy XML UI unless otherwise directed.
3. **Delete Only When Unused**: Do not delete the XML layout until the Compose equivalent is complete, validated, and all references to the XML file are removed from the project.
4. **Mandatory Previews**: Every top-level (screen) composable must have a `@Preview` function to visualize its state.
5. **No Kotlin Not-Null Assertions**: Do not use `!!` in migrated Compose screens, Activity/Fragment lifecycle code, callbacks, or UI state mapping. Use safe calls (`?.`), Elvis defaults (`?:`), `takeIf`, explicit validation, or early returns. If a required value is missing, fail safely by showing validation state, returning from the action, or preserving the previous valid value.

## Step-by-Step Workflow

### 1. Identify and Analyze Candidate
* Review the target Activity/Fragment and its associated XML layout(s).
* Identify state variables and how they map to the UI.
* Identify UI actions (clicks, swipes) and how they trigger logic.
* Note custom views, themes, dimensions, and drawable resources.

### 2. Capture Baseline
* (If possible) Take a screenshot of the legacy screen in various states to ensure parity later.
* Ensure existing UI and unit tests pass before making changes.

### 3. Setup Compose Foundation
* Create a generic `Theme.kt` for Jetpack Compose that mirrors the existing XML theme if one does not exist for the module.
* Create a new file (e.g., `[ScreenName]Screen.kt`) and an empty top-level Composable function.
* Add a minimal `@Preview` block.

### 4. Migrate the Layout
* Translate the XML view hierarchy into Compose equivalents (e.g., `LinearLayout` to `Column`/`Row`, `FrameLayout` to `Box`, `TextView` to `Text`).
* Translate layout parameters, margins, and padding into Compose `Modifier` equivalents.
* Bind the Composable parameters to the UI component properties.

### 5. Replace Usage via Interoperability
* In the target Activity/Fragment, locate `setContentView` or ViewBinding setup.
* Replace the root layout inflation with a `ComposeView` block (or `setContent` for Activities).
* Invoke the new top-level Composable and map state variables to its inputs, and existing action handlers to its lambda parameters.
* When mapping Fragment/Activity state into Compose, avoid `!!`. Fragment views can be recreated and callbacks can be detached, so use nullable-safe access and lifecycle-aware guards.

### 6. Validate Parity
* Run the app and visually inspect the migrated screen against the baseline.
* Verify all interactions, animations, and inputs trigger the original logic correctly.
* Search the migrated files for `!!` and remove any usage before opening the PR:
  `grep -R "!!" app/src/main/java`
* Run existing tests.

### 7. Clean Up
* Remove the old ViewBinding initialization if no longer needed.
* Delete the target XML layout *only if* it has zero remaining usages in the codebase.
* Remove references to unused view IDs.
