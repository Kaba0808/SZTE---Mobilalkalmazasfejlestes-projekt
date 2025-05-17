package com.example.museum;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.museum.CartItem;
import com.example.museum.CartManager;
import com.example.museum.Ticket;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {
    private final List<Ticket> allTickets; // teljes lista
    private List<Ticket> filteredTickets;  // szűrt lista

    public TicketAdapter(List<Ticket> tickets) {
        this.allTickets = new ArrayList<>(tickets);  // eredeti példány
        this.filteredTickets = new ArrayList<>(tickets); // kezdő állapotban teljes
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ticket ticket = filteredTickets.get(position);
        holder.name.setText(ticket.getType());
        holder.desc.setText(ticket.getDescription());
        holder.price.setText(ticket.getPrice() + " Ft");
        holder.quantity.setText("1");

        // Csökkentés gomb
        holder.decreaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(holder.quantity.getText().toString());
            if (currentQuantity > 1) {
                holder.quantity.setText(String.valueOf(currentQuantity - 1));
            }
        });

        // Növelés gomb
        holder.increaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(holder.quantity.getText().toString());
            holder.quantity.setText(String.valueOf(currentQuantity + 1));
        });

        // Kosárba hozzáadás gomb
        holder.btnAdd.setOnClickListener(v -> {
            String ticketType = ticket.getType();
            int unitPrice = ticket.getPrice();
            int quantity = Integer.parseInt(holder.quantity.getText().toString());

            // Létrehozzuk a CartItem-ot
            CartItem item = new CartItem(ticketType, ticket.getDescription(), unitPrice, quantity);

            // Kosár frissítése
            CartManager.addToCart(item);

            // Visszajelzés a felhasználónak
            Toast.makeText(holder.itemView.getContext(), "Hozzáadva a kosárhoz!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return filteredTickets.size();
    }

    public void filterList(String query) {
        Log.d("TICKET_FILTER", "Keresett szöveg: \"" + query + "\"");

        List<Ticket> filtered = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            filtered = new ArrayList<>(allTickets);
            Log.d("TICKET_FILTER", "Üres keresés -> Teljes lista betöltve: " + filtered.size() + " jegy");
        } else {
            String lowerQuery = query.toLowerCase();
            for (Ticket ticket : allTickets) {
                if (ticket.getType().toLowerCase().contains(lowerQuery)) {
                    filtered.add(ticket);
                }
            }
            Log.d("TICKET_FILTER", "Szűrt lista hossza: " + filtered.size());
        }

        filteredTickets = filtered;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, desc, price, quantity;
        Button btnAdd, decreaseButton, increaseButton;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.ticketType);
            desc = itemView.findViewById(R.id.ticketDescription);
            price = itemView.findViewById(R.id.ticketPrice);
            quantity = itemView.findViewById(R.id.quantityText);
            btnAdd = itemView.findViewById(R.id.addToCartButton);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
        }
    }
}
