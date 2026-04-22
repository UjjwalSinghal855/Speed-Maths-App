# ──────────────────────────────────────────────────────────────────────────────
# SpeedMath ProGuard Rules
# ──────────────────────────────────────────────────────────────────────────────

# Keep Room entity classes (needed for reflection-based DB access)
-keep class com.ujjwal.speedmath.data.entity.** { *; }

# Keep MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Keep Kotlin coroutines internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
