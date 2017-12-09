# BuoyNOW
Udacity Capstone Project

There are 2 places to fill in for the same API keys in the mobile AndroidManifest.xml

Please replace the ***API Key*** with a working API Key
<meta-data
     android:name="com.google.android.awareness.API_KEY"
     android:value="***API Key***" />
<meta-data
     android:name="com.google.android.geo.API_KEY"
     android:value="***API Key***" />

There are changes and adjustment throughout developing of the app 
(e.g. Google does not accept custom voice action command anymore , therefore activity will not be voice activated) 
An updated project plan Capstone_Stage_Updated.pdf also attached.

-------------------------------------------------------
08/28/2017 - Rejected because using gradle beta version

Solution: Changed build.gradle as below:
   dependencies {
        //classpath 'com.android.tools.build:gradle:3.0.0-beta3'
        classpath 'com.android.tools.build:gradle:2.3.3'

However, the previously added Android Studio 3 beta 3 Wear Modules DOES NOT compatible with the rolled back Gradle.
Since the app does not develop the wear modules anyway, the settings.gradle has been updated:
     // include ':mobile', ':wear'
     include ':mobile'
In addition the new command for build.gradle "implementation" has to change back to "compile"

Also, change the App build.gradle to exclude wear modules
     // wearApp project(':wear')
     
-------------------------------------------------------
08/29/2017 - Rejected because Strings and Signature

1. Clean up hardcoded string refering to Analyze tool on Android Studio.
2. Created InstallRelease Gradle build.
3. Created a new keystore: cap_keystore and add it to the root of the project
4. Created a new key alias: udacity and ssigned key to project
