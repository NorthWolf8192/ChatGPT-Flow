package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.SystemClock;

public final class FlowRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private final long start = SystemClock.uptimeMillis();
    private LinearGradient gradient;
    private float width, height;
    private int base, strength = -1;

    public void draw(Canvas canvas, float w, float h, int background, boolean enabled,
                     int speed, int intensity) {
        draw(canvas, w, h, background, background, enabled, speed, intensity);
    }

    public void draw(Canvas canvas, float w, float h, int background, int original, boolean enabled,
                     int speed, int intensity) {
        drawAt(canvas,w,h,background,original,enabled,speed,intensity,SystemClock.uptimeMillis());
    }

    public void drawAt(Canvas canvas, float w, float h, int background, int original, boolean enabled,
                       int speed, int intensity, long time) {
        drawRegion(canvas,0,0,w,h,w,h,background,original,enabled,speed,intensity,time);
    }

    public void drawRegion(Canvas canvas, float left,float top,float right,float bottom,
                           float w,float h,int background,int original,boolean enabled,
                           int speed,int intensity,long time) {
        if (w <= 0 || h <= 0) return;
        if (!enabled || intensity == 0) {
            paint.setShader(null);
            paint.setColor(original);
            canvas.drawRect(left, top, right, bottom, paint);
            return;
        }
        if (gradient == null || width != w || height != h || base != background || strength != intensity) {
            width = w; height = h; base = background; strength = intensity;
            gradient = new LinearGradient(0, 0, w * 1.6f, h * .8f,
                    Palette.colors(background, intensity), null, Shader.TileMode.REPEAT);
        }
        float phase = Palette.phase(time, start, speed);
        matrix.setTranslate(-phase * w * 1.6f, -phase * h * .8f);
        gradient.setLocalMatrix(matrix);
        paint.setColor(0xffffffff);
        paint.setShader(gradient);
        canvas.drawRect(left, top, right, bottom, paint);
    }
}
