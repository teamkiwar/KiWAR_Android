package com.yg.mykiwar.placement.draw;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = MainRenderer.class.getSimpleName();

    private boolean mViewportChanged;
    private int mViewportWidth;
    private int mViewportHeight;

    private CameraRenderer mCamera;
    private PointCloudRenderer mPointCloud;

    private List<Path> mPaths = new ArrayList<Path>();
    private List<Sphere> mSpheres = new ArrayList<Sphere>();

    private float[] mProjMatrix = new float[16];

    private RenderCallback mRenderCallback;

    public interface RenderCallback {
        void preRender();
    }

    public MainRenderer(RenderCallback callback) {
        mCamera = new CameraRenderer();
        mPointCloud = new PointCloudRenderer();

        mRenderCallback = callback;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        mCamera.init();

        mPointCloud.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mViewportChanged = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mRenderCallback.preRender();

        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);

        mPointCloud.draw();

        for (int i = 0; i < mSpheres.size(); i++) {
            Sphere sphere = mSpheres.get(i);
            if (!sphere.isInitialized()) {
                sphere.init();
            }
            sphere.draw();
        }

        for (int i = 0; i < mPaths.size(); i++) {
            Path path = mPaths.get(i);
            if (!path.isInitialized()) {
                path.init();
            }
            path.update();
            path.draw();
        }
    }

    public int getTextureId() {
        return mCamera == null ? -1 : mCamera.getTextureId();
    }

    public void onDisplayChanged() {
        mViewportChanged = true;
    }

    public boolean isViewportChanged() {
        return mViewportChanged;
    }

    public int getWidth() {
        return mViewportWidth;
    }

    public int getHeight() {
        return mViewportHeight;
    }

    public void updateSession(Session session, int displayRotation) {
        if (mViewportChanged) {
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
            mViewportChanged = false;
        }
    }

    public void transformDisplayGeometry(Frame frame) {
        mCamera.transformDisplayGeometry(frame);
    }

    public void updatePointCloud(PointCloud pointCloud) {
        mPointCloud.update(pointCloud);
    }

    public void setProjectionMatrix(float[] matrix) {
        System.arraycopy(matrix, 0, mProjMatrix, 0, 16);

        mPointCloud.setProjectionMatrix(matrix);
    }

    public void updateViewMatrix(float[] matrix) {
        mPointCloud.setViewMatrix(matrix);

        for (int i = 0; i < mSpheres.size(); i++) {
            mSpheres.get(i).setViewMatrix(matrix);
        }

        for (int i = 0; i < mPaths.size(); i++) {
            mPaths.get(i).setViewMatrix(matrix);
        }
    }

    public void setModelMatrix(float[] matrix) {
    }

    public void addPath(float x, float y, float z) {
        Path currentPath = new Path(50, Color.WHITE);
        currentPath.setProjectionMatrix(mProjMatrix);

        float[] identity = new float[16];
        Matrix.setIdentityM(identity, 0);
        currentPath.setModelMatrix(identity);

        currentPath.updatePoint(x, y, z);

        mPaths.add(currentPath);
    }

    public void addPoint(float x, float y, float z) {
        if (mPaths.size() >= 1) {
            Sphere currentPoint = new Sphere(0.01f, Color.GREEN);
            currentPoint.setProjectionMatrix(mProjMatrix);

            float[] translation = new float[16];
            Matrix.setIdentityM(translation, 0);
            Matrix.translateM(translation, 0, x, y, z);
            currentPoint.setModelMatrix(translation);

            Path path = mPaths.get(mPaths.size() - 1);
            path.updatePoint(x, y, z);
        }
    }

    public void removePath() {
        if (mPaths.size() >= 1) {
            mPaths.remove(mPaths.size() - 1);
        }
    }

 }
