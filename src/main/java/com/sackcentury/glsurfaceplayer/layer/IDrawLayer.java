package com.sackcentury.glsurfaceplayer.layer;

/**
 * Create by SongChao on 2019/2/12
 */
public interface IDrawLayer {

    void prepare(boolean isRecord);

    void onSurfaceCreated(boolean isRecord);

    void onSurfaceChanged(int width, int height);

    void drawFrame();

    void start();

    void onPause();

    void onResume();

    void onDestroy();
}
