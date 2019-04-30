#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES s_texture;
uniform samplerExternalOES s_mask_texture;
uniform vec2 u_resolution;

void main() {
    vec2 st = gl_FragCoord.xy/u_resolution.xy;
    vec4 color = texture2D(s_texture, textureCoordinate);
    vec4 color_mask = texture2D(s_mask_texture, textureCoordinate);

    //蒙版标准：黑色为透明区域，白色为不透明区域，黑白混合为半透明区域
    // r|g|b / 255 即为当前透明度 （使用0.99而不是小于1是因为避免白色不纯导致全部透明）
    if (color_mask.r <= 0.99 ){
            gl_FragColor = vec4(color.r, color.g, color.b, color_mask.r);
    }else{
            gl_FragColor =  vec4(color.r, color.g, color.b, 1.0);
    }


}