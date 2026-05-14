package com.example.medievalapp;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {

    public interface QuestCompletionListener {
        void onComplete(int position, int questId);
    }

    Context context;
    List<Quest> questList;
    DatabaseHelper db;
    QuestCompletionListener completionListener;

    public QuestAdapter(Context context, List<Quest> questList, DatabaseHelper db, QuestCompletionListener completionListener) {
        this.context = context;
        this.questList = questList;
        this.db = db;
        this.completionListener = completionListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, java.util.List<Object> payloads) {
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if ("TIMER_UPDATE".equals(payload)) {
                    updateTimerOnly(holder, position);
                }
            }
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Quest quest = questList.get(position);

        holder.tvNumber.setText((position + 1) + ".");
        holder.tvTitle.setText(quest.title);
        
        // Reset View State (Geri dönüşümde temiz gelsin)
        holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.ink_color));
        holder.itemView.setAlpha(1.0f);
        holder.itemView.setVisibility(View.VISIBLE);
        holder.btnSeal.setEnabled(true);
        holder.btnSeal.setScaleX(1f);
        holder.btnSeal.setScaleY(1f);

        updateTimerOnly(holder, position);

        holder.btnSeal.setOnClickListener(v -> {
            // Butona tekrar basılmasını engelle
            v.setEnabled(false);

            // Strikethrough Effect (Görsel Geri Bildirim)
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(0xFF888888);

            // Mühür Animasyonu (Ufak tıklama efekti kalabilir)
            v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }).start();

            // Biraz bekleyip silme işlemini tetikle (Native RecyclerView animasyonu kullanılacak)
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                     completionListener.onComplete(currentPos, quest.id);
                }
            }, 300); 
        });

        // UZUN BASINCA ZORLA SİL (FORCE DELETE)
        holder.btnSeal.setOnLongClickListener(v -> {
            android.widget.Toast.makeText(context, "Zorla Mühürlendi!", android.widget.Toast.LENGTH_SHORT).show();
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                 completionListener.onComplete(currentPos, quest.id);
            }
            return true;
        });
    }

    private void updateTimerOnly(ViewHolder holder, int position) {
        // Liste boyutu değişmiş olabilir, kontrol et
        if (position >= questList.size()) return;
        
        Quest quest = questList.get(position);
        long endTime = quest.endTime;

        if (endTime > 0) {
            holder.tvTimer.setVisibility(View.VISIBLE);
            long now = System.currentTimeMillis();
            long diff = endTime - now;

            if (diff > 0) {
                long days = diff / (24 * 60 * 60 * 1000);
                long hours = (diff / (60 * 60 * 1000)) % 24;
                long minutes = (diff / (60 * 1000)) % 60;
                long seconds = (diff / 1000) % 60;
                
                if (days > 0) {
                    holder.tvTimer.setText(String.format("Kalan: %dgn %dsa %ddk %dsn", days, hours, minutes, seconds));
                } else if (hours > 0) {
                     holder.tvTimer.setText(String.format("Kalan: %dsa %ddk %dsn", hours, minutes, seconds));
                } else {
                    holder.tvTimer.setText(String.format("Kalan: %ddk %dsn", minutes, seconds));
                }

                holder.tvTimer.setTextColor(ContextCompat.getColor(context, R.color.wax_red));
            } else {
                holder.tvTimer.setText("Süre Doldu!");
                holder.tvTimer.setTextColor(0xFF880000);
            }
        } else {
            holder.tvTimer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return questList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvNumber;
        TextView tvTimer;
        ImageButton btnSeal;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuestTitle);
            tvNumber = itemView.findViewById(R.id.tvQuestNumber);
            tvTimer = itemView.findViewById(R.id.tvTimer);
            btnSeal = itemView.findViewById(R.id.btnCompleteQuest);
        }
    }
}