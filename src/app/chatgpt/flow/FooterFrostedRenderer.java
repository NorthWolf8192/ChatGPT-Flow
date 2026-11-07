package app.chatgpt.flow;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;

/** Background-only glass with a feathered alpha mask; no original black gradient is drawn. */
final class FooterFrostedRenderer {
    private final GlassRenderer glass=new GlassRenderer();
    private final Paint mask=new Paint(3);
    private final RectF bounds=new RectF();
    private float lastStart=Float.NaN,lastEnd=Float.NaN;
    private int lastFirst,lastLast;
    FooterFrostedRenderer()throws Exception{
        mask.setColor(0xffffffff);
        mask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }
    boolean draw(Canvas canvas,FooterFrostedGeometry geometry,float width,float x,float y,
                 FlowRenderer backdrop,float viewportWidth,float viewportHeight,int base,
                 int speed,int strength,long time,float density,int blur,int tint)throws Exception{
        if(geometry==null || geometry.alpha<=0 || width<=0 || !canvas.isHardwareAccelerated() || backdrop==null)return false;
        bounds.left=0;bounds.top=geometry.top;bounds.right=width;bounds.bottom=geometry.bottom;
        int solid=(Math.round(geometry.alpha*255)<<24)|0xffffff;
        int first=geometry.fadesIn?0x00ffffff:solid,last=geometry.fadesIn?solid:0x00ffffff;
        if(lastStart!=geometry.maskStart || lastEnd!=geometry.maskEnd || first!=lastFirst || last!=lastLast){
            mask.setShader(new LinearGradient(0,geometry.maskStart,0,geometry.maskEnd,
                    new int[]{first,last},null,Shader.TileMode.CLAMP));
            lastStart=geometry.maskStart;lastEnd=geometry.maskEnd;lastFirst=first;lastLast=last;
        }
        int save=canvas.saveLayer(bounds,null,31);
        try{
            boolean drawn=glass.draw(canvas,bounds,0,0,x,y,backdrop,viewportWidth,viewportHeight,base,
                    Palette.isDark(base)?0xff212121:0xffffffff,speed,strength,time,density,blur,tint,false);
            if(drawn)canvas.drawRect(bounds,mask);
            return drawn;
        }finally{canvas.restoreToCount(save);}
    }
}
