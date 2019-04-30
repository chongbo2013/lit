package com.sackcentury.glsurfaceplayer.layer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import com.sackcentury.glsurfaceplayer.R;
import com.sackcentury.utils.GlUtil;
import com.sackcentury.utils.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


/**
 * Create by SongChao on 2019/2/13
 */
public class TestLayer extends MaskVideoLayer implements IDrawLayer, SurfaceTexture.OnFrameAvailableListener, MediaPlayer.OnCompletionListener {

    private Uri mColorUrl;
    private Context mContext;

    private MediaPlayer mMediaPlayerColor = new MediaPlayer();

    private int mTextureColorId;
    private SurfaceTexture mSurfaceTextureColor;

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private FloatBuffer mUVTexVertexBuffer;
    private FloatBuffer mMaskUVTexVertexBuffer;
    private int mProgram = 0;
    private int mPositionHandle = 0;
    private int mTextureCoordinatorHandle = 0;
    private int mMaskTextureCoordinatorHandle = 0;
    private int mMVPMatrixHandle = 0;
    private int mTextureColorHandle = 0;
    private int mTextureMaskHandle = 0;

    private float mVertex[] = {
            1f, 1f, 0f,    // top right
            -1f, 1f, 0f, // top left
            -1f, -1f, 0f, // bottom left
            1f, -1f, 0f // bottom right
    };

    private float mUVTexVertex[] = {
            0.5f, 0f,
            0f, 0f,
            0f, 1f,
            0.5f, 1f
    };

    private float mMaskUVTexVertex[] = {
            1f, 0f,
            0.5f, 0f,
            0.5f, 1f,
            1f, 1f
    };

    private short DRAW_ORDER[] = {0, 1, 2, 2, 0, 3};

    private float mMVP[] = new float[16];

    private MaskVideoListener mMaskVideoListener;
    private boolean isFirstAvailable = true;

    public TestLayer(Context context, Uri maskUrl) {
        super(context, maskUrl, maskUrl);
        mColorUrl = maskUrl;
        mContext = context;
    }


    @Override
    public void prepare(boolean isRecord) {
        try {
            mMediaPlayerColor.setDataSource(mContext, mColorUrl);
            mMediaPlayerColor.setLooping(true);
            mMediaPlayerColor.prepare();

            mMediaPlayerColor.setOnCompletionListener(this);
            if ( isRecord ) {
                mMediaPlayerColor.setVolume(0, 0);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        int textures[] = GlUtil.createTextureID(1);
        if ( textures.length > 0 ) {
            mTextureColorId = textures[0];
            mSurfaceTextureColor = new SurfaceTexture(mTextureColorId);
            mSurfaceTextureColor.setOnFrameAvailableListener(this);
        }

        initShader();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.e("Sync", "onFrameAvailable");
        if ( mMaskVideoListener != null && isFirstAvailable ) {
            isFirstAvailable = false;
            mMaskVideoListener.onFrameAvailable();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("Sync", "onPlayComplete");
        if ( mMaskVideoListener != null ) {
            mMaskVideoListener.onPlayComplete();
        }
    }

    @Override
    public void onSurfaceCreated(boolean isRecord) {
        prepare(isRecord);
    }

    private void initShader() {
        String vertexShader = TextResourceReader.readTextFileFromResource(mContext, R.raw.video_single_mask_vertex_shader);
        String fragmentShader = TextResourceReader.readTextFileFromResource(mContext, R.raw.video_alpha_single_mask_fragment_shader);

        mProgram = GlUtil.createProgram(vertexShader, fragmentShader); // create vertex's shader and fragment's shader, add to shader for build
        if ( mProgram == 0 ) {
            return;
        }
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GlUtil.checkLocation(mPositionHandle, "vPosition");

        mTextureCoordinatorHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GlUtil.checkLocation(mTextureCoordinatorHandle, "inputTextureCoordinate");

        mMaskTextureCoordinatorHandle = GLES20.glGetAttribLocation(mProgram, "inputMaskTextureCoordinate");
        GlUtil.checkLocation(mMaskTextureCoordinatorHandle, "inputMaskTextureCoordinate");

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GlUtil.checkLocation(mMVPMatrixHandle, "uMVPMatrix");

        mTextureColorHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
        GlUtil.checkLocation(mTextureColorHandle, "s_texture");

        mDrawListBuffer = ByteBuffer.allocateDirect(DRAW_ORDER.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(DRAW_ORDER);
        mVertexBuffer = ByteBuffer.allocateDirect(mVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertex);
        mUVTexVertexBuffer = ByteBuffer.allocateDirect(mUVTexVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mUVTexVertex);

        mMaskUVTexVertexBuffer = ByteBuffer.allocateDirect(mMaskUVTexVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mMaskUVTexVertex);

        mUVTexVertexBuffer.position(0);
        mMaskUVTexVertexBuffer.position(0);
        mDrawListBuffer.position(0);
        mVertexBuffer.position(0);
        Matrix.setIdentityM(mMVP, 0);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float rate = (float) height / (float) width;
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();

    }

    @Override
    public void drawFrame() {
        mSurfaceTextureColor.updateTexImage();

        GLES20.glUseProgram(mProgram);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mTextureCoordinatorHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinatorHandle, 2, GLES20.GL_FLOAT, false, 0, mUVTexVertexBuffer);


        GLES20.glEnableVertexAttribArray(mMaskTextureCoordinatorHandle);
        GLES20.glVertexAttribPointer(mMaskTextureCoordinatorHandle, 2, GLES20.GL_FLOAT, false, 0, mMaskUVTexVertexBuffer);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVP, 0);

        //增加文理贴图层
        GLES20.glUniform1i(mTextureColorHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureColorId);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, DRAW_ORDER.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinatorHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
        GLES20.glDisableVertexAttribArray(mTextureColorHandle);
        GLES20.glDisableVertexAttribArray(mTextureMaskHandle);
    }

    @Override
    public void start() {
        if ( mMediaPlayerColor != null ) {
            mMediaPlayerColor.setSurface(new Surface(mSurfaceTextureColor));
            mMediaPlayerColor.start();
        }
    }

    @Override
    public void onPause() {
        if ( mMediaPlayerColor != null ) {
            mMediaPlayerColor.pause();
        }
    }

    @Override
    public void onResume() {
        if ( mMediaPlayerColor != null ) {
            mMediaPlayerColor.start();
        }
    }

    @Override
    public void onDestroy() {
        releasePlayer(mMediaPlayerColor);
        isFirstAvailable = true;
    }


    public int getDuration() {
        if ( mMediaPlayerColor != null ) {
            return mMediaPlayerColor.getDuration();
        }
        return 0;
    }

    public void setMaskVideoListener(MaskVideoListener maskVideoListener) {
        mMaskVideoListener = maskVideoListener;
    }

}
