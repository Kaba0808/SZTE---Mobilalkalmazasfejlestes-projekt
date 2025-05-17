package com.example.museum;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyTicketAdapter extends RecyclerView.Adapter<MyTicketAdapter.MyTicketViewHolder> {

    private final List<MyTicketItem> aggregatedTickets;
    private List<MyTicketItem> allTickets;

    public MyTicketAdapter(List<MyTicketItem> ticketList) {
        this.aggregatedTickets = ticketList;
        this.allTickets = new ArrayList<>(ticketList);
    }

    @NonNull
    @Override
    public MyTicketViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_ticket_item, parent, false);
        return new MyTicketViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyTicketViewHolder holder, int position) {
        MyTicketItem ticket = aggregatedTickets.get(position);

        int totalPrice = ticket.getPrice() * ticket.getQuantity();

        holder.typeTextView.setText(ticket.getType());
        holder.descriptionTextView.setText(ticket.getDescription());
        holder.quantityTextView.setText("Mennyiség: " + ticket.getQuantity());
        holder.totalPriceTextView.setText("Összesen: " + totalPrice + " Ft");
    }

    @Override
    public int getItemCount() {
        return aggregatedTickets.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        aggregatedTickets.clear();
        if (query == null || query.trim().isEmpty()) {
            aggregatedTickets.addAll(allTickets);
        } else {
            String lowerQuery = query.toLowerCase();
            for (MyTicketItem item : allTickets) {
                if ((item.getType() != null && item.getType().toLowerCase().contains(lowerQuery)) ||
                        (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery))) {
                    aggregatedTickets.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setAllTickets(List<MyTicketItem> allTickets) {
        this.allTickets = allTickets;
    }

    public static class MyTicketViewHolder extends RecyclerView.ViewHolder {

        TextView typeTextView, descriptionTextView, quantityTextView, totalPriceTextView;

        public MyTicketViewHolder(View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.ticketType);
            quantityTextView = itemView.findViewById(R.id.ticketQuantity);
            totalPriceTextView = itemView.findViewById(R.id.ticketTotalPrice);
            descriptionTextView = itemView.findViewById(R.id.ticketDescription);
        }
    }
}
