# 🏰 Hayat Pusulası (The Chronicler)

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Language-Java-007396?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/Database-SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC%20%E2%86%92%20MVVM-blue?style=for-the-badge)

---

Hayat Pusulası, klasik “Yapılacaklar Listesi” ve ajanda uygulamalarının sıkıcılığını ortadan kaldıran; günlük görevleri, alışkanlık takibini ve karar alma süreçlerini **Ortaçağ RPG atmosferi** içinde sunan oyunlaştırılmış bir kişisel gelişim asistanıdır.

---

## ✨ Temel Özellikler

- 📜 **Mistik Günlük (Journal):** Özel çizilmiş parşömen arayüzü (`Custom View`) ve 3D sayfa çevirme animasyonları (`CameraDistance`, `ObjectAnimator`)
- ⚔️ **Şövalye Görevleri (Quests):** Zamanlayıcılı görev sistemi, tamamlanınca “mum mühür” animasyonu ile arşivleme
- 🏆 **Gamification Sistemi:** Altın ödüller, günlük streak takibi ve başarı animasyonları
- 🔮 **Kahinin Gözü (Oracle):** Kararsızlık durumları için rastgele karar çarkı (`DecelerateInterpolator`)
- 🔐 **Güvenli Parşömen:** SQLite tabanlı yerel veri yönetimi + `SharedPreferences` ile giriş hatırlama

---

## 🛠️ Teknolojiler & Mimari

- **Dil:** Java (Android SDK)
- **Veritabanı:** SQLite (`SQLiteOpenHelper`)
- **UI:** Custom XML, Vector Drawables, RecyclerView
- **Animasyonlar:** `ObjectAnimator`, `RotateAnimation`, `OvershootInterpolator`
- **Mimari:** MVC → MVVM + Clean Architecture (refactoring aşamasında)

---
