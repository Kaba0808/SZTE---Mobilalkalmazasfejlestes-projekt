package com.example.museum;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText mFirstName, mLastName, mPhoneNumber, mAddress, mBirthDate, mUsername, mEmail, mPassword;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Teljes képernyő mód
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mFirstName = findViewById(R.id.etFirstName);
        mLastName = findViewById(R.id.etLastName);
        mPhoneNumber = findViewById(R.id.etPhoneNumber);
        mAddress = findViewById(R.id.etAddress);
        mBirthDate = findViewById(R.id.etBirthDate);
        mUsername = findViewById(R.id.etUsername);
        mEmail = findViewById(R.id.etEmail);
        mPassword = findViewById(R.id.etPassword);
        mRegisterBtn = findViewById(R.id.btnRegister);
        mLoginBtn = findViewById(R.id.tvLogin);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        // Dátumválasztó megnyitása
        mBirthDate.setOnClickListener(v -> showDatePickerDialog());

        mRegisterBtn.setOnClickListener(v -> {
            final String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            final String phoneNumber = mPhoneNumber.getText().toString();
            final String address = mAddress.getText().toString();
            final String birthDate = mBirthDate.getText().toString();
            final String username = mUsername.getText().toString();
            final String firstName = mFirstName.getText().toString();
            final String lastName = mLastName.getText().toString();
            final String userName = mUsername.getText().toString();

            // Input validation
            if (TextUtils.isEmpty(firstName)) {
                mFirstName.setError("Vezetéknév megadása kötelező!");
                return;
            }
            if (TextUtils.isEmpty(lastName)) {
                mLastName.setError("Keresztnév megadása kötelező!");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email-cím megadása kötelező!");
                return;
            }

            if(TextUtils.isEmpty(userName)){
                mUsername.setError("Felhasználónév megadása kötelező!");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                mPassword.setError("Jelszó megadása kötelező!");
                return;
            }

            // Jelszó ellenőrzés: legalább 8 karakter és legalább egy nagybetű
            if (password.length() < 8) {
                mPassword.setError("A jelszónak legalább 8 karakter hosszúnak kell lennie!");
                return;
            }

            String uppercasePattern = ".*[A-Z].*";
            if (!password.matches(uppercasePattern)) {
                mPassword.setError("A jelszónak tartalmaznia kell legalább egy nagybetűt!");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Register the user in Firebase
            fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // User successfully created, now saving additional data in Firestore

                    userID = fAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userID);

                    // Create a map to store user details
                    Map<String, Object> user = new HashMap<>();
                    user.put("firstName", firstName);
                    user.put("lastName", lastName);
                    user.put("username", username);
                    user.put("email", email);
                    user.put("phone", phoneNumber);
                    user.put("address", address);
                    user.put("birthdate", birthDate);

                    documentReference.set(user).addOnSuccessListener(aVoid -> {
                        Toast.makeText(RegisterActivity.this, "Felhasználó sikeresen létrehozva.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), BottomNavigation.class));
                    }).addOnFailureListener(e -> {
                        Toast.makeText(RegisterActivity.this, "Ez az email cím már regisztrálva van!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Ez az email cím már regisztrálva van!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        });

        mLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    // Method to show DatePickerDialog
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth1) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth1);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    mBirthDate.setText(dateFormat.format(selectedDate.getTime()));
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }
}

