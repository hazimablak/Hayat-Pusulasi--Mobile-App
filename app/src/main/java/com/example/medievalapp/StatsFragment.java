package com.example.medievalapp;

import androidx.core.content.res.ResourcesCompat;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class StatsFragment extends Fragment {

    DatabaseHelper db;
    String currentUsername;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        db = new DatabaseHelper(getContext());
        
        // KULLANICI ADINI AL
        SharedPreferences prefsUser = getContext().getSharedPreferences("MedievalPrefs", Context.MODE_PRIVATE);
        currentUsername = prefsUser.getString("current_active_user", "guest");
        
        TextView tvTotal = view.findViewById(R.id.tvTotalCompleted);
        TextView tvStreak = view.findViewById(R.id.tvStreak);
        
        // Yeni UI Elemanları
        android.widget.ImageView imgFlame = view.findViewById(R.id.imgFlame);
        android.widget.ImageView imgMedal = view.findViewById(R.id.imgMedal);
        TextView tvMedalText = view.findViewById(R.id.tvMedalText);
        
        // Animation Targets
        View cardGoal = view.findViewById(R.id.cardGoal);
        View cardStreak = view.findViewById(R.id.cardStreak);
        LinearLayout layoutRecent = view.findViewById(R.id.layoutRecentQuests);
        
        // --- BAŞLANGIÇ ANİMASYONU ---
        // Kartlar ve Liste sırayla gelsin
        prepareViewForAnimation(cardGoal, 100);
        prepareViewForAnimation(cardStreak, 100);
        prepareViewForAnimation(layoutRecent, 200); // Listeyi de hazırla
        
        animateViewEntry(cardGoal, 200);
        animateViewEntry(cardStreak, 400);
        animateViewEntry(layoutRecent, 600); // En son liste gelsin

        LinearLayout containerLastQuests = view.findViewById(R.id.containerLastQuests);

        // --- İSTATİSTİK HESAPLAMA (USERNAME İLE) ---
        int completedCount = db.getCompletedQuestCount(currentUsername);
        int journalDays = db.getJournalCount(currentUsername);

        // 1. Streak Logic (Ay/Gün Dönüşümü + Alev)
        int months = journalDays / 30;
        int days = journalDays % 30;

        String streakText = "";
        if (months > 0) {
            streakText += months + " Ay ";
        }
        streakText += days + " Gün";

        tvStreak.setText("Günlük Serisi: " + streakText);

        // Alev Mantığı
        imgFlame.setVisibility(View.GONE);
        if (days >= 20 || (days >= 20 && months >= 0)) {
             if (months > 0 || days >= 20) {
                 imgFlame.setImageResource(R.drawable.ic_flame_red);
                 imgFlame.setVisibility(View.VISIBLE);
             } else if (days >= 10) {
                 imgFlame.setImageResource(R.drawable.ic_flame_yellow);
                 imgFlame.setVisibility(View.VISIBLE);
             }
        } else if (days >= 10) {
            imgFlame.setImageResource(R.drawable.ic_flame_yellow);
            imgFlame.setVisibility(View.VISIBLE);
        } else if (months > 0) {
            imgFlame.setImageResource(R.drawable.ic_flame_red);
            imgFlame.setVisibility(View.VISIBLE);
        }

        // 2. Hedef ve Madalya Mantığı
        tvTotal.setText("Tamamlanan Görevler: " + completedCount);
        
        // Madalya Emojisi Hesaplama
        TextView tvEmojiMedals = view.findViewById(R.id.tvEmojiMedals);
        int medalCount = completedCount / 10;
        StringBuilder medals = new StringBuilder();
        for (int i = 0; i < medalCount; i++) {
            medals.append("🥇 ");
        }
        tvEmojiMedals.setText(medals.toString());
        
        checkAndSetGoal(completedCount, imgMedal, tvMedalText);

        // Son 5 Görevi Yükle (USERNAME İLE)
        loadLast5Quests(containerLastQuests);

        // Listeye tıklanınca "Tümünü Göster" sayfasına git
        layoutRecent.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    // Smooth Transition: Fade In/Fade Out
                    .setCustomAnimations(
                        android.R.anim.fade_in, 
                        android.R.anim.fade_out, 
                        android.R.anim.fade_in, 
                        android.R.anim.fade_out
                    )
                    .replace(R.id.fragment_container, new CompletedQuestsFragment())
                    .addToBackStack(null) // Geri tuşuyla buraya dönebilsin diye
                    .commit();
        });

        return view;
    }

    private void prepareViewForAnimation(View view, float translationY) {
        view.setAlpha(0f);
        view.setTranslationY(translationY);
    }

    private void animateViewEntry(View view, long delay) {
        view.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();
    }

    private void checkAndSetGoal(int currentCompleted, View medalIcon, View medalText) {
        android.content.SharedPreferences prefs = getContext().getSharedPreferences("MedievalAppPrefs", android.content.Context.MODE_PRIVATE);
        // Hedef kullanıcıya özel olsun
        String targetKey = "monthly_quest_target_" + currentUsername;
        int target = prefs.getInt(targetKey, -1);

        if (target == -1) {
            // Hedef sorulmamış, sor
            showGoalDialog(prefs, targetKey);
        } else {
            // Hedef var, kontrol et
            if (currentCompleted >= target && target > 0) {
                medalIcon.setVisibility(View.VISIBLE);
                medalText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showGoalDialog(android.content.SharedPreferences prefs, String targetKey) {
        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Örn: 10");

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Yeni Bir Hedef Belirle!")
                .setMessage("Bu ay kaç görev tamamlamak istersin lordum?")
                .setView(input)
                .setPositiveButton("Kaydet", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        int goal = Integer.parseInt(val);
                        prefs.edit().putInt(targetKey, goal).apply();
                    }
                })
                .setCancelable(false) // Zorunlu
                .show();
    }

    private void loadLast5Quests(LinearLayout container) {
        // Hata kontrolü: Eğer veritabanı veya container hazır değilse işlem yapma
        if (getContext() == null || db == null) return;

        try {
            // USERNAME İLE SORGULA
            Cursor cursor = db.getLast5CompletedQuests(currentUsername);
            container.removeAllViews(); // Temizle

            if (cursor.getCount() == 0) {
                TextView emptyView = new TextView(getContext());
                emptyView.setText("- Henüz bir zafer kazanılmadı -");
                emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emptyView.setTextColor(getResources().getColor(R.color.ink_color));
                container.addView(emptyView);
            }

            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));

                TextView item = new TextView(getContext());
                item.setText("⚔ " + title);
                item.setTextColor(getResources().getColor(R.color.ink_color));
                item.setTextSize(18f);
                item.setPadding(0, 10, 0, 10);

                // --- GÜVENLİ FONT YÜKLEME YÖNTEMİ ---
                item.setTypeface(ResourcesCompat.getFont(getContext(), R.font.marck_script));

                container.addView(item);
            }
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}