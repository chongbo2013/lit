#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES s_texture;
uniform vec2 u_resolution;

void main() {
    vec2 st = gl_FragCoord.xy/u_resolution.xy;
    vec4 color = texture2D(s_texture, textureCoordinate);
    gl_FragColor = vec4(color.r, color.g, color.b, color.r);

//    gl_FragColor = vec4(color.r, color.g, color.b, 0.5);

//if (color.r == color.g && color.g == color.b && color.r < (252.0/255.0) ) {
//          gl_FragColor = vec4(color.r, color.g, color.b, color.r);
//    } else {
//          gl_FragColor = vec4(color.r, color.g, color.b, 1.0);
//    }

}