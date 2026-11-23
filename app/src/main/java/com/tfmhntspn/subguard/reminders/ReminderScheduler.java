
package com.tfmhntspn.subguard.reminders;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    private static final int REMINDER_DAYS_BEFORE = 3;
    private static final int REMINDER_HOUR = 9;

    // Keep a separate reminder for each user's subscription
    private static String getWorkName(String userId, String subscriptionId) {
        return "renewal_" + userId + "_" + subscriptionId;
    }

    // Schedule a reminder when a subscription is added or edited
    public static void scheduleReminder(
            Context context,
            String userId,
            String subscriptionId,
            String name,
            String renewalDate,
            String billingCycle
    ) {
        scheduleReminderInternal(
                context,
                userId,
                subscriptionId,
                name,
                renewalDate,
                billingCycle,
                ExistingWorkPolicy.REPLACE
        );
    }

    // Queue the following reminder after the current one
    public static void scheduleNextReminder(
            Context context,
            String userId,
            String subscriptionId,
            String name,
            String originalRenewalDate,
            String billingCycle
    ) {
        scheduleReminderInternal(
                context,
                userId,
                subscriptionId,
                name,
                originalRenewalDate,
                billingCycle,
                ExistingWorkPolicy.APPEND_OR_REPLACE
        );
    }

    // Calculate the next reminder and register it with WorkManager
    private static void scheduleReminderInternal(
            Context context,
            String userId,
            String subscriptionId,
            String name,
            String renewalDate,
            String billingCycle,
            ExistingWorkPolicy policy
    ) {

        if (userId == null || userId.isEmpty()
                || subscriptionId == null || subscriptionId.isEmpty()) {
            return;
        }

        String workName = getWorkName(userId, subscriptionId);

        WorkManager workManager =
                WorkManager.getInstance(context.getApplicationContext());

        if (name == null || name.trim().isEmpty()
                || renewalDate == null
                || billingCycle == null
                || (!"Monthly".equalsIgnoreCase(billingCycle)
                && !"Yearly".equalsIgnoreCase(billingCycle))) {

            // Only the normal scheduling path should cancel old work
            if (policy == ExistingWorkPolicy.REPLACE) {
                workManager.cancelUniqueWork(workName);
            }
            return;
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        dateFormat.setLenient(false);

        Calendar originalRenewal = Calendar.getInstance();

        try {
            originalRenewal.setTime(dateFormat.parse(renewalDate));
        } catch (ParseException e) {
            if (policy == ExistingWorkPolicy.REPLACE) {
                workManager.cancelUniqueWork(workName);
            }
            return;
        }

        boolean yearly = "Yearly".equalsIgnoreCase(billingCycle);

        long now = System.currentTimeMillis();

        Calendar nextRenewal = null;
        Calendar nextReminder = null;

        // Find the next renewal with a future reminder date
        for (int cycle = 0; cycle < 1200; cycle++) {

            Calendar candidateRenewal =
                    (Calendar) originalRenewal.clone();

            if (yearly) {
                candidateRenewal.add(Calendar.YEAR, cycle);
            } else {
                candidateRenewal.add(Calendar.MONTH, cycle);
            }

            Calendar candidateReminder =
                    (Calendar) candidateRenewal.clone();

            candidateReminder.add(
                    Calendar.DAY_OF_MONTH,
                    -REMINDER_DAYS_BEFORE
            );

            candidateReminder.set(
                    Calendar.HOUR_OF_DAY, REMINDER_HOUR
            );
            candidateReminder.set(Calendar.MINUTE, 0);
            candidateReminder.set(Calendar.SECOND, 0);
            candidateReminder.set(Calendar.MILLISECOND, 0);

            if (candidateReminder.getTimeInMillis() > now) {
                nextRenewal = candidateRenewal;
                nextReminder = candidateReminder;
                break;
            }
        }

        if (nextReminder == null || nextRenewal == null) {
            return;
        }

        long delay = Math.max(
                0,
                nextReminder.getTimeInMillis() - now
        );

        String upcomingRenewal =
                dateFormat.format(nextRenewal.getTime());

        // Pass the details needed to display and repeat the reminder
        Data inputData = new Data.Builder()
                .putString("user_id", userId)
                .putString("subscription_id", subscriptionId)
                .putString("subscription_name", name)
                .putString("renewal_date", upcomingRenewal)
                .putString("original_renewal_date", renewalDate)
                .putString("billing_cycle", billingCycle)
                .build();

        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(
                        RenewalReminderWorker.class
                )
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .build();

        workManager.enqueueUniqueWork(
                workName,
                policy,
                request
        );
    }

    // Cancel a reminder when a subscription is deleted or disabled
    public static void cancelReminder(
            Context context,
            String userId,
            String subscriptionId
    ) {

        if (userId == null || subscriptionId == null) {
            return;
        }

        WorkManager.getInstance(
                context.getApplicationContext()
        ).cancelUniqueWork(
                getWorkName(userId, subscriptionId)
        );
    }
}
