package com.example.medievalapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class LinedEditText extends AppCompatEditText {
    private final Rect mRect;
    private final Paint mPaint;

    // Ayarlar
    private static final int LINE_COLOR = Color.parseColor("#D7CCC8"); // Açık Gri/Mavi Çizgiler
    private static final int MARGIN_COLOR = Color.parseColor("#8D6E63"); // Kırmızı Kenar Çizgisi
    private static final int MARGIN_WIDTH = 60; // Kenar boşluğu (piksel)

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(LINE_COLOR);
        mPaint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Orijinal metni çizmek için super metodunu çağır
        // Ancak çizgilerin metnin arkasında kalması için önce çizgileri çizsem daha iyi olurdu
        // Ama EditText'in kendi çizimi background üstüne olduğu için, çizgileri "Background" olarak çizmek lazım
        // Veya çizgileri çizip sonra super.onDraw() çağıralım.

        int height = getHeight();
        int lineHeight = getLineHeight();
        
        // İlk satırın tabanından başla
        int baseline = getLineBounds(0, mRect); 
        // Eğer boşsa padding'i baz al
        if (getLineCount() == 0) {
            baseline = getPaddingTop() + lineHeight;
        }

        // Görünür alanın alt sınırına kadar çizgi çiz
        // Not: Çok uzun metinlerde performans için sadece görünen satırları çizmek daha iyidir
        // ama şimdilik tüm görünür alan kadar çizelim.
        
        // 1. Yatay Çizgiler (Satırlar)
        for (int i = baseline; i < height; i += lineHeight) {
            canvas.drawLine(mRect.left, i + 5, mRect.right, i + 5, mPaint);
        }
        
        // 2. Dikey Çizgi (Kenar Süsü - Margin)
        // Kaldırıldı.

        // Metni çiz
        super.onDraw(canvas);
    }
}
