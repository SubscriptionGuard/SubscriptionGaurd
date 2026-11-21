package com.tfmhntspn.subguard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tfmhntspn.subguard.databinding.ItemSelectConnectionBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConnectionSelectionAdapter extends
        RecyclerView.Adapter<ConnectionSelectionAdapter.ConnectionViewHolder> {

    public static class ConnectionOption {
        public final String userId;
        public final String displayName;

        public ConnectionOption(String userId, String displayName) {
            this.userId = userId;
            this.displayName = displayName;
        }
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(List<ConnectionOption> selectedConnections);
    }

    private final List<ConnectionOption> connections = new ArrayList<>();
    private final Set<String> selectedIds = new HashSet<>();

    private final OnSelectionChangedListener listener;

    public ConnectionSelectionAdapter(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void updateConnections(List<ConnectionOption> newConnections) {
        connections.clear();
        connections.addAll(newConnections);

        Set<String> validIds = new HashSet<>();

        for (ConnectionOption connection : connections) {
            validIds.add(connection.userId);
        }

        selectedIds.retainAll(validIds);

        notifyDataSetChanged();
        notifySelectionChanged();
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        ItemSelectConnectionBinding binding =
                ItemSelectConnectionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new ConnectionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ConnectionViewHolder holder,
            int position) {

        ConnectionOption connection = connections.get(position);

        holder.binding.tvConnectionName.setText(connection.displayName);

        // Remove old listener before changing checkbox state.
        holder.binding.cbSelectConnection.setOnCheckedChangeListener(null);

        holder.binding.cbSelectConnection.setChecked(
                selectedIds.contains(connection.userId)
        );

        holder.binding.cbSelectConnection.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {
                        selectedIds.add(connection.userId);
                    } else {
                        selectedIds.remove(connection.userId);
                    }

                    notifySelectionChanged();
                }
        );

        holder.binding.getRoot().setOnClickListener(v ->
                holder.binding.cbSelectConnection.setChecked(
                        !holder.binding.cbSelectConnection.isChecked()
                )
        );
    }

    @Override
    public int getItemCount() {
        return connections.size();
    }

    public List<ConnectionOption> getSelectedConnections() {
        List<ConnectionOption> selected = new ArrayList<>();

        for (ConnectionOption connection : connections) {
            if (selectedIds.contains(connection.userId)) {
                selected.add(connection);
            }
        }

        return selected;
    }

    private void notifySelectionChanged() {
        if (listener != null) {
            listener.onSelectionChanged(getSelectedConnections());
        }
    }

    static class ConnectionViewHolder extends RecyclerView.ViewHolder {

        final ItemSelectConnectionBinding binding;

        ConnectionViewHolder(ItemSelectConnectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}