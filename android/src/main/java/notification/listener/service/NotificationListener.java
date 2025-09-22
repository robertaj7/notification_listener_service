package notification.listener.service;

import static notification.listener.service.NotificationUtils.getBitmapFromDrawable;
import static notification.listener.service.models.ActionCache.cachedNotifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;

import notification.listener.service.models.Action;
import notification.listener.service.models.NotificationCache;


@SuppressLint("OverrideAbstract")
@RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {
    private static NotificationListener instance;

    public static NotificationListener getInstance() {
        return instance;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        instance = this;
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        Log.d("NotificationListener", "Notification Posted: " + notification.getPackageName() + " - " + notification.getId());
        handleNotification(notification, false);
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        handleNotification(sbn, true);
    }

    @RequiresApi(api = VERSION_CODES.KITKAT_WATCH)
    private void handleNotification(StatusBarNotification notification, boolean isRemoved) {
        String packageName = notification.getPackageName();
        Bundle extras = notification.getNotification().extras;
        byte[] appIcon = getAppIcon(packageName);
        byte[] largeIcon = null;
        byte[] smallIcon = null;
        Action action = NotificationUtils.getQuickReplyAction(notification.getNotification(), packageName);

        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            largeIcon = getNotificationLargeIcon(getApplicationContext(), notification.getNotification());
        }
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            smallIcon = getNotificationSmallIcon(getApplicationContext(), notification.getNotification());
        }

        Intent intent = new Intent(NotificationConstants.INTENT);
        intent.putExtra(NotificationConstants.PACKAGE_NAME, packageName);
        intent.putExtra(NotificationConstants.ID, notification.getId());
        intent.putExtra(NotificationConstants.KEY, notification.getKey());
        intent.putExtra(NotificationConstants.CAN_REPLY, action != null);
        intent.putExtra(NotificationConstants.POST_TIME, notification.getPostTime());

        if (NotificationUtils.getQuickReplyAction(notification.getNotification(), packageName) != null) {
            cachedNotifications.put(notification.getId(), action);
        }

        PendingIntent clickIntent = notification.getNotification().contentIntent;
        if (clickIntent != null) {
            NotificationCache.cachedIntents.put(notification.getId(), clickIntent);
        }

        intent.putExtra(NotificationConstants.NOTIFICATIONS_ICON, appIcon);
        intent.putExtra(NotificationConstants.NOTIFICATIONS_LARGE_ICON, largeIcon);
        intent.putExtra(NotificationConstants.NOTIFICATIONS_SMALL_ICON, smallIcon);

        if (extras != null) {
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

            intent.putExtra(NotificationConstants.NOTIFICATION_TITLE, title == null ? null : title.toString());
            intent.putExtra(NotificationConstants.NOTIFICATION_CONTENT, text == null ? null : text.toString());
            intent.putExtra(NotificationConstants.IS_REMOVED, isRemoved);
            intent.putExtra(NotificationConstants.HAVE_EXTRA_PICTURE, extras.containsKey(Notification.EXTRA_PICTURE));

            if (extras.containsKey(Notification.EXTRA_PICTURE)) {
                Bitmap bmp = (Bitmap) extras.get(Notification.EXTRA_PICTURE);
                if (bmp != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    intent.putExtra(NotificationConstants.EXTRAS_PICTURE, stream.toByteArray());
                } else {
                    Log.w("NotificationListener", "Notification.EXTRA_PICTURE exists but is null.");
                }
            }
        }

        Log.d("NotificationCheck", "Broadcasting intent for package: " + packageName + ", id: " + notification.getId() + ", key: " + notification.getKey() + ", extras: " + (extras != null));
        sendBroadcast(intent);
    }


    public byte[] getAppIcon(String packageName) {
        try {
            PackageManager manager = getBaseContext().getPackageManager();
            Drawable icon = manager.getApplicationIcon(packageName);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            getBitmapFromDrawable(icon).compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequiresApi(api = VERSION_CODES.M)
    private byte[] getNotificationLargeIcon(Context context, Notification notification) {
        try {
            Icon largeIcon = notification.getLargeIcon();
            if (largeIcon == null) {
                Log.d("NotificationListener", "Large icon is null");
                return null;
            }
            Drawable iconDrawable = largeIcon.loadDrawable(context);
            Bitmap iconBitmap = drawableToBitmap(iconDrawable);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            Log.d("NotificationListener", "Large Icon retrieved successfully");
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ERROR LARGE ICON", "getNotificationLargeIcon: " + e.getMessage());
            return null;
        }
    }

    @RequiresApi(api = VERSION_CODES.M)
    private byte[] getNotificationSmallIcon(Context context, Notification notification) {
        try {
            Icon smallIcon = notification.getSmallIcon();
            if (smallIcon == null) {
                Log.d("NotificationListener", "Small icon is null");
                return null;
            }
            Drawable iconDrawable = smallIcon.loadDrawable(context);
            Bitmap iconBitmap = drawableToBitmap(iconDrawable);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            Log.d("NotificationListener", "Small Icon retrieved successfully");
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR SMALL ICON", "getNotificationSmallIcon: " + e.getMessage());
            return null;
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 1;
        int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public List<Map<String, Object>> getActiveNotificationData() {
        List<Map<String, Object>> notificationList = new ArrayList<>();
        StatusBarNotification[] activeNotifications = getActiveNotifications();

        for (StatusBarNotification sbn : activeNotifications) {
            Map<String, Object> notifData = new HashMap<>();
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;

            notifData.put("id", sbn.getId());
            notifData.put("key", sbn.getKey());
            notifData.put("packageName", sbn.getPackageName());
            notifData.put("title", extras.getCharSequence(Notification.EXTRA_TITLE) != null
                    ? extras.getCharSequence(Notification.EXTRA_TITLE).toString()
                    : null);
            notifData.put("content", extras.getCharSequence(Notification.EXTRA_TEXT) != null
                    ? extras.getCharSequence(Notification.EXTRA_TEXT).toString()
                    : null);

            notificationList.add(notifData);
        }
        return notificationList;
    }

}
