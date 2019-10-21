package com.beemdevelopment.aegis.receivers;

import android.Manifest;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.beemdevelopment.aegis.helpers.NotificationHelper;
import com.beemdevelopment.aegis.helpers.PermissionHelper;

public abstract class BackupBroadcastReceiver extends BroadcastReceiver {

    protected boolean hasPermissions(Context context) {
        if (!PermissionHelper.granted(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Notification failedNotification = NotificationHelper.create(context,
                    "backup_fail_channel",
                    "Backup failed",
                    "Aegis has no read permission, please grant this permission",
                    NotificationCompat.PRIORITY_HIGH);

            NotificationHelper.show(context, failedNotification);

            return false;
        }

        if (!PermissionHelper.granted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Notification failedNotification = NotificationHelper.create(context,
                    "backup_fail_channel",
                    "Backup failed",
                    "Aegis has no write permission, please grant this permission",
                    NotificationCompat.PRIORITY_HIGH);

            NotificationHelper.show(context, failedNotification);

            return false;
        }

        return true;
    }

    protected boolean checkSettings(Context context) {
        
    }
}
