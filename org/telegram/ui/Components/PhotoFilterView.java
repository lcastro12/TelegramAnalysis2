package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build.VERSION;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.Semaphore;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.ui.Cells.PhotoEditToolCell;
import org.telegram.ui.Components.PhotoEditorSeekBar.PhotoEditorSeekBarDelegate;
import org.telegram.ui.Components.PhotoFilterBlurControl.PhotoFilterLinearBlurControlDelegate;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;

@SuppressLint({"NewApi"})
public class PhotoFilterView extends FrameLayout {
    private Bitmap bitmapToEdit;
    private float blurAngle = 1.5707964f;
    private PhotoFilterBlurControl blurControl;
    private float blurExcludeBlurSize = 0.15f;
    private Point blurExcludePoint = new Point(0.5f, 0.5f);
    private float blurExcludeSize = 0.35f;
    private FrameLayout blurLayout;
    private TextView blurLinearButton;
    private TextView blurOffButton;
    private TextView blurRadialButton;
    private TextView blurTextView;
    private int blurTool = 9;
    private int blurType = 0;
    private TextView cancelTextView;
    private int contrastTool = 2;
    private float contrastValue = 0.0f;
    private TextView doneTextView;
    private FrameLayout editView;
    private EGLThread eglThread;
    private int enhanceTool = 0;
    private float enhanceValue = 0.0f;
    private int exposureTool = 1;
    private float exposureValue = 0.0f;
    private int grainTool = 8;
    private float grainValue = 0.0f;
    private int highlightsTool = 5;
    private float highlightsValue = 0.0f;
    private int orientation;
    private TextView paramTextView;
    private float previousValue;
    private RecyclerListView recyclerListView;
    private int saturationTool = 4;
    private float saturationValue = 0.0f;
    private int selectedTool = -1;
    private int shadowsTool = 6;
    private float shadowsValue = 0.0f;
    private int sharpenTool = 10;
    private float sharpenValue = 0.0f;
    private boolean showOriginal;
    private TextureView textureView;
    private ToolsAdapter toolsAdapter;
    private FrameLayout toolsView;
    private PhotoEditorSeekBar valueSeekBar;
    private TextView valueTextView;
    private int vignetteTool = 7;
    private float vignetteValue = 0.0f;
    private int warmthTool = 3;
    private float warmthValue = 0.0f;

    class C09371 implements SurfaceTextureListener {

        class C09361 implements Runnable {
            C09361() {
            }

            public void run() {
                PhotoFilterView.this.eglThread.requestRender(false);
            }
        }

        C09371() {
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (PhotoFilterView.this.eglThread == null && surface != null) {
                PhotoFilterView.this.eglThread = new EGLThread(surface, PhotoFilterView.this.bitmapToEdit);
                PhotoFilterView.this.eglThread.setSurfaceTextureSize(width, height);
                PhotoFilterView.this.eglThread.requestRender(true);
            }
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (PhotoFilterView.this.eglThread != null) {
                PhotoFilterView.this.eglThread.setSurfaceTextureSize(width, height);
                PhotoFilterView.this.eglThread.requestRender(false);
                PhotoFilterView.this.eglThread.postRunnable(new C09361());
            }
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (PhotoFilterView.this.eglThread != null) {
                PhotoFilterView.this.eglThread.shutdown();
                PhotoFilterView.this.eglThread = null;
            }
            return true;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

    class C09384 implements OnClickListener {
        C09384() {
        }

        public void onClick(View v) {
            if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.enhanceTool) {
                PhotoFilterView.this.enhanceValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.highlightsTool) {
                PhotoFilterView.this.highlightsValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.contrastTool) {
                PhotoFilterView.this.contrastValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.exposureTool) {
                PhotoFilterView.this.exposureValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.warmthTool) {
                PhotoFilterView.this.warmthValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.saturationTool) {
                PhotoFilterView.this.saturationValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.vignetteTool) {
                PhotoFilterView.this.vignetteValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.shadowsTool) {
                PhotoFilterView.this.shadowsValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.grainTool) {
                PhotoFilterView.this.grainValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.sharpenTool) {
                PhotoFilterView.this.sharpenValue = PhotoFilterView.this.previousValue;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.blurTool) {
                PhotoFilterView.this.blurType = (int) PhotoFilterView.this.previousValue;
            }
            if (PhotoFilterView.this.eglThread != null) {
                PhotoFilterView.this.eglThread.requestRender(PhotoFilterView.this.selectedTool != PhotoFilterView.this.blurTool);
            }
            PhotoFilterView.this.switchToOrFromEditMode();
        }
    }

    class C09395 implements OnClickListener {
        C09395() {
        }

        public void onClick(View v) {
            PhotoFilterView.this.toolsAdapter.notifyDataSetChanged();
            PhotoFilterView.this.switchToOrFromEditMode();
        }
    }

    class C09407 implements OnClickListener {
        C09407() {
        }

        public void onClick(View v) {
            PhotoFilterView.this.blurType = 0;
            PhotoFilterView.this.updateSelectedBlurType();
            PhotoFilterView.this.blurControl.setVisibility(4);
            if (PhotoFilterView.this.eglThread != null) {
                PhotoFilterView.this.eglThread.requestRender(false);
            }
        }
    }

    class C09418 implements OnClickListener {
        C09418() {
        }

        public void onClick(View v) {
            PhotoFilterView.this.blurType = 1;
            PhotoFilterView.this.updateSelectedBlurType();
            PhotoFilterView.this.blurControl.setVisibility(0);
            PhotoFilterView.this.blurControl.setType(1);
            if (PhotoFilterView.this.eglThread != null) {
                PhotoFilterView.this.eglThread.requestRender(false);
            }
        }
    }

    class C09429 implements OnClickListener {
        C09429() {
        }

        public void onClick(View v) {
            PhotoFilterView.this.blurType = 2;
            PhotoFilterView.this.updateSelectedBlurType();
            PhotoFilterView.this.blurControl.setVisibility(0);
            PhotoFilterView.this.blurControl.setType(0);
            if (PhotoFilterView.this.eglThread != null) {
                PhotoFilterView.this.eglThread.requestRender(false);
            }
        }
    }

    class C15692 implements PhotoFilterLinearBlurControlDelegate {
        C15692() {
        }

        public void valueChanged(Point centerPoint, float falloff, float size, float angle) {
            PhotoFilterView.this.blurExcludeSize = size;
            PhotoFilterView.this.blurExcludePoint = centerPoint;
            PhotoFilterView.this.blurExcludeBlurSize = falloff;
            PhotoFilterView.this.blurAngle = angle;
            if (PhotoFilterView.this.eglThread != null) {
                PhotoFilterView.this.eglThread.requestRender(false);
            }
        }
    }

    class C15703 implements OnItemClickListener {
        C15703() {
        }

        public void onItemClick(View view, int position) {
            PhotoFilterView.this.selectedTool = position;
            if (position == PhotoFilterView.this.enhanceTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.enhanceValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(0, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Enhance", C0553R.string.Enhance));
            } else if (position == PhotoFilterView.this.highlightsTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.highlightsValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(0, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Highlights", C0553R.string.Highlights));
            } else if (position == PhotoFilterView.this.contrastTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.contrastValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(-100, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Contrast", C0553R.string.Contrast));
            } else if (position == PhotoFilterView.this.exposureTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.exposureValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(-100, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Exposure", C0553R.string.Exposure));
            } else if (position == PhotoFilterView.this.warmthTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.warmthValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(-100, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Warmth", C0553R.string.Warmth));
            } else if (position == PhotoFilterView.this.saturationTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.saturationValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(-100, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Saturation", C0553R.string.Saturation));
            } else if (position == PhotoFilterView.this.vignetteTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.vignetteValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(0, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Vignette", C0553R.string.Vignette));
            } else if (position == PhotoFilterView.this.shadowsTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.shadowsValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(0, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Shadows", C0553R.string.Shadows));
            } else if (position == PhotoFilterView.this.grainTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.grainValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(0, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Grain", C0553R.string.Grain));
            } else if (position == PhotoFilterView.this.sharpenTool) {
                PhotoFilterView.this.previousValue = PhotoFilterView.this.sharpenValue;
                PhotoFilterView.this.valueSeekBar.setMinMax(0, 100);
                PhotoFilterView.this.paramTextView.setText(LocaleController.getString("Sharpen", C0553R.string.Sharpen));
            } else if (position == PhotoFilterView.this.blurTool) {
                PhotoFilterView.this.previousValue = (float) PhotoFilterView.this.blurType;
            }
            PhotoFilterView.this.valueSeekBar.setProgress((int) PhotoFilterView.this.previousValue, false);
            PhotoFilterView.this.updateValueTextView();
            PhotoFilterView.this.switchToOrFromEditMode();
        }
    }

    class C15716 implements PhotoEditorSeekBarDelegate {
        C15716() {
        }

        public void onProgressChanged() {
            int progress = PhotoFilterView.this.valueSeekBar.getProgress();
            if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.enhanceTool) {
                PhotoFilterView.this.enhanceValue = (float) progress;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.highlightsTool) {
                PhotoFilterView.this.highlightsValue = (float) progress;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.contrastTool) {
                PhotoFilterView.this.contrastValue = (float) progress;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.exposureTool) {
                PhotoFilterView.this.exposureValue = (float) progress;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.warmthTool) {
                PhotoFilterView.this.warmthValue = (float) progress;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.saturationTool) {
                PhotoFilterView.this.saturationValue = (float) progress;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.vignetteTool) {
                PhotoFilterView.this.vignetteValue = (float) progress;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.shadowsTool) {
                PhotoFilterView.this.shadowsValue = (float) progress;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.grainTool) {
                PhotoFilterView.this.grainValue = (float) progress;
            } else if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.sharpenTool) {
                PhotoFilterView.this.sharpenValue = (float) progress;
            }
            PhotoFilterView.this.updateValueTextView();
            if (PhotoFilterView.this.eglThread != null) {
                PhotoFilterView.this.eglThread.requestRender(true);
            }
        }
    }

    public class EGLThread extends DispatchQueue {
        private static final int PGPhotoEnhanceHistogramBins = 256;
        private static final int PGPhotoEnhanceSegments = 4;
        private static final String blurFragmentShaderCode = "uniform sampler2D sourceImage;varying highp vec2 blurCoordinates[9];void main() {lowp vec4 sum = vec4(0.0);sum += texture2D(sourceImage, blurCoordinates[0]) * 0.133571;sum += texture2D(sourceImage, blurCoordinates[1]) * 0.233308;sum += texture2D(sourceImage, blurCoordinates[2]) * 0.233308;sum += texture2D(sourceImage, blurCoordinates[3]) * 0.135928;sum += texture2D(sourceImage, blurCoordinates[4]) * 0.135928;sum += texture2D(sourceImage, blurCoordinates[5]) * 0.051383;sum += texture2D(sourceImage, blurCoordinates[6]) * 0.051383;sum += texture2D(sourceImage, blurCoordinates[7]) * 0.012595;sum += texture2D(sourceImage, blurCoordinates[8]) * 0.012595;gl_FragColor = sum;}";
        private static final String blurVertexShaderCode = "attribute vec4 position;attribute vec4 inputTexCoord;uniform highp float texelWidthOffset;uniform highp float texelHeightOffset;varying vec2 blurCoordinates[9];void main() {gl_Position = position;vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);blurCoordinates[0] = inputTexCoord.xy;blurCoordinates[1] = inputTexCoord.xy + singleStepOffset * 1.458430;blurCoordinates[2] = inputTexCoord.xy - singleStepOffset * 1.458430;blurCoordinates[3] = inputTexCoord.xy + singleStepOffset * 3.403985;blurCoordinates[4] = inputTexCoord.xy - singleStepOffset * 3.403985;blurCoordinates[5] = inputTexCoord.xy + singleStepOffset * 5.351806;blurCoordinates[6] = inputTexCoord.xy - singleStepOffset * 5.351806;blurCoordinates[7] = inputTexCoord.xy + singleStepOffset * 7.302940;blurCoordinates[8] = inputTexCoord.xy - singleStepOffset * 7.302940;}";
        private static final String enhanceFragmentShaderCode = "precision highp float;varying vec2 texCoord;uniform sampler2D sourceImage;uniform sampler2D inputImageTexture2;uniform float intensity;float enhance(float value) {const vec2 offset = vec2(0.001953125, 0.03125);value = value + offset.x;vec2 coord = (clamp(texCoord, 0.125, 1.0 - 0.125001) - 0.125) * 4.0;vec2 frac = fract(coord);coord = floor(coord);float p00 = float(coord.y * 4.0 + coord.x) * 0.0625 + offset.y;float p01 = float(coord.y * 4.0 + coord.x + 1.0) * 0.0625 + offset.y;float p10 = float((coord.y + 1.0) * 4.0 + coord.x) * 0.0625 + offset.y;float p11 = float((coord.y + 1.0) * 4.0 + coord.x + 1.0) * 0.0625 + offset.y;vec3 c00 = texture2D(inputImageTexture2, vec2(value, p00)).rgb;vec3 c01 = texture2D(inputImageTexture2, vec2(value, p01)).rgb;vec3 c10 = texture2D(inputImageTexture2, vec2(value, p10)).rgb;vec3 c11 = texture2D(inputImageTexture2, vec2(value, p11)).rgb;float c1 = ((c00.r - c00.g) / (c00.b - c00.g));float c2 = ((c01.r - c01.g) / (c01.b - c01.g));float c3 = ((c10.r - c10.g) / (c10.b - c10.g));float c4 = ((c11.r - c11.g) / (c11.b - c11.g));float c1_2 = mix(c1, c2, frac.x);float c3_4 = mix(c3, c4, frac.x);return mix(c1_2, c3_4, frac.y);}vec3 hsv_to_rgb(vec3 c) {vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);}void main() {vec4 texel = texture2D(sourceImage, texCoord);vec4 hsv = texel;hsv.y = min(1.0, hsv.y * 1.2);hsv.z = min(1.0, enhance(hsv.z) * 1.1);gl_FragColor = vec4(hsv_to_rgb(mix(texel.xyz, hsv.xyz, intensity)), texel.w);}";
        private static final String linearBlurFragmentShaderCode = "varying highp vec2 texCoord;uniform sampler2D sourceImage;uniform sampler2D inputImageTexture2;uniform lowp float excludeSize;uniform lowp vec2 excludePoint;uniform lowp float excludeBlurSize;uniform highp float angle;uniform highp float aspectRatio;void main() {lowp vec4 sharpImageColor = texture2D(sourceImage, texCoord);lowp vec4 blurredImageColor = texture2D(inputImageTexture2, texCoord);highp vec2 texCoordToUse = vec2(texCoord.x, (texCoord.y * aspectRatio + 0.5 - 0.5 * aspectRatio));highp float distanceFromCenter = abs((texCoordToUse.x - excludePoint.x) * aspectRatio * cos(angle) + (texCoordToUse.y - excludePoint.y) * sin(angle));gl_FragColor = mix(sharpImageColor, blurredImageColor, smoothstep(excludeSize - excludeBlurSize, excludeSize, distanceFromCenter));}";
        private static final String radialBlurFragmentShaderCode = "varying highp vec2 texCoord;uniform sampler2D sourceImage;uniform sampler2D inputImageTexture2;uniform lowp float excludeSize;uniform lowp vec2 excludePoint;uniform lowp float excludeBlurSize;uniform highp float aspectRatio;void main() {lowp vec4 sharpImageColor = texture2D(sourceImage, texCoord);lowp vec4 blurredImageColor = texture2D(inputImageTexture2, texCoord);highp vec2 texCoordToUse = vec2(texCoord.x, (texCoord.y * aspectRatio + 0.5 - 0.5 * aspectRatio));highp float distanceFromCenter = distance(excludePoint, texCoordToUse);gl_FragColor = mix(sharpImageColor, blurredImageColor, smoothstep(excludeSize - excludeBlurSize, excludeSize, distanceFromCenter));}";
        private static final String rgbToHsvFragmentShaderCode = "precision highp float;varying vec2 texCoord;uniform sampler2D sourceImage;vec3 rgb_to_hsv(vec3 c) {vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);vec4 p = c.g < c.b ? vec4(c.bg, K.wz) : vec4(c.gb, K.xy);vec4 q = c.r < p.x ? vec4(p.xyw, c.r) : vec4(c.r, p.yzx);float d = q.x - min(q.w, q.y);float e = 1.0e-10;return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);}void main() {vec4 texel = texture2D(sourceImage, texCoord);gl_FragColor = vec4(rgb_to_hsv(texel.rgb), texel.a);}";
        private static final String sharpenFragmentShaderCode = "precision highp float;varying vec2 texCoord;varying vec2 leftTexCoord;varying vec2 rightTexCoord;varying vec2 topTexCoord;varying vec2 bottomTexCoord;uniform sampler2D sourceImage;uniform float sharpen;void main() {vec4 result = texture2D(sourceImage, texCoord);vec3 leftTextureColor = texture2D(sourceImage, leftTexCoord).rgb;vec3 rightTextureColor = texture2D(sourceImage, rightTexCoord).rgb;vec3 topTextureColor = texture2D(sourceImage, topTexCoord).rgb;vec3 bottomTextureColor = texture2D(sourceImage, bottomTexCoord).rgb;result.rgb = result.rgb * (1.0 + 4.0 * sharpen) - (leftTextureColor + rightTextureColor + topTextureColor + bottomTextureColor) * sharpen;gl_FragColor = result;}";
        private static final String sharpenVertexShaderCode = "attribute vec4 position;attribute vec2 inputTexCoord;varying vec2 texCoord;uniform highp float inputWidth;uniform highp float inputHeight;varying vec2 leftTexCoord;varying vec2 rightTexCoord;varying vec2 topTexCoord;varying vec2 bottomTexCoord;void main() {gl_Position = position;texCoord = inputTexCoord;highp vec2 widthStep = vec2(1.0 / inputWidth, 0.0);highp vec2 heightStep = vec2(0.0, 1.0 / inputHeight);leftTexCoord = inputTexCoord - widthStep;rightTexCoord = inputTexCoord + widthStep;topTexCoord = inputTexCoord + heightStep;bottomTexCoord = inputTexCoord - heightStep;}";
        private static final String simpleFragmentShaderCode = "varying highp vec2 texCoord;uniform sampler2D sourceImage;void main() {gl_FragColor = texture2D(sourceImage, texCoord);}";
        private static final String simpleVertexShaderCode = "attribute vec4 position;attribute vec2 inputTexCoord;varying vec2 texCoord;void main() {gl_Position = position;texCoord = inputTexCoord;}";
        private static final String toolsFragmentShaderCode = "precision highp float;varying vec2 texCoord;uniform float inputWidth;uniform float inputHeight;uniform sampler2D sourceImage;uniform float shadows;uniform float width;uniform float height;const vec3 hsLuminanceWeighting = vec3(0.3, 0.3, 0.3);uniform float highlights;uniform float exposure;uniform float contrast;const vec3 satLuminanceWeighting = vec3(0.2126, 0.7152, 0.0722);uniform float saturation;uniform float warmth;uniform float grain;const float permTexUnit = 1.0 / 256.0;const float permTexUnitHalf = 0.5 / 256.0;const float grainsize = 2.3;uniform float vignette;float getLuma(vec3 rgbP) { return (0.299 * rgbP.r) + (0.587 * rgbP.g) + (0.114 * rgbP.b); }vec3 rgbToYuv(vec3 inP) {vec3 outP;outP.r = getLuma(inP);outP.g = (1.0 / 1.772) * (inP.b - outP.r);outP.b = (1.0 / 1.402) * (inP.r - outP.r);return outP; }vec3 yuvToRgb(vec3 inP) {return vec3(1.402 * inP.b + inP.r, (inP.r - (0.299 * 1.402 / 0.587) * inP.b - (0.114 * 1.772 / 0.587) * inP.g), 1.772 * inP.g + inP.r);}float easeInOutSigmoid(float value, float strength) {float t = 1.0 / (1.0 - strength);if (value > 0.5) {return 1.0 - pow(2.0 - 2.0 * value, t) * 0.5;} else {return pow(2.0 * value, t) * 0.5; }}vec4 rnm(in vec2 tc) {float noise = sin(dot(tc,vec2(12.9898,78.233))) * 43758.5453;float noiseR = fract(noise)*2.0-1.0;float noiseG = fract(noise*1.2154)*2.0-1.0;float noiseB = fract(noise*1.3453)*2.0-1.0;float noiseA = fract(noise*1.3647)*2.0-1.0;return vec4(noiseR,noiseG,noiseB,noiseA);}float fade(in float t) {return t*t*t*(t*(t*6.0-15.0)+10.0);}float pnoise3D(in vec3 p) {vec3 pi = permTexUnit*floor(p)+permTexUnitHalf;vec3 pf = fract(p);float perm00 = rnm(pi.xy).a;vec3 grad000 = rnm(vec2(perm00, pi.z)).rgb * 4.0 - 1.0;float n000 = dot(grad000, pf);vec3 grad001 = rnm(vec2(perm00, pi.z + permTexUnit)).rgb * 4.0 - 1.0;float n001 = dot(grad001, pf - vec3(0.0, 0.0, 1.0));float perm01 = rnm(pi.xy + vec2(0.0, permTexUnit)).a;vec3 grad010 = rnm(vec2(perm01, pi.z)).rgb * 4.0 - 1.0;float n010 = dot(grad010, pf - vec3(0.0, 1.0, 0.0));vec3 grad011 = rnm(vec2(perm01, pi.z + permTexUnit)).rgb * 4.0 - 1.0;float n011 = dot(grad011, pf - vec3(0.0, 1.0, 1.0));float perm10 = rnm(pi.xy + vec2(permTexUnit, 0.0)).a;vec3 grad100 = rnm(vec2(perm10, pi.z)).rgb * 4.0 - 1.0;float n100 = dot(grad100, pf - vec3(1.0, 0.0, 0.0));vec3 grad101 = rnm(vec2(perm10, pi.z + permTexUnit)).rgb * 4.0 - 1.0;float n101 = dot(grad101, pf - vec3(1.0, 0.0, 1.0));float perm11 = rnm(pi.xy + vec2(permTexUnit, permTexUnit)).a;vec3 grad110 = rnm(vec2(perm11, pi.z)).rgb * 4.0 - 1.0;float n110 = dot(grad110, pf - vec3(1.0, 1.0, 0.0));vec3 grad111 = rnm(vec2(perm11, pi.z + permTexUnit)).rgb * 4.0 - 1.0;float n111 = dot(grad111, pf - vec3(1.0, 1.0, 1.0));vec4 n_x = mix(vec4(n000, n001, n010, n011), vec4(n100, n101, n110, n111), fade(pf.x));vec2 n_xy = mix(n_x.xy, n_x.zw, fade(pf.y));float n_xyz = mix(n_xy.x, n_xy.y, fade(pf.z));return n_xyz;}vec2 coordRot(in vec2 tc, in float angle) {float rotX = ((tc.x * 2.0 - 1.0) * cos(angle)) - ((tc.y * 2.0 - 1.0) * sin(angle));float rotY = ((tc.y * 2.0 - 1.0) * cos(angle)) + ((tc.x * 2.0 - 1.0) * sin(angle));return vec2(rotX * 0.5 + 0.5, rotY * 0.5 + 0.5);}void main() {vec4 result = texture2D(sourceImage, texCoord);const float toolEpsilon = 0.005;float hsLuminance = dot(result.rgb, hsLuminanceWeighting);float shadow = clamp((pow(hsLuminance, 1.0 / (shadows + 1.0)) + (-0.76) * pow(hsLuminance, 2.0 / (shadows + 1.0))) - hsLuminance, 0.0, 1.0);float highlight = clamp((1.0 - (pow(1.0 - hsLuminance, 1.0 / (2.0 - highlights)) + (-0.8) * pow(1.0 - hsLuminance, 2.0 / (2.0 - highlights)))) - hsLuminance, -1.0, 0.0);vec3 shresult = (hsLuminance + shadow + highlight) * (result.rgb / hsLuminance);result = vec4(shresult.rgb, result.a);if (abs(exposure) > toolEpsilon) {float mag = exposure * 1.045;float exppower = 1.0 + abs(mag);if (mag < 0.0) {exppower = 1.0 / exppower;}result.r = 1.0 - pow((1.0 - result.r), exppower);result.g = 1.0 - pow((1.0 - result.g), exppower);result.b = 1.0 - pow((1.0 - result.b), exppower);}result = vec4(((result.rgb - vec3(0.5)) * contrast + vec3(0.5)), result.a);float satLuminance = dot(result.rgb, satLuminanceWeighting);vec3 greyScaleColor = vec3(satLuminance);result = vec4(mix(greyScaleColor, result.rgb, saturation), result.a);if (abs(warmth) > toolEpsilon) {vec3 yuvVec; if (warmth > 0.0 ) {yuvVec = vec3(0.1765, -0.1255, 0.0902);} else {yuvVec = -vec3(0.0588, 0.1569, -0.1255);}vec3 yuvColor = rgbToYuv(result.rgb);float luma = yuvColor.r;float curveScale = sin(luma * 3.14159);yuvColor += 0.375 * warmth * curveScale * yuvVec;result.rgb = yuvToRgb(yuvColor);}if (abs(grain) > toolEpsilon) {vec3 rotOffset = vec3(1.425, 3.892, 5.835);vec2 rotCoordsR = coordRot(texCoord, rotOffset.x);vec3 noise = vec3(pnoise3D(vec3(rotCoordsR * vec2(width / grainsize, height / grainsize),0.0)));vec3 lumcoeff = vec3(0.299,0.587,0.114);float luminance = dot(result.rgb, lumcoeff);float lum = smoothstep(0.2, 0.0, luminance);lum += luminance;noise = mix(noise,vec3(0.0),pow(lum,4.0));result.rgb = result.rgb + noise * grain;}if (abs(vignette) > toolEpsilon) {const float midpoint = 0.7;const float fuzziness = 0.62;float radDist = length(texCoord - 0.5) / sqrt(0.5);float mag = easeInOutSigmoid(radDist * midpoint, fuzziness) * vignette * 0.645;result.rgb = mix(pow(result.rgb, vec3(1.0 / (1.0 - mag))), vec3(0.0), mag * mag);}gl_FragColor = result;}";
        private final int EGL_CONTEXT_CLIENT_VERSION = 12440;
        private final int EGL_OPENGL_ES2_BIT = 4;
        private int blurHeightHandle;
        private int blurInputTexCoordHandle;
        private int blurPositionHandle;
        private int blurShaderProgram;
        private int blurSourceImageHandle;
        private int blurWidthHandle;
        private boolean blured;
        private int contrastHandle;
        private Bitmap currentBitmap;
        private Runnable drawRunnable = new C09431();
        private EGL10 egl10;
        private EGLConfig eglConfig;
        private EGLContext eglContext;
        private EGLDisplay eglDisplay;
        private EGLSurface eglSurface;
        private int enhanceInputImageTexture2Handle;
        private int enhanceInputTexCoordHandle;
        private int enhanceIntensityHandle;
        private int enhancePositionHandle;
        private int enhanceShaderProgram;
        private int enhanceSourceImageHandle;
        private int[] enhanceTextures = new int[2];
        private int exposureHandle;
        private GL gl;
        private int grainHandle;
        private int heightHandle;
        private int highlightsHandle;
        private boolean hsvGenerated;
        private boolean initied;
        private int inputTexCoordHandle;
        private int linearBlurAngleHandle;
        private int linearBlurAspectRatioHandle;
        private int linearBlurExcludeBlurSizeHandle;
        private int linearBlurExcludePointHandle;
        private int linearBlurExcludeSizeHandle;
        private int linearBlurInputTexCoordHandle;
        private int linearBlurPositionHandle;
        private int linearBlurShaderProgram;
        private int linearBlurSourceImage2Handle;
        private int linearBlurSourceImageHandle;
        private boolean needUpdateBlurTexture = true;
        private int positionHandle;
        private int radialBlurAspectRatioHandle;
        private int radialBlurExcludeBlurSizeHandle;
        private int radialBlurExcludePointHandle;
        private int radialBlurExcludeSizeHandle;
        private int radialBlurInputTexCoordHandle;
        private int radialBlurPositionHandle;
        private int radialBlurShaderProgram;
        private int radialBlurSourceImage2Handle;
        private int radialBlurSourceImageHandle;
        private int renderBufferHeight;
        private int renderBufferWidth;
        private int[] renderFrameBuffer = new int[3];
        private int[] renderTexture = new int[3];
        private int rgbToHsvInputTexCoordHandle;
        private int rgbToHsvPositionHandle;
        private int rgbToHsvShaderProgram;
        private int rgbToHsvSourceImageHandle;
        private int saturationHandle;
        private int shadowsHandle;
        private int sharpenHandle;
        private int sharpenHeightHandle;
        private int sharpenInputTexCoordHandle;
        private int sharpenPositionHandle;
        private int sharpenShaderProgram;
        private int sharpenSourceImageHandle;
        private int sharpenWidthHandle;
        private int simpleInputTexCoordHandle;
        private int simplePositionHandle;
        private int simpleShaderProgram;
        private int simpleSourceImageHandle;
        private int sourceImageHandle;
        private volatile int surfaceHeight;
        private SurfaceTexture surfaceTexture;
        private volatile int surfaceWidth;
        private FloatBuffer textureBuffer;
        private int toolsShaderProgram;
        private FloatBuffer vertexBuffer;
        private FloatBuffer vertexInvertBuffer;
        private int vignetteHandle;
        private int warmthHandle;
        private int widthHandle;

        class C09431 implements Runnable {
            C09431() {
            }

            public void run() {
                if (!EGLThread.this.initied) {
                    return;
                }
                if ((EGLThread.this.eglContext.equals(EGLThread.this.egl10.eglGetCurrentContext()) && EGLThread.this.eglSurface.equals(EGLThread.this.egl10.eglGetCurrentSurface(12377))) || EGLThread.this.egl10.eglMakeCurrent(EGLThread.this.eglDisplay, EGLThread.this.eglSurface, EGLThread.this.eglSurface, EGLThread.this.eglContext)) {
                    GLES20.glViewport(0, 0, EGLThread.this.renderBufferWidth, EGLThread.this.renderBufferHeight);
                    EGLThread.this.drawEnhancePass();
                    EGLThread.this.drawSharpenPass();
                    EGLThread.this.drawCustomParamsPass();
                    EGLThread.this.blured = EGLThread.this.drawBlurPass();
                    GLES20.glViewport(0, 0, EGLThread.this.surfaceWidth, EGLThread.this.surfaceHeight);
                    GLES20.glBindFramebuffer(36160, 0);
                    GLES20.glClear(0);
                    GLES20.glUseProgram(EGLThread.this.simpleShaderProgram);
                    GLES20.glActiveTexture(33984);
                    GLES20.glBindTexture(3553, EGLThread.this.renderTexture[EGLThread.this.blured ? 0 : 1]);
                    GLES20.glUniform1i(EGLThread.this.simpleSourceImageHandle, 0);
                    GLES20.glEnableVertexAttribArray(EGLThread.this.simpleInputTexCoordHandle);
                    GLES20.glVertexAttribPointer(EGLThread.this.simpleInputTexCoordHandle, 2, 5126, false, 8, EGLThread.this.textureBuffer);
                    GLES20.glEnableVertexAttribArray(EGLThread.this.simplePositionHandle);
                    GLES20.glVertexAttribPointer(EGLThread.this.simplePositionHandle, 2, 5126, false, 8, EGLThread.this.vertexBuffer);
                    GLES20.glDrawArrays(5, 0, 4);
                    EGLThread.this.egl10.eglSwapBuffers(EGLThread.this.eglDisplay, EGLThread.this.eglSurface);
                    return;
                }
                FileLog.m609e("tmessages", "eglMakeCurrent failed " + GLUtils.getEGLErrorString(EGLThread.this.egl10.eglGetError()));
            }
        }

        class C09453 implements Runnable {
            C09453() {
            }

            public void run() {
                EGLThread.this.finish();
                EGLThread.this.currentBitmap = null;
                Looper looper = Looper.myLooper();
                if (looper != null) {
                    looper.quit();
                }
            }
        }

        public EGLThread(SurfaceTexture surface, Bitmap bitmap) {
            super("EGLThread");
            this.surfaceTexture = surface;
            this.currentBitmap = bitmap;
        }

        private int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shader, 35713, compileStatus, 0);
            if (compileStatus[0] != 0) {
                return shader;
            }
            GLES20.glDeleteShader(shader);
            return 0;
        }

        private boolean initGL() {
            this.egl10 = (EGL10) EGLContext.getEGL();
            this.eglDisplay = this.egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (this.eglDisplay == EGL10.EGL_NO_DISPLAY) {
                FileLog.m609e("tmessages", "eglGetDisplay failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
                finish();
                return false;
            }
            if (this.egl10.eglInitialize(this.eglDisplay, new int[2])) {
                int[] configsCount = new int[1];
                EGLConfig[] configs = new EGLConfig[1];
                if (!this.egl10.eglChooseConfig(this.eglDisplay, new int[]{12352, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12325, 0, 12326, 0, 12344}, configs, 1, configsCount)) {
                    FileLog.m609e("tmessages", "eglChooseConfig failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
                    finish();
                    return false;
                } else if (configsCount[0] > 0) {
                    this.eglConfig = configs[0];
                    this.eglContext = this.egl10.eglCreateContext(this.eglDisplay, this.eglConfig, EGL10.EGL_NO_CONTEXT, new int[]{12440, 2, 12344});
                    if (this.eglContext == null) {
                        FileLog.m609e("tmessages", "eglCreateContext failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
                        finish();
                        return false;
                    } else if (this.surfaceTexture instanceof SurfaceTexture) {
                        this.eglSurface = this.egl10.eglCreateWindowSurface(this.eglDisplay, this.eglConfig, this.surfaceTexture, null);
                        if (this.eglSurface == null || this.eglSurface == EGL10.EGL_NO_SURFACE) {
                            FileLog.m609e("tmessages", "createWindowSurface failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
                            finish();
                            return false;
                        }
                        if (this.egl10.eglMakeCurrent(this.eglDisplay, this.eglSurface, this.eglSurface, this.eglContext)) {
                            this.gl = this.eglContext.getGL();
                            float[] squareCoordinates = new float[]{GroundOverlayOptions.NO_DIMENSION, 1.0f, 1.0f, 1.0f, GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, 1.0f, GroundOverlayOptions.NO_DIMENSION};
                            ByteBuffer bb = ByteBuffer.allocateDirect(squareCoordinates.length * 4);
                            bb.order(ByteOrder.nativeOrder());
                            this.vertexBuffer = bb.asFloatBuffer();
                            this.vertexBuffer.put(squareCoordinates);
                            this.vertexBuffer.position(0);
                            float[] squareCoordinates2 = new float[]{GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, 1.0f, GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, 1.0f, 1.0f, 1.0f};
                            bb = ByteBuffer.allocateDirect(squareCoordinates2.length * 4);
                            bb.order(ByteOrder.nativeOrder());
                            this.vertexInvertBuffer = bb.asFloatBuffer();
                            this.vertexInvertBuffer.put(squareCoordinates2);
                            this.vertexInvertBuffer.position(0);
                            float[] textureCoordinates = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
                            bb = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
                            bb.order(ByteOrder.nativeOrder());
                            this.textureBuffer = bb.asFloatBuffer();
                            this.textureBuffer.put(textureCoordinates);
                            this.textureBuffer.position(0);
                            GLES20.glGenTextures(2, this.enhanceTextures, 0);
                            int vertexShader = loadShader(35633, simpleVertexShaderCode);
                            int fragmentShader = loadShader(35632, toolsFragmentShaderCode);
                            if (vertexShader == 0 || fragmentShader == 0) {
                                finish();
                                return false;
                            }
                            this.toolsShaderProgram = GLES20.glCreateProgram();
                            GLES20.glAttachShader(this.toolsShaderProgram, vertexShader);
                            GLES20.glAttachShader(this.toolsShaderProgram, fragmentShader);
                            GLES20.glBindAttribLocation(this.toolsShaderProgram, 0, "position");
                            GLES20.glBindAttribLocation(this.toolsShaderProgram, 1, "inputTexCoord");
                            GLES20.glLinkProgram(this.toolsShaderProgram);
                            int[] linkStatus = new int[1];
                            GLES20.glGetProgramiv(this.toolsShaderProgram, 35714, linkStatus, 0);
                            if (linkStatus[0] == 0) {
                                GLES20.glDeleteProgram(this.toolsShaderProgram);
                                this.toolsShaderProgram = 0;
                            } else {
                                this.positionHandle = GLES20.glGetAttribLocation(this.toolsShaderProgram, "position");
                                this.inputTexCoordHandle = GLES20.glGetAttribLocation(this.toolsShaderProgram, "inputTexCoord");
                                this.sourceImageHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "sourceImage");
                                this.shadowsHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "shadows");
                                this.highlightsHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "highlights");
                                this.exposureHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "exposure");
                                this.contrastHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "contrast");
                                this.saturationHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "saturation");
                                this.warmthHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "warmth");
                                this.vignetteHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "vignette");
                                this.grainHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "grain");
                                this.widthHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "width");
                                this.heightHandle = GLES20.glGetUniformLocation(this.toolsShaderProgram, "height");
                            }
                            vertexShader = loadShader(35633, sharpenVertexShaderCode);
                            fragmentShader = loadShader(35632, sharpenFragmentShaderCode);
                            if (vertexShader == 0 || fragmentShader == 0) {
                                finish();
                                return false;
                            }
                            this.sharpenShaderProgram = GLES20.glCreateProgram();
                            GLES20.glAttachShader(this.sharpenShaderProgram, vertexShader);
                            GLES20.glAttachShader(this.sharpenShaderProgram, fragmentShader);
                            GLES20.glBindAttribLocation(this.sharpenShaderProgram, 0, "position");
                            GLES20.glBindAttribLocation(this.sharpenShaderProgram, 1, "inputTexCoord");
                            GLES20.glLinkProgram(this.sharpenShaderProgram);
                            linkStatus = new int[1];
                            GLES20.glGetProgramiv(this.sharpenShaderProgram, 35714, linkStatus, 0);
                            if (linkStatus[0] == 0) {
                                GLES20.glDeleteProgram(this.sharpenShaderProgram);
                                this.sharpenShaderProgram = 0;
                            } else {
                                this.sharpenPositionHandle = GLES20.glGetAttribLocation(this.sharpenShaderProgram, "position");
                                this.sharpenInputTexCoordHandle = GLES20.glGetAttribLocation(this.sharpenShaderProgram, "inputTexCoord");
                                this.sharpenSourceImageHandle = GLES20.glGetUniformLocation(this.sharpenShaderProgram, "sourceImage");
                                this.sharpenWidthHandle = GLES20.glGetUniformLocation(this.sharpenShaderProgram, "inputWidth");
                                this.sharpenHeightHandle = GLES20.glGetUniformLocation(this.sharpenShaderProgram, "inputHeight");
                                this.sharpenHandle = GLES20.glGetUniformLocation(this.sharpenShaderProgram, "sharpen");
                            }
                            vertexShader = loadShader(35633, blurVertexShaderCode);
                            fragmentShader = loadShader(35632, blurFragmentShaderCode);
                            if (vertexShader == 0 || fragmentShader == 0) {
                                finish();
                                return false;
                            }
                            this.blurShaderProgram = GLES20.glCreateProgram();
                            GLES20.glAttachShader(this.blurShaderProgram, vertexShader);
                            GLES20.glAttachShader(this.blurShaderProgram, fragmentShader);
                            GLES20.glBindAttribLocation(this.blurShaderProgram, 0, "position");
                            GLES20.glBindAttribLocation(this.blurShaderProgram, 1, "inputTexCoord");
                            GLES20.glLinkProgram(this.blurShaderProgram);
                            linkStatus = new int[1];
                            GLES20.glGetProgramiv(this.blurShaderProgram, 35714, linkStatus, 0);
                            if (linkStatus[0] == 0) {
                                GLES20.glDeleteProgram(this.blurShaderProgram);
                                this.blurShaderProgram = 0;
                            } else {
                                this.blurPositionHandle = GLES20.glGetAttribLocation(this.blurShaderProgram, "position");
                                this.blurInputTexCoordHandle = GLES20.glGetAttribLocation(this.blurShaderProgram, "inputTexCoord");
                                this.blurSourceImageHandle = GLES20.glGetUniformLocation(this.blurShaderProgram, "sourceImage");
                                this.blurWidthHandle = GLES20.glGetUniformLocation(this.blurShaderProgram, "texelWidthOffset");
                                this.blurHeightHandle = GLES20.glGetUniformLocation(this.blurShaderProgram, "texelHeightOffset");
                            }
                            vertexShader = loadShader(35633, simpleVertexShaderCode);
                            fragmentShader = loadShader(35632, linearBlurFragmentShaderCode);
                            if (vertexShader == 0 || fragmentShader == 0) {
                                finish();
                                return false;
                            }
                            this.linearBlurShaderProgram = GLES20.glCreateProgram();
                            GLES20.glAttachShader(this.linearBlurShaderProgram, vertexShader);
                            GLES20.glAttachShader(this.linearBlurShaderProgram, fragmentShader);
                            GLES20.glBindAttribLocation(this.linearBlurShaderProgram, 0, "position");
                            GLES20.glBindAttribLocation(this.linearBlurShaderProgram, 1, "inputTexCoord");
                            GLES20.glLinkProgram(this.linearBlurShaderProgram);
                            linkStatus = new int[1];
                            GLES20.glGetProgramiv(this.linearBlurShaderProgram, 35714, linkStatus, 0);
                            if (linkStatus[0] == 0) {
                                GLES20.glDeleteProgram(this.linearBlurShaderProgram);
                                this.linearBlurShaderProgram = 0;
                            } else {
                                this.linearBlurPositionHandle = GLES20.glGetAttribLocation(this.linearBlurShaderProgram, "position");
                                this.linearBlurInputTexCoordHandle = GLES20.glGetAttribLocation(this.linearBlurShaderProgram, "inputTexCoord");
                                this.linearBlurSourceImageHandle = GLES20.glGetUniformLocation(this.linearBlurShaderProgram, "sourceImage");
                                this.linearBlurSourceImage2Handle = GLES20.glGetUniformLocation(this.linearBlurShaderProgram, "inputImageTexture2");
                                this.linearBlurExcludeSizeHandle = GLES20.glGetUniformLocation(this.linearBlurShaderProgram, "excludeSize");
                                this.linearBlurExcludePointHandle = GLES20.glGetUniformLocation(this.linearBlurShaderProgram, "excludePoint");
                                this.linearBlurExcludeBlurSizeHandle = GLES20.glGetUniformLocation(this.linearBlurShaderProgram, "excludeBlurSize");
                                this.linearBlurAngleHandle = GLES20.glGetUniformLocation(this.linearBlurShaderProgram, "angle");
                                this.linearBlurAspectRatioHandle = GLES20.glGetUniformLocation(this.linearBlurShaderProgram, "aspectRatio");
                            }
                            vertexShader = loadShader(35633, simpleVertexShaderCode);
                            fragmentShader = loadShader(35632, radialBlurFragmentShaderCode);
                            if (vertexShader == 0 || fragmentShader == 0) {
                                finish();
                                return false;
                            }
                            this.radialBlurShaderProgram = GLES20.glCreateProgram();
                            GLES20.glAttachShader(this.radialBlurShaderProgram, vertexShader);
                            GLES20.glAttachShader(this.radialBlurShaderProgram, fragmentShader);
                            GLES20.glBindAttribLocation(this.radialBlurShaderProgram, 0, "position");
                            GLES20.glBindAttribLocation(this.radialBlurShaderProgram, 1, "inputTexCoord");
                            GLES20.glLinkProgram(this.radialBlurShaderProgram);
                            linkStatus = new int[1];
                            GLES20.glGetProgramiv(this.radialBlurShaderProgram, 35714, linkStatus, 0);
                            if (linkStatus[0] == 0) {
                                GLES20.glDeleteProgram(this.radialBlurShaderProgram);
                                this.radialBlurShaderProgram = 0;
                            } else {
                                this.radialBlurPositionHandle = GLES20.glGetAttribLocation(this.radialBlurShaderProgram, "position");
                                this.radialBlurInputTexCoordHandle = GLES20.glGetAttribLocation(this.radialBlurShaderProgram, "inputTexCoord");
                                this.radialBlurSourceImageHandle = GLES20.glGetUniformLocation(this.radialBlurShaderProgram, "sourceImage");
                                this.radialBlurSourceImage2Handle = GLES20.glGetUniformLocation(this.radialBlurShaderProgram, "inputImageTexture2");
                                this.radialBlurExcludeSizeHandle = GLES20.glGetUniformLocation(this.radialBlurShaderProgram, "excludeSize");
                                this.radialBlurExcludePointHandle = GLES20.glGetUniformLocation(this.radialBlurShaderProgram, "excludePoint");
                                this.radialBlurExcludeBlurSizeHandle = GLES20.glGetUniformLocation(this.radialBlurShaderProgram, "excludeBlurSize");
                                this.radialBlurAspectRatioHandle = GLES20.glGetUniformLocation(this.radialBlurShaderProgram, "aspectRatio");
                            }
                            vertexShader = loadShader(35633, simpleVertexShaderCode);
                            fragmentShader = loadShader(35632, rgbToHsvFragmentShaderCode);
                            if (vertexShader == 0 || fragmentShader == 0) {
                                finish();
                                return false;
                            }
                            this.rgbToHsvShaderProgram = GLES20.glCreateProgram();
                            GLES20.glAttachShader(this.rgbToHsvShaderProgram, vertexShader);
                            GLES20.glAttachShader(this.rgbToHsvShaderProgram, fragmentShader);
                            GLES20.glBindAttribLocation(this.rgbToHsvShaderProgram, 0, "position");
                            GLES20.glBindAttribLocation(this.rgbToHsvShaderProgram, 1, "inputTexCoord");
                            GLES20.glLinkProgram(this.rgbToHsvShaderProgram);
                            linkStatus = new int[1];
                            GLES20.glGetProgramiv(this.rgbToHsvShaderProgram, 35714, linkStatus, 0);
                            if (linkStatus[0] == 0) {
                                GLES20.glDeleteProgram(this.rgbToHsvShaderProgram);
                                this.rgbToHsvShaderProgram = 0;
                            } else {
                                this.rgbToHsvPositionHandle = GLES20.glGetAttribLocation(this.rgbToHsvShaderProgram, "position");
                                this.rgbToHsvInputTexCoordHandle = GLES20.glGetAttribLocation(this.rgbToHsvShaderProgram, "inputTexCoord");
                                this.rgbToHsvSourceImageHandle = GLES20.glGetUniformLocation(this.rgbToHsvShaderProgram, "sourceImage");
                            }
                            vertexShader = loadShader(35633, simpleVertexShaderCode);
                            fragmentShader = loadShader(35632, enhanceFragmentShaderCode);
                            if (vertexShader == 0 || fragmentShader == 0) {
                                finish();
                                return false;
                            }
                            this.enhanceShaderProgram = GLES20.glCreateProgram();
                            GLES20.glAttachShader(this.enhanceShaderProgram, vertexShader);
                            GLES20.glAttachShader(this.enhanceShaderProgram, fragmentShader);
                            GLES20.glBindAttribLocation(this.enhanceShaderProgram, 0, "position");
                            GLES20.glBindAttribLocation(this.enhanceShaderProgram, 1, "inputTexCoord");
                            GLES20.glLinkProgram(this.enhanceShaderProgram);
                            linkStatus = new int[1];
                            GLES20.glGetProgramiv(this.enhanceShaderProgram, 35714, linkStatus, 0);
                            if (linkStatus[0] == 0) {
                                GLES20.glDeleteProgram(this.enhanceShaderProgram);
                                this.enhanceShaderProgram = 0;
                            } else {
                                this.enhancePositionHandle = GLES20.glGetAttribLocation(this.enhanceShaderProgram, "position");
                                this.enhanceInputTexCoordHandle = GLES20.glGetAttribLocation(this.enhanceShaderProgram, "inputTexCoord");
                                this.enhanceSourceImageHandle = GLES20.glGetUniformLocation(this.enhanceShaderProgram, "sourceImage");
                                this.enhanceIntensityHandle = GLES20.glGetUniformLocation(this.enhanceShaderProgram, "intensity");
                                this.enhanceInputImageTexture2Handle = GLES20.glGetUniformLocation(this.enhanceShaderProgram, "inputImageTexture2");
                            }
                            vertexShader = loadShader(35633, simpleVertexShaderCode);
                            fragmentShader = loadShader(35632, simpleFragmentShaderCode);
                            if (vertexShader == 0 || fragmentShader == 0) {
                                finish();
                                return false;
                            }
                            this.simpleShaderProgram = GLES20.glCreateProgram();
                            GLES20.glAttachShader(this.simpleShaderProgram, vertexShader);
                            GLES20.glAttachShader(this.simpleShaderProgram, fragmentShader);
                            GLES20.glBindAttribLocation(this.simpleShaderProgram, 0, "position");
                            GLES20.glBindAttribLocation(this.simpleShaderProgram, 1, "inputTexCoord");
                            GLES20.glLinkProgram(this.simpleShaderProgram);
                            linkStatus = new int[1];
                            GLES20.glGetProgramiv(this.simpleShaderProgram, 35714, linkStatus, 0);
                            if (linkStatus[0] == 0) {
                                GLES20.glDeleteProgram(this.simpleShaderProgram);
                                this.simpleShaderProgram = 0;
                            } else {
                                this.simplePositionHandle = GLES20.glGetAttribLocation(this.simpleShaderProgram, "position");
                                this.simpleInputTexCoordHandle = GLES20.glGetAttribLocation(this.simpleShaderProgram, "inputTexCoord");
                                this.simpleSourceImageHandle = GLES20.glGetUniformLocation(this.simpleShaderProgram, "sourceImage");
                            }
                            if (this.currentBitmap != null) {
                                loadTexture(this.currentBitmap);
                            }
                            return true;
                        }
                        FileLog.m609e("tmessages", "eglMakeCurrent failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
                        finish();
                        return false;
                    } else {
                        finish();
                        return false;
                    }
                } else {
                    FileLog.m609e("tmessages", "eglConfig not initialized");
                    finish();
                    return false;
                }
            }
            FileLog.m609e("tmessages", "eglInitialize failed " + GLUtils.getEGLErrorString(this.egl10.eglGetError()));
            finish();
            return false;
        }

        public void finish() {
            if (this.eglSurface != null) {
                this.egl10.eglMakeCurrent(this.eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                this.egl10.eglDestroySurface(this.eglDisplay, this.eglSurface);
                this.eglSurface = null;
            }
            if (this.eglContext != null) {
                this.egl10.eglDestroyContext(this.eglDisplay, this.eglContext);
                this.eglContext = null;
            }
            if (this.eglDisplay != null) {
                this.egl10.eglTerminate(this.eglDisplay);
                this.eglDisplay = null;
            }
        }

        private void drawEnhancePass() {
            if (!this.hsvGenerated) {
                GLES20.glBindFramebuffer(36160, this.renderFrameBuffer[0]);
                GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.renderTexture[0], 0);
                GLES20.glClear(0);
                GLES20.glUseProgram(this.rgbToHsvShaderProgram);
                GLES20.glActiveTexture(33984);
                GLES20.glBindTexture(3553, this.renderTexture[1]);
                GLES20.glUniform1i(this.rgbToHsvSourceImageHandle, 0);
                GLES20.glEnableVertexAttribArray(this.rgbToHsvInputTexCoordHandle);
                GLES20.glVertexAttribPointer(this.rgbToHsvInputTexCoordHandle, 2, 5126, false, 8, this.textureBuffer);
                GLES20.glEnableVertexAttribArray(this.rgbToHsvPositionHandle);
                GLES20.glVertexAttribPointer(this.rgbToHsvPositionHandle, 2, 5126, false, 8, this.vertexBuffer);
                GLES20.glDrawArrays(5, 0, 4);
                Buffer hsvBuffer = ByteBuffer.allocateDirect((this.renderBufferWidth * this.renderBufferHeight) * 4);
                GLES20.glReadPixels(0, 0, this.renderBufferWidth, this.renderBufferHeight, 6408, 5121, hsvBuffer);
                GLES20.glBindTexture(3553, this.enhanceTextures[0]);
                GLES20.glTexParameteri(3553, 10241, 9729);
                GLES20.glTexParameteri(3553, 10240, 9729);
                GLES20.glTexParameteri(3553, 10242, 33071);
                GLES20.glTexParameteri(3553, 10243, 33071);
                GLES20.glTexImage2D(3553, 0, 6408, this.renderBufferWidth, this.renderBufferHeight, 0, 6408, 5121, hsvBuffer);
                ByteBuffer byteBuffer = null;
                try {
                    byteBuffer = ByteBuffer.allocateDirect(16384);
                    Utilities.calcCDT(hsvBuffer, this.renderBufferWidth, this.renderBufferHeight, byteBuffer);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                GLES20.glBindTexture(3553, this.enhanceTextures[1]);
                GLES20.glTexParameteri(3553, 10241, 9729);
                GLES20.glTexParameteri(3553, 10240, 9729);
                GLES20.glTexParameteri(3553, 10242, 33071);
                GLES20.glTexParameteri(3553, 10243, 33071);
                GLES20.glTexImage2D(3553, 0, 6408, 256, 16, 0, 6408, 5121, byteBuffer);
                this.hsvGenerated = true;
            }
            GLES20.glBindFramebuffer(36160, this.renderFrameBuffer[1]);
            GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.renderTexture[1], 0);
            GLES20.glClear(0);
            GLES20.glUseProgram(this.enhanceShaderProgram);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, this.enhanceTextures[0]);
            GLES20.glUniform1i(this.enhanceSourceImageHandle, 0);
            GLES20.glActiveTexture(33985);
            GLES20.glBindTexture(3553, this.enhanceTextures[1]);
            GLES20.glUniform1i(this.enhanceInputImageTexture2Handle, 1);
            if (PhotoFilterView.this.showOriginal) {
                GLES20.glUniform1f(this.enhanceIntensityHandle, 0.0f);
            } else {
                GLES20.glUniform1f(this.enhanceIntensityHandle, PhotoFilterView.this.getEnhanceValue());
            }
            GLES20.glEnableVertexAttribArray(this.enhanceInputTexCoordHandle);
            GLES20.glVertexAttribPointer(this.enhanceInputTexCoordHandle, 2, 5126, false, 8, this.textureBuffer);
            GLES20.glEnableVertexAttribArray(this.enhancePositionHandle);
            GLES20.glVertexAttribPointer(this.enhancePositionHandle, 2, 5126, false, 8, this.vertexBuffer);
            GLES20.glDrawArrays(5, 0, 4);
        }

        private void drawSharpenPass() {
            GLES20.glBindFramebuffer(36160, this.renderFrameBuffer[0]);
            GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.renderTexture[0], 0);
            GLES20.glClear(0);
            GLES20.glUseProgram(this.sharpenShaderProgram);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, this.renderTexture[1]);
            GLES20.glUniform1i(this.sharpenSourceImageHandle, 0);
            if (PhotoFilterView.this.showOriginal) {
                GLES20.glUniform1f(this.sharpenHandle, 0.0f);
            } else {
                GLES20.glUniform1f(this.sharpenHandle, PhotoFilterView.this.getSharpenValue());
            }
            GLES20.glUniform1f(this.sharpenWidthHandle, (float) this.renderBufferWidth);
            GLES20.glUniform1f(this.sharpenHeightHandle, (float) this.renderBufferHeight);
            GLES20.glEnableVertexAttribArray(this.sharpenInputTexCoordHandle);
            GLES20.glVertexAttribPointer(this.sharpenInputTexCoordHandle, 2, 5126, false, 8, this.textureBuffer);
            GLES20.glEnableVertexAttribArray(this.sharpenPositionHandle);
            GLES20.glVertexAttribPointer(this.sharpenPositionHandle, 2, 5126, false, 8, this.vertexInvertBuffer);
            GLES20.glDrawArrays(5, 0, 4);
        }

        private void drawCustomParamsPass() {
            GLES20.glBindFramebuffer(36160, this.renderFrameBuffer[1]);
            GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.renderTexture[1], 0);
            GLES20.glClear(0);
            GLES20.glUseProgram(this.toolsShaderProgram);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, this.renderTexture[0]);
            GLES20.glUniform1i(this.sourceImageHandle, 0);
            if (PhotoFilterView.this.showOriginal) {
                GLES20.glUniform1f(this.shadowsHandle, 0.0f);
                GLES20.glUniform1f(this.highlightsHandle, 1.0f);
                GLES20.glUniform1f(this.exposureHandle, 0.0f);
                GLES20.glUniform1f(this.contrastHandle, 1.0f);
                GLES20.glUniform1f(this.saturationHandle, 1.0f);
                GLES20.glUniform1f(this.warmthHandle, 0.0f);
                GLES20.glUniform1f(this.vignetteHandle, 0.0f);
                GLES20.glUniform1f(this.grainHandle, 0.0f);
            } else {
                GLES20.glUniform1f(this.shadowsHandle, PhotoFilterView.this.getShadowsValue());
                GLES20.glUniform1f(this.highlightsHandle, PhotoFilterView.this.getHighlightsValue());
                GLES20.glUniform1f(this.exposureHandle, PhotoFilterView.this.getExposureValue());
                GLES20.glUniform1f(this.contrastHandle, PhotoFilterView.this.getContrastValue());
                GLES20.glUniform1f(this.saturationHandle, PhotoFilterView.this.getSaturationValue());
                GLES20.glUniform1f(this.warmthHandle, PhotoFilterView.this.getWarmthValue());
                GLES20.glUniform1f(this.vignetteHandle, PhotoFilterView.this.getVignetteValue());
                GLES20.glUniform1f(this.grainHandle, PhotoFilterView.this.getGrainValue());
            }
            GLES20.glUniform1f(this.widthHandle, (float) this.renderBufferWidth);
            GLES20.glUniform1f(this.heightHandle, (float) this.renderBufferHeight);
            GLES20.glEnableVertexAttribArray(this.inputTexCoordHandle);
            GLES20.glVertexAttribPointer(this.inputTexCoordHandle, 2, 5126, false, 8, this.textureBuffer);
            GLES20.glEnableVertexAttribArray(this.positionHandle);
            GLES20.glVertexAttribPointer(this.positionHandle, 2, 5126, false, 8, this.vertexInvertBuffer);
            GLES20.glDrawArrays(5, 0, 4);
        }

        private boolean drawBlurPass() {
            if (PhotoFilterView.this.showOriginal || PhotoFilterView.this.blurType == 0) {
                return false;
            }
            if (this.needUpdateBlurTexture) {
                GLES20.glUseProgram(this.blurShaderProgram);
                GLES20.glUniform1i(this.blurSourceImageHandle, 0);
                GLES20.glEnableVertexAttribArray(this.blurInputTexCoordHandle);
                GLES20.glVertexAttribPointer(this.blurInputTexCoordHandle, 2, 5126, false, 8, this.textureBuffer);
                GLES20.glEnableVertexAttribArray(this.blurPositionHandle);
                GLES20.glVertexAttribPointer(this.blurPositionHandle, 2, 5126, false, 8, this.vertexInvertBuffer);
                GLES20.glBindFramebuffer(36160, this.renderFrameBuffer[0]);
                GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.renderTexture[0], 0);
                GLES20.glClear(0);
                GLES20.glActiveTexture(33984);
                GLES20.glBindTexture(3553, this.renderTexture[1]);
                GLES20.glUniform1f(this.blurWidthHandle, 0.0f);
                GLES20.glUniform1f(this.blurHeightHandle, 1.0f / ((float) this.renderBufferHeight));
                GLES20.glDrawArrays(5, 0, 4);
                GLES20.glBindFramebuffer(36160, this.renderFrameBuffer[2]);
                GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.renderTexture[2], 0);
                GLES20.glClear(0);
                GLES20.glActiveTexture(33984);
                GLES20.glBindTexture(3553, this.renderTexture[0]);
                GLES20.glUniform1f(this.blurWidthHandle, 1.0f / ((float) this.renderBufferWidth));
                GLES20.glUniform1f(this.blurHeightHandle, 0.0f);
                GLES20.glDrawArrays(5, 0, 4);
                this.needUpdateBlurTexture = false;
            }
            GLES20.glBindFramebuffer(36160, this.renderFrameBuffer[0]);
            GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.renderTexture[0], 0);
            GLES20.glClear(0);
            if (PhotoFilterView.this.blurType == 1) {
                GLES20.glUseProgram(this.radialBlurShaderProgram);
                GLES20.glUniform1i(this.radialBlurSourceImageHandle, 0);
                GLES20.glUniform1i(this.radialBlurSourceImage2Handle, 1);
                GLES20.glUniform1f(this.radialBlurExcludeSizeHandle, PhotoFilterView.this.blurExcludeSize);
                GLES20.glUniform1f(this.radialBlurExcludeBlurSizeHandle, PhotoFilterView.this.blurExcludeBlurSize);
                GLES20.glUniform2f(this.radialBlurExcludePointHandle, PhotoFilterView.this.blurExcludePoint.f58x, PhotoFilterView.this.blurExcludePoint.f59y);
                GLES20.glUniform1f(this.radialBlurAspectRatioHandle, ((float) this.renderBufferHeight) / ((float) this.renderBufferWidth));
                GLES20.glEnableVertexAttribArray(this.radialBlurInputTexCoordHandle);
                GLES20.glVertexAttribPointer(this.radialBlurInputTexCoordHandle, 2, 5126, false, 8, this.textureBuffer);
                GLES20.glEnableVertexAttribArray(this.radialBlurPositionHandle);
                GLES20.glVertexAttribPointer(this.radialBlurPositionHandle, 2, 5126, false, 8, this.vertexInvertBuffer);
            } else if (PhotoFilterView.this.blurType == 2) {
                GLES20.glUseProgram(this.linearBlurShaderProgram);
                GLES20.glUniform1i(this.linearBlurSourceImageHandle, 0);
                GLES20.glUniform1i(this.linearBlurSourceImage2Handle, 1);
                GLES20.glUniform1f(this.linearBlurExcludeSizeHandle, PhotoFilterView.this.blurExcludeSize);
                GLES20.glUniform1f(this.linearBlurExcludeBlurSizeHandle, PhotoFilterView.this.blurExcludeBlurSize);
                GLES20.glUniform1f(this.linearBlurAngleHandle, PhotoFilterView.this.blurAngle);
                GLES20.glUniform2f(this.linearBlurExcludePointHandle, PhotoFilterView.this.blurExcludePoint.f58x, PhotoFilterView.this.blurExcludePoint.f59y);
                GLES20.glUniform1f(this.linearBlurAspectRatioHandle, ((float) this.renderBufferHeight) / ((float) this.renderBufferWidth));
                GLES20.glEnableVertexAttribArray(this.linearBlurInputTexCoordHandle);
                GLES20.glVertexAttribPointer(this.linearBlurInputTexCoordHandle, 2, 5126, false, 8, this.textureBuffer);
                GLES20.glEnableVertexAttribArray(this.linearBlurPositionHandle);
                GLES20.glVertexAttribPointer(this.linearBlurPositionHandle, 2, 5126, false, 8, this.vertexInvertBuffer);
            }
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(3553, this.renderTexture[1]);
            GLES20.glActiveTexture(33985);
            GLES20.glBindTexture(3553, this.renderTexture[2]);
            GLES20.glDrawArrays(5, 0, 4);
            return true;
        }

        private Bitmap getRenderBufferBitmap() {
            ByteBuffer buffer = ByteBuffer.allocateDirect((this.renderBufferWidth * this.renderBufferHeight) * 4);
            GLES20.glReadPixels(0, 0, this.renderBufferWidth, this.renderBufferHeight, 6408, 5121, buffer);
            Bitmap bitmap = Bitmap.createBitmap(this.renderBufferWidth, this.renderBufferHeight, Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            return bitmap;
        }

        public Bitmap getTexture() {
            if (!this.initied) {
                return null;
            }
            final Semaphore semaphore = new Semaphore(0);
            final Bitmap[] object = new Bitmap[1];
            try {
                postRunnable(new Runnable() {
                    public void run() {
                        int i = 1;
                        GLES20.glBindFramebuffer(36160, EGLThread.this.renderFrameBuffer[1]);
                        int[] access$3100 = EGLThread.this.renderTexture;
                        if (EGLThread.this.blured) {
                            i = 0;
                        }
                        GLES20.glFramebufferTexture2D(36160, 36064, 3553, access$3100[i], 0);
                        GLES20.glClear(0);
                        object[0] = EGLThread.this.getRenderBufferBitmap();
                        semaphore.release();
                        GLES20.glBindFramebuffer(36160, 0);
                        GLES20.glClear(0);
                    }
                });
                semaphore.acquire();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            return object[0];
        }

        private Bitmap createBitmap(Bitmap bitmap, int w, int h, float scale) {
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postRotate((float) PhotoFilterView.this.orientation);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        private void loadTexture(Bitmap bitmap) {
            this.renderBufferWidth = bitmap.getWidth();
            this.renderBufferHeight = bitmap.getHeight();
            float maxSize = (float) AndroidUtilities.getPhotoSize();
            if (((float) this.renderBufferWidth) > maxSize || ((float) this.renderBufferHeight) > maxSize || PhotoFilterView.this.orientation != 0) {
                float scale = 1.0f;
                if (((float) this.renderBufferWidth) > maxSize || ((float) this.renderBufferHeight) > maxSize) {
                    float scaleX = maxSize / ((float) bitmap.getWidth());
                    float scaleY = maxSize / ((float) bitmap.getHeight());
                    if (scaleX < scaleY) {
                        this.renderBufferWidth = (int) maxSize;
                        this.renderBufferHeight = (int) (((float) bitmap.getHeight()) * scaleX);
                        scale = scaleX;
                    } else {
                        this.renderBufferHeight = (int) maxSize;
                        this.renderBufferWidth = (int) (((float) bitmap.getWidth()) * scaleY);
                        scale = scaleY;
                    }
                }
                if (PhotoFilterView.this.orientation == 90 || PhotoFilterView.this.orientation == 270) {
                    int temp = this.renderBufferWidth;
                    this.renderBufferWidth = this.renderBufferHeight;
                    this.renderBufferHeight = temp;
                }
                this.currentBitmap = createBitmap(bitmap, this.renderBufferWidth, this.renderBufferHeight, scale);
            }
            GLES20.glGenFramebuffers(3, this.renderFrameBuffer, 0);
            GLES20.glGenTextures(3, this.renderTexture, 0);
            GLES20.glBindTexture(3553, this.renderTexture[0]);
            GLES20.glTexParameteri(3553, 10241, 9729);
            GLES20.glTexParameteri(3553, 10240, 9729);
            GLES20.glTexParameteri(3553, 10242, 33071);
            GLES20.glTexParameteri(3553, 10243, 33071);
            GLES20.glTexImage2D(3553, 0, 6408, this.renderBufferWidth, this.renderBufferHeight, 0, 6408, 5121, null);
            GLES20.glBindTexture(3553, this.renderTexture[1]);
            GLES20.glTexParameteri(3553, 10241, 9729);
            GLES20.glTexParameteri(3553, 10240, 9729);
            GLES20.glTexParameteri(3553, 10242, 33071);
            GLES20.glTexParameteri(3553, 10243, 33071);
            GLUtils.texImage2D(3553, 0, this.currentBitmap, 0);
            GLES20.glBindTexture(3553, this.renderTexture[2]);
            GLES20.glTexParameteri(3553, 10241, 9729);
            GLES20.glTexParameteri(3553, 10240, 9729);
            GLES20.glTexParameteri(3553, 10242, 33071);
            GLES20.glTexParameteri(3553, 10243, 33071);
            GLES20.glTexImage2D(3553, 0, 6408, this.renderBufferWidth, this.renderBufferHeight, 0, 6408, 5121, null);
        }

        public void shutdown() {
            postRunnable(new C09453());
        }

        public void setSurfaceTextureSize(int width, int height) {
            this.surfaceWidth = width;
            this.surfaceHeight = height;
        }

        public void run() {
            this.initied = initGL();
            super.run();
        }

        public void requestRender(final boolean updateBlur) {
            postRunnable(new Runnable() {
                public void run() {
                    if (!EGLThread.this.needUpdateBlurTexture) {
                        EGLThread.this.needUpdateBlurTexture = updateBlur;
                    }
                    EGLThread.this.cancelRunnable(EGLThread.this.drawRunnable);
                    EGLThread.this.postRunnable(EGLThread.this.drawRunnable);
                }
            });
        }
    }

    public class ToolsAdapter extends Adapter {
        private Context mContext;

        private class Holder extends ViewHolder {
            public Holder(View itemView) {
                super(itemView);
            }
        }

        public ToolsAdapter(Context context) {
            this.mContext = context;
        }

        public int getItemCount() {
            return 11;
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new Holder(new PhotoEditToolCell(this.mContext));
        }

        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            Holder holder = (Holder) viewHolder;
            if (i == PhotoFilterView.this.enhanceTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_enhance, LocaleController.getString("Enhance", C0553R.string.Enhance), PhotoFilterView.this.enhanceValue);
            } else if (i == PhotoFilterView.this.highlightsTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_highlights, LocaleController.getString("Highlights", C0553R.string.Highlights), PhotoFilterView.this.highlightsValue);
            } else if (i == PhotoFilterView.this.contrastTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_contrast, LocaleController.getString("Contrast", C0553R.string.Contrast), PhotoFilterView.this.contrastValue);
            } else if (i == PhotoFilterView.this.exposureTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_brightness, LocaleController.getString("Exposure", C0553R.string.Exposure), PhotoFilterView.this.exposureValue);
            } else if (i == PhotoFilterView.this.warmthTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_warmth, LocaleController.getString("Warmth", C0553R.string.Warmth), PhotoFilterView.this.warmthValue);
            } else if (i == PhotoFilterView.this.saturationTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_saturation, LocaleController.getString("Saturation", C0553R.string.Saturation), PhotoFilterView.this.saturationValue);
            } else if (i == PhotoFilterView.this.vignetteTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_vignette, LocaleController.getString("Vignette", C0553R.string.Vignette), PhotoFilterView.this.vignetteValue);
            } else if (i == PhotoFilterView.this.shadowsTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_shadows, LocaleController.getString("Shadows", C0553R.string.Shadows), PhotoFilterView.this.shadowsValue);
            } else if (i == PhotoFilterView.this.grainTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_grain, LocaleController.getString("Grain", C0553R.string.Grain), PhotoFilterView.this.grainValue);
            } else if (i == PhotoFilterView.this.sharpenTool) {
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_details, LocaleController.getString("Sharpen", C0553R.string.Sharpen), PhotoFilterView.this.sharpenValue);
            } else if (i == PhotoFilterView.this.blurTool) {
                String value = "";
                if (PhotoFilterView.this.blurType == 1) {
                    value = "R";
                } else if (PhotoFilterView.this.blurType == 2) {
                    value = "L";
                }
                ((PhotoEditToolCell) holder.itemView).setIconAndTextAndValue((int) C0553R.drawable.tool_blur, LocaleController.getString("Blur", C0553R.string.Blur), value);
            }
        }
    }

    public PhotoFilterView(Context context, Bitmap bitmap, int rotation) {
        LayoutParams layoutParams;
        FrameLayout frameLayout;
        super(context);
        this.bitmapToEdit = bitmap;
        this.orientation = rotation;
        this.textureView = new TextureView(context);
        LinearLayoutManager layoutManager;
        if (VERSION.SDK_INT == 14 || VERSION.SDK_INT == 15) {
            addView(this.textureView);
            this.textureView.setVisibility(4);
            layoutParams = (LayoutParams) this.textureView.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = -1;
            layoutParams.gravity = 51;
            this.textureView.setLayoutParams(layoutParams);
            this.textureView.setSurfaceTextureListener(new C09371());
            this.blurControl = new PhotoFilterBlurControl(context);
            this.blurControl.setVisibility(4);
            addView(this.blurControl);
            layoutParams = (LayoutParams) this.blurControl.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = -1;
            layoutParams.gravity = 51;
            this.blurControl.setLayoutParams(layoutParams);
            this.blurControl.setDelegate(new C15692());
            this.toolsView = new FrameLayout(context);
            addView(this.toolsView);
            layoutParams = (LayoutParams) this.toolsView.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = AndroidUtilities.dp(126.0f);
            layoutParams.gravity = 83;
            this.toolsView.setLayoutParams(layoutParams);
            frameLayout = new FrameLayout(context);
            frameLayout.setBackgroundColor(-15066598);
            this.toolsView.addView(frameLayout);
            layoutParams = (LayoutParams) frameLayout.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = AndroidUtilities.dp(48.0f);
            layoutParams.gravity = 83;
            frameLayout.setLayoutParams(layoutParams);
            this.cancelTextView = new TextView(context);
            this.cancelTextView.setTextSize(1, 14.0f);
            this.cancelTextView.setTextColor(-1);
            this.cancelTextView.setGravity(17);
            this.cancelTextView.setBackgroundResource(C0553R.drawable.bar_selector_picker);
            this.cancelTextView.setPadding(AndroidUtilities.dp(29.0f), 0, AndroidUtilities.dp(29.0f), 0);
            this.cancelTextView.setText(LocaleController.getString("Cancel", C0553R.string.Cancel).toUpperCase());
            this.cancelTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            frameLayout.addView(this.cancelTextView);
            layoutParams = (LayoutParams) this.cancelTextView.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -1;
            layoutParams.gravity = 51;
            this.cancelTextView.setLayoutParams(layoutParams);
            this.doneTextView = new TextView(context);
            this.doneTextView.setTextSize(1, 14.0f);
            this.doneTextView.setTextColor(-11420173);
            this.doneTextView.setGravity(17);
            this.doneTextView.setBackgroundResource(C0553R.drawable.bar_selector_picker);
            this.doneTextView.setPadding(AndroidUtilities.dp(29.0f), 0, AndroidUtilities.dp(29.0f), 0);
            this.doneTextView.setText(LocaleController.getString("Done", C0553R.string.Done).toUpperCase());
            this.doneTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            frameLayout.addView(this.doneTextView);
            layoutParams = (LayoutParams) this.doneTextView.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -1;
            layoutParams.gravity = 53;
            this.doneTextView.setLayoutParams(layoutParams);
            this.recyclerListView = new RecyclerListView(context);
            layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(0);
            this.recyclerListView.setLayoutManager(layoutManager);
            this.recyclerListView.setClipToPadding(false);
        } else {
            addView(this.textureView);
            this.textureView.setVisibility(4);
            layoutParams = (LayoutParams) this.textureView.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = -1;
            layoutParams.gravity = 51;
            this.textureView.setLayoutParams(layoutParams);
            this.textureView.setSurfaceTextureListener(new C09371());
            this.blurControl = new PhotoFilterBlurControl(context);
            this.blurControl.setVisibility(4);
            addView(this.blurControl);
            layoutParams = (LayoutParams) this.blurControl.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = -1;
            layoutParams.gravity = 51;
            this.blurControl.setLayoutParams(layoutParams);
            this.blurControl.setDelegate(new C15692());
            this.toolsView = new FrameLayout(context);
            addView(this.toolsView);
            layoutParams = (LayoutParams) this.toolsView.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = AndroidUtilities.dp(126.0f);
            layoutParams.gravity = 83;
            this.toolsView.setLayoutParams(layoutParams);
            frameLayout = new FrameLayout(context);
            frameLayout.setBackgroundColor(-15066598);
            this.toolsView.addView(frameLayout);
            layoutParams = (LayoutParams) frameLayout.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = AndroidUtilities.dp(48.0f);
            layoutParams.gravity = 83;
            frameLayout.setLayoutParams(layoutParams);
            this.cancelTextView = new TextView(context);
            this.cancelTextView.setTextSize(1, 14.0f);
            this.cancelTextView.setTextColor(-1);
            this.cancelTextView.setGravity(17);
            this.cancelTextView.setBackgroundResource(C0553R.drawable.bar_selector_picker);
            this.cancelTextView.setPadding(AndroidUtilities.dp(29.0f), 0, AndroidUtilities.dp(29.0f), 0);
            this.cancelTextView.setText(LocaleController.getString("Cancel", C0553R.string.Cancel).toUpperCase());
            this.cancelTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            frameLayout.addView(this.cancelTextView);
            layoutParams = (LayoutParams) this.cancelTextView.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -1;
            layoutParams.gravity = 51;
            this.cancelTextView.setLayoutParams(layoutParams);
            this.doneTextView = new TextView(context);
            this.doneTextView.setTextSize(1, 14.0f);
            this.doneTextView.setTextColor(-11420173);
            this.doneTextView.setGravity(17);
            this.doneTextView.setBackgroundResource(C0553R.drawable.bar_selector_picker);
            this.doneTextView.setPadding(AndroidUtilities.dp(29.0f), 0, AndroidUtilities.dp(29.0f), 0);
            this.doneTextView.setText(LocaleController.getString("Done", C0553R.string.Done).toUpperCase());
            this.doneTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            frameLayout.addView(this.doneTextView);
            layoutParams = (LayoutParams) this.doneTextView.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -1;
            layoutParams.gravity = 53;
            this.doneTextView.setLayoutParams(layoutParams);
            this.recyclerListView = new RecyclerListView(context);
            layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(0);
            this.recyclerListView.setLayoutManager(layoutManager);
            this.recyclerListView.setClipToPadding(false);
        }
        if (VERSION.SDK_INT >= 9) {
            this.recyclerListView.setOverScrollMode(2);
        }
        RecyclerListView recyclerListView = this.recyclerListView;
        Adapter toolsAdapter = new ToolsAdapter(context);
        this.toolsAdapter = toolsAdapter;
        recyclerListView.setAdapter(toolsAdapter);
        this.toolsView.addView(this.recyclerListView);
        layoutParams = (LayoutParams) this.recyclerListView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
        layoutParams.gravity = 51;
        this.recyclerListView.setLayoutParams(layoutParams);
        this.recyclerListView.setOnItemClickListener(new C15703());
        this.editView = new FrameLayout(context);
        this.editView.setVisibility(8);
        addView(this.editView);
        layoutParams = (LayoutParams) this.editView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = AndroidUtilities.dp(126.0f);
        layoutParams.gravity = 83;
        this.editView.setLayoutParams(layoutParams);
        frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(-15066598);
        this.editView.addView(frameLayout);
        layoutParams = (LayoutParams) frameLayout.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = AndroidUtilities.dp(48.0f);
        layoutParams.gravity = 83;
        frameLayout.setLayoutParams(layoutParams);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(C0553R.drawable.edit_cancel);
        imageView.setBackgroundResource(C0553R.drawable.bar_selector_picker);
        imageView.setPadding(AndroidUtilities.dp(22.0f), 0, AndroidUtilities.dp(22.0f), 0);
        frameLayout.addView(imageView);
        layoutParams = (LayoutParams) imageView.getLayoutParams();
        layoutParams.width = -2;
        layoutParams.height = -1;
        layoutParams.gravity = 51;
        imageView.setLayoutParams(layoutParams);
        imageView.setOnClickListener(new C09384());
        imageView = new ImageView(context);
        imageView.setImageResource(C0553R.drawable.edit_doneblue);
        imageView.setBackgroundResource(C0553R.drawable.bar_selector_picker);
        imageView.setPadding(AndroidUtilities.dp(22.0f), AndroidUtilities.dp(1.0f), AndroidUtilities.dp(22.0f), 0);
        frameLayout.addView(imageView);
        layoutParams = (LayoutParams) imageView.getLayoutParams();
        layoutParams.width = -2;
        layoutParams.height = -1;
        layoutParams.gravity = 53;
        imageView.setLayoutParams(layoutParams);
        imageView.setOnClickListener(new C09395());
        this.blurTextView = new TextView(context);
        this.blurTextView.setTextSize(1, 20.0f);
        this.blurTextView.setTextColor(-1);
        this.blurTextView.setText(LocaleController.getString("Blur", C0553R.string.Blur));
        frameLayout.addView(this.blurTextView);
        layoutParams = (LayoutParams) this.blurTextView.getLayoutParams();
        layoutParams.width = -2;
        layoutParams.height = -2;
        layoutParams.gravity = 1;
        layoutParams.topMargin = AndroidUtilities.dp(9.0f);
        this.blurTextView.setLayoutParams(layoutParams);
        this.paramTextView = new TextView(context);
        this.paramTextView.setTextSize(1, 12.0f);
        this.paramTextView.setTextColor(-8355712);
        frameLayout.addView(this.paramTextView);
        layoutParams = (LayoutParams) this.paramTextView.getLayoutParams();
        layoutParams.width = -2;
        layoutParams.height = -2;
        layoutParams.gravity = 1;
        layoutParams.topMargin = AndroidUtilities.dp(26.0f);
        this.paramTextView.setLayoutParams(layoutParams);
        this.valueTextView = new TextView(context);
        this.valueTextView.setTextSize(1, 20.0f);
        this.valueTextView.setTextColor(-1);
        frameLayout.addView(this.valueTextView);
        layoutParams = (LayoutParams) this.valueTextView.getLayoutParams();
        layoutParams.width = -2;
        layoutParams.height = -2;
        layoutParams.gravity = 1;
        layoutParams.topMargin = AndroidUtilities.dp(3.0f);
        this.valueTextView.setLayoutParams(layoutParams);
        this.valueSeekBar = new PhotoEditorSeekBar(context);
        this.valueSeekBar.setDelegate(new C15716());
        this.editView.addView(this.valueSeekBar);
        layoutParams = (LayoutParams) this.valueSeekBar.getLayoutParams();
        layoutParams.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
        layoutParams.leftMargin = AndroidUtilities.dp(14.0f);
        layoutParams.rightMargin = AndroidUtilities.dp(14.0f);
        layoutParams.topMargin = AndroidUtilities.dp(10.0f);
        if (AndroidUtilities.isTablet()) {
            layoutParams.width = AndroidUtilities.dp(498.0f);
            layoutParams.gravity = 49;
        } else {
            layoutParams.width = -1;
            layoutParams.gravity = 51;
        }
        this.valueSeekBar.setLayoutParams(layoutParams);
        this.blurLayout = new FrameLayout(context);
        this.editView.addView(this.blurLayout);
        layoutParams = (LayoutParams) this.blurLayout.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(280.0f);
        layoutParams.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
        layoutParams.topMargin = AndroidUtilities.dp(10.0f);
        layoutParams.gravity = 1;
        this.blurLayout.setLayoutParams(layoutParams);
        this.blurOffButton = new TextView(context);
        this.blurOffButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_off_active, 0, 0);
        this.blurOffButton.setTextSize(1, 13.0f);
        this.blurOffButton.setTextColor(-11420173);
        this.blurOffButton.setGravity(1);
        this.blurOffButton.setText(LocaleController.getString("BlurOff", C0553R.string.BlurOff));
        this.blurLayout.addView(this.blurOffButton);
        layoutParams = (LayoutParams) this.blurOffButton.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(80.0f);
        layoutParams.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
        this.blurOffButton.setLayoutParams(layoutParams);
        this.blurOffButton.setOnClickListener(new C09407());
        this.blurRadialButton = new TextView(context);
        this.blurRadialButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_radial, 0, 0);
        this.blurRadialButton.setTextSize(1, 13.0f);
        this.blurRadialButton.setTextColor(-1);
        this.blurRadialButton.setGravity(1);
        this.blurRadialButton.setText(LocaleController.getString("BlurRadial", C0553R.string.BlurRadial));
        this.blurLayout.addView(this.blurRadialButton);
        layoutParams = (LayoutParams) this.blurRadialButton.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(80.0f);
        layoutParams.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
        layoutParams.leftMargin = AndroidUtilities.dp(100.0f);
        this.blurRadialButton.setLayoutParams(layoutParams);
        this.blurRadialButton.setOnClickListener(new C09418());
        this.blurLinearButton = new TextView(context);
        this.blurLinearButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_linear, 0, 0);
        this.blurLinearButton.setTextSize(1, 13.0f);
        this.blurLinearButton.setTextColor(-1);
        this.blurLinearButton.setGravity(1);
        this.blurLinearButton.setText(LocaleController.getString("BlurLinear", C0553R.string.BlurLinear));
        this.blurLayout.addView(this.blurLinearButton);
        layoutParams = (LayoutParams) this.blurLinearButton.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(80.0f);
        layoutParams.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
        layoutParams.leftMargin = AndroidUtilities.dp(200.0f);
        this.blurLinearButton.setLayoutParams(layoutParams);
        this.blurLinearButton.setOnClickListener(new C09429());
    }

    private void updateSelectedBlurType() {
        if (this.blurType == 0) {
            this.blurOffButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_off_active, 0, 0);
            this.blurOffButton.setTextColor(-11420173);
            this.blurRadialButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_radial, 0, 0);
            this.blurRadialButton.setTextColor(-1);
            this.blurLinearButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_linear, 0, 0);
            this.blurLinearButton.setTextColor(-1);
        } else if (this.blurType == 1) {
            this.blurOffButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_off, 0, 0);
            this.blurOffButton.setTextColor(-1);
            this.blurRadialButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_radial_active, 0, 0);
            this.blurRadialButton.setTextColor(-11420173);
            this.blurLinearButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_linear, 0, 0);
            this.blurLinearButton.setTextColor(-1);
        } else if (this.blurType == 2) {
            this.blurOffButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_off, 0, 0);
            this.blurOffButton.setTextColor(-1);
            this.blurRadialButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_radial, 0, 0);
            this.blurRadialButton.setTextColor(-1);
            this.blurLinearButton.setCompoundDrawablesWithIntrinsicBounds(0, C0553R.drawable.blur_linear_active, 0, 0);
            this.blurLinearButton.setTextColor(-11420173);
        }
    }

    private void updateValueTextView() {
        int value = 0;
        if (this.selectedTool == this.enhanceTool) {
            value = (int) this.enhanceValue;
        } else if (this.selectedTool == this.highlightsTool) {
            value = (int) this.highlightsValue;
        } else if (this.selectedTool == this.contrastTool) {
            value = (int) this.contrastValue;
        } else if (this.selectedTool == this.exposureTool) {
            value = (int) this.exposureValue;
        } else if (this.selectedTool == this.warmthTool) {
            value = (int) this.warmthValue;
        } else if (this.selectedTool == this.saturationTool) {
            value = (int) this.saturationValue;
        } else if (this.selectedTool == this.vignetteTool) {
            value = (int) this.vignetteValue;
        } else if (this.selectedTool == this.shadowsTool) {
            value = (int) this.shadowsValue;
        } else if (this.selectedTool == this.grainTool) {
            value = (int) this.grainValue;
        } else if (this.selectedTool == this.sharpenTool) {
            value = (int) this.sharpenValue;
        }
        if (value > 0) {
            this.valueTextView.setText("+" + value);
        } else {
            this.valueTextView.setText("" + value);
        }
    }

    public boolean hasChanges() {
        return (this.enhanceValue == 0.0f && this.contrastValue == 0.0f && this.highlightsValue == 0.0f && this.exposureValue == 0.0f && this.warmthValue == 0.0f && this.saturationValue == 0.0f && this.vignetteValue == 0.0f && this.shadowsValue == 0.0f && this.grainValue == 0.0f && this.sharpenValue == 0.0f) ? false : true;
    }

    public void onTouch(MotionEvent event) {
        if (event.getActionMasked() == 0 || event.getActionMasked() == 5) {
            LayoutParams layoutParams = (LayoutParams) this.textureView.getLayoutParams();
            if (layoutParams != null && event.getX() >= ((float) layoutParams.leftMargin) && event.getY() >= ((float) layoutParams.topMargin) && event.getX() <= ((float) (layoutParams.leftMargin + layoutParams.width)) && event.getY() <= ((float) (layoutParams.topMargin + layoutParams.height))) {
                setShowOriginal(true);
            }
        } else if (event.getActionMasked() == 1 || event.getActionMasked() == 6) {
            setShowOriginal(false);
        }
    }

    private void setShowOriginal(boolean value) {
        if (this.showOriginal != value) {
            this.showOriginal = value;
            if (this.eglThread != null) {
                this.eglThread.requestRender(false);
            }
        }
    }

    public void switchToOrFromEditMode() {
        View viewFrom;
        View viewTo;
        if (this.editView.getVisibility() == 8) {
            viewFrom = this.toolsView;
            viewTo = this.editView;
            if (this.selectedTool == this.blurTool) {
                this.blurLayout.setVisibility(0);
                this.valueSeekBar.setVisibility(4);
                this.blurTextView.setVisibility(0);
                this.paramTextView.setVisibility(4);
                this.valueTextView.setVisibility(4);
                if (this.blurType != 0) {
                    this.blurControl.setVisibility(0);
                }
                updateSelectedBlurType();
            } else {
                this.blurLayout.setVisibility(4);
                this.valueSeekBar.setVisibility(0);
                this.blurTextView.setVisibility(4);
                this.paramTextView.setVisibility(0);
                this.valueTextView.setVisibility(0);
                this.blurControl.setVisibility(4);
            }
        } else {
            this.selectedTool = -1;
            viewFrom = this.editView;
            viewTo = this.toolsView;
            this.blurControl.setVisibility(4);
        }
        AnimatorSetProxy animatorSet = new AnimatorSetProxy();
        Object[] objArr = new Object[1];
        objArr[0] = ObjectAnimatorProxy.ofFloat(viewFrom, "translationY", 0.0f, (float) AndroidUtilities.dp(126.0f));
        animatorSet.playTogether(objArr);
        animatorSet.addListener(new AnimatorListenerAdapterProxy() {

            class C15681 extends AnimatorListenerAdapterProxy {
                C15681() {
                }

                public void onAnimationEnd(Object animation) {
                    viewTo.clearAnimation();
                    if (PhotoFilterView.this.selectedTool == PhotoFilterView.this.enhanceTool) {
                        PhotoFilterView.this.checkEnhance();
                    }
                }
            }

            public void onAnimationEnd(Object animation) {
                viewFrom.clearAnimation();
                viewFrom.setVisibility(8);
                viewTo.setVisibility(0);
                ViewProxy.setTranslationY(viewTo, (float) AndroidUtilities.dp(126.0f));
                AnimatorSetProxy animatorSet = new AnimatorSetProxy();
                Object[] objArr = new Object[1];
                objArr[0] = ObjectAnimatorProxy.ofFloat(viewTo, "translationY", 0.0f);
                animatorSet.playTogether(objArr);
                animatorSet.addListener(new C15681());
                animatorSet.setDuration(200);
                animatorSet.start();
            }
        });
        animatorSet.setDuration(200);
        animatorSet.start();
    }

    public void shutdown() {
        if (this.eglThread != null) {
            this.eglThread.shutdown();
            this.eglThread = null;
        }
        this.textureView.setVisibility(8);
    }

    public void init() {
        this.textureView.setVisibility(0);
    }

    public Bitmap getBitmap() {
        return this.eglThread != null ? this.eglThread.getTexture() : null;
    }

    private void fixLayout(int viewWidth, int viewHeight) {
        if (this.bitmapToEdit != null) {
            float bitmapW;
            float bitmapH;
            viewWidth -= AndroidUtilities.dp(28.0f);
            viewHeight -= AndroidUtilities.dp(154.0f);
            if (this.orientation == 90 || this.orientation == 270) {
                bitmapW = (float) this.bitmapToEdit.getHeight();
                bitmapH = (float) this.bitmapToEdit.getWidth();
            } else {
                bitmapW = (float) this.bitmapToEdit.getWidth();
                bitmapH = (float) this.bitmapToEdit.getHeight();
            }
            float scaleX = ((float) viewWidth) / bitmapW;
            float scaleY = ((float) viewHeight) / bitmapH;
            if (scaleX > scaleY) {
                bitmapH = (float) viewHeight;
                bitmapW = (float) ((int) Math.ceil((double) (bitmapW * scaleY)));
            } else {
                bitmapW = (float) viewWidth;
                bitmapH = (float) ((int) Math.ceil((double) (bitmapH * scaleX)));
            }
            int bitmapY = (int) Math.ceil((double) (((((float) viewHeight) - bitmapH) / 2.0f) + ((float) AndroidUtilities.dp(14.0f))));
            LayoutParams layoutParams = (LayoutParams) this.textureView.getLayoutParams();
            layoutParams.leftMargin = (int) Math.ceil((double) (((((float) viewWidth) - bitmapW) / 2.0f) + ((float) AndroidUtilities.dp(14.0f))));
            layoutParams.topMargin = bitmapY;
            layoutParams.width = (int) bitmapW;
            layoutParams.height = (int) bitmapH;
            this.textureView.setLayoutParams(layoutParams);
            this.blurControl.setActualAreaSize((float) layoutParams.width, (float) layoutParams.height);
            layoutParams = (LayoutParams) this.blurControl.getLayoutParams();
            layoutParams.height = AndroidUtilities.dp(28.0f) + viewHeight;
            this.blurControl.setLayoutParams(layoutParams);
            if (AndroidUtilities.isTablet()) {
                int total = AndroidUtilities.dp(86.0f) * 10;
                layoutParams = (LayoutParams) this.recyclerListView.getLayoutParams();
                if (total < viewWidth) {
                    layoutParams.width = total;
                    layoutParams.leftMargin = (viewWidth - total) / 2;
                } else {
                    layoutParams.width = -1;
                    layoutParams.leftMargin = 0;
                }
                this.recyclerListView.setLayoutParams(layoutParams);
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        fixLayout(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float getShadowsValue() {
        return (this.shadowsValue / 100.0f) * 0.65f;
    }

    private float getHighlightsValue() {
        return 1.0f - (this.highlightsValue / 100.0f);
    }

    private float getEnhanceValue() {
        return this.enhanceValue / 100.0f;
    }

    private float getExposureValue() {
        return this.exposureValue / 100.0f;
    }

    private float getContrastValue() {
        return ((this.contrastValue / 100.0f) * 0.3f) + 1.0f;
    }

    private float getWarmthValue() {
        return this.warmthValue / 100.0f;
    }

    private float getVignetteValue() {
        return this.vignetteValue / 100.0f;
    }

    private float getSharpenValue() {
        return 0.11f + ((this.sharpenValue / 100.0f) * 0.6f);
    }

    private float getGrainValue() {
        return (this.grainValue / 100.0f) * 0.04f;
    }

    private float getSaturationValue() {
        float parameterValue = this.saturationValue / 100.0f;
        if (parameterValue > 0.0f) {
            parameterValue *= 1.05f;
        }
        return 1.0f + parameterValue;
    }

    public FrameLayout getToolsView() {
        return this.toolsView;
    }

    public FrameLayout getEditView() {
        return this.editView;
    }

    public TextView getDoneTextView() {
        return this.doneTextView;
    }

    public TextView getCancelTextView() {
        return this.cancelTextView;
    }

    public void setEditViewFirst() {
        this.selectedTool = 0;
        this.previousValue = this.enhanceValue;
        this.enhanceValue = 50.0f;
        this.valueSeekBar.setMinMax(0, 100);
        this.paramTextView.setText(LocaleController.getString("Enhance", C0553R.string.Enhance));
        this.editView.setVisibility(0);
        this.toolsView.setVisibility(8);
        this.valueSeekBar.setProgress(50, false);
        updateValueTextView();
    }

    private void checkEnhance() {
        if (this.enhanceValue == 0.0f) {
            AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
            animatorSetProxy.setDuration(200);
            Object[] objArr = new Object[1];
            objArr[0] = ObjectAnimatorProxy.ofInt(this.valueSeekBar, "progress", 50);
            animatorSetProxy.playTogether(objArr);
            animatorSetProxy.start();
        }
    }
}
