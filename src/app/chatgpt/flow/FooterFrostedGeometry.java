package app.chatgpt.flow;

/** Bounds and fade ramp of the existing scrolling footer effect, in local pixels. */
public final class FooterFrostedGeometry {
    public final float top,bottom,maskStart,maskEnd,alpha;
    public final boolean fadesIn;
    private FooterFrostedGeometry(float top,float bottom,float start,float end,float alpha,boolean fadesIn){
        this.top=top;this.bottom=bottom;maskStart=start;maskEnd=end;this.alpha=alpha;this.fadesIn=fadesIn;
    }
    public static FooterFrostedGeometry create(float height,float density,int direction,boolean extended,float distance){
        if(!Float.isFinite(height) || !Float.isFinite(density) || !Float.isFinite(distance)
                || height<=0 || density<=0 || (direction!=1 && direction!=2))return null;
        float padding=(extended?40:12)*density;
        float span=extended?height+padding:40*density;
        float top=direction==1?0:-padding,bottom=direction==1?height+padding:height;
        float start=direction==1?bottom-span:top;
        float alpha=Math.max(0,Math.min(1,distance/(20*density)));
        return new FooterFrostedGeometry(top,bottom,start,start+span,alpha,direction==2);
    }
}
