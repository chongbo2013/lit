package com.sackcentury.recoder;

import android.opengl.GLSurfaceView;

/**
 * Create by SongChao on 2019/2/15
 */
public interface IRecordRender extends GLSurfaceView.Renderer{
    void start();
    void release();
}
