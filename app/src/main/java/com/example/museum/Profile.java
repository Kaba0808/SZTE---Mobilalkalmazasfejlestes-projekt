package com.example.museum;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Profile extends Fragment {

    private FirebaseFirestore db;
    private EditText profileBirthDateEdit;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar beállítása
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Profil");
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white)); // A cím színének beállítása
        toolbar.setNavigationOnClickListener(v -> {
            // Menü gomb (drawer vagy más logika szerint)
            Toast.makeText(requireContext(), "Menü nyitása", Toast.LENGTH_SHORT).show();
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Firebase példányok inicializálása
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elemek
        TextView profileName = view.findViewById(R.id.profileName);
        TextView profileEmail = view.findViewById(R.id.profileEmail);
        TextView profilePhone = view.findViewById(R.id.profilePhone);
        TextView profileAddress = view.findViewById(R.id.profileAddress);
        TextView profileBirthDate = view.findViewById(R.id.profileBirthDate);
        TextView profileUsername = view.findViewById(R.id.profileUsername);

        // Szerkeszthető mezők
        EditText profilePhoneEdit = view.findViewById(R.id.profilePhoneEdit);
        EditText profileAddressEdit = view.findViewById(R.id.profileAddressEdit);
        profileBirthDateEdit = view.findViewById(R.id.profileBirthDateEdit);
        EditText profileUsernameEdit = view.findViewById(R.id.profileUsernameEdit);

        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        // Aktuális felhasználó
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Ellenőrizzük, hogy Google fiókkal jelentkezett-e be
            boolean isGoogle = user.getProviderData().stream().anyMatch(info ->
                    info.getProviderId().equals("google.com"));

            if (isGoogle) {
                // Google fiókhoz tartozó adatok megjelenítése
                profileName.setText("Név: " + user.getDisplayName());
                profileEmail.setText("Email: " + user.getEmail());

                // Kezdeti adatok (amíg nincsenek eltárolva)
                profilePhone.setText("Telefon: Nincs telefon");
                profileAddress.setText("Cím: Nincs cím");
                profileBirthDate.setText("Születési dátum: Nincs születési dátum");
                profileUsername.setText("Felhasználónév: Nincs felhasználónév");

                // Szerkeszthető mezők megjelenítése
                profilePhoneEdit.setVisibility(View.VISIBLE);
                profileAddressEdit.setVisibility(View.VISIBLE);
                profileUsernameEdit.setVisibility(View.VISIBLE);
                profileBirthDateEdit.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.VISIBLE);
                btnLogout.setVisibility(View.VISIBLE);

                // Dátumválasztó engedélyezése
                profileBirthDateEdit.setOnClickListener(v -> showDatePickerDialog());

                // Felhasználói adatok lekérése Firestore-ból
                db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // A dokumentumból lekért adatok
                        String phone = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");
                        String birthDate = documentSnapshot.getString("birthdate");
                        String username = documentSnapshot.getString("username");

                        // Képernyőn való megjelenítés
                        profilePhone.setText("Telefon: " + phone);
                        profileAddress.setText("Cím: " + address);
                        profileBirthDate.setText("Születési dátum: " + birthDate);
                        profileUsername.setText("Felhasználónév: " + username);

                        // Szerkeszthető mezők kitöltése
                        profilePhoneEdit.setText(phone);
                        profileAddressEdit.setText(address);
                        profileBirthDateEdit.setText(birthDate);
                        profileUsernameEdit.setText(username);

                        // Mentés gomb működtetése
                        btnSave.setOnClickListener(v -> {
                            String newPhone = profilePhoneEdit.getText().toString().trim();
                            String newAddress = profileAddressEdit.getText().toString().trim();
                            String newBirthDate = profileBirthDateEdit.getText().toString().trim();
                            String newUsername = profileUsernameEdit.getText().toString().trim();

                            // Adatok frissítése Firestore-ban
                            db.collection("users").document(user.getUid())
                                    .update("phone", newPhone, "address", newAddress, "birthdate", newBirthDate, "username", newUsername)
                                    .addOnSuccessListener(aVoid -> {
                                        // Frissített adatok megjelenítése
                                        Toast.makeText(getContext(), "Adatok frissítve!", Toast.LENGTH_SHORT).show();
                                        profilePhone.setText("Telefon: " + newPhone);
                                        profileAddress.setText("Cím: " + newAddress);
                                        profileBirthDate.setText("Születési dátum: " + newBirthDate);
                                        profileUsername.setText("Felhasználónév: " + newUsername);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Hiba történt az adatok frissítésekor", Toast.LENGTH_SHORT).show());
                        });

                    } else {
                        // Ha nincs adat a Firestore-ban, új felhasználóként kezeljük
                        profilePhoneEdit.setVisibility(View.VISIBLE);
                        profileAddressEdit.setVisibility(View.VISIBLE);
                        profileUsernameEdit.setVisibility(View.VISIBLE);
                        profileBirthDateEdit.setVisibility(View.VISIBLE);

                        // Adatok mentése a Firestore-ba
                        btnSave.setOnClickListener(v -> {
                            String newPhone = profilePhoneEdit.getText().toString().trim();
                            String newAddress = profileAddressEdit.getText().toString().trim();
                            String newBirthDate = profileBirthDateEdit.getText().toString().trim();
                            String newUsername = profileUsernameEdit.getText().toString().trim();

                            db.collection("users").document(user.getUid())
                                    .set(new User(user.getDisplayName(), user.getEmail(), newPhone, newAddress, newBirthDate, newUsername))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Adatok mentve!", Toast.LENGTH_SHORT).show();
                                        profilePhone.setText("Telefon: " + newPhone);
                                        profileAddress.setText("Cím: " + newAddress);
                                        profileBirthDate.setText("Születési dátum: " + newBirthDate);
                                        profileUsername.setText("Felhasználónév: " + newUsername);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Hiba történt az adatok mentésekor", Toast.LENGTH_SHORT).show());
                        });
                    }
                });

            } else {
                // Ha nem Google fiók, akkor e-mailes bejelentkezés esetén kezeljük
                String currentDisplayName = (user.getDisplayName() != null) ? user.getDisplayName() : "";

                profileName.setText("Név: " + currentDisplayName);
                profileEmail.setText("Email: " + user.getEmail());
                profilePhone.setVisibility(View.VISIBLE);
                profileAddressEdit.setVisibility(View.VISIBLE);
                profileBirthDateEdit.setVisibility(View.VISIBLE);
                profileUsernameEdit.setVisibility(View.VISIBLE);

                profileBirthDateEdit.setOnClickListener(v -> showDatePickerDialog());

                // E-mailes felhasználó adatai Firestore-ból
                db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String phone = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");
                        String birthDate = documentSnapshot.getString("birthdate");
                        String username = documentSnapshot.getString("username");

                        // Az adatok megjelenítése a profil képernyőn
                        profilePhone.setText("Telefon: " + phone);
                        profileAddress.setText("Cím: " + address);
                        profileBirthDate.setText("Születési dátum: " + birthDate);
                        profileUsername.setText("Felhasználónév: " + username);

                        profilePhoneEdit.setVisibility(View.VISIBLE);
                        profileAddressEdit.setVisibility(View.VISIBLE);
                        profileBirthDateEdit.setVisibility(View.VISIBLE);
                        profileUsernameEdit.setVisibility(View.VISIBLE);
                        // Szerkeszthető mezők kitöltése
                        profilePhoneEdit.setText(phone);
                        profileAddressEdit.setText(address);
                        profileBirthDateEdit.setText(birthDate);
                        profileUsernameEdit.setText(username);

                        // Mentés gomb működtetése
                        btnSave.setOnClickListener(v -> {
                            String newPhone = profilePhoneEdit.getText().toString().trim();
                            String newAddress = profileAddressEdit.getText().toString().trim();
                            String newBirthDate = profileBirthDateEdit.getText().toString().trim();
                            String newUsername = profileUsernameEdit.getText().toString().trim();

                            // Adatok frissítése Firestore-ban
                            db.collection("users").document(user.getUid())
                                    .update("phone", newPhone, "address", newAddress, "birthdate", newBirthDate, "username", newUsername)
                                    .addOnSuccessListener(aVoid -> {
                                        // Frissített adatok megjelenítése
                                        Toast.makeText(getContext(), "Adatok frissítve!", Toast.LENGTH_SHORT).show();
                                        profilePhone.setText("Telefon: " + newPhone);
                                        profileAddress.setText("Cím: " + newAddress);
                                        profileBirthDate.setText("Születési dátum: " + newBirthDate);
                                        profileUsername.setText("Felhasználónév: " + newUsername);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Hiba történt az adatok frissítésekor", Toast.LENGTH_SHORT).show());
                        });

                        btnSave.setVisibility(View.VISIBLE);
                        btnLogout.setVisibility(View.VISIBLE); // Ha szükséges, mutasd a kijelentkezés gombot
                    }
                }).addOnFailureListener(e -> {
                    // Hiba kezelése, ha nem található adat a Firestore-ban
                    Toast.makeText(getContext(), "Hiba történt az adatok betöltésekor", Toast.LENGTH_SHORT).show();
                });
            }

            // Kijelentkezés gomb
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                GoogleSignIn.getClient(getActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class)); // Irány a belépési oldal
                getActivity().finish();
            });

            // Regisztrációs gomb megjelenítése a Google fiókhoz
            btnRegister.setVisibility(View.GONE); // Hide if already registered
        }

        return view;
    }

    // Dátumválasztó dialógus
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, monthOfYear, dayOfMonth);
                    profileBirthDateEdit.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}