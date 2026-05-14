package com.example.medievalapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CompletedQuestsFragment extends Fragment {

    DatabaseHelper db;
    String currentUsername;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_quests, container, false);

        db = new DatabaseHelper(getContext());
        
        // KULLANICI ADINI AL
        SharedPreferences prefs = getContext().getSharedPreferences("MedievalPrefs", Context.MODE_PRIVATE);
        currentUsername = prefs.getString("current_active_user", "guest");

        RecyclerView rv = view.findViewById(R.id.rvAllCompleted);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // USERNAME İLE GETİR
        Cursor cursor = db.getAllCompletedQuests(currentUsername);
        
        rv.setAdapter(new CompletedAdapter(cursor));

        return view;
    }

    // --- İç Adapter (Sadece listelemek için) ---
    private class CompletedAdapter extends RecyclerView.Adapter<CompletedAdapter.ViewHolder> {
        Cursor cursor;
        CompletedAdapter(Cursor c) { this.cursor = c; }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quest, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            cursor.moveToPosition(position);
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));

            holder.tvTitle.setText(title);
            holder.tvNumber.setText((position + 1) + ".");

            // "Mühür" butonunu burada gizliyoruz çünkü görev zaten bitmiş
            holder.btnSeal.setVisibility(View.INVISIBLE);

            // Tamamlandığı belli olsun
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(0xFF888888);
        }

        @Override
        public int getItemCount() { return cursor.getCount(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvNumber;
            View btnSeal;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvQuestTitle);
                tvNumber = itemView.findViewById(R.id.tvQuestNumber);
                btnSeal = itemView.findViewById(R.id.btnCompleteQuest);
            }
        }
    }
}