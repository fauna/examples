# Android Example

This is an example project created with [Android Studio 3.4.1](https://developer.android.com/studio)

## Prerequisites

Obtain an API Key and update the `fauna_secret` in the [strings.xml](app/src/main/res/values/strings.xml) file

## Create your own project

If you want to create a new project from scratch, follow the steps:

* pick `Java` as Language (default)
* create a new Android project, the minimum SDK version is: `26`
* add internet permission in the AndroidManifest.xml: 
```
<uses-permission android:name="android.permission.INTERNET" />
```
* add FaunaDB dependency in `build.gradle` file:
```
dependecies {
	// .. other dependencies
	implementation 'com.faunadb:faunadb-java:2.6.2'
}
```
* add extra configs in `build.gradle` file: 
```
android {	
	packagingOptions {
        exclude 'META-INF/INDEX.LIST'
        exclude 'META-INF/io.netty.versions.properties'
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

