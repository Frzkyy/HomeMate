package homemate.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Format angka dan tanggal yang dipakai bersama. Milik Said. */
public class Format {

    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    private static final NumberFormat ANGKA = NumberFormat.getInstance(INDONESIA);
    private static final DateTimeFormatter HARI_TANGGAL = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", INDONESIA);
    private static final DateTimeFormatter TANGGAL_PENDEK = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    /** "2026-11-11" menjadi "11/11/2026" (tampilan tabel). Kembalikan "-" kalau kosong/salah. */
    public static String tampil(String isoTanggal) {
        LocalDate t = tanggal(isoTanggal);
        return t == null ? "-" : t.format(TANGGAL_PENDEK);
    }

    /** Tanggal untuk judul halaman, contoh "Rabu, 11 November 2026". */
    public static String hariTanggal(LocalDate tanggal) {
        return tanggal.format(HARI_TANGGAL);
    }

    /** Kode database jadi teks tampilan: "SELESAI" menjadi "Selesai". */
    public static String kapital(String kode) {
        if (kode == null || kode.isEmpty()) {
            return "-";
        }
        return kode.substring(0, 1).toUpperCase(INDONESIA) + kode.substring(1).toLowerCase(INDONESIA);
    }
}
