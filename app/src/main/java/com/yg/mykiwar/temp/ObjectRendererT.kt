package com.yg.mykiwar.temp

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import de.javagl.obj.Obj
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import java.io.IOException

class ObjectRendererT(var context: Context, var objName: String, var textureName: String) {
        private val TAG = ObjectRendererT::class.java.getSimpleName()

    private val vertexShaderString = "uniform mat4 uMvMatrix;\n" +
    "uniform mat4 uMvpMatrix;\n" +
    "attribute vec4 aPosition;\n" +
    "attribute vec3 aNormal;\n" +
    "attribute vec2 aTexCoord;\n" +
    "varying vec3 vPosition;\n" +
    "varying vec3 vNormal;\n" +
    "varying vec2 vTexCoord;\n" +
    "void main() {\n" +
    "   vPosition = (uMvMatrix * aPosition).xyz;\n" +
    "   vNormal = normalize((uMvMatrix * vec4(aNormal, 0.0)).xyz);\n" +
    "   vTexCoord = aTexCoord;\n" +
    "   gl_Position = uMvpMatrix * vec4(aPosition.xyz, 1.0);\n" +
    "}"

    private val fragmentShaderString = (
"precision mediump float;\n" +
"uniform sampler2D uTexture;\n" +
"varying vec3 vPosition;\n" +
"varying vec3 vNormal;\n" +
"varying vec2 vTexCoord;\n" +
"void main() {\n" +
"    gl_FragColor = texture2D(uTexture, vec2(vTexCoord.x, 1.0 - vTexCoord.y));\n" +
"}")


    private var mObj: Obj? = null

    private var mProgram:Int = 0
    private var mTextures:IntArray? = null
    private var mVbos:IntArray? = null
    private var mVerticesBaseAddress:Int = 0
    private var mTexCoordsBaseAddress:Int = 0
    private var mNormalsBaseAddress:Int = 0
    private var mIndicesCount:Int = 0

    private val mModelMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mProjMatrix = FloatArray(16)

    private var mMinPoint:FloatArray? = null
    private var mMaxPoint:FloatArray? = null

    fun init() {
        try {
            val inStream = context.getAssets().open(objName)
            val bmp = BitmapFactory.decodeStream(context.getAssets().open(textureName))
            mObj = ObjReader.read(inStream)
            mObj = ObjUtils.convertToRenderable(mObj)

            mTextures = IntArray(1)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glGenTextures(1, mTextures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures!![0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

            bmp.recycle()
        } catch (e: IOException) {
            Log.e(TAG, e.message)
        }

        if (mObj == null || mTextures!![0] == -1) {
            Log.e(TAG, "Failed to init obj - $objName, $textureName")
        }

        val indices = ObjData.convertToShortBuffer(ObjData.getFaceVertexIndices(mObj, 3))
        val vertices = ObjData.getVertices(mObj)
        val texCoords = ObjData.getTexCoords(mObj, 2)
        val normals = ObjData.getNormals(mObj)

        mVbos = IntArray(2)
        GLES20.glGenBuffers(2, mVbos, 0)

        mVerticesBaseAddress = 0
        mTexCoordsBaseAddress = mVerticesBaseAddress + 4 * vertices.limit()
        mNormalsBaseAddress = mTexCoordsBaseAddress + 4 * texCoords.limit()
        val totalBytes = mNormalsBaseAddress + 4 * normals.limit()

        mIndicesCount = indices.limit()

        // vertexBufferId
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbos!![0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, totalBytes, null, GLES20.GL_STATIC_DRAW)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mVerticesBaseAddress, 4 * vertices.limit(), vertices)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mTexCoordsBaseAddress, 4 * texCoords.limit(), texCoords)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mNormalsBaseAddress, 4 * normals.limit(), normals)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        // indexBufferId
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVbos!![1])
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, 2 * mIndicesCount, indices, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

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
        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mvMatrix, 0)

        GLES20.glUseProgram(mProgram)

        val mv = GLES20.glGetUniformLocation(mProgram, "uMvMatrix")
        val mvp = GLES20.glGetUniformLocation(mProgram, "uMvpMatrix")

        val position = GLES20.glGetAttribLocation(mProgram, "aPosition")
        val normal = GLES20.glGetAttribLocation(mProgram, "aNormal")
        val texCoord = GLES20.glGetAttribLocation(mProgram, "aTexCoord")

        val texture = GLES20.glGetUniformLocation(mProgram, "uTexture")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures!![0])
        GLES20.glUniform1i(texture, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbos!![0])
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 0, mVerticesBaseAddress)
        GLES20.glVertexAttribPointer(normal, 3, GLES20.GL_FLOAT, false, 0, mNormalsBaseAddress)
        GLES20.glVertexAttribPointer(texCoord, 2, GLES20.GL_FLOAT, false, 0, mTexCoordsBaseAddress)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glUniformMatrix4fv(mv, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0)

        GLES20.glEnableVertexAttribArray(position)
        GLES20.glEnableVertexAttribArray(normal)
        GLES20.glEnableVertexAttribArray(texCoord)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVbos!![1])
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndicesCount, GLES20.GL_UNSIGNED_SHORT, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        GLES20.glDisableVertexAttribArray(position)
        GLES20.glDisableVertexAttribArray(normal)
        GLES20.glDisableVertexAttribArray(texCoord)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
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

    fun getMinPoint(): FloatArray {
        calculateMinMaxPoint()

        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mvMatrix, 0)

        val minPoint = FloatArray(4)
        Matrix.multiplyMV(minPoint, 0, mModelMatrix, 0, floatArrayOf(mMinPoint!![0], mMinPoint!![1], mMinPoint!![2], 1.0f), 0)

        val maxPoint = FloatArray(4)
        Matrix.multiplyMV(maxPoint, 0, mModelMatrix, 0, floatArrayOf(mMaxPoint!![0], mMaxPoint!![1], mMaxPoint!![2], 1.0f), 0)

        val result = FloatArray(3)
        result[0] = Math.min(minPoint[0], maxPoint[0])
        result[1] = Math.min(minPoint[1], maxPoint[1])
        result[2] = Math.min(minPoint[2], maxPoint[2])

        return result
    }

    fun getMaxPoint(): FloatArray {
        calculateMinMaxPoint()

        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mvMatrix, 0)

        val minPoint = FloatArray(4)
        Matrix.multiplyMV(minPoint, 0, mModelMatrix, 0, floatArrayOf(mMinPoint!![0], mMinPoint!![1], mMinPoint!![2], 1.0f), 0)

        val maxPoint = FloatArray(4)
        Matrix.multiplyMV(maxPoint, 0, mModelMatrix, 0, floatArrayOf(mMaxPoint!![0], mMaxPoint!![1], mMaxPoint!![2], 1.0f), 0)

        val result = FloatArray(3)
        result[0] = Math.max(minPoint[0], maxPoint[0])
        result[1] = Math.max(minPoint[1], maxPoint[1])
        result[2] = Math.max(minPoint[2], maxPoint[2])

        return result
    }

    fun calculateMinMaxPoint() {
        if (mMinPoint == null || mMaxPoint == null) {
            mMinPoint = FloatArray(3)
            mMaxPoint = FloatArray(3)

            val vertices = ObjData.getVerticesArray(mObj)

            for (i in 1 until mObj!!.getNumVertices()) {
                mMinPoint!![0] = Math.min(mMinPoint!![0], vertices[i * 3])
                mMinPoint!![1] = Math.min(mMinPoint!![1], vertices[i * 3 + 1])
                mMinPoint!![2] = Math.min(mMinPoint!![2], vertices[i * 3 + 2])

                mMaxPoint!![0] = Math.max(mMaxPoint!![0], vertices[i * 3])
                mMaxPoint!![1] = Math.max(mMaxPoint!![1], vertices[i * 3 + 1])
                mMaxPoint!![2] = Math.max(mMaxPoint!![2], vertices[i * 3 + 2])
            }
        }
    }


}