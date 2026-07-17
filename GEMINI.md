# GainzAssist Android Agent Instructions

You are assisting with the GainzAssist XML to Jetpack Compose migration.

## Local reference project

Use the Gainz Bae project as a local style and architecture reference:

~/AndroidStudioProjects/gainz-bae/

Do not copy code blindly. Use it only for conventions, package structure, Compose style, previews, UI state naming, and project organization.

## Migration source skill

Follow the Android XML to Jetpack Compose migration skill located at:

.agent/skills/migrate-xml-views-to-jetpack-compose/SKILL.md

## Rules

- Migrate one screen or XML layout at a time.
- Start with the Login screen.
- Preserve existing behavior before improving design.
- Keep ViewModel, repository, Firebase, Room, and navigation behavior unchanged unless required for interoperability.
- Use Compose inside existing Activities or Fragments first through ComposeView.
- Do not delete XML until the Compose replacement is validated and all references are removed.
- Add a Compose Preview for every new screen-level composable.
- Add UI tests or screenshot validation where practical.
- Keep changes small and reviewable.
