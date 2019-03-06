package com.chengww.demo.utils;

import android.text.TextUtils;

/**
 * Created by chengww on 2019/2/28.<br/>
 * String operations.
 */
public class StringUtils {
    /**
     * Get file/folder name from the api response
     * @param key key
     * @return file/folder name
     */
    public static String getFileName(String key) {
        if (TextUtils.isEmpty(key)) return key;
        int index = key.lastIndexOf("/");
        if (index > -1) {
            if (index == key.length() - 1) {
                String substring = key.substring(0, key.length() - 1);
                int i = substring.lastIndexOf("/");
                if (i > -1) {
                    return substring.substring(i + 1);
                }
                return substring;
            }
            return key.substring(index + 1);
        }
        return key;
    }

}
