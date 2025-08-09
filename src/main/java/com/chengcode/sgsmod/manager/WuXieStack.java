package com.chengcode.sgsmod.manager;


import com.chengcode.sgsmod.entity.WuZhongEntity;

import java.util.HashMap;
import java.util.Map;

public class WuXieStack {
    private static Map<String, Integer> wuXieStack = new HashMap<>();

    public static void addWuXieStack(String key, int value) {
        wuXieStack.put(key, value);
    }

    public static int getWuxieCnt(String key) {
        return wuXieStack.get(key);
    }

    public static void plusWuxieCnt(String key) {
        wuXieStack.put(key, wuXieStack.get(key) + 1);
    }

    public static void minusWuxieCnt(String key) {
        wuXieStack.put(key, wuXieStack.get(key) - 1);
    }

    public static void clearWuxieStack() {
        wuXieStack.clear();
    }

    public static void removeWuxieStack(String key) {
        wuXieStack.remove(key);
    }
}
