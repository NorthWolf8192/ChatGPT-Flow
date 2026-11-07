package app.chatgpt.flow;

import java.lang.ref.WeakReference;

/** Pausing stops frame updates; it must not switch cached controls back to stock paints. */
public final class RenderSession {
    private WeakReference<Object> owner=new WeakReference<>(null);
    private boolean resumed;
    private long generation;
    public void resume(Object activity){owner=new WeakReference<>(activity);resumed=true;generation++;}
    public void pause(){resumed=false;generation++;}
    public void destroy(){resumed=false;owner.clear();generation++;}
    public boolean isResumed(){return resumed && owner.get()!=null;}
    public boolean visualsAllowed(boolean enabled,boolean feature,boolean versionOk){
        return enabled && feature && versionOk && owner.get()!=null;
    }
    public long newRefresh(){return ++generation;}
    public boolean current(long ticket){return isResumed() && generation==ticket;}
}
