package notification.listener.service.models;

import android.app.PendingIntent;

import java.util.HashMap;

abstract public class NotificationCache {
    public static final HashMap<Integer, PendingIntent> cachedIntents = new HashMap<>();
}
