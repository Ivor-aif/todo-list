package com.ivor.todolist.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        long todoId = intent.getLongExtra("todo_id", -1);
        String todoTitle = intent.getStringExtra("todo_title");
        String todoDescription = intent.getStringExtra("todo_description");
        
        if (todoId != -1 && todoTitle != null) {
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showNotification(todoId, todoTitle, todoDescription);
        }
    }
}