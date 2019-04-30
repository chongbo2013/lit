package com.sackcentury.utils;

import android.content.Context;
import android.view.WindowManager;

/**
 * Create by SongChao on 2019/2/16
 */
public class DeviceUtil {
    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }
}
