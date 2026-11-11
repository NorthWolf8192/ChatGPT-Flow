package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Shader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** A shared, downsampled copy of the backdrop for all surfaces in a viewport. */
final class BackdropBlur {
    private static final int DOWNSAMPLE = 4;
    private final Object node;
    private boolean dirty = true;
    private float appliedRadius = -1;

    BackdropBlur() throws ReflectiveOperationException {
        node = Api.CONSTRUCTOR.newInstance("ChatGPTFlowBackdropBlur");
    }

    void invalidate() {
        dirty = true;
    }

    void draw(Canvas canvas, FlowRenderer scene, float width, float height, float radius, RectF bounds)
            throws ReflectiveOperationException {
        float scaledRadius = radius / DOWNSAMPLE;
        if (scaledRadius != appliedRadius) {
            Object effect = Api.CREATE_BLUR.invoke(null, scaledRadius, scaledRadius, Shader.TileMode.CLAMP);
            Api.SET_EFFECT.invoke(node, effect);
            appliedRadius = scaledRadius;
            dirty = true;
        }
        if (dirty) {
            // Include the neighboring pixels needed by the kernel, including at screen edges.
            int padding = Math.max(1, (int) Math.ceil(3 * scaledRadius));
            int left = -padding;
            int top = -padding;
            int right = (int) Math.ceil(width / DOWNSAMPLE) + padding;
            int bottom = (int) Math.ceil(height / DOWNSAMPLE) + padding;
            Api.POSITION.invoke(node, left, top, right, bottom);
            Canvas recording = (Canvas) Api.BEGIN.invoke(node, right - left, bottom - top);
            try {
                recording.translate(-left, -top);
                recording.scale(1f / DOWNSAMPLE, 1f / DOWNSAMPLE);
                scene.drawScene(recording, left * DOWNSAMPLE, top * DOWNSAMPLE,
                        right * DOWNSAMPLE, bottom * DOWNSAMPLE);
            } finally {
                Api.END.invoke(node);
            }
            dirty = false;
        }

        // Popup windows can extend outside the padded chat viewport.
        if (bounds.left < -3 * radius || bounds.top < -3 * radius
                || bounds.right > width + 3 * radius || bounds.bottom > height + 3 * radius) {
            scene.drawScene(canvas, bounds.left, bounds.top, bounds.right, bounds.bottom);
        }
        int save = canvas.save();
        try {
            canvas.scale(DOWNSAMPLE, DOWNSAMPLE);
            Api.DRAW.invoke(canvas, node);
        } finally {
            canvas.restoreToCount(save);
        }
    }

    // These public Android APIs are newer than the build's Android stub.
    private static final class Api {
        static final Constructor<?> CONSTRUCTOR;
        static final Method POSITION, BEGIN, END, DRAW, CREATE_BLUR, SET_EFFECT;
        static {
            try {
                Class<?> renderNode = Class.forName("android.graphics.RenderNode");
                Class<?> effect = Class.forName("android.graphics.RenderEffect");
                CONSTRUCTOR = renderNode.getConstructor(String.class);
                POSITION = renderNode.getMethod("setPosition", int.class, int.class, int.class, int.class);
                BEGIN = renderNode.getMethod("beginRecording", int.class, int.class);
                END = renderNode.getMethod("endRecording");
                DRAW = Canvas.class.getMethod("drawRenderNode", renderNode);
                CREATE_BLUR = effect.getMethod("createBlurEffect", float.class, float.class, Shader.TileMode.class);
                SET_EFFECT = renderNode.getMethod("setRenderEffect", effect);
            } catch (ReflectiveOperationException error) {
                throw new ExceptionInInitializerError(error);
            }
        }
    }
}
