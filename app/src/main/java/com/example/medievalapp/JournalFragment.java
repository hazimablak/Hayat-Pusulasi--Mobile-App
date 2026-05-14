package com.example.medievalapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.Context; // EKLENDİ
import android.content.SharedPreferences; // EKLENDİ
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class JournalFragment extends Fragment {

    // Bileşenler
    private TextView tvDate;
    private LinedEditText etJournalContent;
    private ImageButton btnNextDay, btnPreviousDay, btnSaveJournal, btnCalendar;
    private FrameLayout layoutJournalCover;
    private RelativeLayout contentContainer;
    private View leftInnerCoverView;
    private View overlayShadow;

    // Değişkenler
    private Calendar currentDisplayDate;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Kurulum
        dbHelper = new DatabaseHelper(getContext());
        currentDisplayDate = Calendar.getInstance(); // Uygulama açılınca "Bugün" ile başlar

        // Bağlamalar
        tvDate = view.findViewById(R.id.tvDate);
        etJournalContent = view.findViewById(R.id.etJournalContent);
        btnNextDay = view.findViewById(R.id.btnNextDay);
        btnPreviousDay = view.findViewById(R.id.btnPreviousDay);
        btnSaveJournal = view.findViewById(R.id.btnSaveJournal);
        btnCalendar = view.findViewById(R.id.btnCalendar);

        layoutJournalCover = view.findViewById(R.id.layoutJournalCover);
        contentContainer = view.findViewById(R.id.contentContainer);
        leftInnerCoverView = view.findViewById(R.id.leftInnerCoverView);
        overlayShadow = view.findViewById(R.id.overlayShadow);

        loadJournalData();

        // Başlangıç Durumları
        btnNextDay.setVisibility(View.GONE);
        btnPreviousDay.setVisibility(View.GONE);
        leftInnerCoverView.setVisibility(View.INVISIBLE);

        // Olaylar
        layoutJournalCover.setOnClickListener(v -> openCinematicBookAnimation());

        btnNextDay.setOnClickListener(v -> animatePageTurn(true));
        btnPreviousDay.setOnClickListener(v -> animatePageTurn(false));
        btnCalendar.setOnClickListener(v -> showDatePickerDialog());

        btnSaveJournal.setOnClickListener(v -> {
            saveJournalEntry();
            Toast.makeText(getContext(), "Günlük Kaydedildi", Toast.LENGTH_SHORT).show();
        });
    }

    private void openCinematicBookAnimation() {
        layoutJournalCover.setPivotX(0f);
        layoutJournalCover.setPivotY(layoutJournalCover.getHeight() / 2f);
        float distance = 20000 * getResources().getDisplayMetrics().density;
        layoutJournalCover.setCameraDistance(distance);

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator coverRotation = ObjectAnimator.ofFloat(layoutJournalCover, "rotationY", 0f, -105f);
        ObjectAnimator shadowFade = ObjectAnimator.ofFloat(overlayShadow, "alpha", 1f, 0f);
        ObjectAnimator coverFade = ObjectAnimator.ofFloat(layoutJournalCover, "alpha", 1f, 0f);
        coverFade.setStartDelay(600);

        animatorSet.playTogether(coverRotation, shadowFade, coverFade);
        animatorSet.setDuration(1200);
        animatorSet.setInterpolator(new AccelerateInterpolator());

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                leftInnerCoverView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                layoutJournalCover.setVisibility(View.GONE);
                overlayShadow.setVisibility(View.GONE);
                updateButtonVisibility();
            }
        });

        animatorSet.start();
    }

    private void showDatePickerDialog() {
        int year = currentDisplayDate.get(Calendar.YEAR);
        int month = currentDisplayDate.get(Calendar.MONTH);
        int day = currentDisplayDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    saveJournalEntry();
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(selectedYear, selectedMonth, selectedDay);
                    boolean isNext = newDate.after(currentDisplayDate);
                    currentDisplayDate = newDate;
                    animatePageFlipForJump(isNext);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void animatePageFlipForJump(boolean isNext) {
        contentContainer.setPivotX(0f);
        contentContainer.setPivotY(contentContainer.getHeight() / 2f);
        float distance = 15000 * getResources().getDisplayMetrics().density;
        contentContainer.setCameraDistance(distance);

        float targetAngle = isNext ? -90f : 90f;
        float startAngle = isNext ? 90f : -90f;

        contentContainer.animate()
                .rotationY(targetAngle)
                .alpha(0.5f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    loadJournalData();
                    updateButtonVisibility();
                    contentContainer.setRotationY(startAngle);
                    contentContainer.animate()
                            .rotationY(0f)
                            .alpha(1f)
                            .setDuration(300)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                })
                .start();
    }

    private void animatePageTurn(boolean isNext) {
        saveJournalEntry();
        contentContainer.setPivotX(0f);
        contentContainer.setPivotY(contentContainer.getHeight() / 2f);
        float distance = 15000 * getResources().getDisplayMetrics().density;
        contentContainer.setCameraDistance(distance);

        float targetAngle = isNext ? -90f : 90f;
        float startAngle = isNext ? 90f : -90f;

        contentContainer.animate()
                .rotationY(targetAngle)
                .alpha(0.5f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    int amount = isNext ? 1 : -1;
                    currentDisplayDate.add(Calendar.DAY_OF_YEAR, amount);
                    loadJournalData();
                    updateButtonVisibility();
                    contentContainer.setRotationY(startAngle);
                    contentContainer.animate()
                            .rotationY(0f)
                            .alpha(1f)
                            .setDuration(300)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                })
                .start();
    }

    // --- GÜNCELLENEN METOD: Sadece aktif kullanıcının verisini çek ---
    private void loadJournalData() {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("tr", "TR"));
        tvDate.setText(sdf.format(currentDisplayDate.getTime()));

        // Aktif kullanıcıyı al
        SharedPreferences prefs = requireContext().getSharedPreferences("MedievalPrefs", Context.MODE_PRIVATE);
        String currentUser = prefs.getString("current_active_user", "");

        String dateKey = getDateStringForDB(currentDisplayDate);
        String content = dbHelper.getJournal(dateKey, currentUser); // username parametresi eklendi

        if (content != null) {
            etJournalContent.setText(content);
        } else {
            etJournalContent.setText("");
        }
    }

    // --- GÜNCELLENEN METOD: Aktif kullanıcı adına kaydet ---
    private void saveJournalEntry() {
        String content = etJournalContent.getText().toString().trim();

        // Aktif kullanıcıyı al
        SharedPreferences prefs = requireContext().getSharedPreferences("MedievalPrefs", Context.MODE_PRIVATE);
        String currentUser = prefs.getString("current_active_user", "");

        String dateKey = getDateStringForDB(currentDisplayDate);

        // Kullanıcı adı boş değilse kaydet
        if (!currentUser.isEmpty()) {
            dbHelper.saveJournal(dateKey, currentUser, content);
        }
    }

    private void updateButtonVisibility() {
        btnPreviousDay.setVisibility(View.VISIBLE);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar current = (Calendar) currentDisplayDate.clone();
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);

        if (current.compareTo(today) >= 0) {
            btnNextDay.setVisibility(View.GONE);
        } else {
            btnNextDay.setVisibility(View.VISIBLE);
        }
    }

    private String getDateStringForDB(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    @Override
    public void onPause() {
        super.onPause();
        saveJournalEntry();
    }
}