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
uniform bool usesColormap;
uniform int blendMode;

varying vec2 v_texCoord;

/*void main() {
  vec4 index = texture2D(u_texture, v_texCoord);
  vec4 texel = texture2D(ColorTable, index.xy);
  gl_FragColor = texel;
}*/

/*void main() {
  vec4 color       = texture2D(u_texture, v_texCoord);
  vec2 mappedIndex = vec2(color.r, colormapIndex);
  vec4 mapping     = texture2D(ColorMap, mappedIndex);
  vec4 texel       = texture2D(ColorTable, mapping.xy);
  gl_FragColor = texel;
}*/

void main() {
  vec4 color = texture2D(u_texture, v_texCoord);
  vec4 mapping = color;
  if (usesColormap) {
    vec2 mappedIndex = vec2(color.r, float(colormapIndex) / 1727.0);
    mapping = texture2D(ColorMap, mappedIndex);
  }

  vec4 texel = texture2D(ColorTable, mapping.xy);
  // NO SWITCH STATEMENT SUPPORT ON ANDROID
  if (blendMode == 1) {
    if (texel.a > 0.0) {
      //texel.a = (texel.r + texel.g + texel.b) / 3.0;
      texel.a = (0.299*texel.r + 0.587*texel.g + 0.114*texel.b) * 2.0;
    }
  } else if (blendMode == 2) {
    if (texel.a > 0.0) {
      texel.a = 1.0 - texel.r;
      texel.r = texel.g = texel.b = 0.0;
    }
  } else if (blendMode == 3) {
    texel = vec4(1.0, 0.0, 0.0, 1.0);
  }
  gl_FragColor = texel;

  //float gamma = 1.2;
  //gl_FragColor.rgb = pow(gl_FragColor.rgb, vec3(1.0/gamma));
}
