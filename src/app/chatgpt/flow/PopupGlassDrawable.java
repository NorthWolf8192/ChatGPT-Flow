package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import java.lang.ref.WeakReference;

/** Native PopupWindow backing, including menus without a Compose background fill. */
final class PopupGlassDrawable extends Drawable {
    private final GlassHooks hooks;
    private final WeakReference<View> content;
    private final Drawable original;
    private Drawable rootFallback;
    private GlassRenderer renderer;
    PopupGlassDrawable(GlassHooks hooks,View content,Drawable original){
        this.hooks=hooks;this.content=new WeakReference<>(content);this.original=original;
    }
    @Override public void draw(Canvas canvas){
        View view=content.get();boolean drawn=false;
        try{
            Rect r=getBounds();float density=hooks.density();
            if(view!=null && r.width()>=80*density && r.height()>=28*density && hooks.menuReady()){
                if(renderer==null)renderer=new GlassRenderer();
                drawn=hooks.drawPopup(renderer,view,canvas,new RectF(r.left,r.top,r.right,r.bottom));
            }
        }catch(Throwable error){hooks.popupError(error);}
        if(!drawn){
            if(original!=null){original.setBounds(getBounds());original.draw(canvas);}
            if(rootFallback!=null){rootFallback.setBounds(getBounds());rootFallback.draw(canvas);}
        }
    }
    void rootFallback(Drawable drawable){rootFallback=drawable;}
    void reset(){renderer=null;invalidateSelf();}
    @Override public boolean getPadding(Rect padding){return original!=null && original.getPadding(padding);}
    @Override public int getIntrinsicWidth(){return original==null?-1:original.getIntrinsicWidth();}
    @Override public int getIntrinsicHeight(){return original==null?-1:original.getIntrinsicHeight();}
    @Override public void setAlpha(int alpha){}
    @Override public void setColorFilter(ColorFilter filter){}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
