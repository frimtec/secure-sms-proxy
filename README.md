# Secure SMS Proxy (S2SMP)
[![Build Status](https://travis-ci.org/frimtec/secure-sms-proxy.svg?branch=master)](https://travis-ci.org/frimtec/secure-sms-proxy) 
[![Coverage Status](https://coveralls.io/repos/github/frimtec/secure-sms-proxy/badge.svg?branch=master)](https://coveralls.io/github/frimtec/secure-sms-proxy?branch=master)
[![license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

![Deactivated PAssist](app/src/main/res/mipmap-hdpi/ic_launcher.png) 

As Google strongly restricts the use of SMS permissions for applications on the Play-Store, S2SMP provides a API for third party applications
to send and receive SMS to specific phone numbers via a secure SMS proxy.
Each application that wants to send/receive SMS to a phone number can register itself on S2SMP. 
S2SMP asks the user for permission if the requesting application is allowed to send/receive SMS from and to the requested phone number.
All SMS exchanged with third party applications are strongly encrypted.
Beside SMS permissions, S2SMP does not request any other permissions to ensure the integrity and security of S2SMP.

## Installation
S2SMP cannot be offered on Google-Play as Google does only allow applications using SMS permissions for very rare cases. 

### Install pre build APK
You can download the APK file from the [GitHub release page](https://github.com/frimtec/secure-sms-proxy/releases).
To install the APK you need to disable "Play Protect" in "Google Play" for the time of the installation (don't forget to re-enable "Play Protect" after the installation). 
This is only required for the first installation. Updates can be installed with "Play Protect" enabled.

### Self build
Build S2SMP on your own and then install the APK via ADB to your android phone.

## Integrating applications with S2SMP
S2SMP provides an easy API to integrate applications. The API supports the registration process, sending and receiving SMS, 
as well querying if specific phone numbers are already granted for the application.  

## Supported languages
Currently the following languages are supported in S2SMP:
* English
* German

## Open-Source and free
S2SMP is Open-Source and available under Apache-2.0 licence.
If you find S2SMP useful and use it on a regular basis for your on-call duties, a voluntary donation is warmly welcome.

## Disclaimer
The use of S2SMP is at your own risk. The author assumes no liability for malfunctions of the application.
Any warranty claims are excluded.

## Development
S2SMP is developed with [Android-Studio 3.5](https://developer.android.com/studio) with Java 8.
The current Android target SDK is 29 (Android 10-Q) and the minimal SDK is 24 (Android 7.0-Nougat).

## Feedback
Feedback, bug reports or feature requests are very welcome.
You can send an email to [frimtec@gmx.ch](mailto:frimtec@gmx.ch) or [open an issue on GitHub](https://github.com/frimtec/secure-sms-proxy/issues).
