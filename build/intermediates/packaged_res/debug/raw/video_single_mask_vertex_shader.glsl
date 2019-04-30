uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
attribute vec2 inputTextureCoordinate;
attribute vec2 inputMaskTextureCoordinate;
varying vec2 textureCoordinate;
varying vec2 maskTextureCoordinate;

void main() {
    gl_Position =  uMVPMatrix * vPosition  ;
    textureCoordinate = inputTextureCoordinate;
    maskTextureCoordinate = inputMaskTextureCoordinate;
}
