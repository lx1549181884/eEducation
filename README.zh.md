_Read this article in another language: [English](README.md)_


> 本文介绍如何使用 eEducation 6.0.0 版本。这是一个不向下兼容的版本。如需使用 eEducation 5.0.0 版本，请查看 [eEducation 5.0.0 使用指南](https://github.com/AgoraIO-Usecase/eEducation/wiki/eEducation-5.0.0-%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97)。

## 目录

* [项目概述](#overview)
  * [支持场景](#scene)
  * [平台兼容](#platform)
  * [功能列表](#function)
  * [限制条件](#restriction)
* [快速开始](#start)
  * [前提条件](#prerequisites)
  * [运行示例项目](#run)
* [Agora 教育云服务](#edu-cloud-service)
* [常见问题](#faq)

## <a name="overview"></a>项目概述
Agora eEducation 是声网专为教育行业提供的示例项目，演示了如何通过 [Agora 教育云服务](https://agoradoc.github.io/cn/edu-cloud-service/restfulapi/)，并配合 **Agora RTC SDK**、**Agora RTM SDK**、**Agora 云端录制服务**和**第三方 Netless 白板 SDK**，快速实现基本的在线互动教学场景。

### <a name="scene"></a>支持场景
eEducation 示例项目支持以下教学场景：
* 1 对 1 互动教学：1 位老师对 1 名学生进行专属在线辅导教学，老师和学生能够进行实时音视频互动。
* 1 对 N 在线小班课：1 位教师对 N 名学生（2 ≤ N ≤ 16）进行在线辅导教学，老师和学生能够进行实时音视频互动。
* 互动直播大班课：一名老师进行教学，多名学生实时观看和收听，学生人数无上限。与此同时，学生可以“举手”请求发言，与老师进行实时音视频互动。
* 超级小班课：将直播大班课的规模优势和互动小班课的教学体验相结合，支持将一个千人直播大班课里的学生拆分成若干个小班，每个小班最多 4 人。大班中主讲老师进行直播授课，小班中学生进行实时音视频分组讨论。此外，还可为小班配备助教老师。

### <a name="platform"></a>平台兼容
eEducation 示例项目支持以下平台和版本：
* iOS 10 及以上。iOS 9 系列版本未经验证。
* Android 4.4 及以上。
* Web Chrome 72 及以上，Web 其他浏览器未经验证。

### <a name="function"></a>功能列表
eEducation 示例项目支持以下功能：

| 功能简介                                 | iOS/Android (学生端)                       | Web(教师端) | Web(学生端)                                | Web(助教端，仅超小课有) | 功能描述                                                     |
| :--------------------------------------- | :----------------------------------------- | :---------- | :----------------------------------------- | :-------------------- | :----------------------------------------------------------- |
| 实时音视频互动                           | ✅                                          | ✅           | ✅                                          | ✅                     | 助教端仅能实时接收教师和学生的音视频。                       |
| 文字聊天                                 | ✅                                          | ✅           | ✅                                          | ✅                     | /                                                            |
| 互动白板                                 | ✅ 1 对 1 互动教学 <br>✅ 小班课 <br>❌ 大班课/超小课 | ✅           | ✅ 1 对 1 互动教学 <br>✅ 小班课 <br>❌ 大班课/超小课 | ❌                     | <li>1 对 1 互动教学中，学生和老师默认都可以操作白板。<li>1 对 N 在线小班课中，学生默认没有权限操作白板，老师可以授权学生操作白板。<li>互动直播大班课和超级小班课中，学生不能操作白板，只能观看。 |
| 白板跟随                                 | ✅                                          | ✅           | ✅                                          | ✅                     | 老师端点击白板跟随后，学生和助教的白板视野跟随老师的白板。   |
| 教学文件上传（PPT、Word、PDF、音视频等） | ❌                                          | ✅           | ❌                                          | ❌                     | 老师端上传文件，学生端只能观看。                             |
| 举手连麦                                 | ✅                                          | ✅           | ✅                                          | ❌                     | 互动直播大班课中，学生“举手”请求发言，老师同意或取消。       |
| 屏幕共享                                 | ❌                                          | ✅           | ❌                                          | ❌                     | 老师端发起屏幕共享，学生端只能观看。                         |
| 录制回放                                 | ❌                                          | ✅           | ❌                                          | ❌                     | 老师端开启录制，需要录制至少 15 秒。录制结束后链接会显示在聊天消息里面，点击链接即可跳转到回放页面。 |

### <a name="restriction"></a>限制条件
eEducation 示例项目目前存在以下限制条件。
1. **需要自行集成云端录制**：该示例项目中，云端录制功能只作为展示。如果你需要正式使用声网云端录制功能，请参考[云端录制快速开始](https://docs.agora.io/cn/cloud-recording/cloud_recording_rest?platform=All%20Platforms)进行集成。
2. **OSS 只支持阿里云和七牛云**：由于白板课件存储的原因，该示例项目目前只支持阿里云和七牛云的 OSS。详见[阿里云 OSS 配置指南](https://github.com/AgoraIO-Usecase/eEducation/wiki/%E6%90%AD%E5%BB%BA%E4%B8%80%E4%B8%AAaliyun-oss)。
3. **白板课件管理**：该示例项目中，白板课件管理部署在前端。我们建议你实现排课业务后，在课程里预先上传课件，Web 端只读取课件即可。我们不建议使用示例项目中的 `accessKey` 和 `secretKey` 的上传方案集成，可能存在安全隐患。
4. **教室内用户异常退出，无法及时更新用户信息**：由于该示例项目没有实现排课系统，使用 Agora RTM SDK 查询在线人数。教室内用户异常退出后，RTM 无法及时知晓该用户状态。一般需要等待 30 秒左右，RTM 才能更新当前用户状态。你可以通过自行实现排课系统来规避此问题。
5. **对接自己的业务**：Agora 教育云服务无法直接扩展业务，但是我们预留了 `userUuid` 和 `roomUuid` 字段用于你对接自己的用户系统和排课系统。比如添加排课系统，你可以传入这 2 个字段用于对接 Agora 教育云服务。
6. **并发频道限制**：目前每个 AppId 同时最多 200 个频道，如果需要继续更多频道，请联系我们。
7. **等待 5 分钟**：创建 AppId 后需要等待 5 分钟进行后续操作，这一步是为了等待后台数据同步完成。

## <a name="start"></a>快速开始

### <a name="prerequisites"></a>前提条件
在编译及运行 eEducation 示例项目之前，你需要完成以下准备工作。

#### 获取声网 App ID

1. 在声网[控制台](https://console.agora.io/)创建一个账号。
2. 登录声网控制台，创建一个项目，鉴权方式选择 **“App ID + App 证书 + Token”**。注意，请确保该项目启用了 [App 证书](https://docs.agora.io/en/Agora%20Platform/token?platform=All%20Platforms#appcertificate)。
3. 前往**项目管理**页面，获取该项目的 App ID。

#### 获取声网 Customer ID 和 Customer 密钥
1. 登录声网[控制台](https://console.agora.io/)，点击页面右上角的用户名，在下拉列表中打开 RESTful API 页面。
2. 点击下载，即可获取客户 ID（customerId）和客户密钥（customerSecret）。

#### 获取第三方白板 Netless 的 AppIdentifier 和 sdkToken，并把 sdkToken 注册到 Agora 教育云服务
1. 登录 [Netless 控制台](https://console.herewhite.com/)，点击左侧导航栏**应用管理**按钮，创建一个应用后，点击**配置**，获取 `AppIdentifier`，然后点击**生成 sdkToken**，然后复制此 `sdkToken`。
2. 登录 [Agora 控制台](https://console.agora.io/)，点击左侧导航栏项目管理按钮，再点击对应项目的**编辑**按钮，点击**更新 token**，然后将上一步复制的白板 `sdkToken` 粘贴至弹出的对话框中。

### <a name="run"></a>运行示例项目

参考以下文档在对应的平台编译及运行示例项目：
* [Android 运行指南](./education_Android/README.zh.md)
* [iOS 运行指南](./education_iOS/README.zh.md)
* [Web & Electron 运行指南](./education_web/README.zh.md)

## <a name="edu-cloud-service"></a>教育云服务
教育云服务是 Agora 专为后端开发能力不够的开发者提供的云服务，能够实现房间、用户和流的状态管理以及状态变更的消息通知。详见[教育云服务 RESTful API 文档](https://agoradoc.github.io/cn/edu-cloud-service/restfulapi/)。

## <a name="faq"></a>常见问题
### 安全相关

如果你担心白板 sdkToken 安全问题，你可以部署你自己的生成 Token 的服务。你需要将白板的 sdkToken 保存在你自己的服务端，然后参考以下 Netless 相关文档在你的客户端代码中部署一个生成当前白板房间 Token 的服务：
* JS: [白板鉴权](https://developer.netless.link/document-zh/home/project-and-authority/)
* Android: [创建白板房间和获取白板房间信息](https://developer.netless.link/android-zh/home/android-create-room/)
* iOS：[创建白板房间和获取白板房间信息](https://developer.netless.link/ios-zh/home/ios-create-room/)

### Web & Electron 项目相关
#### 1. 中国区安装速度慢
中国区用户可以通过预设安装变量来提高安装速度。
```text
# 中国区macOS用户可通过以下命令设置环境变量
export ELECTRON_MIRROR="https://npm.taobao.org/mirrors/electron/"
export ELECTRON_CUSTOM_DIR="5.0.8"
export SASS_BINARY_SITE="https://npm.taobao.org/mirrors/node-sass/"
# 中国区Windows用户可通过以下命令设置环境变量
set ELECTRON_MIRROR=https://npm.taobao.org/mirrors/electron/
set ELECTRON_CUSTOM_DIR=5.0.8
set SASS_BINARY_SITE=https://npm.taobao.org/mirrors/node-sass/
```
预设安装变量后，建议中国区用户通过以下方式安装npm依赖包
```text
npm i --registry=https://registry.npm.taobao.org/
```
#### 2. 运行 Electron 项目失败
排查步骤：
1. 查看当前环境机器是否占用了 localhost:3000 。
2. 也可能是因为 Electron 没有下载成功。清理 `node_modules/electron`，预设安装变量，然后运行 `npm i electron`。
#### 3. 如何打包 Windows Electron demo
Windows 系统上打包 Electron demo 时，注意安装的 `agora-electron-sdk` 版本是否和打包的版本一致。例如安装 win32 agora-electron-sdk 的必须在打包之前 `npm install --arch=ia32 electron@5.0.8`。
#### 4. 如何打包 macOS Electron demo
如需在 App Store 发布，请参考 Electron 和 App Store 相关资料。
#### 5. 运行时遇到 window.__netlessJavaScriptLoader was override 报错
排查步骤：
1. 用 `npm list | grep 'white-web-sdk'` 查找当前他安装了几个 SDK。
2. 在 `node_modules` 里找到 `white-web-sdk` 最新的版本，然后删除其他多余的包。
#### 6. 使用非 npm 方式安装环境
如果不是用 npm 安装，建议移除 `node_modules`，`yarn.lock`，`package-lock.json`。
#### 7. 运行时遇到 agora_node_ext.node is not a valid Win32 application 报错
排查步骤：
1. 先删掉 `node_modules/electron`
2. `npm install electron@<需要的版本> electron --arch=ia32`
3. 在 `package.json` 里加入以下字段，然后重新安装 `npm i agora-electron-sdk`
```
"agora_electron": {
  "electron_version": "7.1.2",
  "prebuilt": true,
  "platform": "win32"
},
```