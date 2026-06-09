package com.example.freebite.aktiviteler;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.freebite.R;
import com.example.freebite.veritabani.VeritabaniYardimcisi;

public class ENumarasiActivity extends AppCompatActivity {

    // E-numarası arama kutusu
    EditText edtENumarasiAra;

    // E-numarası kartlarının ekleneceği ana layout
    LinearLayout layoutENumaralari;

    // SQLite veritabanı yardımcısı
    VeritabaniYardimcisi veritabani;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enumarasi);

        // XML tarafındaki arama kutusunu Java koduna bağla
        edtENumarasiAra = findViewById(R.id.edtENumarasiAra);

        // Kartların gösterileceği LinearLayout'u bağla
        layoutENumaralari = findViewById(R.id.layoutENumaralari);

        // Veritabanı bağlantısını oluştur
        veritabani = new VeritabaniYardimcisi(this);

        // Sayfa ilk açıldığında tüm E-numaralarını listele
        eNumaralariniYukle("");

        // Arama kutusuna yazı yazıldıkça listeyi anlık olarak filtrele
        edtENumarasiAra.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Yazı değişmeden önce yapılacak özel bir işlem yok
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Kullanıcının yazdığı metne göre E-numaralarını tekrar yükle
                eNumaralariniYukle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Yazı değiştikten sonra yapılacak özel bir işlem yok
            }
        });
    }

    private void eNumaralariniYukle(String aramaMetni) {

        // Enter veya boşluk gibi karakterler aramayı bozmasın diye temizlenir
        aramaMetni = aramaMetni.trim().replace("\n", "").replace("\r", "");

        // Önce ekranda bulunan eski kartları temizle
        layoutENumaralari.removeAllViews();

        // Veritabanını okuma modunda aç
        SQLiteDatabase db = veritabani.getReadableDatabase();

        Cursor cursor;

        // Arama kutusu boşsa tüm kayıtları getir
        if (aramaMetni.isEmpty()) {
            cursor = db.rawQuery(
                    "SELECT kod, isim, kategori, aciklama FROM e_numaralari ORDER BY kod ASC",
                    null
            );
        } else {

            // LIKE sorgusu için arama metninin başına ve sonuna % eklenir
            String arama = "%" + aramaMetni + "%";

            // Kod, isim, kategori veya açıklama alanlarında arama yapılır
            cursor = db.rawQuery(
                    "SELECT kod, isim, kategori, aciklama FROM e_numaralari " +
                            "WHERE kod LIKE ? OR isim LIKE ? OR kategori LIKE ? OR aciklama LIKE ? " +
                            "ORDER BY kod ASC",
                    new String[]{arama, arama, arama, arama}
            );
        }

        // Eğer sonuç bulunamazsa kullanıcıya bilgi ver
        if (cursor.getCount() == 0) {
            TextView bosMetin = new TextView(this);
            bosMetin.setText("Aradığınız katkı maddesi bulunamadı.");
            bosMetin.setTextSize(16);
            bosMetin.setTextColor(Color.parseColor("#333333"));
            bosMetin.setPadding(10, 20, 10, 20);

            layoutENumaralari.addView(bosMetin);
        }

        // Sorgudan gelen her kayıt için ayrı bir kart oluştur
        while (cursor.moveToNext()) {

            String kod = cursor.getString(0);
            String isim = cursor.getString(1);
            String kategori = cursor.getString(2);
            String aciklama = cursor.getString(3);

            // Gelen verileri kart şeklinde ekrana ekle
            kartEkle(kod, isim, kategori, aciklama);
        }

        // Cursor ve veritabanı kapatılır
        cursor.close();
        db.close();
    }

    private void kartEkle(String kod, String isim, String kategori, String aciklama) {

        // Her E-numarası için dikey bir kart layout'u oluştur
        LinearLayout kart = new LinearLayout(this);
        kart.setOrientation(LinearLayout.VERTICAL);
        kart.setPadding(28, 24, 28, 24);

        // Kartın arka planı, köşeleri ve kenarlığı ayarlanır
        GradientDrawable arkaPlan = new GradientDrawable();
        arkaPlan.setColor(Color.WHITE);
        arkaPlan.setCornerRadius(28);
        arkaPlan.setStroke(2, Color.parseColor("#E6E2D7"));

        kart.setBackground(arkaPlan);

        // Kartın hafif gölgeli görünmesini sağlar
        kart.setElevation(5);

        // Kartın genişlik, yükseklik ve dış boşluk ayarları
        LinearLayout.LayoutParams kartParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        kartParams.setMargins(0, 0, 0, 18);
        kart.setLayoutParams(kartParams);

        // Kart başlığı: E kodu ve katkı maddesi adı
        TextView txtBaslik = new TextView(this);
        txtBaslik.setText(kod + "  " + isim);
        txtBaslik.setTextSize(22);
        txtBaslik.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        txtBaslik.setTextColor(Color.parseColor("#5F7F2F"));

        // Katkı maddesinin kategorisi
        TextView txtKategori = new TextView(this);
        txtKategori.setText("Kategori: " + kategori);
        txtKategori.setTextSize(16);
        txtKategori.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        txtKategori.setTextColor(Color.parseColor("#333333"));
        txtKategori.setPadding(0, 10, 0, 0);

        // Katkı maddesinin kısa açıklaması
        TextView txtAciklama = new TextView(this);
        txtAciklama.setText("Açıklama: " + aciklama);
        txtAciklama.setTextSize(15);
        txtAciklama.setTextColor(Color.parseColor("#444444"));
        txtAciklama.setPadding(0, 6, 0, 0);

        // Oluşturulan yazılar karta eklenir
        kart.addView(txtBaslik);
        kart.addView(txtKategori);
        kart.addView(txtAciklama);

        // Kart ana liste layout'una eklenir
        layoutENumaralari.addView(kart);
    }
}