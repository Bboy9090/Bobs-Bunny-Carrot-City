package com.example.myapplication.ui.game3d

import android.opengl.GLES30

/**
 * Stylized 3D cartoon / N64-modern shader with directional sun lighting,
 * warm hemisphere ambient, and crisp rim highlight.
 */
class DioramaShader {
    val programId: Int
    val posLoc: Int
    val normLoc: Int
    val mvpLoc: Int
    val modelLoc: Int
    val colorLoc: Int
    val lightDirLoc: Int

    init {
        val vertexSrc = """
            #version 300 es
            layout(location = 0) in vec3 aPosition;
            layout(location = 1) in vec3 aNormal;
            uniform mat4 uMvpMatrix;
            uniform mat4 uModelMatrix;
            out vec3 vWorldNormal;
            out vec3 vWorldPos;
            void main() {
                vec4 worldPos = uModelMatrix * vec4(aPosition, 1.0);
                vWorldPos = worldPos.xyz;
                vWorldNormal = normalize(mat3(uModelMatrix) * aNormal);
                gl_Position = uMvpMatrix * vec4(aPosition, 1.0);
            }
        """.trimIndent()

        val fragmentSrc = """
            #version 300 es
            precision mediump float;
            in vec3 vWorldNormal;
            in vec3 vWorldPos;
            uniform vec4 uColor;
            uniform vec3 uLightDir;
            out vec4 fragColor;
            void main() {
                vec3 n = normalize(vWorldNormal);
                vec3 l = normalize(uLightDir);
                float ndotl = max(dot(n, l), 0.0);
                // Stylized two-band + smooth wrap diffuse for vibrant cartoon look
                float toon = smoothstep(0.05, 0.45, ndotl) * 0.55 + ndotl * 0.25;
                float hemi = (n.y * 0.5 + 0.5) * 0.22;
                // Rim highlight from front-top camera direction
                vec3 viewDir = normalize(vec3(0.0, 0.25, 1.0));
                float rim = pow(1.0 - max(dot(n, viewDir), 0.0), 2.5) * 0.20;
                vec3 lit = uColor.rgb * (0.38 + toon + hemi) + vec3(1.0, 0.96, 0.85) * rim;
                fragColor = vec4(clamp(lit, 0.0, 1.0), uColor.a);
            }
        """.trimIndent()

        val vs = compileShader(GLES30.GL_VERTEX_SHADER, vertexSrc)
        val fs = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSrc)
        programId = GLES30.glCreateProgram().also { prog ->
            GLES30.glAttachShader(prog, vs)
            GLES30.glAttachShader(prog, fs)
            GLES30.glLinkProgram(prog)
        }
        posLoc = GLES30.glGetAttribLocation(programId, "aPosition")
        normLoc = GLES30.glGetAttribLocation(programId, "aNormal")
        mvpLoc = GLES30.glGetUniformLocation(programId, "uMvpMatrix")
        modelLoc = GLES30.glGetUniformLocation(programId, "uModelMatrix")
        colorLoc = GLES30.glGetUniformLocation(programId, "uColor")
        lightDirLoc = GLES30.glGetUniformLocation(programId, "uLightDir")
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, src)
        GLES30.glCompileShader(shader)
        return shader
    }
}
