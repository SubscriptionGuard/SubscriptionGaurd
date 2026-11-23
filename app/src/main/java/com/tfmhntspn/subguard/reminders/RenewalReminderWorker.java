
package com.tfmhntspn.subguard.reminders;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.tfmhntspn.subguard.models.Subscription;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RenewalReminderWorker extends Worker {

    private static final String CHANNEL_ID = "renewal_reminders";

    public RenewalReminderWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        String userId = getInputData().getString("user_id");
        String subscriptionId =
                getInputData().getString("subscription_id");
        String originalRenewalDate =
                getInputData().getString("original_renewal_date");
        String billingCycle =
                getInputData().getString("billing_cycle");

        if (userId == null || subscriptionId == null
                || originalRenewalDate == null
                || billingCycle == null) {
            return Result.failure();
        }

        // Only run reminders for the signed-in account
        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null
                || !currentUser.getUid().equals(userId)) {
            return Result.success();
        }

        // Get the latest subscription information
        DatabaseReference subscriptionRef =
                FirebaseDatabase.getInstance()
                        .getReference("subscriptions")
                        .child(userId)
                        .child(subscriptionId);

        Subscription subscription;

        try {
            DataSnapshot snapshot = Tasks.await(
                    subscriptionRef.get(),
                    20,
                    TimeUnit.SECONDS
            );

            if (!snapshot.exists()) {
                return Result.success();
            }

            subscription = snapshot.getValue(Subscription.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.retry();

        } catch (ExecutionException | TimeoutException e) {
            // Try again if Firebase is temporarily unavailable
            return Result.retry();
        }

        if (subscription == null
                || !subscription.isReminderEnabled()) {
            return Result.success();
        }

        // Ignore stale reminders after an edit
        if (!originalRenewalDate.equals(
                subscription.getNextRenewalDate())
                || !billingCycle.equalsIgnoreCase(
                subscription.getBillingCycle())) {
            return Result.success();
        }

        // Avoid posting if the work has been cancelled
        if (isStopped()) {
            return Result.success();
        }

        // Confirm the same account is still signed in
        FirebaseUser activeUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (activeUser == null
                || !activeUser.getUid().equals(userId)) {
            return Result.success();
        }

        String name = subscription.getName();
        String upcomingRenewal =
                getInputData().getString("renewal_date");

        if (name == null || name.trim().isEmpty()
                || upcomingRenewal == null) {
            return Result.failure();
        }

        createNotificationChannel();

        boolean hasPermission =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                        || ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED;

        if (hasPermission) {
            showNotification(name, upcomingRenewal, subscriptionId);
        }

        // Queue the next monthly or yearly reminder
        if (!isStopped()) {
            ReminderScheduler.scheduleNextReminder(
                    getApplicationContext(),
                    userId,
                    subscriptionId,
                    name,
                    subscription.getNextRenewalDate(),
                    subscription.getBillingCycle()
            );
        }

        return Result.success();
    }

    private void showNotification(
            String name,
            String renewalDate,
            String subscriptionId
    ) {

        String message = name + " renews on " + renewalDate;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        getApplicationContext(),
                        CHANNEL_ID
                )
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(name + " renewal reminder")
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        int notificationId =
                subscriptionId.hashCode() & 0x7fffffff;

        try {
            NotificationManagerCompat.from(getApplicationContext())
                    .notify(notificationId, builder.build());

        } catch (SecurityException ignored) {
            // Notification permission may have been revoked
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Subscription Renewal Reminders",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );

            channel.setDescription(
                    "Notifications about upcoming subscription renewals"
            );

            NotificationManager manager =
                    (NotificationManager) getApplicationContext()
                            .getSystemService(
                                    Context.NOTIFICATION_SERVICE
                            );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
