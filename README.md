# LocationManager

<a href="http://developer.android.com/index.html" target="_blank"><img src="https://img.shields.io/badge/platform-android-green.svg"/></a> <a href="https://android-arsenal.com/api?level=14" target="_blank"><img src="https://img.shields.io/badge/API-14%2B-green.svg?style=flat"/></a> [![codecov](https://codecov.io/gh/yayaa/LocationManager/branch/master/graph/badge.svg)](https://codecov.io/gh/yayaa/LocationManager) <a href="http://www.methodscount.com/?lib=com.yayandroid%3ALocationManager%3A2.0.0"><img src="https://img.shields.io/badge/Methods count-517-e91e63.svg" target="_blank"/></a> <a href="http://www.methodscount.com/?lib=com.yayandroid%3ALocationManager%3A2.0.0"><img src="https://img.shields.io/badge/Size-70 KB-e91e63.svg" target="_blank"/></a>

<a href="https://opensource.org/licenses/Apache-2.0" target="_blank"><img src="https://img.shields.io/badge/License-Apache_v2.0-blue.svg?style=flat"/></a> <a href="http://search.maven.org/#search%7Cga%7C1%7CLocationManager" target="_blank"><img src="https://img.shields.io/maven-central/v/com.yayandroid/LocationManager.svg"/></a> <a href="http://android-arsenal.com/details/1/3148" target="_blank"><img src="https://img.shields.io/badge/Android%20Arsenal-LocationManager-brightgreen.svg?style=flat"/></a>

To get location on Android Devices, there are 'some' steps you need to go through!
What are those? Let's see...

<ul>
<li>Check whether the application has required permission or now</li>
<li>If not, ask runtime permissions from user</li>
<li>Check whether user granted the permissions or not</li>
</ul>

Let's assume we got the permission, now what?
We have this cool Google Play Services optimised location provider [FusedLocationProviderApi][1] which provides a common location pool that any application use this api can retrieve location in which interval they require. It reduces battery usage, and decreases waiting time (most of the time) for location.
YES, we want that! Right?

<ul>
<li>Check whether Google Play Services is available on device</li>
<li>If not, see if user can handle this problem?</li>
<li>If so, ask user to do it</li>
<li>Then, check again to see if user actually did it or not</li>
<li>If user did actually handle it, then start location update request</li>
<li>Of course, handle GoogleApiClient connection issues first</li>
<li>And have a fallback plan, if you're not able to get location in certain time period</li>
</ul>

Ouu and yes, this new even cooler SettingsApi, that user doesn't need to go to settings to activate GPS or Wifi. Isn't that cool, let's implement that too!

<ul>
<li>Call SettingsApi to adapt device settings up to your location requirement</li>
<li>Wait for the result and check your options</li>
<li>Is current settings are enough to get required location?</li>
<li>Or can you ask user to adapt them?</li>
<li>Maybe it is not even possible to use it.</li>
<li>Let's assume we asked user to adapt, but he/she didn't want to do it.</li>
</ul>

Well whatever we tried to optimise, right? But till now, all depends on GooglePlayServices what happens if user's device doesn't have GooglePlayServices, or user didn't want to handle GooglePlayServices issue or user did everything and waited long enough but somehow GooglePlayServices weren't able to return any location. What now?
Surely we still have good old times GPS and Network Providers, right? Let's switch to them and see what we need to do!

<ul>
<li>Check whether GPS Provider is enabled or not</li>
<li>If it is not, ask user to enable it</li>
<li>Check again whether user actually did enable it or not</li>
<li>If it is enabled, start location update request</li>
<li>But switch to network if GPS is not retrieving location after waiting long enough</li>
<li>Check whether Network Provider is enabled or not</li>
<li>If it is, start location update request</li>
<li>If none of these work, then fail</li>
</ul>

All of these steps, just to retrieve user's current location. And in every application, you need to reconsider what you did and what you need to add for this time.

<b>With this library you just need to provide a Configuration object with your requirements, and you will receive a location or a fail reason with all the stuff are described above handled.</b>
 
This library requires quite a lot of lifecycle information to handle all the steps between onCreate - onResume - onPause - onDestroy - onActivityResult - onRequestPermissionsResult.
You can simply use one of [LocationBaseActivity][2], [LocationBaseFragment][3], [LocationBaseService][4] or you can manually call those methods as required.

[See the sample application][5] for detailed usage!

## Configuration

All those settings below are optional. Use only those you really want to customize. Please do not copy-paste this configuration directly. If you want to use pre-defined configurations, see [Configurations][6].

```java 
LocationConfiguration awesomeConfiguration = new LocationConfiguration.Builder()
    .keepTracking(false)
    .askForPermission(new PermissionConfiguration.Builder()
        .permissionProvider(new YourCustomPermissionProvider())
        .rationalMessage("Gimme the permission!")
        .rationaleDialogProvider(new YourCustomDialogProvider())
        .requiredPermissions(new String[] { permission.ACCESS_FINE_LOCATION })
        .build())
    .useGooglePlayServices(new GooglePlayServicesConfiguration.Builder()
        .locationRequest(YOUR_CUSTOM_LOCATION_REQUEST_OBJECT)
        .fallbackToDefault(true)
        .askForGooglePlayServices(false)
        .askForSettingsApi(true)
        .failOnConnectionSuspended(true)
        .failOnSettingsApiSuspended(false)
        .ignoreLastKnowLocation(false)
        .setWaitPeriod(20 * 1000)
        .build())
    .useDefaultProviders(new DefaultProviderConfiguration.Builder()
        .requiredTimeInterval(5 * 60 * 1000)
        .requiredDistanceInterval(0)
        .acceptableAccuracy(5.0f)
        .acceptableTimePeriod(5 * 60 * 1000)
        .gpsMessage("Turn on GPS?")
        .gpsDialogProvider(new YourCustomDialogProvider())
        .setWaitPeriod(ProviderType.GPS, 20 * 1000)
        .setWaitPeriod(ProviderType.NETWORK, 20 * 1000)
        .build())
    .build();
```

Library is modular enough to let you create your own way for Permission request, Dialog display, or even a whole LocationProvider process. (Custom LocationProvider implementation is described below in LocationManager section)

You can create your own [PermissionProvider][7] implementation and simply set it to [PermissionConfiguration][8], and then library will use your implementation. Your custom PermissionProvider implementation will receive your configuration requirements from PermissionConfiguration object once it's built. If you don't specify any PermissionProvider to PermissionConfiguration [DefaultPermissionProvider][9] will be used. If you don't specify PermissionConfiguration to LocationConfiguration [StubPermissionProvider][10] will be used instead.

You can create your own [DialogProvider][11] implementation to display `rationale message` or `gps request message` to user, and simply set them to required configuration objects. If you don't specify any [SimpleMessageDialogProvider][12] will be used as default.

## LocationManager

Ok, we have our configuration object up to requirements, now we need a manager configured with it.

```java
// LocationManager MUST be initialized with Application context in order to prevent MemoryLeaks
LocationManager awesomeLocationManager = new LocationManager.Builder(getApplicationContext())
    .activity(activityInstance) // Only required to ask permission and/or GoogleApi - SettingsApi
    .fragment(fragmentInstance) // Only required to ask permission and/or GoogleApi - SettingsApi
    .configuration(awesomeConfiguration)
    .locationProvider(new YourCustomLocationProvider())
    .notify(new LocationListener() { ... })
    .build();
```

LocationManager doesn't keep strong reference of your activity **OR** fragment in order not to cause any memory leak. They are required to ask for permission and/or GoogleApi - SettingsApi in case they need to be resolved.

You can create your own [LocationProvider][13] implementation and ask library to use it. If you don't set any, library will use [DispatcherLocationProvider][14], which will do all the stuff is described above, as default.

Enough, gimme the location now!

```java
awesomeLocationManager.get();
```

Done! Enjoy :)

## Logging

Library has a lot of log implemented, in order to make tracking the process easy, you can simply enable or disable it.
It is highly recommended to disable in release mode.

```java 
LocationManager.enableLog(false);
```

## Restrictions
If you are using LocationManager in a
- Fragment, you need to redirect your `onActivityResult` to fragment manually, because GooglePlayServices Api and SettingsApi calls `startActivityForResult` from activity. For the sample implementation please see [SampleFragmentActivity][15].
- Service, you need to have the permission already otherwise library will fail immediately with PermissionDenied error type. Because runtime permissions can be asked only from a fragment or an activity, not from a context. For the sample implementation please see [SampleService][16].

## AndroidManifest

Library requires 3 permission;
 - 2 of them `ACCESS_NETWORK_STATE` and `INTERNET` are not in `Dangerous Permissions` and they are required in order to use Network Provider. So if your configuration doesn't require them, you don't need to define them, otherwise they need to be defined.
 - The other one is `ACCESS_FINE_LOCATION` and it is marked as `Dangerous Permissions`, so you need to define it in Manifest and library will ask runtime permission for that if the application is running on Android M or higher OS  version. If you don't specify in Manifest, library will fail immediately with PermissionDenied when location is required.

```html
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

You might also need to consider information below from [the location guide page.][17]

<blockquote>
<b>Caution:</b> If your app targets Android 5.0 (API level 21) or higher, you must declare that your app uses the android.hardware.location.network or android.hardware.location.gps hardware feature in the manifest file, depending on whether your app receives location updates from NETWORK_PROVIDER or from GPS_PROVIDER. If your app receives location information from either of these location provider sources, you need to declare that the app uses these hardware features in your app manifest. On devices running versions prior to Android 5.0 (API 21), requesting the ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission includes an implied request for location hardware features. However, requesting those permissions does not automatically request location hardware features on Android 5.0 (API level 21) and higher.
</blockquote>

## Download
Add library dependency to your `build.gradle` file:

```groovy
dependencies {    
     compile 'com.yayandroid:LocationManager:x.y.z'
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
[2]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/base/LocationBaseActivity.java
[3]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/base/LocationBaseFragment.java
[4]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/base/LocationBaseService.java
[5]: https://github.com/yayaa/LocationManager/tree/master/app
[6]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/configuration/Configurations.java
[7]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/providers/permissionprovider/PermissionProvider.java
[8]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/configuration/PermissionConfiguration.java
[9]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/providers/permissionprovider/DefaultPermissionProvider.java
[10]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/providers/permissionprovider/StubPermissionProvider.java
[11]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/providers/dialogprovider/DialogProvider.java
[12]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/providers/dialogprovider/SimpleMessageDialogProvider.java
[13]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/providers/locationprovider/LocationProvider.java
[14]: https://github.com/yayaa/LocationManager/blob/master/library/src/main/java/com/yayandroid/locationmanager/providers/locationprovider/DispatcherLocationProvider.java
[15]: https://github.com/yayaa/LocationManager/blob/master/app/src/main/java/com/yayandroid/locationmanager/sample/fragment/SampleFragmentActivity.java
[16]: https://github.com/yayaa/LocationManager/blob/master/app/src/main/java/com/yayandroid/locationmanager/sample/service/SampleService.java
[17]:https://developer.android.com/guide/topics/location/strategies.html
