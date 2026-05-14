package com.example.medievalapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Chronicler.db";
    private static final int DB_VERSION = 5; // VERSİYON ATLADI (Tablo yapısı değişti)

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. GÜNLÜK TABLOSU (Artık kullanıcıya özel)
        db.execSQL("CREATE TABLE journal (date TEXT, username TEXT, content TEXT, PRIMARY KEY(date, username))");

        // 2. GÖREVLER TABLOSU (end_time eklendi)
        db.execSQL("CREATE TABLE quests (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, title TEXT, status INTEGER, completed_date INTEGER, end_time INTEGER)");

        // 3. KULLANICILAR TABLOSU
        db.execSQL("CREATE TABLE users (username TEXT PRIMARY KEY, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Basitçe tabloları düşürüp yeniden oluşturuyoruz (Veri kaybı olur ama geliştirme aşamasındayız)
        db.execSQL("DROP TABLE IF EXISTS journal");
        db.execSQL("DROP TABLE IF EXISTS quests");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ==========================================
    // --- GÜNLÜK (JOURNAL) İŞLEMLERİ ---
    // ==========================================

    public void saveJournal(String date, String username, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("username", username);
        values.put("content", content);

        // Çakışma olursa (Aynı tarih + Aynı kişi) üzerine yaz
        db.insertWithOnConflict("journal", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getJournal(String date, String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Sadece o kullanıcının o tarihteki yazısını getir
        Cursor cursor = db.rawQuery("SELECT content FROM journal WHERE date = ? AND username = ?", new String[]{date, username});

        if (cursor.moveToFirst()) {
            String content = cursor.getString(0);
            cursor.close();
            return content;
        }
        cursor.close();
        return "";
    }

    public int getJournalCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM journal WHERE username = ?", new String[]{username});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ==========================================
    // --- GÖREV (QUEST) İŞLEMLERİ ---
    // ==========================================

    public void addQuest(String title, String username, long endTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("title", title);
        values.put("status", 0);
        values.put("completed_date", 0);
        values.put("end_time", endTime);
        db.insert("quests", null, values);
    }

    public java.util.List<Quest> getActiveQuestList(String username) {
        java.util.List<Quest> questList = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM quests WHERE status = 0 AND username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                long endTime = 0;
                // Eski versiyondan geçişte sütun yoksa hata vermemesi için kontrol edilebilir ama burada recreate yapıyoruz
                try {
                     endTime = cursor.getLong(cursor.getColumnIndexOrThrow("end_time"));
                } catch (Exception e) { endTime = 0; }
                
                int status = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
                questList.add(new Quest(id, title, endTime, status));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return questList;
    }

    public Cursor getActiveQuests(String username) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM quests WHERE status = 0 AND username = ?", new String[]{username});
    }

    public void completeQuest(int id) {
        // Görev ID'si zaten benzersiz olduğu için burada username şart değil ama eklenebilir.
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", 1);
        values.put("completed_date", System.currentTimeMillis());
        db.update("quests", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public int getCompletedQuestCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM quests WHERE status = 1 AND username = ?", new String[]{username});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // --- İSTATİSTİK İÇİN ---

    public Cursor getLast5CompletedQuests(String username) {
        return this.getReadableDatabase().rawQuery(
                "SELECT * FROM quests WHERE status = 1 AND username = ? ORDER BY completed_date DESC LIMIT 5", new String[]{username});
    }

    public Cursor getAllCompletedQuests(String username) {
        return this.getReadableDatabase().rawQuery(
                "SELECT * FROM quests WHERE status = 1 AND username = ? ORDER BY completed_date DESC", new String[]{username});
    }

    // ==========================================
    // --- KULLANICI İŞLEMLERİ ---
    // ==========================================

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}