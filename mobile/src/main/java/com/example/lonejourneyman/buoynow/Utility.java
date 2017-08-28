package com.example.lonejourneyman.buoynow;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import com.example.lonejourneyman.buoynow.messaging.BuoyMessagingService;
import com.example.lonejourneyman.buoynow.widget.QuickAddService;

/**
 * Created by lonejourneyman on 8/26/17.
 */

public class Utility {

    public static Boolean getInitialAutoSave(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_autosave_key), true);
    }

    public static Boolean getSendNotification(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_notification_key), true);
    }

    public static String getListSorting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key), "0");
    }

    public static void sendNotification(Context context) {
        //Setting for Auto Messaging when Read
        int thisConversationId = 6;
        NotificationCompat.Builder autoBuilder;
        Intent msgHeardIntent = new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(BuoyMessagingService.READ_ACTION)
                .putExtra(BuoyMessagingService.CONVERSATION_ID, thisConversationId);
        PendingIntent msgHeardPendingIntent =
                PendingIntent.getBroadcast(context, thisConversationId,
                        msgHeardIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Setting for Auto Messaging when Reply
        Intent msgReplyIntent = new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(BuoyMessagingService.REPLY_ACTION)
                .putExtra(BuoyMessagingService.CONVERSATION_ID, thisConversationId);
        PendingIntent msgReplyPendingIntent =
                PendingIntent.getBroadcast(context, thisConversationId,
                        msgReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Auto Remote
        RemoteInput remoteInput = new RemoteInput.Builder(context.getString(R.string.reply_key))
                .setLabel(context.getString(R.string.reply_prompt))
                .build();
        //Auto Conversation
        String conversationName = MainActivity.AUTO_CONVERSATION_NAME;
        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConvBuild =
                new android.support.v4.app.NotificationCompat.CarExtender.UnreadConversation.Builder(
                        conversationName)
                        .setReadPendingIntent(msgHeardPendingIntent)
                        .setReplyAction(msgReplyPendingIntent, remoteInput);
        unreadConvBuild.addMessage(context.getString(R.string.notification_text))
                .setLatestTimestamp(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT > 25) {
            autoBuilder = new NotificationCompat.Builder(context,
                    NotificationChannel.DEFAULT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_list_item)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_text))
                    .setAutoCancel(true)
                    .extend(new android.support.v4.app.NotificationCompat.CarExtender()
                            .setUnreadConversation(unreadConvBuild.build()));
        } else {
            autoBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_list_item)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_text))
                    .setAutoCancel(true)
                    .extend(new android.support.v4.app.NotificationCompat.CarExtender()
                            .setUnreadConversation(unreadConvBuild.build()));
        }
        NotificationManagerCompat autoNotify =
                NotificationManagerCompat.from(context);
        autoNotify.notify(thisConversationId, autoBuilder.build());
    }

    public static void updateWidget(Context context) {
        Intent dataUpdatedIntent = new Intent(QuickAddService.ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}