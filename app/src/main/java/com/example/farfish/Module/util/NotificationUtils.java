package com.example.farfish.Module.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.farfish.Activities.MainActivity;
import com.example.farfish.Module.dataclasses.Message;
import com.example.farfish.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationUtils {


    private static final int FARFISH_NOTIFICATION_ID = 102;
    private static final String TAG = NotificationUtils.class.getSimpleName();
    private static final String FARFISH_NOTIFICATION = "farfish_notification";


    public static void notifyUserOfNewMessage(final Context context, Message newMessage) {

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

        notificationBuilder
                .setContentTitle(newMessage.getSenderName())
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_icon_round)
                .setAutoCancel(true);
        String messageItSelf = newMessage.getText();
        if (!messageItSelf.equals("")){
            notificationBuilder.setContentText(messageItSelf);
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



    public static void notifyUserOfNewMessages(final Context context, List<Message> newMessages) {

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

        notificationBuilder
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_icon_round)
                .setAutoCancel(true)
                .setContentIntent(contentIntent(context));
        //
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        List<Notification> notifications = new ArrayList<>();
        for (Message newMessage: newMessages) {
            notificationBuilder.setContentTitle(newMessage.getSenderName());
            String messageItSelf = newMessage.getText();
            if (!messageItSelf.equals("")){
                notificationBuilder.setContentText(messageItSelf);
            }else{
                notificationBuilder.setContentTitle(context.getResources().getString(R.string.new_photo_view_holder));
            }
            notifications.add(notificationBuilder.build());
        }
        int tracker = 0;
        for (Notification notification: notifications) {
            notificationManager.notify(tracker, notification);
            tracker++;
            Log.d(TAG, "user notified successfully");
        }


    }
    public static PendingIntent contentIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context,
                FARFISH_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT // using the same PendingIntent and update the older one in reusing it:)
        );
    }
}
