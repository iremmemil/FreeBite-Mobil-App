package com.example.freebite.aktiviteler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.freebite.R;
import com.example.freebite.veritabani.VeritabaniYardimcisi;

public class AlerjenActivity extends AppCompatActivity {

    // Ekrandaki tüm alerjen kutucuklarını tutan dizi
    CheckBox[] kutular;

    // Veritabanına kaydedilecek alerjen isimleri
    String[] alerjenAdlari;

    Button btnKaydet;

    // SQLite veritabanı yardımcısı
    VeritabaniYardimcisi veritabani;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerjen);

        // Veritabanı bağlantısını oluştur
        veritabani = new VeritabaniYardimcisi(this);

        // Kaydet butonunu bağla
        btnKaydet = findViewById(R.id.btnKaydet);

        // Layout içerisindeki checkboxları diziye aktar
        kutular = new CheckBox[]{
                findViewById(R.id.chkGluten),
                findViewById(R.id.chkLaktoz),
                findViewById(R.id.chkYumurta),
                findViewById(R.id.chkSoya),
                findViewById(R.id.chkFistik),
                findViewById(R.id.chkFindik),
                findViewById(R.id.chkBadem),
                findViewById(R.id.chkCeviz),
                findViewById(R.id.chkKaju),
                findViewById(R.id.chkAntepFistigi),
                findViewById(R.id.chkCilek),
                findViewById(R.id.chkMuz),
                findViewById(R.id.chkKivi),
                findViewById(R.id.chkSeftali),
                findViewById(R.id.chkBalik),
                findViewById(R.id.chkKarides),
                findViewById(R.id.chkYengec),
                findViewById(R.id.chkMidye),
                findViewById(R.id.chkSusam),
                findViewById(R.id.chkBal)
        };

        // Checkboxların karşılık geldiği alerjen isimleri
        alerjenAdlari = new String[]{
                "Gluten", "Laktoz / Süt", "Yumurta", "Soya", "Yer Fıstığı",
                "Fındık", "Badem", "Ceviz", "Kaju", "Antep Fıstığı",
                "Çilek", "Muz", "Kivi", "Şeftali", "Balık",
                "Karides", "Yengeç", "Midye", "Susam", "Bal"
        };

        // Daha önce kaydedilmiş alerjenleri yükle
        kayitliAlerjenleriYukle();

        // Kaydet butonuna tıklanınca seçili alerjenleri veritabanına kaydet
        btnKaydet.setOnClickListener(v -> alerjenleriKaydet());
    }

    private void alerjenleriKaydet() {

        // Yazma işlemleri için veritabanını aç
        SQLiteDatabase db = veritabani.getWritableDatabase();

        // Eski kayıtları temizle
        db.delete("alerjenler", null, null);

        // Tüm checkboxları dolaş
        for (int i = 0; i < alerjenAdlari.length; i++) {

            ContentValues values = new ContentValues();

            // Alerjen adını kaydet
            values.put("ad", alerjenAdlari[i]);

            // Seçiliyse 1, değilse 0 olarak kaydet
            values.put("secili", kutular[i].isChecked() ? 1 : 0);

            // Veritabanına ekle
            db.insert("alerjenler", null, values);
        }

        // Veritabanını kapat
        db.close();

        Toast.makeText(this,
                "Alerjen profiliniz kaydedildi.",
                Toast.LENGTH_SHORT).show();
    }

    private void kayitliAlerjenleriYukle() {

        // Okuma işlemleri için veritabanını aç
        SQLiteDatabase db = veritabani.getReadableDatabase();

        // Kayıtlı alerjenleri çek
        Cursor cursor = db.rawQuery(
                "SELECT ad, secili FROM alerjenler",
                null
        );

        // Tüm kayıtları sırayla oku
        while (cursor.moveToNext()) {

            String ad = cursor.getString(0);
            int secili = cursor.getInt(1);

            // Veritabanındaki alerjen ile checkbox eşleşmesini bul
            for (int i = 0; i < alerjenAdlari.length; i++) {

                if (alerjenAdlari[i].equals(ad)) {

                    // Kayıtlı durumuna göre checkboxı işaretle
                    kutular[i].setChecked(secili == 1);

                    break;
                }
            }
        }

        // Bellek sızıntısını önlemek için kaynakları kapat
        cursor.close();
        db.close();
    }
}