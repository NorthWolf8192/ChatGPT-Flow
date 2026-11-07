import app.chatgpt.flow.RenderSession;
class RenderSessionTest {
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){
  Object activity=new Object();RenderSession session=new RenderSession();
  ok(!session.visualsAllowed(true,true,true),"No activity");
  session.resume(activity);long first=session.newRefresh();
  ok(session.current(first)&&session.isResumed(),"Foreground refresh");
  session.pause();
  ok(!session.current(first)&&!session.isResumed(),"Cancel frame work while paused");
  ok(session.visualsAllowed(true,true,true),"Preserve materials for background render caches");
  session.resume(activity);long second=session.newRefresh();
  ok(session.current(second)&&!session.current(first),"Resume with fresh recovery generation");
  long focus=session.newRefresh();ok(session.current(focus)&&!session.current(second),"Focus recovery replaces earlier work");
  ok(!session.visualsAllowed(true,false,true)&&!session.visualsAllowed(false,true,true),"Honor switches");
  ok(!session.visualsAllowed(true,true,false),"Honor APK version gate");
  session.destroy();ok(!session.visualsAllowed(true,true,true)&&!session.current(focus),"Drop destroyed activity");
  System.out.println("PASS: background material retention, animation suspension, foreground generations, focus recovery and teardown");
 }
}
