package app.chatgpt.flow;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

/** One-time import of the previous module's public, visual-only configuration. */
final class PreferenceMigration {
    private static final String DONE="legacy_import_checked";
    private static final String[] FLAGS={"enabled","ime_compat","glass_enabled","footer_frosted"};
    private static final String[] VALUES={"speed","strength","glass_blur","glass_tint"};
    private static final int[] MIN={1,0,0,0},MAX={100,100,60,100};
    static void importOnce(Context context,SharedPreferences prefs){
        if(prefs.getBoolean(DONE,false))return;
        for(String key:FLAGS)if(prefs.contains(key)){prefs.edit().putBoolean(DONE,true).apply();return;}
        for(String key:VALUES)if(prefs.contains(key)){prefs.edit().putBoolean(DONE,true).apply();return;}
        SharedPreferences.Editor edit=prefs.edit();Cursor cursor=null;
        try{
            cursor=context.getContentResolver().query(Uri.parse("content://app.chatgpt.flow.settings/config"),null,null,null,null);
            if(cursor!=null&&cursor.moveToFirst()){
                for(String key:FLAGS){int column=cursor.getColumnIndex(key);if(column>=0&&!cursor.isNull(column))edit.putBoolean(key,cursor.getInt(column)==1);}
                for(int i=0;i<VALUES.length;i++){int column=cursor.getColumnIndex(VALUES[i]);if(column>=0&&!cursor.isNull(column))edit.putInt(VALUES[i],Palette.clamp(cursor.getInt(column),MIN[i],MAX[i]));}
            }
        }catch(RuntimeException ignored){
            // An absent or unavailable old app leaves all original defaults intact.
        }finally{if(cursor!=null)cursor.close();}
        edit.putBoolean(DONE,true).apply();
    }
    private PreferenceMigration(){}
}
