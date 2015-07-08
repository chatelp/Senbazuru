-renamesourcefileattribute SourceFile    
-keepattributes SourceFile,LineNumberTable
-keepnames class *

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# for picasso
-dontwarn com.squareup.okhttp.**
-dontwarn sun.misc.Unsafe

#for crashlytics
-keepattributes SourceFile,LineNumberTable


#for GCM - http://stackoverflow.com/questions/16229765/how-to-configure-proguard-for-android-application-using-google-drive-sdk/16279755#16279755
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault

-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keep class com.google.** { *;}
-keep interface com.google.** { *;}
-dontwarn com.google.**

# Needed by google-http-client-android when linking against an older platform version
-dontwarn com.google.api.client.extensions.android.**

# Needed by google-api-client-android when linking against an older platform version
-dontwarn com.google.api.client.googleapis.extensions.android.**