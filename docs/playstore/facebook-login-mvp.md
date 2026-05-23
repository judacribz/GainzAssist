# Facebook Login MVP Release Toggle

This document explains how to disable or enable Facebook login functionality for an MVP Play Store release.
Facebook login requires extensive review and configuration (such as data deletion callbacks and valid Privacy Policies), which can complicate or delay an initial release.

## How to Toggle Facebook Login

The feature toggle is controlled by the `ENABLE_FACEBOOK_LOGIN` property inside your `secrets.properties` file.

### Disabling Facebook Login (Recommended for MVP)
To disable Facebook login, ensure the following line is set in `secrets.properties`:

```properties
ENABLE_FACEBOOK_LOGIN=false
```

When set to `false`:
- The Facebook login button is hidden on the login screen.
- Facebook app events are not activated.
- You can leave other Facebook properties (`FACEBOOK_APP_ID`, `FACEBOOK_CLIENT_TOKEN`, `FB_LOGIN_PROTOCOL_SCHEME`) blank or set to dummy values without causing the app to crash in release mode.
- Email and Google login remain functional.

### Enabling Facebook Login
To enable Facebook login, set the property to `true`:

```properties
ENABLE_FACEBOOK_LOGIN=true
```

When set to `true`:
- The Facebook login button will be visible.
- The Facebook app events will be activated.
- **Important:** All required Facebook configuration properties *must* be correctly populated in `secrets.properties` (otherwise the app will crash in release mode to prevent shipping an invalid config).

Required properties:
- `FACEBOOK_APP_ID`
- `FACEBOOK_CLIENT_TOKEN`
- `FB_LOGIN_PROTOCOL_SCHEME`

## Play Store MVP Recommendation

If you have not fully verified and reviewed your Facebook login configuration via the Meta Developer portal (and the corresponding Play Store Data Safety section), it is highly recommended to disable Facebook login (`ENABLE_FACEBOOK_LOGIN=false`) for your initial MVP launch. Once the app is live and you've verified all external dependencies, you can toggle it back on and push an update.
