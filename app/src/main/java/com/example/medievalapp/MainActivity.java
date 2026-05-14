package com.example.medievalapp;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Menüden gelen "Hangi sayfayı açayım?" emrini oku
        String target = getIntent().getStringExtra("TARGET_FRAGMENT");

        // --- YÖNLENDİRME KONTROLÜ ---
        if ("quest".equals(target)) {
            loadFragment(new QuestFragment());
        } else if ("stats".equals(target)) {
            loadFragment(new StatsFragment());
        } else if ("oracle".equals(target)) {
            loadFragment(new OracleFragment()); // KAHİN (Oracle) Sayfası
        } else {
            loadFragment(new JournalFragment()); // Varsayılan: GÜNLÜK Sayfası
        }

        // --- GERİ BUTONU (AKILLI NAVİGASYON) ---
        ImageButton btnBack = findViewById(R.id.btnBackToMenu);
        btnBack.setOnClickListener(v -> {
            FragmentManager fm = getSupportFragmentManager();

            // Eğer hafızada (BackStack) bekleyen bir alt sayfa varsa oraya dön
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                // Yoksa uygulamayı (bu aktiviteyi) kapat ve Menüye dön
                finish();
            }
        });
    }

    // Fragment'ı ekrana yükleyen yardımcı metot
    private void loadFragment(Fragment fragment) {
        // Ana menüden her girişimizde geçmiş yığını (back stack) temizlensin
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}