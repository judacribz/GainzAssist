# Gainz Assist #

## Release Build Instructions

To build a signed release Android App Bundle (AAB) for Play Store upload, you must provide your private signing configuration. Do not commit your real keys or passwords to version control.

### Setup

1. Copy the template properties file:
   ```bash
   cp keystore.properties.template keystore.properties
   ```
2. Edit `keystore.properties` and fill in your actual values:
   - `storeFile`: The absolute or relative path to your release `.jks` or `.keystore` file.
   - `storePassword`: The keystore password.
   - `keyAlias`: The key alias.
   - `keyPassword`: The key password.

Alternatively, you can provide these values via environment variables: `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`.

### Building

Once configured, run the following command to generate the release AAB:
```bash
./gradlew :app:bundleRelease
```

The output bundle will be located at:
`app/build/outputs/bundle/release/app-release.aab`
