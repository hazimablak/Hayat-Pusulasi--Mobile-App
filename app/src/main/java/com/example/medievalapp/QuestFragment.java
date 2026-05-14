package com.example.medievalapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class QuestFragment extends Fragment {

    DatabaseHelper db;
    QuestAdapter adapter;
    RecyclerView recyclerView;
    android.os.Handler timerHandler = new android.os.Handler();
    String currentUsername;

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) {
                // Timer güncellemesi için payload gönder (Rebind yapma)
                adapter.notifyItemRangeChanged(0, adapter.getItemCount(), "TIMER_UPDATE");
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest, container, false);

        db = new DatabaseHelper(getContext());
        
        // KULLANICI ADINI AL
        SharedPreferences prefs = getContext().getSharedPreferences("MedievalPrefs", Context.MODE_PRIVATE);
        currentUsername = prefs.getString("current_active_user", "guest");

        recyclerView = view.findViewById(R.id.rvQuests);
        View btnAdd = view.findViewById(R.id.btnAddQuest);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadQuests();

        btnAdd.setOnClickListener(v -> {
            showAddQuestDialog();
        });

        return view;
    }

    private void showAddQuestDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        
        // Layout oluşturma (Programmatik XML yerine)
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etTitle = new EditText(getContext());
        etTitle.setHint("Görev Adı");
        layout.addView(etTitle);
        
        final TextView tvDate = new TextView(getContext());
        tvDate.setText("Tarih Seç (İsteğe Bağlı)");
        tvDate.setTextSize(16f);
        tvDate.setPadding(0, 30, 0, 30);
        tvDate.setTextColor(getResources().getColor(R.color.ink_color));
        layout.addView(tvDate);

        // Tarih seçimi için değişken
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        final long[] selectedTime = {0}; // 0 = Süresiz

        tvDate.setOnClickListener(v -> {
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                new android.app.TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(java.util.Calendar.MINUTE, minute);

                    long pickedTime = calendar.getTimeInMillis();
                    if (pickedTime < System.currentTimeMillis()) {
                        android.widget.Toast.makeText(getContext(), "Geçmiş bir zaman seçemezsiniz!", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        selectedTime[0] = pickedTime;
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM HH:mm", java.util.Locale.getDefault());
                        tvDate.setText("Bitiş: " + sdf.format(calendar.getTime()));
                    }

                }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show();
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH));
            
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        builder.setView(layout);
        builder.setTitle("Yeni Emir Ver");
        builder.setPositiveButton("Mühürle", (dialog, which) -> {
            String title = etTitle.getText().toString();
            if (!title.isEmpty()) {
                // USERNAME EKLENDİ
                db.addQuest(title, currentUsername, selectedTime[0]);
                loadQuests();
            }
        });
        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    java.util.List<Quest> activeQuests = new java.util.ArrayList<>();

    private void loadQuests() {
        timerHandler.removeCallbacks(timerRunnable);
        
        activeQuests.clear();
        // USERNAME EKLENDİ
        activeQuests.addAll(db.getActiveQuestList(currentUsername));
        
        if (adapter == null) {
            adapter = new QuestAdapter(getContext(), activeQuests, db, this::onQuestCompleted);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void onQuestCompleted(int position, int questId) {
        // ID'ye göre doğru indeksi bul (Eşzamanlı tıklamalarda pozisyon kayabilir)
        int index = -1;
        for (int i = 0; i < activeQuests.size(); i++) {
            if (activeQuests.get(i).id == questId) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            activeQuests.remove(index);
            
            // Native RecyclerView animasyonlarını tetikle
            adapter.notifyItemRemoved(index);
            // Geri kalanların numaralarını güncelle
            adapter.notifyItemRangeChanged(index, activeQuests.size());
            
            // Veritabanı işlemini yap
            db.completeQuest(questId);
            
            android.widget.Toast.makeText(getContext(), "Mühürlendi!", android.widget.Toast.LENGTH_SHORT).show();
        } else {
             loadQuests();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadQuests();
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }
}