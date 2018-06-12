# EasyProtector  [ ![Download](https://api.bintray.com/packages/lamster2018/maven/easy-protector-release/images/download.svg) ](https://bintray.com/lamster2018/maven/easy-protector-release/_latestVersion)

EasyProtector，a simple way to check root/virtual app/emulator/xposed framework/tracer/debugger.



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
  <version>1.0.1</version>
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