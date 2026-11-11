package homemate.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Format angka dan tanggal yang dipakai bersama. Milik Said. */
public class Format {

    private static final NumberFormat ANGKA = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));

    /** 1500000 menjadi "Rp 1.500.000". */
    public static String rupiah(long nominal) {
        return "Rp " + ANGKA.format(nominal);
    }

    /** Ubah teks "yyyy-MM-dd" jadi tanggal. Kembalikan null kalau formatnya salah. */
    public static LocalDate tanggal(String teks) {
        try {
            return LocalDate.parse(teks.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }
}
