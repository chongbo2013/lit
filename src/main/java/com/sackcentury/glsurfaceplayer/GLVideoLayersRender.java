package com.sackcentury.glsurfaceplayer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.airbnb.lottie.LottieComposition;
import com.sackcentury.glsurfaceplayer.layer.LottieLayer;
import com.sackcentury.glsurfaceplayer.layer.LottieLayerListener;
import com.sackcentury.glsurfaceplayer.layer.MaskLayer;
import com.sackcentury.glsurfaceplayer.layer.MaskVideoListener;
import com.sackcentury.glsurfaceplayer.parser.MvImageAsset;
import com.sackcentury.recoder.IRecordRender;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Create by SongChao on 2019/2/12
 */
public class GLVideoLayersRender implements IRecordRender {

    private OnPrepareListener mOnPrepareListener;
    private LottieLayer mLottieLayer;
    private MaskLayer mMaskLayer;
    private LottieLayer mWatermarkLayer;
    private boolean mMaskBottom = false;

    public GLVideoLayersRender(MaskLayer maskLayer, LottieLayer lottieLayer) {
        setMaskLayer(maskLayer);
        mLottieLayer = lottieLayer;
        if(maskLayer == null){
            mLottieLayer.setLoop(true);
        }
    }

    private void setMaskLayer(MaskLayer maskLayer) {
        mMaskLayer = maskLayer;
        if ( mMaskLayer != null ) {
            mMaskLayer.setMaskVideoListener(new MaskVideoListener() {
                @Override
                public void onFrameAvailable() {
                    startOtherLayer();
                }

                @Override
                public void onPlayComplete() {
                    mLottieLayer.stop();
                    Log.e("setMaskLayer", "onPlayComplete");
                    restart();
                }
            });
        }
    }

    public void setWatermarkLayer(LottieLayer layer) {
        mWatermarkLayer = layer;
    }

    public void setMaskBottom(boolean maskBottom) {
        this.mMaskBottom = maskBottom;
    }

    public void startOtherLayer() {
        if ( mLottieLayer != null ) {
            mLottieLayer.start();
        }
        if ( mWatermarkLayer != null ) {
            mWatermarkLayer.start();
        }

    }

    public void setOnPrepareListener(OnPrepareListener onPrepareListener) {
        mOnPrepareListener = onPrepareListener;
        mLottieLayer.setLottieLayerListener(new LottieLayerListener() {
            @Override
            public void onLoadLottie(LottieComposition lottieComposition) {
                if ( mOnPrepareListener != null ) {
                    mOnPrepareListener.onLoadLottie(lottieComposition);
                }
            }
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if ( mOnPrepareListener != null ) {
            mOnPrepareListener.onPrepare();
        }
        if ( mLottieLayer != null ) {
            mLottieLayer.onSurfaceCreated(gl == null && config == null);
        }
        if ( mMaskLayer != null ) {
            mMaskLayer.onSurfaceCreated(gl == null && config == null);
        }
        if ( mWatermarkLayer != null ) {
            mWatermarkLayer.onSurfaceCreated(gl == null && config == null);
        }
        if ( mOnPrepareListener != null ) {
            mOnPrepareListener.onPrepared();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if ( mLottieLayer != null ) {
            mLottieLayer.onSurfaceChanged(width, height);
        }
        if ( mMaskLayer != null ) {
            mMaskLayer.onSurfaceChanged(width, height);
        }

        if ( mWatermarkLayer != null ) {
            mWatermarkLayer.onSurfaceChanged(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if ( !mMaskBottom ) {
            if ( mLottieLayer != null ) {
                mLottieLayer.drawFrame();
            }
            if ( mMaskLayer != null ) {
                mMaskLayer.drawFrame();
            }
        } else {
            if ( mMaskLayer != null ) {
                mMaskLayer.drawFrame();
            }
            if ( mLottieLayer != null ) {
                mLottieLayer.drawFrame();
            }
        }
        if ( mWatermarkLayer != null ) {
            mWatermarkLayer.drawFrame();
        }
    }

    public void onResume() {
        if ( mLottieLayer != null ) {
            mLottieLayer.onResume();
        }
        if ( mMaskLayer != null ) {
            mMaskLayer.onResume();
        }
        if ( mWatermarkLayer != null ) {
            mWatermarkLayer.onResume();
        }
    }

    public void onPause() {
        if ( mLottieLayer != null ) {
            mLottieLayer.onPause();
        }
        if ( mMaskLayer != null ) {
            mMaskLayer.onPause();
        }
        if ( mWatermarkLayer != null ) {
            mWatermarkLayer.onPause();
        }
    }

    public void onDestroy() {
        if ( mLottieLayer != null ) {
            mLottieLayer.onDestroy();
        }
        if ( mMaskLayer != null ) {
            mMaskLayer.onDestroy();
        }
        if ( mWatermarkLayer != null ) {
            mWatermarkLayer.onDestroy();
        }
    }

    @Override
    public void start() {
        if ( mMaskLayer != null ) {
            mMaskLayer.start();
        } else {
            startOtherLayer();
        }

    }

    @Override
    public void release() {
        onDestroy();
    }

    public int getDuration() {
        if ( mMaskLayer != null ) {
            return mMaskLayer.getDuration();
        }
        return 0;
    }

    public void restart() {
        if ( mMaskLayer != null ) {
            mMaskLayer.restart();
        }
        if ( mLottieLayer != null ) {
            mLottieLayer.stop();
            mLottieLayer.start();
        }
    }

    public void updateBitmap(String key, Bitmap rawBitmap) {
        if ( mLottieLayer != null ) {
            mLottieLayer.updateImageBitmap(key, rawBitmap);
        }
    }


    public interface OnPrepareListener {
        void onPrepare();

        void onPrepared();

        void onLoadLottie(LottieComposition lottieComposition);
    }
}
