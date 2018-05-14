# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # 设置编译出来的so包的名字. 不需要添加lib前缀
             antitrace

             # 设置为共享链接库. 有SHARED，STATIC两种可选
             SHARED

             # 设置源文件的相对路径，可将多个源文件进行编译
             src/main/jni/antitrace.cpp )

find_library( # 设置外部引用库.这个库支持你在c/c++中打印log，具体请见  android/log.h
             log-lib

             # 外部引用库的名称
             log )

target_link_libraries( # 指定被链接的库.
             antitrace

             # 链接log-lib到ctest
             ${log-lib} )
