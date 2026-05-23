# Release Configuration

## Required Files
- `local.defaults.properties` (contains blank defaults, checked into source control)
- `secrets.properties` (contains actual secrets, **ignored by source control**)
- `app/google-services.json` (Firebase configuration, **ignored by source control** unless public)

## Required Keys in `secrets.properties`
- `FACEBOOK_APP_ID`: Your Facebook App ID.
- `FACEBOOK_CLIENT_TOKEN`: Your Facebook Client Token.
- `FB_LOGIN_PROTOCOL_SCHEME`: Format: `fb<FACEBOOK_APP_ID>`.
- `GOOGLE_API_KEY`: API Key with YouTube Data API v3 enabled.

## Where to Get Each Key
- **Facebook keys**: Meta for Developers -> App Dashboard -> Settings -> Basic / Advanced.
- **Google API Key**: Google Cloud Console -> APIs & Services -> Credentials.

## ⚠️ WARNING
**Do not commit real secrets!** Ensure `secrets.properties` and `app/google-services.json` are not committed to source control. Only commit the template file (`secrets.properties.template`).

## Local Validation Steps
1. Create a `secrets.properties` file in the root directory and fill in your actual keys.
2. Ensure you have `app/google-services.json`.
3. Run `./gradlew :app:assembleDebug` to verify local debug build.
4. Run `./gradlew :app:bundleRelease` to verify the release build works with your secrets.
