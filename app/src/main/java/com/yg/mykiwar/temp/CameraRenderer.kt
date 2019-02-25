package com.yg.mykiwar.temp

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.google.ar.core.Frame
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class CameraRenderer {
    private val TAG = CameraRenderer::class.java.simpleName

        private val vertexShaderString =
                "attribute vec4 aPosition;\n" +
                "attribute vec2 aTexCoord;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "   vTexCoord = aTexCoord;\n" +
                "   gl_Position = aPosition;\n" +
                "}"

    private val fragmentShaderString =
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "    gl_FragColor = texture2D(sTexture, vTexCoord);\n" +
                "}"

    private val QUAD_COORDS = floatArrayOf(-1.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f)

    private val QUAD_TEXCOORDS = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f)

    private val COORDS_PER_VERTEX = 3
    private val TEXCOORDS_PER_VERTEX = 2

    private var mTextures: IntArray? = null
    private var mVertices: FloatBuffer
    private var mTexCoords: FloatBuffer
    private var mTexCoordsTransformed: FloatBuffer
    private var mProgram: Int = 0

    init{
        mVertices = ByteBuffer.allocateDirect(QUAD_COORDS.size * java.lang.Float.SIZE / 8).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mVertices.put(QUAD_COORDS)
        mVertices.position(0)

        mTexCoords = ByteBuffer.allocateDirect(QUAD_TEXCOORDS.size * java.lang.Float.SIZE / 8).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTexCoords.put(QUAD_TEXCOORDS)
        mTexCoords.position(0)

        mTexCoordsTransformed = ByteBuffer.allocateDirect(QUAD_TEXCOORDS.size * java.lang.Float.SIZE / 8).order(ByteOrder.nativeOrder()).asFloatBuffer()
    }

    fun init() {
        mTextures = IntArray(1)
        GLES20.glGenTextures(1, mTextures, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextures!![0])
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

        Log.d(TAG, "[EDWARDS] texture id : " + mTextures!![0])

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

    fun draw() {
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextures!![0])

        GLES20.glUseProgram(mProgram)

        val position = GLES20.glGetAttribLocation(mProgram, "aPosition")
        val texcoord = GLES20.glGetAttribLocation(mProgram, "aTexCoord")

        GLES20.glVertexAttribPointer(position, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mVertices)
        GLES20.glVertexAttribPointer(texcoord, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mTexCoordsTransformed)

        GLES20.glEnableVertexAttribArray(position)
        GLES20.glEnableVertexAttribArray(texcoord)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(position)
        GLES20.glDisableVertexAttribArray(texcoord)
    }


    fun getTextureId(): Int {
        return mTextures!![0]
    }

    fun transformDisplayGeometry(frame: Frame) {
        frame.transformDisplayUvCoords(mTexCoords, mTexCoordsTransformed)
    }
}