# EasyProtector  [ ![Download](https://api.bintray.com/packages/lamster2018/maven/easy-protector-release/images/download.svg) ](https://bintray.com/lamster2018/maven/easy-protector-release/_latestVersion)

EasyProtector，a simple way to check root/virtual app/emulator/xposed framework/tracer/debugger.

很多朋友是通过[郭霖老师的公众号推送](https://mp.weixin.qq.com/s/XvqUc3drJhdJ9hOuCcfdkg) 或者[陈宇明老师的公众号推送](https://mp.weixin.qq.com/s/7I_vGV77TWqhQR9Myc5FQg)了解到这个库的。

既然来都来了，欢迎大家star/fork,哪怕提个issue都好，我希望这是一个好用的库（省去application的初始化操作，避免更多的权限要求，尽可能的懒加载）

# Document

- [中国人猛戳这里](https://www.jianshu.com/p/c37b1bdb4757)
- English （not yet）



# Download



You can download a jar from GitHub's [releases page](https://github.com/lamster2018/EasyProtector/releases).



Or use Gradle:

```
repositories {
  jcenter()
  maven()
  google()
}

dependencies {
  implementation 'com.lahm.library:easy-protector-release:latest.release'
}
```



Or maven

```
<dependency>
  <groupId>com.lahm.library</groupId>
  <artifactId>easy-protector-release</artifactId>
  <version>1.0.4</version>
  <type>pom</type>
</dependency>
```



# How do I use it？

EasyProtectorLib.checkIsRoot();

EasyProtectorLib.checkIsDebug();

EasyProtectorLib.checkIsPortUsing();

EasyProtectorLib.checkIsXposedExist();

EasyProtectorLib.checkIsBeingTracedByJava();

EasyProtectorLib.checkIsUsingMultiVirtualApp();

EasyProtectorLib.checkIsRunningInEmulator();

......

More function see

SecurityCheckUtil.class

EmulatorCheckUtil.class

VirtualApkCheckUtil.class

AccessibilityServicesCheckUtil.class


# Proguard

no need



# Compatibility

- Minimum Android SDK: requires a minimum API level of 16.
- CPU: support x86 & arm



# Test

| Phone      | SDK         | ROM             |
| ---------- | ----------- | --------------- |
| RedMi 3s   | Android 6.0 | google eng      |
| Huawei P9  | Android 7.0 | EMUI 5.1 root   |
| Mix 2      | Android 8.0 | MIUI 9 stable   |
| OnePlus 5T | Android 8.1 | H2OS 5.1 stable |


![demo](https://upload-images.jianshu.io/upload_images/2554175-7ee67add271a2035.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

自2018-6-13集成并上线自己的项目里，至10-12已经收集了9w+疑似模拟器的检测数据，精确性在低版本机器上不太理想，甚至有些厂商的cpu配置信息乱来。
如果各位需要在业务里做很细致的模拟器鉴别，还需要再加一些检查条目增加判断的精确性。



# License
Apache 2.0. See the [LICENSE](https://github.com/lamster2018/EasyProtector/blob/master/LICENSE) file for details.