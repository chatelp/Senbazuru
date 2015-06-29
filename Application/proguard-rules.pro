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

