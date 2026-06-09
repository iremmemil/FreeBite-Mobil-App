package com.example.freebite.veritabani;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VeritabaniYardimcisi extends SQLiteOpenHelper {

    public VeritabaniYardimcisi(Context context) {
        super(context, "FreeBiteDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE alerjenler (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ad TEXT, " +
                "secili INTEGER)");

        db.execSQL("CREATE TABLE ozel_alerjenler (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ad TEXT)");

        db.execSQL("CREATE TABLE urun_gecmisi (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "urun_adi TEXT, " +
                "marka TEXT, " +
                "durum TEXT, " +
                "eslesen_alerjenler TEXT, " +
                "tarih TEXT)");

        db.execSQL("CREATE TABLE e_numaralari (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "kod TEXT, " +
                "isim TEXT, " +
                "kategori TEXT, " +
                "aciklama TEXT)");

        hazirENumaralariEkle(db);
    }

    private void hazirENumaralariEkle(SQLiteDatabase db) {
        db.execSQL("INSERT INTO e_numaralari (kod, isim, kategori, aciklama) VALUES " +
                "('E100', 'Kurkumin', 'Renklendirici', 'Zerdeçaldan elde edilen doğal sarı renklendiricidir.')," +
                "('E102', 'Tartrazin', 'Renklendirici', 'Sarı renklendiricidir. Bazı kişilerde hassasiyet oluşturabilir.')," +
                "('E202', 'Potasyum Sorbat', 'Koruyucu', 'Gıdalarda küf ve maya oluşumunu önlemek için kullanılır.')," +
                "('E211', 'Sodyum Benzoat', 'Koruyucu', 'Asitli içecekler ve hazır gıdalarda kullanılan koruyucudur.')," +
                "('E250', 'Sodyum Nitrit', 'Koruyucu', 'İşlenmiş et ürünlerinde kullanılan koruyucu katkı maddesidir.')," +
                "('E300', 'Askorbik Asit', 'Antioksidan', 'C vitamini olarak bilinir, gıdalarda oksidasyonu azaltır.')," +
                "('E322', 'Lesitin', 'Emülgatör', 'Yağ ve suyun karışmasına yardımcı olur.')," +
                "('E330', 'Sitrik Asit', 'Asitlik Düzenleyici', 'Limon ve turunçgillerde bulunan asitlik düzenleyicidir.')," +
                "('E407', 'Karagenan', 'Kıvam Artırıcı', 'Gıdalarda kıvam vermek için kullanılır.')," +
                "('E415', 'Ksantan Gam', 'Kıvam Artırıcı', 'Sos ve içeceklerde kıvam artırıcı olarak kullanılır.')," +
                "('E621', 'Monosodyum Glutamat', 'Lezzet Artırıcı', 'Hazır çorba, cips ve işlenmiş ürünlerde lezzet artırıcıdır.')," +
                "('E950', 'Asesülfam K', 'Tatlandırıcı', 'Şekersiz ürünlerde kullanılan yapay tatlandırıcıdır.')," +
                "('E951', 'Aspartam', 'Tatlandırıcı', 'Düşük kalorili ürünlerde kullanılan yapay tatlandırıcıdır.')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS alerjenler");
        db.execSQL("DROP TABLE IF EXISTS ozel_alerjenler");
        db.execSQL("DROP TABLE IF EXISTS urun_gecmisi");
        db.execSQL("DROP TABLE IF EXISTS e_numaralari");
        onCreate(db);
    }
}