package com.example.museum;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cart extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView cartTotal;
    private Button paymentButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false); // A fragment_cart.xml-hez tartozik
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar beállítása
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Kosár");
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white)); // A cím színének beállítása
        toolbar.setNavigationOnClickListener(v -> {
            // Menü gomb (drawer vagy más logika szerint)
            Toast.makeText(requireContext(), "Menü nyitása", Toast.LENGTH_SHORT).show();
        });

        recyclerView = view.findViewById(R.id.cartRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        cartTotal = view.findViewById(R.id.cartTotal);
        paymentButton = view.findViewById(R.id.paymentButton);

        adapter = new CartAdapter(CartManager.getCartItems(), this::updateCartTotal);
        recyclerView.setAdapter(adapter);

        updateCartTotal();

        paymentButton.setOnClickListener(v -> processPayment());
    }

    private void updateCartTotal() {
        int total = 0;
        List<CartItem> items = CartManager.getCartItems();
        for (CartItem item : items) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        cartTotal.setText("Összesen: " + total + " Ft");
        paymentButton.setEnabled(total > 0);
    }

    private void processPayment() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Ha a felhasználó be van jelentkezve
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Lekérjük a felhasználói adatokat, hogy ellenőrizzük a felhasználó típusát
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userType = documentSnapshot.getString("userType");

                            // Ha a felhasználó "vendeg" vagy "anynom", figyelmeztetjük a bejelentkezésre
                            if ("vendeg".equals(userType) || "anynom".equals(userType)) {
                                showLoginDialog();
                            } else {
                                // Ha nem anonim a felhasználó, folytathatja a fizetést
                                processPaymentToFirestore(db, user.getUid());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Hiba történt a felhasználói adatok lekérésekor.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Ha nincs bejelentkezve a felhasználó
            showLoginDialog();
        }
    }

    private void processPaymentToFirestore(FirebaseFirestore db, String uid) {
        // Iterálunk a kosárban lévő jegyeken és mentjük őket a Firestore-ba
        for (CartItem item : CartManager.getCartItems()) {
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("type", item.getTicketType());
            ticketData.put("price", item.getUnitPrice());
            ticketData.put("quantity", item.getQuantity());

            // Dokumentum hozzáadása a purchasedTickets kollekcióhoz
            db.collection("users")
                    .document(uid)
                    .collection("purchasedTickets")
                    .add(ticketData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(requireContext(), "Fizetés sikeres!", Toast.LENGTH_SHORT).show();
                        // Kosár kiürítése a mentés után
                        CartManager.clearCart();
                        updateCartTotal(); // Frissítjük a kosár összesítőt
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Fizetés hiba", "Hiba történt a fizetés során", e);
                        Toast.makeText(requireContext(), "Hiba történt a fizetés során: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Bejelentkezés szükséges")
                .setMessage("A vásárláshoz be kell jelentkezned. Kérlek jelentkezz be, hogy folytathasd a vásárlást.")
                .setPositiveButton("Bejelentkezés", (dialog, which) -> {
                    // A bejelentkezési képernyőre irányítjuk a felhasználót
                    // Nyisd meg a bejelentkezési aktivitást
                    // Intent intent = new Intent(requireContext(), LoginActivity.class);
                    // startActivity(intent);
                    // (Itt jelezheted, hogy valós bejelentkezést hajtsunk végre.)
                })
                .setNegativeButton("Mégse", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Értesítjük az adaptert, hogy frissültek az adatok
        adapter.notifyDataSetChanged();
        updateCartTotal();
    }
}
