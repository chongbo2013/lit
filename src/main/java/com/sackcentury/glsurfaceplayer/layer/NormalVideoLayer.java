package com.sackcentury.glsurfaceplayer.layer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
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
public class NormalVideoLayer implements IDrawLayer {

    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private Uri videoUrl;
    private Context mContext;

    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private FloatBuffer mUVTexVertexBuffer;
    private int mProgram = 0;
    private int mPositionHandle = 0;
    private int mTextureCoordinatorHandle = 0;
    private int mMVPMatrixHandle = 0;
    private int mTextureHandle = 0;

    private float mVertex[] = {
            1f, 1f, 0f,    // top right
            -1f, 1f, 0f, // top left
            -1f, -1f, 0f, // bottom left
            1f, -1f, 0f // bottom right
    };

    private float mUVTexVertex[] = {
            1f, 0f,
            0f, 0f,
            0f, 1f,
            1f, 1f
    };

    private short DRAW_ORDER[] = {0, 1, 2, 2, 0, 3};

    private float mMVP[] = new float[16];

    public NormalVideoLayer(Context context, Uri videoUrl) {
        this.mContext = context;
        this.videoUrl = videoUrl;
    }

    @Override
    public void prepare(boolean isRecord) {
        try {
            mMediaPlayer.setDataSource(mContext, videoUrl);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        int textures[] = GlUtil.createTextureID(1);
        if ( textures.length > 0 ) {
            mTextureId = textures[0];
            mSurfaceTexture = new SurfaceTexture(mTextureId);
        }

        initShader();
    }

    @Override
    public void onSurfaceCreated(boolean isRecord) {
        prepare(isRecord);
    }

    private void initShader() {
        String vertexShader = TextResourceReader.readTextFileFromResource(mContext, R.raw.video_vertex_shader);
        String fragmentShader = TextResourceReader.readTextFileFromResource(mContext, R.raw.video_normal_fragment_shader);

        mProgram = GlUtil.createProgram(vertexShader, fragmentShader); // create vertex's shader and fragment's shader, add to shader for build
        if ( mProgram == 0 ) {
            return;
        }
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GlUtil.checkLocation(mPositionHandle, "vPosition");

        mTextureCoordinatorHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GlUtil.checkLocation(mTextureCoordinatorHandle, "inputTextureCoordinate");

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GlUtil.checkLocation(mMVPMatrixHandle, "uMVPMatrix");

        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
        GlUtil.checkLocation(mTextureHandle, "s_texture");

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

        mUVTexVertexBuffer.position(0);
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
        mSurfaceTexture.updateTexImage();
        GLES20.glUseProgram(mProgram);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mTextureCoordinatorHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinatorHandle, 2, GLES20.GL_FLOAT, false, 0, mUVTexVertexBuffer);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVP, 0);
        GLES20.glUniform1i(mTextureHandle, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, DRAW_ORDER.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinatorHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle);
    }

    @Override
    public void start() {
        if ( mMediaPlayer != null ) {
            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
            mMediaPlayer.start();
        }
    }

    @Override
    public void onPause() {
        if ( mMediaPlayer != null ) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        if ( mMediaPlayer != null ) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onDestroy() {
        if ( mMediaPlayer != null ) {
            try {
                mMediaPlayer.pause();
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
