import app.chatgpt.flow.FooterGradientRegistry;

class FooterGradientRegistryTest {
    static void ok(boolean condition,String reason){if(!condition)throw new AssertionError(reason);}
    public static void main(String[] args){
        FooterGradientRegistry registry=new FooterGradientRegistry();
        Object footerElement=new Object(),otherElement=new Object(),footerNode=new Object(),otherNode=new Object();
        registry.rememberElement(footerElement);
        // Compose materializes nodes after the composable invocation has returned.
        registry.bind(footerElement,footerNode);
        registry.bind(otherElement,otherNode);
        ok(registry.owns(footerNode),"Retain footer ownership after composition");
        ok(!registry.owns(otherNode),"Do not suppress unrelated gradients");
        registry.bind(otherElement,footerNode);
        ok(!registry.owns(footerNode),"Drop ownership when Compose reuses the node outside the footer");
        Object replacement=new Object();registry.rememberElement(replacement);
        registry.bind(replacement,footerNode);
        ok(registry.owns(footerNode),"Restore ownership after recomposition and scrolling");
        ok(!registry.owns(null),"No null node ownership");
        registry.clear();ok(!registry.owns(footerNode),"Clear destroyed activity ownership");
        registry.bind(replacement,footerNode);
        ok(!registry.owns(footerNode),"Clearing also removes old element markers");
        FooterGradientRegistry top=new FooterGradientRegistry();Object topElement=new Object();
        top.rememberElement(topElement);top.bind(topElement,footerNode);registry.bind(topElement,footerNode);
        ok(top.owns(footerNode)&&!registry.owns(footerNode),"Header and footer ownership remain separate");
        registry.rememberElement(replacement);registry.bind(replacement,footerNode);top.bind(replacement,footerNode);
        ok(registry.owns(footerNode)&&!top.owns(footerNode),"Reused node moves from header to footer ownership");
        FooterGradientRegistry settings=new FooterGradientRegistry();Object settingsElement=new Object();
        settings.rememberElement(settingsElement);settings.bind(settingsElement,footerNode);
        registry.bind(settingsElement,footerNode);top.bind(settingsElement,footerNode);
        ok(settings.owns(footerNode)&&!registry.owns(footerNode)&&!top.owns(footerNode),"Reused settings node has its own ownership");
        settings.bind(replacement,footerNode);registry.bind(replacement,footerNode);
        ok(!settings.owns(footerNode)&&registry.owns(footerNode),"Settings node can be reused in conversation");
        settings.clear();settings.bind(settingsElement,otherNode);
        ok(!settings.owns(otherNode),"Destroyed settings elements cannot keep stale ownership");
        System.out.println("PASS: footer gradient materialization, node reuse, recomposition, unrelated gradients and teardown");
    }
}
