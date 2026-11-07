package app.chatgpt.flow;

import java.util.WeakHashMap;

/** Keeps ownership of the composer's outer scroll gradient through Compose node reuse. */
public final class FooterGradientRegistry {
    private final WeakHashMap<Object,Boolean> elements=new WeakHashMap<>();
    private final WeakHashMap<Object,Boolean> nodes=new WeakHashMap<>();
    public void rememberElement(Object element){if(element!=null)elements.put(element,true);}
    public void bind(Object element,Object node){
        if(node==null)return;
        if(elements.containsKey(element))nodes.put(node,true);else nodes.remove(node);
    }
    public boolean owns(Object node){return node!=null && nodes.containsKey(node);}
    public void clear(){elements.clear();nodes.clear();}
}
