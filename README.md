# Office 365 Connect Sample for Android

[![Office 365 Connect sample](/readme-images/O365-Android-Connect-video_play_icon.png)](https://www.youtube.com/watch?v=3IQIDFrqhY4 "Click to see the sample in action")

Connecting to Office 365 is the first step every Android app must take to start working with Office 365 services and data. This sample shows how to connect and then call one API.

## Device requirements

To run the Connect sample, your device needs to meet the following requirements:

* A screen size of 800 x 480 or larger.
* Android API level 15 or later.
 
## Prerequisites

To use the Office 365 Connect sample for Android you need the following:

* [Android Studio](http://developer.android.com/sdk/index.html) version 1.0 or later.
* An Office 365 account. You can sign up for [an Office 365 Developer subscription](http://aka.ms/o365-android-connect-signup) that includes the resources that you need to start building Office 365 apps.
* A client id and redirect uri values of an application registered in Azure. The application must run the **Send mail as a user** permission:

You can also [add a native client application in Azure](http://aka.ms/o365-android-connect-addapp) and [grant proper permissions](https://github.com/OfficeDev/O365-Android-Connect/wiki/Grant-permissions-to-the-Connect-application-in-Azure) to it.

## Open the sample using Android Studio

1. Install [Android Studio](http://developer.android.com/tools/studio/index.html#install-updates) and add the Android SDK packages according to the [instructions](http://developer.android.com/sdk/installing/adding-packages.html) on developer.android.com.
2. Download or clone this sample.
3. Start Android Studio.
	1. Select **Open an existing Android Studio project**.
	2. Browse to your local repository and select the O365-Android-Connect project. Click **OK**.
4. Open the Constants.java file.
	1. Find the CLIENT\_ID constant and set its String value equal to the client id you registered in Azure Active Directory.
	2. Find the REDIRECT\_URI constant and set its String value equal to the redirect URI you registered in Azure Active Directory.
    ![Office 365 Connect sample](/readme-images/O365-Android-Connect-Constants.png "Client ID and Redirect URI values in Constants file")

    > Note: If you have don't have CLIENT\_ID and REDIRECT\_URI values, [add a native client application in Azure](https://msdn.microsoft.com/library/azure/dn132599.aspx#BKMK_Adding) and take note of the CLIENT\_ID and REDIRECT_URI.

Once you've built the Connect sample, you can run it on an emulator or device.

If you want to know more about the sample, visit our [understanding the code](https://github.com/OfficeDev/O365-Android-Connect/wiki/Understanding-the-Connect-sample-code) wiki page.

## Questions and comments

We'd love to hear your feedback on this Connect sample for Android. Here's how you can send your questions and suggestions to us:

* In the [Issues](https://github.com/OfficeDev/O365-Android-Connect/issues) section of this repository.
* Send us an email to [docthis@microsoft.com](mailto:docthis@microsoft.com?subject=Feedback%20on%20the%20Office%20365%20Connect%20sample%20for%20Android).

## Next steps

This sample just shows the essentials that your apps need to work with Office 365. There is so much more that your apps can do using the Office 365 APIs, like helping your users to manage their work day with calendar, find just the information they need in all the files they store in OneDrive, or find the exact person they need from their list of contacts. We have more to share with you in the [Office 365 APIs Starter Project for Android](https://github.com/officedev/O365-Android-Start/). We think it can help you fuel your ideas. 
  
## Additional resources

* [Office 365 APIs documentation](http://aka.ms/o365-android-connect-platformoverview)
* [Office 365 SDK for Android](http://aka.ms/o365-android-connect-sdk)
* [Office Dev Center for Android](http://aka.ms/o365-android-connect-getstarted)
* [Office 365 APIs starter projects and code samples](http://aka.ms/o365-android-connect-codesamples)

## Copyright
Copyright (c) 2015 Microsoft. All rights reserved.