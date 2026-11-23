
package com.tfmhntspn.subguard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.models.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter
        extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    public interface OnSubscriptionClickListener {
        void onSubscriptionClick(Subscription subscription);
    }

    private final List<Subscription> subscriptions = new ArrayList<>();
    private final OnSubscriptionClickListener listener;

    public SubscriptionAdapter(OnSubscriptionClickListener listener) {
        this.listener = listener;
    }

    public void setSubscriptions(List<Subscription> updatedSubscriptions) {
        subscriptions.clear();
        subscriptions.addAll(updatedSubscriptions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription, parent, false);

        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull SubscriptionViewHolder holder, int position) {

        Subscription subscription = subscriptions.get(position);

        String name = subscription.getName() != null
                ? subscription.getName() : "";

        String badge = name.isEmpty()
                ? "?"
                : name.substring(0, 1).toUpperCase(Locale.ROOT);

        holder.serviceBadge.setText(badge);
        holder.serviceName.setText(name);

        String renewalDate = subscription.getNextRenewalDate();
        holder.renewalDate.setText(
                renewalDate == null || renewalDate.isEmpty()
                        ? "Renewal date unavailable"
                        : "Renews " + renewalDate
        );

        String currency = subscription.getCurrency() != null
                ? subscription.getCurrency() : "CAD";

        holder.cost.setText(String.format(
                Locale.CANADA,
                "$%.2f %s",
                subscription.getCost(),
                currency
        ));

        String cycle = subscription.getBillingCycle() != null
                ? subscription.getBillingCycle() : "Monthly";

        holder.billingCycle.setText(cycle);

        holder.itemView.setOnClickListener(v ->
                listener.onSubscriptionClick(subscription));
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }

    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {

        TextView serviceBadge;
        TextView serviceName;
        TextView renewalDate;
        TextView cost;
        TextView billingCycle;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceBadge = itemView.findViewById(R.id.tv_service_badge);
            serviceName = itemView.findViewById(R.id.tv_service_name);
            renewalDate = itemView.findViewById(R.id.tv_renewal_date);
            cost = itemView.findViewById(R.id.tv_subscription_cost);
            billingCycle = itemView.findViewById(R.id.tv_billing_cycle);
        }
    }
}
