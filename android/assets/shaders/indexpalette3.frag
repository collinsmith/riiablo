#ifdef GL_ES
  #define LOWP lowp
  precision mediump float;
#else
  #define LOWP
#endif

uniform sampler2D ColorTable; //256 x 1  pixels
uniform sampler2D ColorMap;   //256 x 22 pixels
uniform sampler2D u_texture;
uniform mat4 u_projTrans;
uniform int blendMode;
uniform int colormapId;
uniform float gamma;

varying vec2 v_texCoord;
varying vec4 tint;

void main() {
  vec4 color = texture2D(u_texture, v_texCoord);
  if (colormapId > 0 && color.a > 0.0) {
    color.r = (float(colormapId) + 0.5) / 22.0;
    color = texture2D(ColorMap, color.ar);
  }

  color = texture2D(ColorTable, color.ar);

  // Set alpha to tint alpha, including palette id 0
  if (blendMode == 0) {
    color.a = tint.a;

  // Set alpha to tint alpha
  } else if (blendMode == 1) {
    if (color.a > 0.0) color.a = tint.a;

  // Set alpha based on luminance
  } else if (blendMode == 2) {
    if (color.a > 0.0) color.a = (0.299*color.r + 0.587*color.g + 0.114*color.b) * 2.0;

  // Set alpha based on luminance and color to tint
  } else if (blendMode == 3) {
    if (color.a > 0.0) {
      //color.a = (0.299*color.r + 0.587*color.g + 0.114*color.b) * 2.0;
      //color.rgb = tint.rgb;
      float avg = (color.r + color.g + color.b) / 3.0;
      color = vec4(avg, avg, avg, 1.0) * tint;
    }

  // Sets color to tint
  } else if (blendMode == 4) {
    if (color.a > 0.0) color = tint;

  // Sets color to tint, blending using src as 1.0 - alpha
  } else if (blendMode == 5) {
    if (color.a > 0.0) {
      color.a = (1.0 - color.r);
      color.rgb = tint.rgb;
    }

  // Sets color to tint, blending using src as alpha
  } else if (blendMode == 6) {
    if (color.a > 0.0) {
      color.a = color.r;
      color.rgb = tint.rgb;
    }

  // Same as 1, except adds tint
  } else if (blendMode == 7) {
    if (color.a > 0.0) {
      color.rgb += tint.rgb;
    }

  // Same as 1, except adds contrast and brightness
  } else if (blendMode == 8) {
    if (color.a > 0.0) {
      // Apply contrast
      color.rgb -= 0.5;
      color.rgb *= 1.8;
      color.rgb += 0.5;

      // Apply brightness
      color.rgb += 0.4;
    }

  // Same as 1, except hard coded red
  } else if (blendMode == 9) {
    if (color.a > 0.0) {
      color.a = tint.a;
      color.r += 0.04;
      color.g = 0.0;
      color.b = 0.0;
    }

  // Same as 8, except darker
  } else if (blendMode == 10) {
    if (color.a > 0.0) {
      color.rgb *= tint.rgb;
    }
  }

  vec3 colorRGB = pow(color.rgb, vec3(1.0 / gamma));

  // TODO: Move this effect and gamma effect to a separate shader
  // TODO: Add configs to this effect for contrast + brightness
  colorRGB -= 0.5;
  colorRGB *= 1.20;
  colorRGB += 0.60;

  gl_FragColor = vec4(colorRGB, color.a);
}
