package com.example.freebite.aktiviteler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.freebite.R;
import com.example.freebite.veritabani.VeritabaniYardimcisi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UrunSorgulaActivity extends AppCompatActivity {

    // Kullanıcının ürün adını yazdığı alan
    EditText edtUrunAdi;

    // Ürün arama işlemini başlatan buton
    Button btnUrunAra;

    // API'den gelen ürün bilgilerini ekranda gösterecek TextView alanları
    TextView txtUrunAdi, txtMarka, txtDurum, txtAlerjenler, txtIcindekiler;

    // SQLite veritabanı işlemleri için yardımcı sınıf
    VeritabaniYardimcisi veritabani;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urun_sorgula);

        // Veritabanı bağlantısı oluşturulur
        veritabani = new VeritabaniYardimcisi(this);

        // XML dosyasındaki arama kutusu ve buton Java tarafına bağlanır
        edtUrunAdi = findViewById(R.id.edtUrunAdi);
        btnUrunAra = findViewById(R.id.btnUrunAra);

        // Ürün sonucunu gösterecek alanlar bağlanır
        txtUrunAdi = findViewById(R.id.txtUrunAdi);
        txtMarka = findViewById(R.id.txtMarka);
        txtDurum = findViewById(R.id.txtDurum);
        txtAlerjenler = findViewById(R.id.txtAlerjenler);
        txtIcindekiler = findViewById(R.id.txtIcindekiler);

        // Kullanıcı butona bastığında ürün arama işlemi başlatılır
        btnUrunAra.setOnClickListener(v -> urunAra());
    }

    private void urunAra() {

        // Kullanıcının yazdığı ürün adı alınır
        String arananUrun = edtUrunAdi.getText().toString().trim();

        // Ürün adı boşsa işlem yapılmaz
        if (arananUrun.isEmpty()) {
            Toast.makeText(this, "Lütfen ürün adı giriniz.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Türkçe karakterlerin URL içinde sorun çıkarmaması için encode işlemi yapılır
            String encoded = URLEncoder.encode(arananUrun, "UTF-8");

            // Open Food Facts API adresi oluşturulur
            String url = "https://world.openfoodfacts.net/cgi/search.pl?search_terms="
                    + encoded
                    + "&search_simple=1&action=process&json=1&page_size=1";

            // Volley ile API isteği kuyruğu oluşturulur
            RequestQueue queue = Volley.newRequestQueue(this);

            // API'ye GET isteği gönderilir
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,

                    // API cevabı başarılı gelirse ürün bilgileri işlenir
                    response -> urunBilgileriniIsle(response),

                    // API bağlantısında hata olursa kullanıcıya bilgi verilir
                    error -> Toast.makeText(this, "API bağlantı hatası oluştu.", Toast.LENGTH_SHORT).show()
            );

            // İstek kuyruğa eklenir
            queue.add(request);

        } catch (Exception e) {
            Toast.makeText(this, "Arama sırasında hata oluştu.", Toast.LENGTH_SHORT).show();
        }
    }

    private void urunBilgileriniIsle(JSONObject response) {
        try {
            // API'den gelen ürün listesi alınır
            JSONArray products = response.getJSONArray("products");

            // Eğer ürün bulunamazsa ekrana bilgilendirme yazılır
            if (products.length() == 0) {
                txtDurum.setText("Sonuç: Dikkat");
                txtUrunAdi.setText("Ürün bulunamadı");
                txtMarka.setText("");
                txtAlerjenler.setText("Bu ürün için bilgi bulunamadı.");
                txtIcindekiler.setText("");
                return;
            }

            // İlk bulunan ürün alınır
            JSONObject product = products.getJSONObject(0);

            // API'den ürün adı alınır
            String urunAdi = product.optString("product_name", "Ürün adı bulunamadı");

            // Ürün adı boş gelirse kullanıcının yazdığı isim kullanılır
            if (urunAdi == null || urunAdi.trim().isEmpty() || urunAdi.equals("Ürün adı bulunamadı")) {
                urunAdi = edtUrunAdi.getText().toString().trim();
            }

            // Ürün adı farklı dillerde uzun gelebileceği için ekranda kullanıcının aradığı kelime sade şekilde gösterilir
            String arananKelime = edtUrunAdi.getText().toString().trim();

            if (!arananKelime.isEmpty()) {
                urunAdi = arananKelime.substring(0, 1).toUpperCase() + arananKelime.substring(1);
            }

            // API'den marka, içerik ve alerjen bilgileri alınır
            String marka = product.optString("brands", "Marka bilgisi yok");
            String icindekiler = product.optString("ingredients_text", "İçindekiler bilgisi yok");
            String alerjenler = product.optString("allergens", "");

            // Ekrana gösterilecek bilgiler Türkçeleştirilerek yazılır
            txtUrunAdi.setText(turkcelestir(urunAdi));
            txtMarka.setText("Marka: " + turkcelestir(marka));
            txtAlerjenler.setText("Alerjen Bilgisi: " + turkcelestir(alerjenler));
            txtIcindekiler.setText("İçindekiler: " + icindekilerOzetiOlustur(icindekiler));

            // Kullanıcının profilde seçtiği alerjenler veritabanından çekilir
            ArrayList<String> seciliAlerjenler = seciliAlerjenleriGetir();

            // Ürün içinde eşleşen alerjenleri tutacak liste
            ArrayList<String> eslesenler = new ArrayList<>();

            // İçindekiler ve alerjenler tek metinde birleştirilerek kontrol edilir
            String kontrolMetni = (icindekiler + " " + alerjenler).toLowerCase();

            // Kullanıcının seçtiği her alerjen ürün içeriğiyle karşılaştırılır
            for (String alerjen : seciliAlerjenler) {
                if (alerjenEslesiyor(alerjen, kontrolMetni)) {
                    eslesenler.add(alerjen);
                }
            }

            String durum;
            String eslesenMetin;

            // Eğer eşleşen alerjen varsa ürün riskli kabul edilir
            if (eslesenler.size() > 0) {
                durum = "Riskli";
                eslesenMetin = eslesenler.toString();
                txtDurum.setText("Sonuç: Riskli Ürün ⚠️\nEşleşen alerjenler: " + eslesenMetin);
            } else {
                // Eşleşme yoksa ürün güvenli olarak gösterilir
                durum = "Güvenli";
                eslesenMetin = "Yok";
                txtDurum.setText("Sonuç: Güvenli Ürün ✅");
            }

            // Yapılan sorgu geçmişe kaydedilir
            gecmiseKaydet(turkcelestir(urunAdi), turkcelestir(marka), durum, eslesenMetin);

        } catch (Exception e) {
            Toast.makeText(this, "Ürün bilgileri okunamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> seciliAlerjenleriGetir() {

        // Seçili alerjenleri tutacak liste
        ArrayList<String> liste = new ArrayList<>();

        // Veritabanı okuma modunda açılır
        SQLiteDatabase db = veritabani.getReadableDatabase();

        // Sadece seçili olan alerjenler çekilir
        Cursor cursor = db.rawQuery("SELECT ad FROM alerjenler WHERE secili = 1", null);

        // Kayıtlar listeye eklenir
        while (cursor.moveToNext()) {
            liste.add(cursor.getString(0));
        }

        // Kaynaklar kapatılır
        cursor.close();
        db.close();

        return liste;
    }

    private void gecmiseKaydet(String urunAdi, String marka, String durum, String eslesenAlerjenler) {

        // Veritabanı yazma modunda açılır
        SQLiteDatabase db = veritabani.getWritableDatabase();

        // Aynı ürün daha önce sorgulandıysa tekrar eklenmemesi için eski kayıt silinir
        db.delete(
                "urun_gecmisi",
                "urun_adi = ? AND marka = ?",
                new String[]{urunAdi, marka}
        );

        // Geçmiş tablosuna eklenecek bilgiler hazırlanır
        ContentValues values = new ContentValues();
        values.put("urun_adi", urunAdi);
        values.put("marka", marka);
        values.put("durum", durum);
        values.put("eslesen_alerjenler", eslesenAlerjenler);

        // Sorgulama tarihi eklenir
        String tarih = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        values.put("tarih", tarih);

        // Ürün geçmişe kaydedilir
        db.insert("urun_gecmisi", null, values);

        db.close();
    }

    private String turkcelestir(String metin) {

        // Boş metin gelirse kullanıcıya anlamlı bir bilgi gösterilir
        if (metin == null || metin.trim().isEmpty()) {
            return "Belirtilmemiş";
        }

        // Open Food Facts verileri bazen İngilizce veya Fransızca geldiği için
        // ekranda daha anlaşılır görünmesi adına bazı kelimeler Türkçeye çevrilir.
        return metin
                .replace("en:", "")
                .replace("fr:", "")
                .replace("lait", "süt")
                .replace("Lait", "Süt")
                .replace("LAIT", "SÜT")
                .replace("milk", "süt")
                .replace("Milk", "Süt")
                .replace("noisettes", "fındık")
                .replace("NOISETTES", "FINDIK")
                .replace("noisette", "fındık")
                .replace("hazelnuts", "fındık")
                .replace("hazelnut", "fındık")
                .replace("fruits à coque", "sert kabuklu yemişler")
                .replace("nuts", "sert kabuklu yemişler")
                .replace("soja", "soya")
                .replace("soy", "soya")
                .replace("Soy", "Soya")
                .replace("sucre", "şeker")
                .replace("Sucre", "Şeker")
                .replace("sugar", "şeker")
                .replace("huile de palme", "palmiye yağı")
                .replace("palm oil", "palmiye yağı")
                .replace("cacao maigre", "yağı azaltılmış kakao")
                .replace("cocoa", "kakao")
                .replace("vanilline", "vanilin")
                .replace("vanillin", "vanilin")
                .replace("émulsifiants", "emülgatörler")
                .replace("emulsifiers", "emülgatörler")
                .replace("lécithines", "lesitinler")
                .replace("lecithins", "lesitinler")
                .replace("arachides", "yer fıstığı")
                .replace("arachide", "yer fıstığı")
                .replace("peanuts", "yer fıstığı")
                .replace("peanut", "yer fıstığı")
                .replace("oeuf", "yumurta")
                .replace("œuf", "yumurta")
                .replace("egg", "yumurta");
    }

    private String icindekilerOzetiOlustur(String metin) {

        // İçindekiler metni küçük harfe çevrilerek kontrol kolaylaştırılır
        String kucuk = metin.toLowerCase();

        // İçerikte bulunan temel maddeler bu listeye eklenir
        ArrayList<String> liste = new ArrayList<>();

        // API farklı dillerde veri döndürebildiği için hem İngilizce hem Fransızca kelimeler kontrol edilir.
        if (kucuk.contains("sucre") || kucuk.contains("sugar")) liste.add("şeker");
        if (kucuk.contains("huile de palme") || kucuk.contains("palm oil")) liste.add("palmiye yağı");
        if (kucuk.contains("noisette") || kucuk.contains("hazelnut")) liste.add("fındık");
        if (kucuk.contains("cacao") || kucuk.contains("cocoa")) liste.add("kakao");
        if (kucuk.contains("lait") || kucuk.contains("milk")) liste.add("süt");
        if (kucuk.contains("lactoserum") || kucuk.contains("whey")) liste.add("peynir altı suyu tozu");
        if (kucuk.contains("soja") || kucuk.contains("soy")) liste.add("soya");
        if (kucuk.contains("vanilline") || kucuk.contains("vanillin")) liste.add("vanilin");
        if (kucuk.contains("gluten")) liste.add("gluten");

        // Hiçbir bilinen içerik bulunamazsa bilgi mesajı döndürülür
        if (liste.isEmpty()) {
            return "İçerik bilgisi Türkçe olarak özetlenemedi.";
        }

        // Bulunan içerikler virgülle ayrılarak ekrana yazılır
        return String.join(", ", liste);
    }

    private boolean alerjenEslesiyor(String alerjen, String kontrolMetni) {

        // Kullanıcının seçtiği alerjen küçük harfe çevrilir
        String a = alerjen.toLowerCase();

        // Open Food Facts ürün içerikleri bazen İngilizce veya Fransızca döndüğü için
        // her alerjenin Türkçe, İngilizce ve Fransızca karşılıkları birlikte kontrol edilir.

        if (a.contains("gluten")) {
            return kontrolMetni.contains("gluten")
                    || kontrolMetni.contains("wheat")
                    || kontrolMetni.contains("blé");
        }

        if (a.contains("laktoz") || a.contains("süt")) {
            return kontrolMetni.contains("milk")
                    || kontrolMetni.contains("lait")
                    || kontrolMetni.contains("lactose")
                    || kontrolMetni.contains("süt");
        }

        if (a.contains("yumurta")) {
            return kontrolMetni.contains("egg")
                    || kontrolMetni.contains("oeuf")
                    || kontrolMetni.contains("œuf")
                    || kontrolMetni.contains("yumurta");
        }

        if (a.contains("soya")) {
            return kontrolMetni.contains("soy")
                    || kontrolMetni.contains("soja")
                    || kontrolMetni.contains("soya");
        }

        if (a.contains("yer fıstığı")) {
            return kontrolMetni.contains("peanut")
                    || kontrolMetni.contains("arachide")
                    || kontrolMetni.contains("yer fıstığı");
        }

        if (a.contains("fındık")) {
            return kontrolMetni.contains("hazelnut")
                    || kontrolMetni.contains("noisette")
                    || kontrolMetni.contains("noisettes")
                    || kontrolMetni.contains("fındık");
        }

        if (a.contains("badem")) {
            return kontrolMetni.contains("almond")
                    || kontrolMetni.contains("amande")
                    || kontrolMetni.contains("badem");
        }

        if (a.contains("ceviz")) {
            return kontrolMetni.contains("walnut")
                    || kontrolMetni.contains("noix")
                    || kontrolMetni.contains("ceviz");
        }

        if (a.contains("kaju")) {
            return kontrolMetni.contains("cashew")
                    || kontrolMetni.contains("cajou")
                    || kontrolMetni.contains("kaju");
        }

        if (a.contains("antep")) {
            return kontrolMetni.contains("pistachio")
                    || kontrolMetni.contains("pistache")
                    || kontrolMetni.contains("antep");
        }

        if (a.contains("çilek")) {
            return kontrolMetni.contains("strawberry")
                    || kontrolMetni.contains("fraise")
                    || kontrolMetni.contains("çilek")
                    || kontrolMetni.contains("cilek");
        }

        if (a.contains("muz")) {
            return kontrolMetni.contains("banana")
                    || kontrolMetni.contains("banane")
                    || kontrolMetni.contains("muz");
        }

        if (a.contains("kivi")) {
            return kontrolMetni.contains("kiwi")
                    || kontrolMetni.contains("kivi");
        }

        if (a.contains("şeftali")) {
            return kontrolMetni.contains("peach")
                    || kontrolMetni.contains("pêche")
                    || kontrolMetni.contains("peche")
                    || kontrolMetni.contains("şeftali");
        }

        if (a.contains("balık")) {
            return kontrolMetni.contains("fish")
                    || kontrolMetni.contains("poisson")
                    || kontrolMetni.contains("balık");
        }

        if (a.contains("karides")) {
            return kontrolMetni.contains("shrimp")
                    || kontrolMetni.contains("crevette")
                    || kontrolMetni.contains("karides");
        }

        if (a.contains("yengeç")) {
            return kontrolMetni.contains("crab")
                    || kontrolMetni.contains("crabe")
                    || kontrolMetni.contains("yengeç");
        }

        if (a.contains("midye")) {
            return kontrolMetni.contains("mussel")
                    || kontrolMetni.contains("moule")
                    || kontrolMetni.contains("midye");
        }

        if (a.contains("susam")) {
            return kontrolMetni.contains("sesame")
                    || kontrolMetni.contains("sésame")
                    || kontrolMetni.contains("susam");
        }

        if (a.contains("bal")) {
            return kontrolMetni.contains("honey")
                    || kontrolMetni.contains("miel")
                    || kontrolMetni.contains("bal");
        }

        // Eşleşme yoksa false döner
        return false;
    }
}