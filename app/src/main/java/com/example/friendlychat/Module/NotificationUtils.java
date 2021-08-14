package com.example.friendlychat.Module;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.friendlychat.Activities.UserContactsActivity;
import com.example.friendlychat.R;

import java.io.IOException;

public class NotificationUtils {


    private static final int FARFISH_NOTIFICATION_ID = 102;
    private static final String TAG = NotificationUtils.class.getSimpleName();
    private static final String FARFISH_NOTIFICATION = "farfish_notification";


    public static void notifyUserOfNewMessage(final Context context, FullMessage newFullMessage) {

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, FARFISH_NOTIFICATION);
        /* Build the URI for today's weather in order to show up to date data in notification */
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(FARFISH_NOTIFICATION,
                    context.getResources().getString(R.string.notification_utils_farfish),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        String messageContent = newFullMessage.getLastMessage().getText();
        notificationBuilder
                .setContentTitle(newFullMessage.getTargetUserName())
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ui_logo)
                .setAutoCancel(true);
        if (!messageContent.equals("")){
            notificationBuilder.setContentText(messageContent);
        }else{
            notificationBuilder.setContentTitle(context.getResources().getString(R.string.new_photo_view_holder));
                   /* .setLargeIcon(Picasso.get().load(newFullMessage.getLastMessage().getPhotoUrl()).get());*/ // will be done if Allah wills
        }
        notificationBuilder.setContentIntent(contentIntent(context));
        //
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(FARFISH_NOTIFICATION_ID, notificationBuilder.build());
        Log.d(TAG, "user notified successfully");


    }


    public static PendingIntent contentIntent(Context context) {
        Intent intent = new Intent(context, UserContactsActivity.class);
        return PendingIntent.getActivity(context,
                FARFISH_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT // using the same PendingIntent and update the older one in reusing it:)
        );
    }
}
