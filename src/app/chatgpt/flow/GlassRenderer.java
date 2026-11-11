package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** Clips the shared backdrop and applies a tint, with a separate layer for edge masks. */
final class GlassRenderer {
    private static Constructor<?> edgeConstructor;
    private static Method position, begin, end, drawNode, clipBounds;
    private Object fadeNode;
    private final Paint tint = new Paint(3);
    private final Path path = new Path();
    private final RectF shape = new RectF();

    private Object createFadeNode() throws Exception {
        if (edgeConstructor==null) {
            Class<?> renderNodeClass=Class.forName("android.graphics.RenderNode");
            position=renderNodeClass.getMethod("setPosition",int.class,int.class,int.class,int.class);
            begin=renderNodeClass.getMethod("beginRecording",int.class,int.class);
            end=renderNodeClass.getMethod("endRecording");
            clipBounds=renderNodeClass.getMethod("setClipToBounds",boolean.class);
            drawNode=Canvas.class.getMethod("drawRenderNode",renderNodeClass);
            edgeConstructor=renderNodeClass.getConstructor(String.class);
        }
        Object node=edgeConstructor.newInstance("ChatGPTFlowEdgeFade");
        clipBounds.invoke(node,true);
        return node;
    }

    boolean drawFaded(Canvas canvas,RectF bounds,Paint mask,float nodeX,float nodeY,
                      FlowRenderer backdrop,float viewportWidth,float viewportHeight,int base,
                      int speed,int strength,long frameTime,float density,int blurDp,int opacity)throws Exception{
        if(!canvas.isHardwareAccelerated() || bounds.isEmpty() || backdrop==null)return false;
        if(fadeNode==null)fadeNode=createFadeNode();
        int left=(int)Math.floor(bounds.left),top=(int)Math.floor(bounds.top);
        int right=(int)Math.ceil(bounds.right),bottom=(int)Math.ceil(bounds.bottom);
        position.invoke(fadeNode,left,top,right,bottom);
        Canvas recording=(Canvas)begin.invoke(fadeNode,right-left,bottom-top);
        try{
            recording.translate(-left,-top);
            // Mask only the glass. Compose's canvas can already contain sibling content.
            int save=recording.saveLayer(bounds,null,31);
            try{
                if(!draw(recording,bounds,0,0,nodeX,nodeY,backdrop,viewportWidth,viewportHeight,base,
                        Palette.isDark(base)?0xff212121:0xffffffff,speed,strength,frameTime,density,blurDp,opacity,false))return false;
                recording.drawRect(bounds,mask);
            }finally{recording.restoreToCount(save);}
        }finally{end.invoke(fadeNode);}
        drawNode.invoke(canvas,fadeNode);
        return true;
    }

    boolean draw(Canvas canvas, RectF rect, float rx, float ry, float nodeX, float nodeY,
                 FlowRenderer backdrop, float viewportWidth, float viewportHeight, int base,
                 int sourceColor, int speed, int strength, long frameTime, float density,
                 int blurDp, int opacity, boolean selected) {
        return draw(canvas,rect,rx,ry,nodeX,nodeY,backdrop,viewportWidth,viewportHeight,base,sourceColor,speed,strength,
                frameTime,density,blurDp,opacity,selected,null);
    }
    boolean draw(Canvas canvas, RectF rect, float rx, float ry, float nodeX, float nodeY,
                 FlowRenderer backdrop, float viewportWidth, float viewportHeight, int base,
                 int sourceColor, int speed, int strength, long frameTime, float density,
                 int blurDp, int opacity, boolean selected, Path outline) {
        if (!canvas.isHardwareAccelerated() || rect.isEmpty() || backdrop==null
                || viewportWidth<=0 || viewportHeight<=0) return false;
        float radius=Math.max(0,blurDp*density);
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
        // Use backdrop coordinates so glass follows the same light as the surrounding scene.
        shape.set(rect);
        shape.offset(nodeX,nodeY);
        boolean rounded=outline!=null || rx>0 || ry>0;
        if(outline!=null){
            outline.offset(nodeX,nodeY,path);
        }else if(rounded){
            path.rewind();
            path.addRoundRect(shape,rx,ry,Path.Direction.CW);
        }
        int save=canvas.save();
        try {
            canvas.translate(-nodeX,-nodeY);
            if(rounded)canvas.clipPath(path);
            else canvas.clipRect(shape);
            backdrop.drawGlass(canvas,shape,viewportWidth,viewportHeight,base,strength,frameTime,speed,radius);
            if(alpha>0){
                tint.setColor((alpha<<24)|tintColor);
                canvas.drawRect(shape,tint);
            }
        } finally { canvas.restoreToCount(save); }
        return true;
    }
}
