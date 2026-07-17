# ===== Retrofit =====
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
# R8 full mode: keep generic signatures of Call/Response and DAO interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep interface com.iepca.app.dao.** { *; }

# ===== OkHttp / Okio =====
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# ===== Gson: keep models and enum SerializedName mappings =====
-keep class com.google.gson.** { *; }
-keepattributes SerializedName
-keep class com.iepca.app.model.** { *; }
-keepclassmembers enum com.iepca.app.model.enums.** { *; }
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===== SLF4J (logging facade used across the app) =====
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# ===== Glide =====
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }

# ===== MPAndroidChart =====
-keep class com.github.mikephil.charting.** { *; }

# ===== Firebase Messaging =====
-keep class com.google.firebase.messaging.** { *; }

# ===== Google Play Services (location) =====
-dontwarn com.google.android.gms.**

# ===== osmdroid (OpenStreetMap) =====
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# ===== CircleImageView =====
-keep class de.hdodenhof.circleimageview.** { *; }
