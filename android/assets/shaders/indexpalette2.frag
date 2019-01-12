#ifdef GL_ES
  #define LOWP lowp
  precision mediump float;
#else
  #define LOWP
#endif

uniform sampler2D ColorTable; //256 x 1 pixels
uniform sampler2D ColorMap;   //256 x 1727 pixels
uniform sampler2D u_texture;
uniform mat4 u_projTrans;
uniform int colormapIndex;
uniform int usesColormap;
uniform int blendMode;

varying vec2 v_texCoord;
varying vec4 tint;

void main() {
  int x;
  vec4 color = texture2D(u_texture, v_texCoord);
  vec4 mapping = color;
  if (usesColormap != 0) {
    vec2 mappedIndex;
    x = int(mapping.a * 255.1);
    mappedIndex.x = (float(x) + 0.5) / 256.0;
    mappedIndex.y = (float(colormapIndex) + 0.5) / 1727.0;
    mapping = texture2D(ColorMap, mappedIndex);
  }

  //x = int(mapping.a * 255.1);
  //mapping.x = (float(x) + 0.5) / 256.0;
  //mapping.y = 0.0;
  //vec4 texel = texture2D(ColorTable, mapping.xy);
  vec4 texel = texture2D(ColorTable, mapping.ar);
  if (blendMode == 0) {
    if (texel.a > 0.0) {
      texel.a = tint.a;
    }
  } else if (blendMode == 1) {
    if (texel.a > 0.0) {
      texel.a = (0.299*texel.r + 0.587*texel.g + 0.114*texel.b) * 2.0;
    }
  } else if (blendMode == 2) {
    if (texel.a > 0.0) {
      texel.a = (1.0 - texel.r);
      texel.rgb = tint.rgb;
    }
  } else if (blendMode == 3) {
    if (texel.a > 0.0) {
      texel = tint;
    }
  } else if (blendMode == 4) {
    texel = vec4(1.0, 0.0, 0.0, 1.0);
  }

  gl_FragColor = texel;
}
