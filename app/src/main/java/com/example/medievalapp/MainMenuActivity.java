package com.example.medievalapp;

import android.content.Intent;
import android.content.SharedPreferences; // Hafızayı silmek için lazım
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton; // Buton türü ImageButton
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainMenuActivity extends AppCompatActivity {

    private final String[] medievalQuotes = {
            "\"Fırtına ne kadar sert eserse essin, dağ ona boyun eğmez.\"",
            "\"Kılıç kınında paslanır, ruh hareketsizlikte çürür.\"",
            "\"Karanlıktan korkan, şafağı asla göremez.\"",
            "\"Yaralar, bir savaşçının en dürüst madalyalarıdır.\"",
            "\"Kader kartları dağıtır, biz ise sadece oynarız.\"",
            "\"Zafer, pes etmeyenlerindir.\""
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // --- 1. GÖRÜNÜMLERİ TANIMLA ---
        // XML'deki ID'si: btnLogout (Sağ üstteki ikon)
        ImageButton btnLogout = findViewById(R.id.btnLogout);

        View titleLayout = findViewById(R.id.layoutTitle);
        View btnJournal = findViewById(R.id.btnMenuJournal);
        View btnQuests = findViewById(R.id.btnMenuQuests);
        View btnStats = findViewById(R.id.btnMenuStats);
        View btnOracle = findViewById(R.id.btnMenuOracle);
        View quoteLayout = findViewById(R.id.layoutQuote);
        TextView tvQuote = findViewById(R.id.tvDailyQuote);
        View footer = findViewById(R.id.tvFooter);

        // --- 2. ÇIKIŞ YAP (LOGOUT) BUTONU İŞLEVİ ---
        // İşte eksik olan kısım burasıydı:
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                // A) Hafızayı Temizle (Beni Hatırla'yı iptal et)
                SharedPreferences preferences = getSharedPreferences("MedievalPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear(); // Kullanıcı adını ve şifreyi siler
                editor.apply(); // Kaydet

                // B) Kullanıcıya Bilgi Ver
                Toast.makeText(MainMenuActivity.this, "Oturum kapatıldı. Görüşmek üzere!", Toast.LENGTH_SHORT).show();

                // C) Giriş Ekranına Geri Gönder
                Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
                startActivity(intent);

                // D) Bu ekranı kapat (Geri tuşuna basınca tekrar buraya girmesin)
                finish();
            });
        }

        // --- 3. DİĞER İŞLEMLER (Sözler ve Animasyonlar) ---
        if (tvQuote != null) {
            tvQuote.setText(medievalQuotes[new Random().nextInt(medievalQuotes.length)]);
        }

        // Animasyonlar (Sırayla giriş)
        prepareViewForAnimation(titleLayout, 0);
        prepareViewForAnimation(btnLogout, 0); // Çıkış butonu da animasyonla gelsin
        prepareViewForAnimation(btnJournal, 100);
        prepareViewForAnimation(btnQuests, 100);
        prepareViewForAnimation(btnStats, 100);
        prepareViewForAnimation(btnOracle, 100);
        prepareViewForAnimation(quoteLayout, 50);
        prepareViewForAnimation(footer, 50);

        animateViewEntry(titleLayout, 0);
        animateViewEntry(btnLogout, 200); // Başlıktan hemen sonra gelsin
        animateViewEntry(btnJournal, 200);
        animateViewEntry(btnQuests, 400);
        animateViewEntry(btnStats, 600);
        animateViewEntry(btnOracle, 800);
        animateViewEntry(quoteLayout, 1000);
        animateViewEntry(footer, 1200);

        // Menü Tıklamaları
        btnJournal.setOnClickListener(v -> openModule("journal"));
        btnQuests.setOnClickListener(v -> openModule("quest"));
        btnStats.setOnClickListener(v -> openModule("stats"));
        btnOracle.setOnClickListener(v -> openModule("oracle"));
    }

    // --- YARDIMCI METOTLAR ---
    private void prepareViewForAnimation(View view, float translationY) {
        if (view != null) {
            view.setAlpha(0f);
            view.setTranslationY(translationY);
        }
    }

    private void animateViewEntry(View view, long delay) {
        if (view != null) {
            view.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(600)
                    .setStartDelay(delay)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }

    private void openModule(String target) {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        intent.putExtra("TARGET_FRAGMENT", target);
        startActivity(intent);
    }
}