package com.example.medievalapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword, etPasswordConfirm;
    Button btnAction;
    TextView tvTabLogin, tvTabRegister;
    CheckBox cbRememberMe;
    DatabaseHelper db;

    boolean isLoginMode = true;
    boolean isPasswordVisible = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- DÜZELTME 1: OTOMATİK GİRİŞ KONTROLÜ (En Başta) ---
        SharedPreferences prefs = getSharedPreferences("MedievalPrefs", MODE_PRIVATE);
        boolean remember = prefs.getBoolean("remember", false);

        if (remember) {
            // Eğer "Beni Hatırla" seçiliyse, Login ekranını gösterme, direkt geç.
            Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish(); // Geri tuşuna basınca tekrar Login'e dönmesin diye aktiviteyi öldür.
            return; // Kodun geri kalanını çalıştırma.
        }

        // Eğer hatırlama yoksa normal ekranı yükle
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        // Görünümleri Bağla
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnAction = findViewById(R.id.btnAction);
        tvTabLogin = findViewById(R.id.tvTabLogin);
        tvTabRegister = findViewById(R.id.tvTabRegister);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        // Olayları Tanımla
        tvTabLogin.setOnClickListener(v -> switchMode(true));
        tvTabRegister.setOnClickListener(v -> switchMode(false));
        btnAction.setOnClickListener(v -> handleAction());

        // Şifre Göster/Gizle
        etPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });

        // Varsayılan olarak Giriş Modu
        switchMode(true);
    }

    // ... togglePasswordVisibility ve switchMode metodları aynı kalacak ...
    // (Yer kaplamaması için tekrar yazmıyorum, senin kodundakiyle aynı)

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isPasswordVisible = false;
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void switchMode(boolean login) {
        isLoginMode = login;
        etUsername.setText("");
        etPassword.setText("");
        etPasswordConfirm.setText("");

        if (login) {
            etPasswordConfirm.setVisibility(View.GONE);
            cbRememberMe.setVisibility(View.VISIBLE);
            btnAction.setText("Maceraya Başla");
            tvTabLogin.setTextColor(Color.parseColor("#3E2723"));
            tvTabRegister.setTextColor(Color.parseColor("#8D6E63"));
        } else {
            etPasswordConfirm.setVisibility(View.VISIBLE);
            cbRememberMe.setVisibility(View.GONE);
            btnAction.setText("Kaydı Tamamla");
            tvTabLogin.setTextColor(Color.parseColor("#8D6E63"));
            tvTabRegister.setTextColor(Color.parseColor("#3E2723"));
        }
    }

    private void handleAction() {
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Lütfen boş alanları doldurunuz.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoginMode) {
            boolean check = db.checkUser(user, pass);
            if (check) {
                SharedPreferences.Editor editor = getSharedPreferences("MedievalPrefs", MODE_PRIVATE).edit();

                // Aktif kullanıcıyı her zaman kaydet
                editor.putString("current_active_user", user);

                if (cbRememberMe.isChecked()) {
                    editor.putBoolean("remember", true);
                    editor.putString("username", user);
                    editor.putString("password", pass);
                } else {
                    // İşaretli değilse "hatırla" özelliğini temizle ama username kalabilir
                    editor.putBoolean("remember", false);
                }
                editor.apply();

                Toast.makeText(this, "Hoşgeldiniz, " + user + "!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Hatalı kimlik bilgisi!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Kayıt kodları aynı...
            String confirm = etPasswordConfirm.getText().toString().trim();
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Parolalar eşleşmiyor!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.checkUsernameExists(user)) {
                Toast.makeText(this, "Bu kullanıcı adı dolu!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.registerUser(user, pass)) {
                Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show();
                switchMode(true);
            } else {
                Toast.makeText(this, "Kayıt Hatası!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}