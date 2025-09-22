package notification.listener.service;

import static notification.listener.service.NotificationConstants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import io.flutter.plugin.common.EventChannel.EventSink;

import java.util.HashMap;

public class NotificationReceiver extends BroadcastReceiver {

    private EventSink eventSink;

    public NotificationReceiver(EventSink eventSink) {
        this.eventSink = eventSink;
    }

    @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String keyName : extras.keySet()) {
                Log.d("NotificationReceiver", "extra " + keyName + " = " + extras.get(keyName));
            }
        }

        String packageName = intent.getStringExtra(PACKAGE_NAME);
        String title = intent.getStringExtra(NOTIFICATION_TITLE);
        String content = intent.getStringExtra(NOTIFICATION_CONTENT);
        byte[] notificationIcon = intent.getByteArrayExtra(NOTIFICATIONS_ICON);
        byte[] notificationExtrasPicture = intent.getByteArrayExtra(EXTRAS_PICTURE);
        byte[] largeIcon = intent.getByteArrayExtra(NOTIFICATIONS_LARGE_ICON);
        byte[] smallIcon = intent.getByteArrayExtra(NOTIFICATIONS_SMALL_ICON);
        boolean haveExtraPicture = intent.getBooleanExtra(HAVE_EXTRA_PICTURE, false);
        boolean hasRemoved = intent.getBooleanExtra(IS_REMOVED, false);
        boolean canReply = intent.getBooleanExtra(CAN_REPLY, false);
        int id = intent.getIntExtra(ID, -1);
        String key = intent.getStringExtra(KEY);
        long postTime = intent.getLongExtra(POST_TIME, -1);

        Log.d("NotificationCheck", "hash=" + this.hashCode() + "Received notification from package: " + packageName + " with id: " + id + " and key: " + key);

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("key", key);
        data.put("packageName", packageName);
        data.put("title", title);
        data.put("content", content);
        data.put("notificationIcon", notificationIcon);
        data.put("notificationExtrasPicture", notificationExtrasPicture);
        data.put("haveExtraPicture", haveExtraPicture);
        data.put("largeIcon", largeIcon);
        data.put("smallIcon", smallIcon);
        data.put("hasRemoved", hasRemoved);
        data.put("canReply", canReply);
        data.put("postTime", postTime);

        eventSink.success(data);
    }
}
