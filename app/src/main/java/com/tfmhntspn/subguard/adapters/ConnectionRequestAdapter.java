package com.tfmhntspn.subguard.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tfmhntspn.subguard.databinding.ItemConnectionRequestBinding;
import com.tfmhntspn.subguard.models.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionRequestAdapter
        extends RecyclerView.Adapter<ConnectionRequestAdapter.ViewHolder> {

    public interface OnCancelClickListener {
        void onCancelClick(Connection connection);
    }

    private final List<Connection> requests = new ArrayList<>();
    private final Map<String, String> displayNames;
    private final OnCancelClickListener cancelListener;

    public ConnectionRequestAdapter(
            Map<String, String> displayNames,
            OnCancelClickListener cancelListener) {

        this.displayNames = displayNames;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        ItemConnectionRequestBinding binding =
                ItemConnectionRequestBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Connection connection = requests.get(position);

        // Look up the recipient's display name
        String name = displayNames.get(connection.getReceiverId());

        if (name == null || name.trim().isEmpty()) {
            name = "Unknown User";
        }

        holder.binding.tvConnectionName.setText(name);
        holder.binding.tvConnectionStatus.setText(
                connection.getStatus()
        );

        // Generate avatar initials
        String initials = getInitials(name);
        holder.binding.tvConnectionAvatar.setText(initials);

        // Pass the selected connection to the fragment
        holder.binding.btnCancelConnection.setOnClickListener(v ->
                cancelListener.onCancelClick(connection)
        );
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateRequests(List<Connection> newRequests) {
        requests.clear();
        requests.addAll(newRequests);
        notifyDataSetChanged();
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");

        if (parts.length == 0 || parts[0].isEmpty()) {
            return "?";
        }

        String initials = parts[0].substring(0, 1).toUpperCase();

        if (parts.length > 1) {
            initials += parts[parts.length - 1]
                    .substring(0, 1).toUpperCase();
        }

        return initials;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ItemConnectionRequestBinding binding;

        public ViewHolder(ItemConnectionRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
