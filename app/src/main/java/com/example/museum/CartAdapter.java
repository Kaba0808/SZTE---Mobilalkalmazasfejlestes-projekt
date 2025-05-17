package com.example.museum;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<CartItem> items;
    private final Runnable onCartChanged;

    public CartAdapter(List<CartItem> items, Runnable onCartChanged) {
        this.items = items;
        this.onCartChanged = onCartChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.type.setText(item.getTicketType());
        holder.price.setText(item.getUnitPrice() * item.getQuantity() + " Ft");
        holder.description.setText(item.getDescription());
        holder.quantity.setText(item.getQuantity() + " db");

        holder.remove.setOnClickListener(v -> {
            CartManager.removeFromCart(item);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
            onCartChanged.run(); // frissítjük az összesítőt is
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView type, description, price, quantity;
        Button remove;

        public ViewHolder(View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.ticketType);
            description = itemView.findViewById(R.id.ticketDescription);
            price = itemView.findViewById(R.id.ticketPrice);
            quantity = itemView.findViewById(R.id.quantityText);
            remove = itemView.findViewById(R.id.removeButton);
        }
    }
}
