# SeChat ProGuard Rules

# Keep crypto classes
-keep class com.sechat.core.crypto.** { *; }

# Keep data model classes
-keep class com.sechat.core.data.database.model.** { *; }

# Keep Koin
-keep class org.koin.** { *; }
