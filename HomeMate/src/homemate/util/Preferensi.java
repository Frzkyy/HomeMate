package homemate.util;

import java.util.prefs.Preferences;

/** Pengaturan kecil yang diingat di komputer pengguna (bukan di database). Milik Said. */
public class Preferensi {

    private static final Preferences PREF = Preferences.userNodeForPackage(Preferensi.class);

    /** Username yang diingat di layar login ("Ingat saya"), kosong kalau tidak ada. */
    public static String getUsernameIngat() {
        return PREF.get("usernameIngat", "");
    }

    public static void setUsernameIngat(String username) {
        if (username == null || username.isEmpty()) {
            PREF.remove("usernameIngat");
        } else {
            PREF.put("usernameIngat", username);
        }
    }

    /** Notifikasi (titik merah di ikon lonceng dashboard). Bawaan: aktif. */
    public static boolean isNotifikasiAktif() {
        return PREF.getBoolean("notifikasi", true);
    }

    public static void setNotifikasiAktif(boolean aktif) {
        PREF.putBoolean("notifikasi", aktif);
    }
}
