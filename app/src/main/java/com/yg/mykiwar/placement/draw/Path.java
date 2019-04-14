package com.yg.mykiwar.placement.draw;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Path {

    private static final String TAG = Path.class.getSimpleName();

    private final String vertexShaderString =
            "attribute vec3 aPosition;\n" +
            "uniform vec4 uColor;\n" +
            "uniform mat4 uMvpMatrix; \n" +
            "varying vec4 vColor;\n" +
            "void main() {\n" +
            "  vColor = uColor;\n" +
            "  gl_Position = uMvpMatrix * vec4(aPosition.x, aPosition.y, aPosition.z, 1.0);\n" +
            "}";

    private final String fragmentShaderString =
            "precision mediump float;\n" +
            "varying vec4 vColor;\n" +
            "void main() {\n" +
            "  gl_FragColor = vColor;\n" +
            "}";

    private boolean mIsInitialized = false;

    private int[] mVbo;
    private int mProgram;

    private int mNumPoints = 0;
    private int mMaxPoints = 1000;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjMatrix = new float[16];

    private float[] mPoints;

    private float mLineWidth = 50.0f;
    private float[] mColor;

    public Path(int lineWidth, int color) {
        mLineWidth = (float) lineWidth;

        float r = Color.red(color) / 255.f;
        float g = Color.green(color) / 255.f;
        float b = Color.blue(color) / 255.f;
        float a = Color.alpha(color) / 255.f;

        mColor = new float[] { r, g, b, a };
        mPoints = new float[mMaxPoints * 3];
    }

    public void init() {
        mVbo = new int[1];
        GLES20.glGenBuffers(1, mVbo, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mMaxPoints * 3 * Float.BYTES, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShader, vertexShaderString);
        GLES20.glCompileShader(vShader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile vertex shader.");
            GLES20.glDeleteShader(vShader);
        }

        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShader, fragmentShaderString);
        GLES20.glCompileShader(fShader);
        GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile fragment shader.");
            GLES20.glDeleteShader(fShader);
        }

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        GLES20.glLinkProgram(mProgram);
        int[] linked = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            Log.e(TAG, "Could not link program.");
        }

        mIsInitialized = true;
    }

    public void updatePoint(float x, float y, float z) {
        if (mNumPoints >= mMaxPoints - 1) {
            return;
        }

        mPoints[mNumPoints * 3] = x;
        mPoints[mNumPoints * 3 + 1] = y;
        mPoints[mNumPoints * 3 + 2] = z;

        mNumPoints++;
    }

    public void update() {
        FloatBuffer buffer = ByteBuffer.allocateDirect(mPoints.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(mPoints);
        buffer.position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, mNumPoints * 3 * Float.BYTES, buffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public int getPointCount() {
        return mNumPoints;
    }

    public void draw() {
        GLES20.glUseProgram(mProgram);

        int position = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int color = GLES20.glGetUniformLocation(mProgram, "uColor");
        int mvp = GLES20.glGetUniformLocation(mProgram, "uMvpMatrix");

        float[] mvMatrix = new float[16];
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mvMatrix, 0);

        GLES20.glEnableVertexAttribArray(position);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 3 * Float.BYTES, 0);

        GLES20.glUniform4f(color, mColor[0], mColor[1], mColor[2], mColor[3]);

        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0);

        GLES20.glLineWidth(mLineWidth);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, mNumPoints);
        GLES20.glLineWidth(1.0f);

        GLES20.glDisableVertexAttribArray(position);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public void setModelMatrix(float[] modelMatrix) {
        System.arraycopy(modelMatrix, 0, mModelMatrix, 0, 16);
    }

    public void setProjectionMatrix(float[] projMatrix) {
        System.arraycopy(projMatrix, 0, mProjMatrix, 0, 16);
    }

    public void setViewMatrix(float[] viewMatrix) {
        System.arraycopy(viewMatrix, 0, mViewMatrix, 0, 16);
    }
}
