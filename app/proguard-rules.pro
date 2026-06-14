# Preserve useful Crashlytics stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep metadata/annotations used by Room, Parceler, Firebase, and generated code
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Parceler - scoped to app models only
-keep @org.parceler.Parcel class ca.gainzassist.domain.model.** { *; }
-keep class ca.gainzassist.domain.model.**$$Parcelable { *; }
-keep class ca.gainzassist.domain.model.**$$Parcelable$Creator { *; }
-keepnames class * implements android.os.Parcelable

# Room-backed domain models also used by Parceler/Firebase map interop
-keep class ca.gainzassist.domain.model.** { *; }

# Firebase service and Firebase helpers
-keep class ca.gainzassist.data.service.FirebaseService { *; }
-keep class ca.gainzassist.data.remote.firebase.** { *; }

# App and Activity entry points
-keep class ca.gainzassist.App { *; }
-keep class ca.gainzassist.activities.**Activity { *; }
-keep class ca.gainzassist.activities.**.view.**Activity { *; }

# Koin modules
-keep class ca.gainzassist.core.di.** { *; }

# YouTube player integration
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }

# Facebook SDK - scoped rules only
-keep class com.facebook.FacebookActivity { *; }
-keep class com.facebook.CustomTabActivity { *; }
-keep class com.facebook.login.LoginManager { *; }
-keep class com.facebook.login.LoginResult { *; }
-keep class com.facebook.CallbackManager { *; }
-keep class com.facebook.FacebookCallback { *; }
-keep class com.facebook.FacebookException { *; }
-keep class com.facebook.AccessToken { *; }