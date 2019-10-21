package com.beemdevelopment.aegis.receivers;

import android.content.Context;
import android.content.Intent;

public class EncryptedBackupBroadcastReceiver extends BackupBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!hasPermissions(context)) {
            return;
        }


    }
}
