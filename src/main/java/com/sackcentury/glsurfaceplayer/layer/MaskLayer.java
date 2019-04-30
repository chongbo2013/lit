package com.sackcentury.glsurfaceplayer.layer;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Create by SongChao on 2019/2/18
 */
public abstract class MaskLayer implements IDrawLayer, SurfaceTexture.OnFrameAvailableListener, MediaPlayer.OnCompletionListener {


    private MaskVideoListener mMaskVideoListener;
    private boolean isFirstAvailable = true;

    public abstract int getDuration();

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if ( mMaskVideoListener != null && isFirstAvailable ) {
            isFirstAvailable = false;
            mMaskVideoListener.onFrameAvailable();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        if ( mMaskVideoListener != null ) {
            mMaskVideoListener.onPlayComplete();
        }
        isFirstAvailable = true;
    }


    void releasePlayer(MediaPlayer player) {
        if ( player != null ) {
            try {
                player.stop();
                player.release();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }


    public void setMaskVideoListener(MaskVideoListener maskVideoListener) {
        mMaskVideoListener = maskVideoListener;
    }

    @Override
    public void onDestroy() {
        isFirstAvailable = true;
    }

    public abstract void restart();
}

