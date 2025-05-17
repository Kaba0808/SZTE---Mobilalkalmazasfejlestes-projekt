package com.example.museum;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTickets extends Fragment {

    private RecyclerView recyclerView;
    private MyTicketAdapter adapter;
    private List<MyTicketItem> aggregatedTickets = new ArrayList<>();
    private TextView noTicketsMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_tickets, container, false);

        // UI elemek inicializálása
        recyclerView = view.findViewById(R.id.myTicketsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyTicketAdapter(aggregatedTickets);
        recyclerView.setAdapter(adapter);

        noTicketsMessage = view.findViewById(R.id.emptyMessage);  // Fontos: innen keressük ki

        // Toolbar beállítása
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Jegyeim");
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        // Menü inflálása és keresés beállítása
        toolbar.inflateMenu(R.menu.menu_search);

        toolbar.setOnMenuItemClickListener(item -> false);

        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Keresés jegytípus alapján");

        // 🎨 Színek beállítása (megtartva a saját ikont)
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.soft_cream));
        searchEditText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent));

        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
        }

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchAndAggregateTickets();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchAndAggregateTickets() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("purchasedTickets")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, MyTicketItem> aggregationMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String type = doc.getString("type");
                        String description = doc.getString("description");
                        long quantity = doc.getLong("quantity") != null ? doc.getLong("quantity") : 0;
                        long price = doc.getLong("price") != null ? doc.getLong("price") : 0;

                        if (aggregationMap.containsKey(type)) {
                            MyTicketItem existing = aggregationMap.get(type);
                            if (existing != null) {
                                existing.setQuantity(existing.getQuantity() + (int) quantity);
                            }
                        } else {
                            aggregationMap.put(type, new MyTicketItem(type, description, (int) price, (int) quantity));
                        }
                    }

                    aggregatedTickets.clear();
                    aggregatedTickets.addAll(aggregationMap.values());
                    adapter.setAllTickets(new ArrayList<>(aggregatedTickets)); // Frissítjük az adapter belső listáját
                    adapter.notifyDataSetChanged();

                    // Biztonságos ellenőrzés és üzenet megjelenítés
                    if (noTicketsMessage != null) {
                        noTicketsMessage.setVisibility(aggregatedTickets.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Hiba történt a jegyek lekérdezésekor.", Toast.LENGTH_SHORT).show();
                });
    }
}
