package app.chatgpt.flow;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/** Public, read-only visual preferences; no permissions or message data. */
public final class SettingsProvider extends ContentProvider {
    public static final Uri URI = Uri.parse("content://io.github.northwolf8192.chatgptflow.settings/config");
    public static final String PREFS = "appearance";
    @Override public boolean onCreate() { return true; }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
        if (!URI.equals(uri)) throw new IllegalArgumentException("Unknown URI");
        SharedPreferences p = getContext().getSharedPreferences(PREFS, 0);
        MatrixCursor c = new MatrixCursor(new String[]{"enabled", "speed", "strength", "ime_compat", "glass_enabled", "glass_blur", "glass_tint", "footer_frosted"});
        c.addRow(new Object[]{p.getBoolean("enabled", true) ? 1 : 0,
                Palette.clamp(p.getInt("speed", 55), 1, 100),
                Palette.clamp(p.getInt("strength", 70), 0, 100),
                p.getBoolean("ime_compat", false) ? 1 : 0,
                p.getBoolean("glass_enabled", true) ? 1 : 0,
                Palette.clamp(p.getInt("glass_blur",22),0,60),
                Palette.clamp(p.getInt("glass_tint",38),0,100),
                p.getBoolean("footer_frosted",true)?1:0});
        c.setNotificationUri(getContext().getContentResolver(), URI);
        return c;
    }
    @Override public String getType(Uri uri) { return "vnd.android.cursor.item/vnd.chatgpt.flow.config"; }
    @Override public Uri insert(Uri u, ContentValues v) { throw new UnsupportedOperationException("Read only"); }
    @Override public int delete(Uri u, String s, String[] a) { throw new UnsupportedOperationException("Read only"); }
    @Override public int update(Uri u, ContentValues v, String s, String[] a) { throw new UnsupportedOperationException("Read only"); }
}
