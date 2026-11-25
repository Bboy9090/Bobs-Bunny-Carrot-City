package com.example.myapplication.ui.game3d

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lightweight 3D triangle-mesh container storing interleaved [x, y, z, nx, ny, nz] vertex data
 * in a GL_ARRAY_BUFFER for mobile OpenGL ES 3.0 rendering.
 */
class Mesh3D(vertices: FloatArray) {
    val vertexCount: Int = vertices.size / 6
    private val vboId: Int

    init {
        val ids = IntArray(1)
        GLES30.glGenBuffers(1, ids, 0)
        vboId = ids[0]

        val buffer: FloatBuffer = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        buffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * 4, buffer, GLES30.GL_STATIC_DRAW)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
    }

    fun draw(posLoc: Int, normLoc: Int) {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        val stride = 6 * 4
        GLES30.glEnableVertexAttribArray(posLoc)
        GLES30.glVertexAttribPointer(posLoc, 3, GLES30.GL_FLOAT, false, stride, 0)
        GLES30.glEnableVertexAttribArray(normLoc)
        GLES30.glVertexAttribPointer(normLoc, 3, GLES30.GL_FLOAT, false, stride, 3 * 4)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
    }
}

object MeshPrimitives {
    fun createBox(): Mesh3D {
        val v = ArrayList<Float>(36 * 6)
        fun addTri(
            x1: Float, y1: Float, z1: Float,
            x2: Float, y2: Float, z2: Float,
            x3: Float, y3: Float, z3: Float,
            nx: Float, ny: Float, nz: Float
        ) {
            v.add(x1); v.add(y1); v.add(z1); v.add(nx); v.add(ny); v.add(nz)
            v.add(x2); v.add(y2); v.add(z2); v.add(nx); v.add(ny); v.add(nz)
            v.add(x3); v.add(y3); v.add(z3); v.add(nx); v.add(ny); v.add(nz)
        }
        fun addFace(
            x1: Float, y1: Float, z1: Float,
            x2: Float, y2: Float, z2: Float,
            x3: Float, y3: Float, z3: Float,
            x4: Float, y4: Float, z4: Float,
            nx: Float, ny: Float, nz: Float
        ) {
            addTri(x1, y1, z1, x2, y2, z2, x3, y3, z3, nx, ny, nz)
            addTri(x1, y1, z1, x3, y3, z3, x4, y4, z4, nx, ny, nz)
        }
        // Front (+Z)
        addFace(-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0f, 0f, 1f)
        // Back (-Z)
        addFace(0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0f, 0f, -1f)
        // Top (+Y)
        addFace(-0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0f, 1f, 0f)
        // Bottom (-Y)
        addFace(-0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0f, -1f, 0f)
        // Right (+X)
        addFace(0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 1f, 0f, 0f)
        // Left (-X)
        addFace(-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -1f, 0f, 0f)
        return Mesh3D(v.toFloatArray())
    }

    fun createSphere(stacks: Int = 10, slices: Int = 14): Mesh3D {
        val v = ArrayList<Float>(stacks * slices * 6 * 6)
        for (i in 0 until stacks) {
            val phi1 = PI * i / stacks
            val phi2 = PI * (i + 1) / stacks
            for (j in 0 until slices) {
                val th1 = 2.0 * PI * j / slices
                val th2 = 2.0 * PI * (j + 1) / slices

                fun push(phi: Double, th: Double) {
                    val x = (sin(phi) * cos(th)).toFloat()
                    val y = cos(phi).toFloat()
                    val z = (sin(phi) * sin(th)).toFloat()
                    v.add(x * 0.5f); v.add(y * 0.5f); v.add(z * 0.5f)
                    v.add(x); v.add(y); v.add(z)
                }

                push(phi1, th1); push(phi2, th1); push(phi2, th2)
                push(phi1, th1); push(phi2, th2); push(phi1, th2)
            }
        }
        return Mesh3D(v.toFloatArray())
    }

    fun createCylinder(slices: Int = 14): Mesh3D {
        val v = ArrayList<Float>(slices * 12 * 6)
        for (j in 0 until slices) {
            val a1 = 2.0 * PI * j / slices
            val a2 = 2.0 * PI * (j + 1) / slices
            val x1 = cos(a1).toFloat() * 0.5f
            val z1 = sin(a1).toFloat() * 0.5f
            val x2 = cos(a2).toFloat() * 0.5f
            val z2 = sin(a2).toFloat() * 0.5f
            val nx1 = cos(a1).toFloat(); val nz1 = sin(a1).toFloat()
            val nx2 = cos(a2).toFloat(); val nz2 = sin(a2).toFloat()

            fun vert(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float) {
                v.add(x); v.add(y); v.add(z); v.add(nx); v.add(ny); v.add(nz)
            }
            // Side quad
            vert(x1, -0.5f, z1, nx1, 0f, nz1)
            vert(x2, -0.5f, z2, nx2, 0f, nz2)
            vert(x2, 0.5f, z2, nx2, 0f, nz2)
            vert(x1, -0.5f, z1, nx1, 0f, nz1)
            vert(x2, 0.5f, z2, nx2, 0f, nz2)
            vert(x1, 0.5f, z1, nx1, 0f, nz1)
            // Top cap
            vert(0f, 0.5f, 0f, 0f, 1f, 0f)
            vert(x1, 0.5f, z1, 0f, 1f, 0f)
            vert(x2, 0.5f, z2, 0f, 1f, 0f)
            // Bottom cap
            vert(0f, -0.5f, 0f, 0f, -1f, 0f)
            vert(x2, -0.5f, z2, 0f, -1f, 0f)
            vert(x1, -0.5f, z1, 0f, -1f, 0f)
        }
        return Mesh3D(v.toFloatArray())
    }

    fun createCone(slices: Int = 12): Mesh3D {
        val v = ArrayList<Float>(slices * 6 * 6)
        for (j in 0 until slices) {
            val a1 = 2.0 * PI * j / slices
            val a2 = 2.0 * PI * (j + 1) / slices
            val x1 = cos(a1).toFloat() * 0.5f
            val z1 = sin(a1).toFloat() * 0.5f
            val x2 = cos(a2).toFloat() * 0.5f
            val z2 = sin(a2).toFloat() * 0.5f
            val nx = ((cos(a1) + cos(a2)) * 0.5).toFloat()
            val nz = ((sin(a1) + sin(a2)) * 0.5).toFloat()

            fun vert(x: Float, y: Float, z: Float, nX: Float, nY: Float, nZ: Float) {
                v.add(x); v.add(y); v.add(z); v.add(nX); v.add(nY); v.add(nZ)
            }
            vert(x1, -0.5f, z1, nx, 0.4f, nz)
            vert(x2, -0.5f, z2, nx, 0.4f, nz)
            vert(0f, 0.5f, 0f, nx, 0.4f, nz)
            vert(0f, -0.5f, 0f, 0f, -1f, 0f)
            vert(x2, -0.5f, z2, 0f, -1f, 0f)
            vert(x1, -0.5f, z1, 0f, -1f, 0f)
        }
        return Mesh3D(v.toFloatArray())
    }
}
