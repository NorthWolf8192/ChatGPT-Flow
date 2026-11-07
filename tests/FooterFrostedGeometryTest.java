import app.chatgpt.flow.FooterFrostedGeometry;

class FooterFrostedGeometryTest {
    static void ok(boolean b,String why){if(!b)throw new AssertionError(why);}
    static void near(float a,float b,String why){ok(Math.abs(a-b)<.001f,why);}
    public static void main(String[] args){
        FooterFrostedGeometry footer=FooterFrostedGeometry.create(128,2,2,false,20);
        near(footer.top,-24,"Original footer overhang");near(footer.bottom,128,"Keep original footer content extent");
        near(footer.maskStart,-24,"Start fade at original top");near(footer.maskEnd,56,"40dp transition");
        near(footer.alpha,.5f,"Half reveal after 10dp scroll");ok(footer.fadesIn,"Footer fades into glass");
        FooterFrostedGeometry top=FooterFrostedGeometry.create(128,2,1,false,40);
        near(top.top,0,"Opposite edge starts at content origin");near(top.bottom,152,"Opposite edge padding");
        near(top.maskStart,72,"Opposite edge ramp");near(top.maskEnd,152,"Opposite edge end");ok(!top.fadesIn,"Opposite direction");
        FooterFrostedGeometry extended=FooterFrostedGeometry.create(128,2,2,true,40);
        near(extended.top,-80,"Expanded original padding");near(extended.maskEnd,128,"Expanded ramp covers entire original region");
        float previous=-1;
        for(int scroll=0;scroll<=80;scroll++){
            FooterFrostedGeometry frame=FooterFrostedGeometry.create(128,2,2,false,scroll);
            ok(frame.alpha>=previous && frame.alpha>=0 && frame.alpha<=1,"Continuous bounded scroll reveal");previous=frame.alpha;
        }
        near(FooterFrostedGeometry.create(720,2,2,false,40).bottom,720,"Follow multiline and image composer height");
        near(FooterFrostedGeometry.create(128,2,2,false,-1).alpha,0,"No effect at resting edge");
        ok(FooterFrostedGeometry.create(0,2,2,false,40)==null,"Empty layout");
        ok(FooterFrostedGeometry.create(128,0,2,false,40)==null,"Invalid density");
        ok(FooterFrostedGeometry.create(128,2,3,false,40)==null,"Unknown native direction");
        ok(FooterFrostedGeometry.create(128,2,2,false,Float.NaN)==null,"Invalid scroll metric");
        System.out.println("PASS: original footer bounds, scroll reveal, expanded layouts and invalid metrics");
    }
}
