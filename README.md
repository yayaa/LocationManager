# LocationManager

<a href="http://developer.android.com/index.html" target="_blank"><img src="https://img.shields.io/badge/platform-android-green.svg"/></a> <a href="https://android-arsenal.com/api?level=14" target="_blank"><img src="https://img.shields.io/badge/API-14%2B-green.svg?style=flat"/></a> <a href="https://opensource.org/licenses/Apache-2.0" target="_blank"><img src="https://img.shields.io/badge/License-Apache_v2.0-blue.svg?style=flat"/></a> <a href="http://search.maven.org/#search%7Cga%7C1%7CLocationManager" target="_blank"><img src="https://img.shields.io/maven-central/v/com.yayandroid/LocationManager.svg"/></a>

<a href="http://www.methodscount.com/?lib=com.yayandroid%3ALocationManager%3A1.0.2" target="_blank"><img src="https://img.shields.io/badge/Methods count-283-e91e63.svg"></img></a> <a href="http://www.methodscount.com/?lib=com.yayandroid%3ALocationManager%3A1.0.2" target="_blank"><img src="https://img.shields.io/badge/Size-46 KB-e91e63.svg"></img></a> <a href="http://android-arsenal.com/details/1/3148" target="_blank"><img src="https://img.shields.io/badge/Android%20Arsenal-LocationManager-brightgreen.svg?style=flat"/></a>

To get location on Android Devices, you need to 
<ul>
<li>Check whether GPS Provider is enabled or not</li>
<li>Ask user to enable it if not</li>
<li>Check again whether user actually did enable it or not</li>
<li>If it is enabled, start location update request</li>
<li>But switch to network if GPS is not retrieving location data</li>
<li>Check whether Network Provider is enabled or not</li>
<li>If it is, start location update request</li>
<li>If none of these work, then fail</li>
</ul>

But wait, it didn't finish yet. Now we have Google Play Services optimised location provider [FusedLocationProviderApi][1] which provides a common location pool that any application use this api can retrieve location in which interval it requires. It reduces battery usage, and increases getting location period. But to implement this, you need to

<ul>
<li>Check whether Google Play Services is available on device</li>
<li>If not, and if user can handle it, ask user to do it</li>
<li>Then, check again to see did user actually do it or not</li>
<li>If still not, move on previous section and do all those steps</li>
<li>If user did actually handle it, then start location update request</li>
</ul>

All of these steps, just to retrieve user's current location. And in every application you build, you need to reconsider what you did and what you need to add for this time.

This library will provide you to handle all of these steps by creating a configuration object:
<ul>
<li>Do your optimisations,</li>
<li>Possible to change the way it works,</li> 
<li>Possible to change configuration on runtime,</li>
<li>Provide your own location provider mechanism,</li>
<li>Provide different configuration objects in different activities,</li>
</ul>
 
This library doesn't use singleton structure, so it will be specified to activity and it requires quite a lot lifecycle information to handle all the steps between onCreate - onResume - onPause - onDestroy - onActivityResult - onRequestPermissionsResult. You can use [LocationBaseActivity][2] or you can manually call those methods in your activity.

[See the sample application][3] for detailed usage

## Configuration

All those options below are optional. Use only those you really want to customize. If you don't set rationalMessage then library will skip the rational message to show user. But for GPS Message, if you set askForEnableGPS true, then you need to specify the message, otherwise it will throw an exception.

```java 
new LocationConfiguration()
                .keepTracking(true)
                .useOnlyGPServices(false)
                .askForGooglePlayServices(true)
                .askForSettingsApi(true)
                .failOnConnectionSuspended(true)
                .failOnSettingsApiSuspended(false)
                .doNotUseGooglePlayServices(false)
                .askForEnableGPS(true)
                .setMinAccuracy(200.0f)
                .setWithinTimePeriod(60 * 1000)
                .setTimeInterval(10 * 1000)
                .setWaitPeriod(ProviderType.GOOGLE_PLAY_SERVICES, 5 * 1000)
                .setWaitPeriod(ProviderType.GPS, 15 * 1000)
                .setWaitPeriod(ProviderType.NETWORK, 10 * 1000)
                .setLocationRequest(YourLocationRequest)
                .setGPSMessage("Would you mind to turn GPS on?")
                .setRationalMessage("Gimme the permission!");
``` 
In [LocationConfiguration][4] class, all of these methods are explained as when and why to use.

Besides these configurations you can also create your own provider which extends [LocationProvider][5] class and implement your own logic instead of using [DefaultLocationProvider][6]. When you call setLocationProvider method below on LocationManager it will pass the configuration you defined and the activity instance to your provider, so you can count on those values as well.

```java 
getLocationManager().setLocationProvider(new YourOwnLocationProvider());
```

Library has a lot of log implemented in it, so you can set your [LogType][7] to get how much information you need to. Suggested to use LogType.GENERAL in debug mode and LogType.NONE in release mode though. Manager has a static method to change logType configuration. As default it is set to LogType.IMPORTANT in order to display only important steps...

```java 
LocationManager.setLogType(LogType.IMPORTANT);
```

## AndroidManifest

Below permissions are required while using NetworkProvider, and they are not in "Dangerous Permissions" categories.

```html 
<!-- Required to check whether user has network connection or not -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```

But these permissions are in "Dangerous Permissions" categories, so library will ask for permission on Android M and above.

```html 
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Download
Add library dependency to your `build.gradle` file:

```groovy
dependencies {    
     compile 'com.yayandroid:LocationManager:1.1.5'
}
```

## License
```
Copyright 2016 yayandroid

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[1]: https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi
[2]: https://github.com/yayaa/LocationManager/blob/master/Library/app/src/main/java/com/yayandroid/locationmanager/LocationBaseActivity.java
[3]: https://github.com/yayaa/LocationManager/blob/master/Sample/app/src/main/java/com/yayandroid/locationmanager/sample/MainActivity.java
[4]: https://github.com/yayaa/LocationManager/blob/master/Library/app/src/main/java/com/yayandroid/locationmanager/LocationConfiguration.java
[5]: https://github.com/yayaa/LocationManager/blob/master/Library/app/src/main/java/com/yayandroid/locationmanager/provider/LocationProvider.java
[6]: https://github.com/yayaa/LocationManager/blob/master/Library/app/src/main/java/com/yayandroid/locationmanager/provider/DefaultLocationProvider.java
[7]: https://github.com/yayaa/LocationManager/blob/master/Library/app/src/main/java/com/yayandroid/locationmanager/constants/LogType.java
