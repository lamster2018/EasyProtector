package com.lahm.library;

/**
 * Project Name:EasyProtector
 * Package Name:com.lahm.library
 * Created by lahm on 2018/5/14 下午9:58 .
 */
public interface LibLoader {
    void loadLibrary(String libName) throws UnsatisfiedLinkError, SecurityException;
}
