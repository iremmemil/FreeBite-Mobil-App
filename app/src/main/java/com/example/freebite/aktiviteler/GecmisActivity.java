package com.example.freebite.aktiviteler;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.freebite.R;
import com.example.freebite.veritabani.VeritabaniYardimcisi;

public class GecmisActivity extends AppCompatActivity {

    // Güvenli ve riskli ürünlerin gösterileceği alanlar
    TextView txtGuvenliUrunler, txtRiskliUrunler;

    // Geçmişi temizleme butonu
    Button btnGecmisiSil;

    // SQLite veritabanı yardımcısı
    VeritabaniYardimcisi veritabani;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gecmis);

        // XML'deki bileşenleri Java tarafına bağla
        txtGuvenliUrunler = findViewById(R.id.txtGuvenliUrunler);
        txtRiskliUrunler = findViewById(R.id.txtRiskliUrunler);
        btnGecmisiSil = findViewById(R.id.btnGecmisiSil);

        // Veritabanı bağlantısını oluştur
        veritabani = new VeritabaniYardimcisi(this);

        // Sayfa açıldığında geçmiş kayıtlarını yükle
        gecmisiYukle();

        // Temizle butonuna basılınca geçmişi sil
        btnGecmisiSil.setOnClickListener(v -> gecmisiSil());
    }

    private void gecmisiYukle() {

        // Veritabanını okuma modunda aç
        SQLiteDatabase db = veritabani.getReadableDatabase();

        // Ürün geçmişini en son eklenen kayıt üstte olacak şekilde getir
        Cursor cursor = db.rawQuery(
                "SELECT urun_adi, marka, durum, eslesen_alerjenler, tarih FROM urun_gecmisi ORDER BY id DESC",
                null
        );

        // Güvenli ve riskli ürünleri ayrı listelerde tutacağız
        StringBuilder guvenliListe = new StringBuilder();
        StringBuilder riskliListe = new StringBuilder();

        // Tüm kayıtları sırayla oku
        while (cursor.moveToNext()) {

            String urunAdi = cursor.getString(0);
            String marka = cursor.getString(1);
            String durum = cursor.getString(2);
            String eslesenAlerjenler = cursor.getString(3);
            String tarih = cursor.getString(4);

            // Her ürün için ekrana yazılacak metni oluştur
            String satir = "• " + urunAdi + "\n"
                    + "  Marka: " + marka + "\n"
                    + "  Tarih: " + tarih + "\n";

            // Riskli ürünlerde eşleşen alerjenleri de göster
            if (durum.equals("Riskli")) {

                satir += "  Eşleşen Alerjenler: "
                        + eslesenAlerjenler + "\n\n";

                riskliListe.append(satir);

            } else {

                guvenliListe.append(satir).append("\n");
            }
        }

        // Kaynakları kapat
        cursor.close();
        db.close();

        // Güvenli ürünleri ekranda göster
        txtGuvenliUrunler.setText(
                guvenliListe.length() > 0
                        ? guvenliListe.toString()
                        : "Henüz güvenli ürün yok."
        );

        // Riskli ürünleri ekranda göster
        txtRiskliUrunler.setText(
                riskliListe.length() > 0
                        ? riskliListe.toString()
                        : "Henüz riskli ürün yok."
        );
    }

    private void gecmisiSil() {

        // Veritabanını yazma modunda aç
        SQLiteDatabase db = veritabani.getWritableDatabase();

        // Ürün geçmişindeki tüm kayıtları sil
        db.delete("urun_gecmisi", null, null);

        db.close();

        // Ekranı varsayılan hale getir
        txtGuvenliUrunler.setText("Henüz güvenli ürün yok.");
        txtRiskliUrunler.setText("Henüz riskli ürün yok.");

        // Kullanıcıya bilgi ver
        Toast.makeText(
                this,
                "Geçmiş başarıyla temizlendi.",
                Toast.LENGTH_SHORT
        ).show();
    }
}