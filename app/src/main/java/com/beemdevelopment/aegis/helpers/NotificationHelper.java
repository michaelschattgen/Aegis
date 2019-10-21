package com.beemdevelopment.aegis.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.beemdevelopment.aegis.R;

public class NotificationHelper {
    public static final String CODE_LOCK_STATUS_ID = "lock_status_channel";
    public static final String CODE_BACKUP_FAIL_ID = "backup_fail_channel";
    public static final String CODE_BACKUP_SUCCESS_ID = "backup_success_channel";

    public static void initChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel lockChannel = new NotificationChannel(CODE_LOCK_STATUS_ID, context.getString(R.string.channel_name_lock_status), NotificationManager.IMPORTANCE_LOW);
            lockChannel.setDescription(context.getString(R.string.channel_description_lock_status));

            NotificationChannel backupFailChannel = new NotificationChannel(CODE_BACKUP_FAIL_ID, "Backup failed", NotificationManager.IMPORTANCE_HIGH);
            backupFailChannel.setDescription("Aegis creates a notification when the backup broadcast results in a failure.");

            NotificationChannel backupSuccessChannel = new NotificationChannel(CODE_BACKUP_SUCCESS_ID, "Backup successful", NotificationManager.IMPORTANCE_LOW);
            backupSuccessChannel.setDescription("Aegis creates a notification when the backup broadcast results in a success.");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(lockChannel);
            notificationManager.createNotificationChannel(backupFailChannel);
            notificationManager.createNotificationChannel(backupSuccessChannel);
        }
    }

    public static Notification create(Context context, String channel, String content) {
        return create(context, channel, context.getString(R.string.app_name_full), content, NotificationCompat.PRIORITY_DEFAULT);
    }

    public static Notification create(Context context, String channel, String title, String content, int priority) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,  channel)
                .setSmallIcon(R.drawable.ic_fingerprint_black_24dp)
                .setContentTitle(title)
                .setContentText(content);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(priority);
        }

        builder.setChannelId(channel);

        return builder.build();
    }

    public static void show(Context context, Notification notification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
