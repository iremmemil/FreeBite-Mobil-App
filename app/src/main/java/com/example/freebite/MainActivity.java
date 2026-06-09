package com.example.freebite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.freebite.aktiviteler.AlerjenActivity;
import com.example.freebite.aktiviteler.UrunSorgulaActivity;
import com.example.freebite.aktiviteler.GecmisActivity;
import com.example.freebite.aktiviteler.ENumarasiActivity;

public class MainActivity extends AppCompatActivity {

    Button btnAlerjen, btnUrunSorgula, btnGecmis, btnENumarasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAlerjen = findViewById(R.id.btnAlerjen);
        btnUrunSorgula = findViewById(R.id.btnUrunSorgula);
        btnGecmis = findViewById(R.id.btnGecmis);
        btnENumarasi = findViewById(R.id.btnENumarasi);

        btnAlerjen.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AlerjenActivity.class)));

        btnUrunSorgula.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UrunSorgulaActivity.class)));

        btnGecmis.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, GecmisActivity.class)));

        btnENumarasi.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ENumarasiActivity.class)));
    }
}