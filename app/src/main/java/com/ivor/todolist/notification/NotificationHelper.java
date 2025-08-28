package com.ivor.todolist.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.ivor.todolist.MainActivity;
import com.ivor.todolist.R;
import com.ivor.todolist.model.Todo;

import java.util.Date;

public class NotificationHelper {
    private static final String CHANNEL_ID = "todo_reminder_channel";
    private static final String CHANNEL_NAME = "Todo提醒";
    private static final String CHANNEL_DESCRIPTION = "Todo任务截止时间提醒";
    
    private Context context;
    private NotificationManager notificationManager;
    private AlarmManager alarmManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public void scheduleNotification(Todo todo) {
        if (todo.getDueDate() == null) {
            return;
        }
        
        // 提前15分钟提醒
        long reminderTime = todo.getDueDate().getTime() - (15 * 60 * 1000);
        
        // 如果提醒时间已经过了，就不设置提醒
        if (reminderTime <= System.currentTimeMillis()) {
            return;
        }
        
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("todo_id", todo.getId());
        intent.putExtra("todo_title", todo.getTitle());
        intent.putExtra("todo_description", todo.getDescription());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) todo.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }
    
    public void cancelNotification(long todoId) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) todoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
    
    public void showNotification(long todoId, String title, String description) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_schedule)
            .setContentTitle("Todo提醒: " + title)
            .setContentText(description != null && !description.isEmpty() ? description : "任务即将到期")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(description != null && !description.isEmpty() ? description : "任务即将到期，请及时处理。"));
        
        notificationManager.notify((int) todoId, builder.build());
    }
    
    public void updateNotification(Todo todo) {
        // 先取消旧的通知
        cancelNotification(todo.getId());
        
        // 如果任务未完成且有截止时间，重新设置通知
        if (!todo.isCompleted() && todo.getDueDate() != null) {
            scheduleNotification(todo);
        }
    }
}