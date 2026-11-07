package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/** Optional backing surface for the area covered by a translucent input method. */
final class ImeWindowCompat {
    private WeakReference<Window> window = new WeakReference<>(null);
    private Drawable original;
    private int originalAdjust;
    private boolean active;
    private final Backdrop backdrop = new Backdrop();
    private static Method rootInsets, imeVisible;

    static boolean isImeVisible(View decor) {
        try {
            if (rootInsets == null) rootInsets = View.class.getMethod("getRootWindowInsets");
            Object insets = rootInsets.invoke(decor);
            if (insets == null) return false;
            if (imeVisible == null) imeVisible = insets.getClass().getMethod("isVisible", int.class);
            return Boolean.TRUE.equals(imeVisible.invoke(insets, 8)); // WindowInsets.Type.ime()
        } catch (ReflectiveOperationException ignored) { return false; }
    }

    boolean isActive() { return active; }

    void update(Window target, boolean use, int base, int speed, int strength) {
        if (window.get() != target) restore();
        if (!use || target == null) { restore(); return; }
        backdrop.base = base; backdrop.speed = speed; backdrop.strength = strength;
        if (!active) {
            window = new WeakReference<>(target);
            original = target.getDecorView().getBackground();
            originalAdjust = target.getAttributes().softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST;
            active = true;
            target.setBackgroundDrawable(backdrop);
        }
        int current = target.getAttributes().softInputMode;
        int desired = (current & ~WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        if (desired != current) target.setSoftInputMode(desired);
        backdrop.invalidateSelf();
    }

    void redraw(int base, int speed, int strength) {
        if (!active) return;
        backdrop.base = base; backdrop.speed = speed; backdrop.strength = strength;
        backdrop.invalidateSelf();
    }

    void restore() {
        Window w = window.get();
        if (active && w != null) {
            if (w.getDecorView().getBackground() == backdrop) w.setBackgroundDrawable(original);
            int current = w.getAttributes().softInputMode;
            if ((current & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
                    == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) {
                w.setSoftInputMode((current & ~WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) | originalAdjust);
            }
        }
        active = false; original = null; window.clear();
    }

    private static final class Backdrop extends Drawable {
        final FlowRenderer renderer = new FlowRenderer();
        int base = 0xff212121, speed = 55, strength = 70;
        @Override public void draw(Canvas canvas) {
            Rect b = getBounds();
            int saved = canvas.save();
            canvas.translate(b.left, b.top);
            renderer.draw(canvas, b.width(), b.height(), base, true, speed, strength);
            canvas.restoreToCount(saved);
        }
        @Override public void setAlpha(int alpha) {}
        @Override public void setColorFilter(ColorFilter filter) {}
        @Override public int getOpacity() { return PixelFormat.OPAQUE; }
    }
}
