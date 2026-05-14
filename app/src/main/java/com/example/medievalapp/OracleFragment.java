package com.example.medievalapp; // Paket ismini kendi projene göre kontrol et

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Random;

public class OracleFragment extends Fragment {

    private ImageView imgNeedle; // Sadece ibre dönecek
    private TextView tvResult;
    private EditText etInput;
    private Button btnAsk;
    private Random random;

    // Mistik Cevap Havuzu (Evet/Hayır Modu İçin)
    private final String[] mysticAnswers = {
            "Yıldızlar seninle parlıyor. (EVET)",
            "Kaderin yolu açık. (EVET)",
            "Ruhlar onaylıyor. (EVET)",
            "Karanlık görünüyor... (HAYIR)",
            "Bu savaş kazanılmaz. (HAYIR)",
            "Henüz zamanı değil. (BEKLE)",
            "Sislerin ardını göremiyorum. (TEKRAR DENE)"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Yeni mistik tasarımı bağla
        return inflater.inflate(R.layout.fragment_oracle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI Elemanlarını Bul
        imgNeedle = view.findViewById(R.id.imgCompassNeedle); // DİKKAT: Sadece ibreyi alıyoruz
        tvResult = view.findViewById(R.id.tvOracleResult);
        etInput = view.findViewById(R.id.etOracleInput); // TextInputLayout içindeki EditText ID'si
        btnAsk = view.findViewById(R.id.btnAskOracle);

        random = new Random();

        // Butona Tıklama Olayı
        btnAsk.setOnClickListener(v -> askTheOracle());
    }

    private void askTheOracle() {
        // 1. Kullanıcı ne yazmış?
        String inputText = etInput.getText().toString().trim();
        String finalAnswer;

        // 2. Modu Belirle (Boşsa Evet/Hayır, Doluysa Seçenekler)
        if (inputText.isEmpty()) {
            // Mod: Evet/Hayır
            finalAnswer = mysticAnswers[random.nextInt(mysticAnswers.length)];
        } else {
            // Mod: Seçenekler (Virgülle ayrılmış)
            String[] options = inputText.split(",");

            // Eğer kullanıcı tek bir şey yazdıysa uyar (Seçim yapamayız)
            if (options.length < 2) {
                Toast.makeText(requireContext(), "En az 2 seçenek yazmalısın! (Örn: Java, Spor)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Rastgele birini seç ve temizle (boşlukları sil)
            finalAnswer = options[random.nextInt(options.length)].trim().toUpperCase();
        }

        // 3. Animasyonu Başlat
        spinNeedle(finalAnswer);
    }

    private void spinNeedle(String answer) {
        // Kullanıcı tekrar basamasın
        btnAsk.setEnabled(false);
        tvResult.setText("Ruhlar fısıldıyor...");

        // Dönüş Açısını Hesapla
        // En az 5 tam tur (360 * 5) + Rastgele duruş açısı
        int stopAngle = random.nextInt(360);
        int fullRotation = (360 * 5) + stopAngle;

        // Animasyon Ayarları
        RotateAnimation rotate = new RotateAnimation(
                0,              // Başlangıç açısı
                fullRotation,   // Bitiş açısı
                Animation.RELATIVE_TO_SELF, 0.5f, // X ekseninde merkeze göre
                Animation.RELATIVE_TO_SELF, 0.5f  // Y ekseninde merkeze göre
        );

        rotate.setDuration(3000); // 3 saniye dönsün (Mistik hava için yavaş)
        rotate.setFillAfter(true); // Durduğu yerde kalsın
        rotate.setInterpolator(new DecelerateInterpolator()); // Başta hızlı, dururken yavaşlasın

        // Animasyon Dinleyicisi
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Başlarken yapılacaklar (Gerekirse ses efekti buraya eklenir)
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Bittiğinde cevabı göster
                tvResult.setText(answer);
                btnAsk.setEnabled(true); // Butonu tekrar aç
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // SADECE İBREYİ DÖNDÜR
        imgNeedle.startAnimation(rotate);
    }
}