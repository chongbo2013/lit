package com.sackcentury.glsurfaceplayer.layer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.airbnb.lottie.ImageAssetDelegate;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.LottieListener;
import com.airbnb.lottie.LottieResult;
import com.airbnb.lottie.LottieTask;
import com.sackcentury.glsurfaceplayer.BuildConfig;
import com.sackcentury.glsurfaceplayer.R;
import com.sackcentury.glsurfaceplayer.parser.MvResourceConfig;
import com.sackcentury.glsurfaceplayer.parser.MvResourceParser;
import com.sackcentury.utils.FileUtil;
import com.sackcentury.utils.ShaderUtils;
import com.sackcentury.utils.TextResourceReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Create by SongChao on 2019/2/13
 */
@SuppressLint ("ViewConstructor")
public class LottieLayer extends View implements IDrawLayer {

    private Context mContext;
    private int mProgram;
    private int glHPosition;
    private int glHTexture;
    private int glHCoordinate;
    private int glHMatrix;
    private int glHUxy;
    private Bitmap mBitmap;

    private FloatBuffer bPos;
    private FloatBuffer bCoord;

    private int textureId;

    private float uXY;

    private String vertex;
    private String fragment;
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private static final float[] sPos = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    private static final float[] sCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private float mOutWidth = 320f;

    private LottieDrawable mLottieDrawable;
    private Canvas mCanvas;

    private String mLottieJson;
    private String mImageFolder;

    LottieLayerListener mLottieLayerListener;

    Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isStart = false;
    private boolean mLoop = false;
    private boolean useCache = true;

    public LottieLayer(Context context, String lottieJson, final String imageFolder) {
        super(context);
        mContext = context;
        this.vertex = TextResourceReader.readTextFileFromResource(mContext, R.raw.default_image_vertex_shader);
        this.fragment = TextResourceReader.readTextFileFromResource(mContext, R.raw.default_image_fragment_shader);
        ByteBuffer bb = ByteBuffer.allocateDirect(sPos.length * 4);
        bb.order(ByteOrder.nativeOrder());
        bPos = bb.asFloatBuffer();
        bPos.put(sPos);
        bPos.position(0);
        ByteBuffer cc = ByteBuffer.allocateDirect(sCoord.length * 4);
        cc.order(ByteOrder.nativeOrder());
        bCoord = cc.asFloatBuffer();
        bCoord.put(sCoord);
        bCoord.position(0);
        mLottieDrawable = new LottieDrawable();
        mLottieJson = lottieJson;
        mImageFolder = imageFolder;


    }

    @Override
    public void prepare(boolean isRecord) {
        mProgram = ShaderUtils.createProgram(vertex, fragment);
        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        glHUxy = GLES20.glGetUniformLocation(mProgram, "uXY");

        mLottieDrawable.setCallback(this);


        try {
            final FileInputStream stream = new FileInputStream(new File(mLottieJson));
            if ( useCache ) {
                LottieCompositionFactory.fromJsonInputStream(stream, mLottieJson).addListener(new LottieListener<LottieComposition>() {
                    @Override
                    public void onResult(LottieComposition result) {
                        initDrawable(result);
                    }
                });
            } else {

                final LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonInputStreamSync(stream, mLottieJson);
                if ( result.getValue() != null ) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            initDrawable(result.getValue());
                        }
                    });
                }
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }
    }

    private void initDrawable(LottieComposition result) {
        Log.e("LottieComposition: ", "getDuration : " + result.getDuration() + "");
        if ( mLottieLayerListener != null ) {
            mLottieLayerListener.onLoadLottie(result);
        }
        if ( !TextUtils.isEmpty(mImageFolder) ) {
            mLottieDrawable.setImagesAssetsFolder(mImageFolder);
            mLottieDrawable.setImageAssetDelegate(new ImageAssetDelegate() {
                @Override
                public Bitmap fetchBitmap(LottieImageAsset asset) {
                    return getBitmap(mImageFolder, asset);
                }
            });
        }
        mLottieDrawable.setComposition(result);
        float scale = getDrawableScale(result.getBounds().width());
        mLottieDrawable.setScale(scale);
        mLottieDrawable.loop(mLoop);
        if ( mLottieDrawable != null ) {
            Bitmap.Config config = mLottieDrawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            mBitmap = Bitmap.createBitmap(mLottieDrawable.getIntrinsicWidth(), mLottieDrawable.getIntrinsicHeight(), config);
            mCanvas = new Canvas(mBitmap);
            Log.e("LottieComposition: ", "mBitmap - width: " + mBitmap.getWidth() + " height: " + mBitmap.getHeight());
        }
    }

    private Bitmap getBitmap(String imageFolder, LottieImageAsset asset) {
        return FileUtil.getBitmapFromPath(mContext, imageFolder + asset.getFileName());
    }

    public void updateImageBitmap(String id, Bitmap bitmap) {
        if ( mLottieDrawable != null ) {
            Bitmap old = mLottieDrawable.getImageAsset(id);
            if ( old != null ) {
                Log.e("updateImageBitmap: ", "width : "+bitmap.getWidth() + "");
                mLottieDrawable.updateBitmap(id, FileUtil.scaleBitmap(bitmap, old.getWidth(), old.getHeight()));
            }
        }
    }

    private float getDrawableScale(int boundWidth) {
        float scale = mOutWidth / boundWidth;
        Log.e("getDrawableScale: ", "scale - scale: " + scale + " boundWidth: " + boundWidth);
        return scale > 0.4f ? 0.4f : scale;
    }

    public void setOutWidth(float outWidth) {
        mOutWidth = outWidth;
    }

    @Override
    public void onSurfaceCreated(boolean isRecord) {
        prepare(false);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        int w = width;
        int h = height;
        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        uXY = sWidthHeight;
        if ( width > height ) {
            if ( sWH > sWidthHeight ) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 5);
            }
        } else {
            if ( sWH > sWidthHeight ) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 5);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

    }

    @Override
    public void drawFrame() {
        if ( isStart ) {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

            GLES20.glUseProgram(mProgram);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            GLES20.glUniform1f(glHUxy, uXY);
            GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
            GLES20.glEnableVertexAttribArray(glHPosition);
            GLES20.glEnableVertexAttribArray(glHCoordinate);
            GLES20.glUniform1i(glHTexture, 0);
            textureId = updateTexture();
            GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos);
            GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        }
    }

    @Override
    public void start() {
        if ( mLottieDrawable != null ) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLottieDrawable.start();
                    isStart = true;
                }
            }, 50);
        }
    }

    public void stop() {
        if ( mLottieDrawable != null ) {
            mLottieDrawable.stop();
        }
    }

    @Override
    public void onPause() {
        if ( mLottieDrawable != null ) {
            mLottieDrawable.pauseAnimation();
        }
    }

    @Override
    public void onResume() {
        if ( mLottieDrawable != null ) {
            mLottieDrawable.resumeAnimation();
        }
    }

    private int updateTexture() {
        if ( textureId > 0 ) {
            if ( mLottieDrawable != null && mLottieDrawable.getIntrinsicWidth() > 0 && mLottieDrawable.getIntrinsicHeight() > 0 ) {
                mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                long start = System.currentTimeMillis();
                mLottieDrawable.draw(mCanvas);
                if ( BuildConfig.DEBUG ) {
                    Log.e("updateTexture", " draw :" + (System.currentTimeMillis() - start));
                }
            }
            configTexture();
        } else {
            int[] texture = new int[1];
            if ( mBitmap != null && !mBitmap.isRecycled() ) {
                //生成纹理
                GLES20.glGenTextures(1, texture, 0);
                configTexture();
                return texture[0];
            }
            return 0;
        }
        return textureId;
    }

    private void configTexture() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //生成纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //根据以上指定的参数，生成一个2D纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
    }

    public void setLottieLayerListener(LottieLayerListener lottieLayerListener) {
        mLottieLayerListener = lottieLayerListener;
    }

    public void setLoop(boolean loop) {
        mLoop = loop;
        if ( mLottieDrawable != null ) {
            mLottieDrawable.loop(true);
        }
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    @Override
    public void onDestroy() {
        if ( mBitmap != null && !mBitmap.isRecycled() ) {
            mBitmap.recycle();
        }
        mLottieDrawable.recycleBitmaps();
        mLottieDrawable.clearComposition();
        mHandler.removeCallbacksAndMessages(null);
    }

}
