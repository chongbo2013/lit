package com.sackcentury.glsurfaceplayer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import com.sackcentury.glsurfaceplayer.layer.IDrawLayer;
import com.sackcentury.glsurfaceplayer.layer.LottieLayer;
import com.sackcentury.glsurfaceplayer.layer.MaskLayer;
import com.sackcentury.glsurfaceplayer.layer.MaskVideoLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by SongChao on 2019/2/12
 */
@RequiresApi (api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class GLLayerTextureView extends GLSurfaceView {
    private static final String TAG = "GLLayerTextureView";
    private static final int GL_CONTEXT_VERSION = 2;

    private GLVideoLayersRender mVideoLayersRender;

    public GLLayerTextureView(Context context) {
        super(context);
        init();
    }

    public GLLayerTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(GL_CONTEXT_VERSION);
    }

    public void setLayers(MaskLayer maskLayer, LottieLayer layer, GLVideoLayersRender.OnPrepareListener onPrepareListener) {
        mVideoLayersRender = new GLVideoLayersRender(maskLayer, layer);
        mVideoLayersRender.setOnPrepareListener(onPrepareListener);
        setRenderer(mVideoLayersRender);
    }

    public void start() {
        if ( mVideoLayersRender != null ) {
            mVideoLayersRender.start();
        }
    }

    public void restart() {
        if ( mVideoLayersRender != null ) {
            mVideoLayersRender.restart();
        }
    }

    public GLVideoLayersRender getVideoLayersRender() {
        return mVideoLayersRender;
    }

    @Override
    public void onResume() {
        try {
            if ( mVideoLayersRender != null ) {
                super.onResume();
                mVideoLayersRender.onResume();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        try {
            super.onPause();
            if ( mVideoLayersRender != null ) {
                mVideoLayersRender.onPause();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        if ( mVideoLayersRender != null ) {
            mVideoLayersRender.onDestroy();
        }
    }

}
