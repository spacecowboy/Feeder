# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/jonas/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontobfuscate

-printusage build/removed_code.txt

-keep class com.nononsenseapps.feeder.** { *; }
-keep class com.nononsenseapps.jsonfeed.** { *; }

# Just keep it all
-keep class com.nononsenseapps.filepicker.** { *; }

-keep class org.joda.time.** { *; }
-keep class org.jsoup.** { *; }
-keep class org.ccil.cowan.tagsoup.** { *; }
-keep class com.rometools.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keep class org.conscrypt.** { *; }
-keep class com.bumptech.glide.** { *; }

-keep class androidx.** { *; }
-keep class androidx.appcompat.app.AppCompatDelegate
-keep interface androidx.** { *; }
