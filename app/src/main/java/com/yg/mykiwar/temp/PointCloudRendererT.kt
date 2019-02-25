package com.yg.mykiwar.temp

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.PointCloud

class PointCloudRendererT {
    private val TAG = PointCloudRendererT::class.java.simpleName

        private val vertexShaderString =
                "uniform mat4 uMvpMatrix;\n" +
        "uniform vec4 uColor;\n" +
        "uniform float uPointSize;\n" +
        "attribute vec4 aPosition;\n" +
        "varying vec4 vColor;\n" +
        "void main() {\n" +
        "   vColor = uColor;\n" +
        "   gl_Position = uMvpMatrix * vec4(aPosition.xyz, 1.0);\n" +
        "   gl_PointSize = uPointSize;\n" +
        "}"

    private val fragmentShaderString = (
"precision mediump float;\n" +
"varying vec4 vColor;\n" +
"void main() {\n" +
"    gl_FragColor = vColor;\n" +
"}")

    private var mVbo: IntArray? = null
    private var mProgram: Int = 0

    private val mViewMatrix = FloatArray(16)
    private val mProjMatrix = FloatArray(16)

    private var mNumPoints = 0

    private var mPointCloud: PointCloud? = null

    fun init() {
        mVbo = IntArray(1)
        GLES20.glGenBuffers(1, mVbo, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo!![0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 1000 * 16, null, GLES20.GL_DYNAMIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        val vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vShader, vertexShaderString)
        GLES20.glCompileShader(vShader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile vertex shader.")
            GLES20.glDeleteShader(vShader)
        }

        val fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fShader, fragmentShaderString)
        GLES20.glCompileShader(fShader)
        GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile fragment shader.")
            GLES20.glDeleteShader(fShader)
        }

        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vShader)
        GLES20.glAttachShader(mProgram, fShader)
        GLES20.glLinkProgram(mProgram)
        val linked = IntArray(1)
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            Log.e(TAG, "Could not link program.")
        }
    }


    fun update(pointCloud: PointCloud) {
        mPointCloud = pointCloud

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo!![0])

        mNumPoints = mPointCloud!!.getPoints().remaining() / 4

        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, mNumPoints * 16, mPointCloud!!.getPoints())
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    fun draw() {
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mViewMatrix, 0)

        GLES20.glUseProgram(mProgram)

        val position = GLES20.glGetAttribLocation(mProgram, "aPosition")
        val color = GLES20.glGetUniformLocation(mProgram, "uColor")
        val mvp = GLES20.glGetUniformLocation(mProgram, "uMvpMatrix")
        val size = GLES20.glGetUniformLocation(mProgram, "uPointSize")

        GLES20.glEnableVertexAttribArray(position)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo!![0])
        GLES20.glVertexAttribPointer(position, 4, GLES20.GL_FLOAT, false, 16, 0)
        GLES20.glUniform4f(color, 31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f)
        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(size, 5.0f)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mNumPoints)
        GLES20.glDisableVertexAttribArray(position)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        System.arraycopy(projMatrix, 0, mProjMatrix, 0, 16)
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        System.arraycopy(viewMatrix, 0, mViewMatrix, 0, 16)
    }


}