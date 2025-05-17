package com.example.museum;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore; // Az adatbázis referencia deklarálása itt
    private GoogleSignInClient mGoogleSignInClient;
    private EditText etUsernameOrEmail, etPassword;
    private Button btnLogin, btnGoogleSignIn, btnGuestLogin;
    private RadioGroup rgLoginMethod;
    private RadioButton rbEmail, rbUsername;
    private ProgressBar progressBar;

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Sikeres Google bejelentkezés", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, BottomNavigation.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    } else {
                        Toast.makeText(this, "Sikertelen Firebase hitelesítés Google-fiókkal", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google bejelentkezés sikertelen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance(); // Itt inicializáljuk az adatbázist

        etUsernameOrEmail = findViewById(R.id.etUsernameOrEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView mCreateBtn = findViewById(R.id.createText);
        btnGoogleSignIn = findViewById(R.id.btnGoogleLogin);
        rgLoginMethod = findViewById(R.id.rgLoginMethod);
        rbEmail = findViewById(R.id.rbEmail);
        rbUsername = findViewById(R.id.rbUsername);
        progressBar = findViewById(R.id.progressBar); // Hozzáadva a progressBar

        // Google Sign-In beállítása
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id)) // Firebase Console-ból
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> {
            String input = etUsernameOrEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Kérjük, töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgLoginMethod.getCheckedRadioButtonId();

            if (selectedId == rbEmail.getId()) {
                if (!isValidEmail(input)) {
                    Toast.makeText(this, "Kérjük, érvényes email címet adj meg!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgress(true);
                loginWithEmail(input, password);
            } else if (selectedId == rbUsername.getId()) {
                if (isValidEmail(input)) {
                    Toast.makeText(this, "Felhasználónév esetén ne adj meg email címet!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgress(true);
                loginWithUsername(input, password); // Későbbi implementáció
            } else {
                Toast.makeText(this, "Kérjük, válassz bejelentkezési módot!", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            // Kijelentkezés a Google-fiókból
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // Fiókválasztó indítása, ha sikeresen kijelentkezett
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
                showProgress(true);
            });
        });

        btnGuestLogin = findViewById(R.id.btnGuestLogin);

        btnGuestLogin.setOnClickListener(v -> {
            signInAsGuest();
            showProgress(true);
        });

        mCreateBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);  // Gombok letiltása
            btnGoogleSignIn.setEnabled(false);
            btnGuestLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);   // Gombok engedélyezése
            btnGoogleSignIn.setEnabled(true);
            btnGuestLogin.setEnabled(true);
        }
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Sikeres bejelentkezés", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, BottomNavigation.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    } else {
                        Toast.makeText(this, "Hibás email vagy jelszó!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginWithUsername(String username, String password) {
        // Show progress for up to 5 seconds
        showProgress(true);

        // Handler for timeout (5 seconds)
        Handler handler = new Handler();
        Runnable timeoutRunnable = () -> {
            // Ha 5 másodperc eltelt, állítsuk le a progressBar-t, és tájékoztassuk a felhasználót
            showProgress(false);
            Toast.makeText(LoginActivity.this, "A bejelentkezés túl hosszú ideig tartott. Próbáld újra.", Toast.LENGTH_SHORT).show();
        };

        // Start the timeout
        handler.postDelayed(timeoutRunnable, 5000); // 5000 ms = 5 seconds

        // A Firestore keresés
        fStore.collection("users")
                .whereEqualTo("username", username)  // Keresés a felhasználónév mezőre
                .get()
                .addOnCompleteListener(task -> {
                    // Ha a keresés sikeres
                    handler.removeCallbacks(timeoutRunnable); // Ha sikerült, töröljük a timeout-ot

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Ha találunk találatot
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String email = document.getString("email");

                        // Logolás a keresett dokumentumok
                        Log.d("Firestore", "Found user: " + username);
                        Log.d("Firestore", "User email: " + email);

                        // Az email alapján próbálunk bejelentkezni
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, authTask -> {
                                    showProgress(false);
                                    if (authTask.isSuccessful()) {
                                        // Sikeres bejelentkezés
                                        Toast.makeText(LoginActivity.this, "Sikeres bejelentkezés", Toast.LENGTH_SHORT).show();
                                        // Navigálás a fő képernyőre
                                        startActivity(new Intent(LoginActivity.this, BottomNavigation.class));
                                        finish();
                                    } else {
                                        // Hiba történt a bejelentkezés során
                                        Toast.makeText(LoginActivity.this, "Hibás jelszó!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Ha nem találunk ilyen felhasználót
                        Log.d("Firestore", "No user found for username: " + username);
                        Toast.makeText(LoginActivity.this, "Nem található ilyen felhasználónév. Kérlek, próbálj más módot vagy regisztrálj!", Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                })
                .addOnFailureListener(e -> {
                    handler.removeCallbacks(timeoutRunnable); // Ha hiba történik, szintén töröljük a timeout-ot
                    showProgress(false);
                    Log.e("Firestore", "Hiba történt a lekérdezés során: " + e.getMessage());
                    Toast.makeText(LoginActivity.this, "Hiba történt a lekérdezés során.", Toast.LENGTH_SHORT).show();
                });
    }

    private void signInAsGuest() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    showProgress(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Sikeres vendég bejelentkezés", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, BottomNavigation.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    } else {
                        Toast.makeText(this, "Vendég bejelentkezés sikertelen", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
