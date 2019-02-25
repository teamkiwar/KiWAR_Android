package com.yg.mykiwar.temp

import android.graphics.Color
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.Plane
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class PlaneRendererT(var color: Int, var alpha: Float) {
        private val TAG = PlaneRendererT::class.java.simpleName

    private val INITIAL_COUNT = 128

    private val vertexShaderString = "uniform mat4 uMvpMatrix;\n" +
    "uniform vec4 uColor;\n" +
    "attribute vec3 aPosition;\n" +
    "varying vec4 vColor;\n" +
    "void main() {\n" +
    "   vColor = uColor;\n" +
    "   gl_Position = uMvpMatrix * vec4(aPosition.xyz, 1.0);\n" +
    "}"

    private val fragmentShaderString = (
        "precision mediump float;\n" +
        "varying vec4 vColor;\n" +
        "void main() {\n" +
        "    gl_FragColor = vColor;\n" +
        "}")

    private var mProgram:Int = 0

    private val mModelMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mProjMatrix = FloatArray(16)

    private var mVertices: FloatBuffer? = null
    private var mIndices: ShortBuffer? = null
    private var mColor:FloatArray

    init {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        val a = Color.alpha(color) / 255f

        mColor = floatArrayOf(r, g, b, alpha)

        mVertices = ByteBuffer.allocateDirect(INITIAL_COUNT * 3 * 2 * java.lang.Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mVertices!!.position(0)

        mIndices = ByteBuffer.allocateDirect(INITIAL_COUNT * 3 * 3 * java.lang.Short.BYTES).order(ByteOrder.nativeOrder()).asShortBuffer()
        mIndices!!.position(0)
    }

    fun init() {

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


    fun update(plane: Plane) {
        val planeMatrix = FloatArray(16)
        plane.centerPose.toMatrix(planeMatrix, 0)

        setModelMatrix(planeMatrix)

        val polygon = plane.polygon
        if (polygon == null) {
            mVertices!!.limit(0)
            mIndices!!.limit(0)
            return
        }

        polygon.rewind()
        val boundaryVertices = polygon.limit() / 2
        val numVertices = boundaryVertices * 2
        val numIndices = boundaryVertices * 3

        if (mVertices!!.capacity() < numVertices * 3) {
            var size = mVertices!!.capacity()
            while (size < numVertices * 3) {
                size *= 2
            }
            mVertices = ByteBuffer.allocateDirect(size * java.lang.Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer()
        }
        if (mIndices!!.capacity() < numIndices) {
            var size = mIndices!!.capacity()
            while (size < numIndices) {
                size *= 2
            }
            mIndices = ByteBuffer.allocateDirect(size * java.lang.Short.BYTES).order(ByteOrder.nativeOrder()).asShortBuffer()
        }

        mVertices!!.rewind()
        mVertices!!.limit(numVertices * 3)

        while (polygon.hasRemaining()) {
            val x = polygon.get()
            val z = polygon.get()
            mVertices!!.put(x)
            mVertices!!.put(0.0f)
            mVertices!!.put(z)
            mVertices!!.put(x)
            mVertices!!.put(0.0f)
            mVertices!!.put(z)
        }

        mIndices!!.rewind()
        mIndices!!.limit(numIndices)

        mIndices!!.put(((boundaryVertices - 1) * 2).toShort())
        for (i in 0 until boundaryVertices) {
            mIndices!!.put((i * 2).toShort())
            mIndices!!.put((i * 2 + 1).toShort())
        }
        mIndices!!.put(1.toShort())

        for (i in 1 until boundaryVertices / 2) {
            mIndices!!.put(((boundaryVertices - 1 - i) * 2 + 1).toShort())
            mIndices!!.put((i * 2 + 1).toShort())
        }
        if (boundaryVertices % 2 != 0) {
            mIndices!!.put((boundaryVertices / 2 * 2 + 1).toShort())
        }

        mVertices!!.rewind()
        mIndices!!.rewind()
    }

    fun draw() {
        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mvMatrix, 0)

        GLES20.glUseProgram(mProgram)

        val position = GLES20.glGetAttribLocation(mProgram, "aPosition")
        val color = GLES20.glGetUniformLocation(mProgram, "uColor")
        val mvp = GLES20.glGetUniformLocation(mProgram, "uMvpMatrix")

        GLES20.glEnableVertexAttribArray(position)
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 3 * java.lang.Float.BYTES, mVertices)

        GLES20.glUniform4f(color, mColor[0], mColor[1], mColor[2], mColor[3])
        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndices!!.limit(), GLES20.GL_UNSIGNED_SHORT, mIndices)

        GLES20.glDisableVertexAttribArray(position)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    fun setModelMatrix(modelMatrix: FloatArray) {
        System.arraycopy(modelMatrix, 0, mModelMatrix, 0, 16)
    }

    fun setProjectionMatrix(projMatrix: FloatArray) {
        System.arraycopy(projMatrix, 0, mProjMatrix, 0, 16)
    }

    fun setViewMatrix(viewMatrix: FloatArray) {
        System.arraycopy(viewMatrix, 0, mViewMatrix, 0, 16)
    }

}