_其他语言版本_: [简体中文](https://github.com/AgoraIO-Usecase/eEducation/wiki/eEducation-5.0.0-%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97)

> This page introduces how to use eEducation 5.0.0. If you use eEducation 6.0.0, please refer to [eEducation 6.0.0 Project Guide](https://github.com/AgoraIO-Usecase/eEducation/wiki/eEducation-6.0.0-Project-Guide).

## <a name="overview"></a>About the project
eEducation is a sample project provided by Agora for developers in the education industry, which demonstrates how to use **Agora Edu Cloud Service**, **Agora RTC SDK**, **Agora RTM SDK**, **Agora Cloud Recording**, and the third-party **Netless whiteboard SDK** to quickly implement basic online interactive tutoring scenarios.

### <a name="scene"></a>Applicable scenarios
eEducation supports the following scenarios:

* One-to-one Classroom: An online teacher gives an exclusive lesson to only one student, and both can interact in real time.
* Small Classroom: A teacher gives an online lesson to multiple students, and both the teacher and students can interact with each other in real time. The number of students in a small classroom should not exceed 16.
* Lecture Hall: Thousands of students watch an online lecture together. Students can "raise their hands" to interact with the teacher, and their responses are viewed by all the other students at the same time.

### <a name="platform"></a>Platform compatibility
eEducation supports the following platforms and versions:

* iOS 10 or later. We do not test Agora e-Education on iOS 9 updates.
* Android 4.4 and later.
* Web Chrome 72 and later. We do not test Agora e-Education on browsers.

### <a name="function"></a>Functions
| Function | Web (Teacher) | Web (Student) | iOS and Android (Student) | Note |
| :------- | :------------ | :------------ | :------------------------ | :--- |
| Real-time audio and video communication | ✅ | ✅ | ✅ | / |
| Real-time messaging | ✅ | ✅ | ✅ | / |
| Interactive Whiteboard | ✅ | ✅ One-to-one Classroom <br> ✅ Small Classroom <br> ❌ Lecture Hall | ✅ One-to-one Classroom <br> ✅ Small Classroom <br> ❌ Lecture Hall | In a one-to-one classroom, both the teacher and students can draw on the whiteboard by default. In a small classroom, students cannot draw on the whiteboard by default, but the teacher can give a student the permission of drawing on the whiteboard. In a lecture hall, students can never draw on the whiteboard. |
| Whiteboard follow | ✅ | ✅ | ✅ | The teacher can enable "whiteboard follow". When the teacher is moving the whiteboard or turning pages, the whiteboard area that students see will be consistent with the teacher's whiteboard area. |
| Uploading files (PPT, Word, PDF, audio files or video files) | ✅ | ❌ | ❌ | Only teachers can upload files to the classroom.
| Students raising hands | ✅ | ✅ | ✅ | In a lecture hall, students do not send their audio and video by default, but they can "raise their hands" to apply for interacting with the teacher. The teacher can approve or decline the application. |
| Screen sharing | ✅ | ✅ | ✅ | The teacher can share the screen. |
| Recording and replay | ✅ | ✅ | ✅ | The teacher can start recording and record the class for at least 15 seconds. After the recording finishes, a link for replaying the class will be displayed in the message box.  |

### <a name="restriction"></a>Restrictions
eEducation currently has the following restrictions:
1. **Integrate Agora Cloud Recording **: The cloud recording in this sample project is only for demonstration. If you need to officially use the cloud recording function in your project, please see [Cloud Recording Quick Start](https://docs.agora.io/en/cloud-recording/cloud_recording_rest?platform=All%20Platforms) to enable Agora Cloud Recording.
2: **Only supports Alibaba and Qiniu Cloud OSS**: Temporarily, this sample project only supports the Object Storage Service (OSS) of Alibaba Cloud and Qiniu Cloud. For details, see [Alibaba Cloud OSS Configuration Guide](https://github.com/AgoraIO-Usecase/eEducation/wiki/Alibaba-Cloud-OSS-Guide).
3. **Whiteboard courseware management**: In this sample project, we deploy the courseware management on the front end and upload courseware using `accessKey` and `secretKey`. However, this is not a best practice and may cause security issues. We suggest you implement the course management system on the back end and upload courseware in advance. So the web client only needs to read the courseware before the class.
4. **Fails to update the user states immediately after a user in the classroom drops offline**: This sample project does not implement a course management system and uses the Agora RTM SDK for querying the number of online users. If a user in the classroom drops offline, RTM cannot get the user states immediately. Generally, it takes about 30 seconds for RTM to update the user states. You can resolve this problem by implementing your own course management system.
5. **Connect with your own business logic**: The functions of the Agora Edu Cloud Service cannot be directly extended. However, we provide the `userUuid` and `roomUuid` parameters for you to connect the Agora Edu Cloud Service with your own user management system and course management system. 
6. **Concurrent channel restrictions**: At present, each appid can have up to 200 channels at the same time. If you need to continue more, please contact us.
7. **Wait 5 minutes**: After the AppId is created, you need to wait for 5 minutes to complete the follow-up process. This step is to wait for the background data synchronization to complete.

## <a name="strat"></a>Get started  
### <a name="prerequisites"></a>Prerequisites  
Make sure you make the following preparations before compiling and running the sample project.

### Get an Agora App ID  
Follow these steps to get an Agora App ID:  

1. Create an account in [Agora Console](https://console.agora.io/).  
2. Log in to Agora Console and create a project. Select **"App ID + App Certificate + Token"** as your authentication mechanism when creating the project. Make sure that you enable the [App Certificate](https://docs.agora.io/en/Agora%20Platform/token?platform=All%20Platforms#appcertificate) of this project.  
3. Get the App ID of this project in **Project Management page**.  

### Pass the basic HTTP authentication  
Before using the Agora RESTful APIs, you need to generate the Authorization parameter with the Customer ID and Customer Certificate provided by Agora and pass the Authorization parameter in the HTTP request header for authentication. For details, see the [steps of basic HTTP authentication](https://docs.agora.io/en/faq/restful_authentication).

### Get a Netless sdkToken and register it in Agora Cloud Service  
Perform the following steps:

1. Log in to the [Netless console](https://console.herewhite.com/), click the application management button in the left navigation bar, then click the configuration button, click Generate sdkToken, and then copy this sdkToken.
2. Log in to the [Agora console](https://console.agora.io/), click the project management button in the left navigation bar, then click the edit button, click updateToken, and then copy the whiteboard sdkToken you get in the previous step into the pop-up dialog box.

### Run the sample project
See the following documents to compile and run the sample project:

* [Run the Android project](https://github.com/AgoraIO-Usecase/eEducation/blob/v5.0.0/education_Android/AgoraEducation/README.md)
* [Run the iOS project](https://github.com/AgoraIO-Usecase/eEducation/blob/v5.0.0/education_iOS/README.md)
* [Run Web and Electron project](https://github.com/AgoraIO-Usecase/eEducation/blob/v5.0.0/education_web/README.md)

## FAQ
### Security
1. In this sample project, we use [Basic HTTP Authentication](https://docs.agora.io/en/Real-time-Messaging/rtm_get_event?platform=RESTful#basicauth), which will generate the Authorization parameter. If you need a more secure way, you can use [Token Authentication](https://docs.agora.io/en/Real-time-Messaging/rtm_get_event?platform=RESTful#tokenauth) and pass in a uid to dynamically generate a token. For more information about how to generate an RTM Token, see [Token Security](https://docs.agora.io/en/Real-time-Messaging/rtm_token?platform=All%20Platforms).
2. If you are worried about the security of the whiteboard sdkToken, you can deploy your own Token generation service. You should store the `sdkToken` on your server, and see the following documents to deploy a service for generating the token of a whiteboard room.
* JS: [Room Authentication](https://developer-en.netless.link/docs/javascript/quick-start/js-token/)
* Android: [Create Room](https://developer-en.netless.link/docs/android/quick-start/android-create-room/)
* iOS：[Create Room](https://developer-en.netless.link/docs/ios/quick-start/ios-create-room/)
**Example**
Request
```
GET {{tokenServiceUrl}}?channelName={channelName}
```
| Request parameter | Type | Description |
| :---------------- | :--- | :---------- |
| channelName | String | The channel name|
Response
```text
Content-Type: application/json;charset=UTF-8
{
    "msg": "Success",
    "code": 0,
    "data": {

        "boardId": "",
        "boardToken": ""

    }
}
```

### Web & Electron

#### 1. Fail to run the Electron demo
Take these steps to find the root cause:

1. Check whether `localhost:3000` is occupied.
2. Check whether you have successfully installed Electron. Delete `node_modules/electron`, set up the variables of installment, and run `npm i electron`.
#### 2. Package the Electron demo on Windows
When packaging the Electron demo on Windows, please check whether the version of agora-electron-sdk that you installed is consistent with the version of Electron.

#### 3. `window.__netlessJavaScriptLoader was override` error
Take these steps to find the root cause:

1. Run `npm list | grep 'white-web-sdk'` to check how many whiteboard sdks that you have installed.
2. Find the latest version of `white-web-sdk` in `node_modules`, and remove other versions.
#### 4. Use other tools than npm
If you do not use yarn or cnpm to install, remove `node_modules`, `yarn.lock`, `package-lock.json`.

#### 5. agora_node_ext.node is not a valid Win32 application error
Take these steps to fix this error:

1. Remove `node_modules/electron`
2. `npm install electron@7.1.14 --platform=win32 --arch=ia32`
3. Add the following code snippet in `package.json` and run `npm i agora-electron-sdk` to re-install.
```text
"agora_electron": {
  "electron_version": "7.1.2",
  "prebuilt": true,
  "platform": "win32"
},
```

## Recommended versions of the Agora RTC SDK
Agora provides high-stability versions of the Agora RTC Native SDK for companies in the education industry. The educational special versions are developed based on the Agora RTC SDK v2.9.0.

* [Android](https://docs-preview.agoralab.co/cn/Interactive%20Broadcast/edu_release_note_android?platform=Android)
* [iOS](https://docs-preview.agoralab.co/cn/Interactive%20Broadcast/edu_release_note_ios?platform=iOS)
* [macOS](https://docs-preview.agoralab.co/cn/Interactive%20Broadcast/edu_release_note_macos?platform=macOS)
* [Windows](https://docs-preview.agoralab.co/cn/Interactive%20Broadcast/edu_release_note_windows?platform=Windows)
* [Electron](https://docs-preview.agoralab.co/cn/Interactive%20Broadcast/edu_release_note_electron?platform=Electron)

For the Agora RTC Web SDK, download the [latest version](https://docs.agora.io/en/Agora%20Platform/downloads) on Agora Developer Portal.