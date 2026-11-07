package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import java.lang.reflect.Method;

/** Blurs only a separate backdrop RenderNode; foreground text never enters that node. */
final class GlassRenderer {
    private static Class<?> renderNodeClass, effectClass;
    private static Method position, begin, end, setEffect, drawNode, makeBlur, clipBounds;
    private static boolean initialized;
    private final Object renderNode;
    private final Paint tint = new Paint(3);
    private final Path clip = new Path();
    private final RectF shape = new RectF();
    private float oldRadius=-1;

    GlassRenderer() throws Exception {
        if (!initialized) {
            renderNodeClass=Class.forName("android.graphics.RenderNode");
            effectClass=Class.forName("android.graphics.RenderEffect");
            position=renderNodeClass.getMethod("setPosition",int.class,int.class,int.class,int.class);
            begin=renderNodeClass.getMethod("beginRecording",int.class,int.class);
            end=renderNodeClass.getMethod("endRecording");
            setEffect=renderNodeClass.getMethod("setRenderEffect",effectClass);
            clipBounds=renderNodeClass.getMethod("setClipToBounds",boolean.class);
            drawNode=Canvas.class.getMethod("drawRenderNode",renderNodeClass);
            makeBlur=effectClass.getMethod("createBlurEffect",float.class,float.class,Shader.TileMode.class);
            initialized=true;
        }
        renderNode=renderNodeClass.getConstructor(String.class).newInstance("ChatGPTFlowGlassBackdrop");
        clipBounds.invoke(renderNode,true);
    }

    boolean draw(Canvas canvas, RectF rect, float rx, float ry, float nodeX, float nodeY,
                 FlowRenderer backdrop, float viewportWidth, float viewportHeight, int base,
                 int sourceColor, int speed, int strength, long frameTime, float density,
                 int blurDp, int opacity, boolean selected) throws Exception {
        return draw(canvas,rect,rx,ry,nodeX,nodeY,backdrop,viewportWidth,viewportHeight,base,sourceColor,speed,strength,
                frameTime,density,blurDp,opacity,selected,null);
    }
    boolean draw(Canvas canvas, RectF rect, float rx, float ry, float nodeX, float nodeY,
                 FlowRenderer backdrop, float viewportWidth, float viewportHeight, int base,
                 int sourceColor, int speed, int strength, long frameTime, float density,
                 int blurDp, int opacity, boolean selected, Path outline) throws Exception {
        if (!canvas.isHardwareAccelerated() || rect.isEmpty() || backdrop==null) return false;
        float radius=Math.max(0,blurDp*density);
        int padding=(int)Math.ceil(radius*2+2*density);
        int w=Math.max(1,(int)Math.ceil(rect.width())+padding*2);
        int h=Math.max(1,(int)Math.ceil(rect.height())+padding*2);
        position.invoke(renderNode,0,0,w,h);
        if (radius!=oldRadius) {
            Object effect=radius>0 ? makeBlur.invoke(null,radius,radius,Shader.TileMode.CLAMP) : null;
            setEffect.invoke(renderNode,effect); oldRadius=radius;
        }
        Canvas recording=(Canvas)begin.invoke(renderNode,w,h);
        try {
            recording.translate(padding-rect.left-nodeX,padding-rect.top-nodeY);
            backdrop.drawRegion(recording,nodeX+rect.left-padding,nodeY+rect.top-padding,
                    nodeX+rect.right+padding,nodeY+rect.bottom+padding,
                    viewportWidth,viewportHeight,base,base,true,speed,strength,frameTime);
        } finally { end.invoke(renderNode); }

        boolean dark=Palette.isDark(base);
        int r=(sourceColor>>>16)&255,g=(sourceColor>>>8)&255,b=sourceColor&255;
        boolean chromatic=Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b))>65;
        int alpha;
        int tintColor;
        if (chromatic) {
            tintColor=sourceColor&0x00ffffff;
            alpha=dark?205:220;
        } else {
            tintColor=dark?0x001c2030:0x00ffffff;
            float f=Palette.clamp(opacity,0,100)/100f;
            alpha=Math.round((dark?.32f:.62f)*f*255);
            if (selected) {
                alpha=dark?240:248;
                tintColor=dark?0x004b3a70:0x00dccaff;
            }
        }
        shape.set(rect); clip.reset();
        if(outline==null)clip.addRoundRect(shape,rx,ry,Path.Direction.CW);else clip.set(outline);
        int save=canvas.save();
        try {
            canvas.clipPath(clip);
            canvas.save();
            canvas.translate(rect.left-padding,rect.top-padding);
            drawNode.invoke(canvas,renderNode);
            canvas.restore();
            tint.setColor((alpha<<24)|tintColor);
            if(outline==null)canvas.drawRoundRect(shape,rx,ry,tint);else canvas.drawPath(clip,tint);
        } finally { canvas.restoreToCount(save); }
        return true;
    }
}
