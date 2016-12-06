/*
 * Copyright 2016 Alexander Sharamkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package sharamkov.androidapp.simplereminder.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;


import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import sharamkov.androidapp.simplereminder.MainActivity;
import sharamkov.androidapp.simplereminder.R;
import sharamkov.androidapp.simplereminder.ReminderAdapter;
import sharamkov.androidapp.simplereminder.ReminderEntity;
import sharamkov.androidapp.simplereminder.database.ReminderDatabaseHelper;
import sharamkov.androidapp.simplereminder.database.ReminderDatabaseManager;


public class NotificationService extends Service {

    public final static String KEY = "key";
    public final static String TITLE = "title";
    public final static String REPEAT_INTERVAL = "repeatInterval";
    public final static String REPEAT_INTERVAL_NUMBER = "repeatIntervalNumber";
    public final static String DATE = "date";
    public final static String PRIORITY = "priority";
    public final static String SERVICE = "service";

    private Context context;
    private Intent intent;

    private Timer timer;
    private NotificationTimerTask notificationTimerTask;

    private SharedPreferences sharedPreferences;

    private Ringtone ringtone;
    private Uri ringtoneUri;

    private Vibrator vibrator;

    private boolean isVibration;
    private boolean isRingtone;

    private String repeatNumberKey, repeatIntervalKey;


    @Override
    public void onCreate() {
        context = getApplicationContext();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        timer = new Timer();

        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.intent = intent;
        sendNotification();

        // The repeatNumber is the number of notification ringtone and (or) vibration repeats the
        // user has set in the app preferences. The default value is -1 (infinite repeats)
        int repeatNumber = Integer.parseInt(sharedPreferences.getString(repeatNumberKey, "-1"));

        if ((isRingtone || isVibration) && repeatNumber != 0) {
            // The repeatInterval is the repeat interval between two subsequent notification signals.
            // The default value is 5 (five seconds).
            int repeatInterval = Integer.parseInt(sharedPreferences.getString(repeatIntervalKey, "5")) * 1000;
            startNotificationTimerTask(repeatNumber, repeatInterval);
        } else if (notificationTimerTask == null) {
            stopSelf();
        }


        return super.onStartCommand(intent, flags, startId);
    }


    private void startNotificationTimerTask(int repeatNumber, int repeatInterval) {

        if (notificationTimerTask != null) {



            if (ringtone != null) {
                ringtone.stop();
            }

            if (isVibration) {
                vibrator.cancel();
            }

            notificationTimerTask.cancel();


        }


        if (isRingtone) {
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            ringtone.setStreamType(AudioManager.STREAM_NOTIFICATION);
        }

        notificationTimerTask = new NotificationTimerTask(repeatNumber);
        timer.schedule(notificationTimerTask, repeatInterval, repeatInterval);

    }


    /*
    This task starts right after the notification is received.
    The task sequentially repeats the notification signal (ringtone and (or) vibration) until
    the user stops it by stopping the NotificationService or another notification is arrived.
    Also the task stops when the repeatNumber becomes zero.
    */
    private class NotificationTimerTask extends TimerTask {

        long repeatNumber;


        NotificationTimerTask(int repeatNumber) {
            this.repeatNumber = repeatNumber;
        }


        @Override
        public void run() {

            if (repeatNumber-- != 0) {


                if (isRingtone) {
                    ringtone.play();
                }

                if (isVibration) {
                    vibrator.vibrate(1500);
                }

            } else {
                cancel();
                stopSelf();
            }
        }
    }


    // This method creates and sends the notification
    private void sendNotification() {

        final long key = intent.getLongExtra(KEY, 0);
        String contentText = intent.getStringExtra(TITLE);
        long date = intent.getLongExtra(DATE, 0L);
        int repeatInterval = intent.getIntExtra(REPEAT_INTERVAL, ReminderEntity.NO_REPEATS);
        int repeatIntervalNumber = intent.getIntExtra(REPEAT_INTERVAL_NUMBER, 1);
        int priority = intent.getIntExtra(PRIORITY, ReminderEntity.PRIORITY_NORMAL);

        String ringtoneKey, vibrationKey;

        Bitmap largeIcon;


        switch (priority) {

            case ReminderEntity.PRIORITY_NORMAL:
            default:
                ringtoneKey = getString(R.string.pref_normal_priority_ringtone_key);
                vibrationKey = getString(R.string.pref_normal_priority_vibration_key);
                repeatNumberKey = getString(R.string.pref_normal_priority_repeat_number_key);
                repeatIntervalKey = getString(R.string.pref_normal_priority_repeat_interval_key);
                largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_alarm_green_48dp);
                break;

            case ReminderEntity.PRIORITY_HIGH:
                ringtoneKey = getString(R.string.pref_high_priority_ringtone_key);
                vibrationKey = getString(R.string.pref_high_priority_vibration_key);
                repeatNumberKey = getString(R.string.pref_high_priority_repeat_number_key);
                repeatIntervalKey = getString(R.string.pref_high_priority_repeat_interval_key);
                largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_alarm_red_48dp);
                break;

        }


        AlarmHelper.init(context);

        // Create an identifier to make a unique PendingIntent
        Uri identifier = Uri.parse(AlarmHelper.FAKE + String.valueOf(key));

        Intent resultIntent = new Intent(context, MainActivity.class);

        resultIntent.setData(identifier);
        resultIntent.putExtra(KEY, key);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent deleteIntent = new Intent(context, AlarmReceiver.class);
        deleteIntent.putExtra(SERVICE, true);
        deleteIntent.setData(identifier);


        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 1,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String uriString = sharedPreferences.getString(ringtoneKey, "content://settings/system/notification_sound");

        isRingtone = !"".equals(uriString);

        ringtoneUri = Uri.parse(uriString);

        builder.setSound(ringtoneUri);
        builder.setContentTitle("SimpleReminder");
        builder.setContentText(contentText);
        builder.setTicker(contentText);
        builder.setSmallIcon(R.drawable.ic_alarm_white_48dp);
        builder.setLargeIcon(largeIcon);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setDeleteIntent(deletePendingIntent);


        int defaultFlags = Notification.DEFAULT_LIGHTS;

        isVibration = vibrator.hasVibrator() && sharedPreferences.getBoolean(vibrationKey, true);

        if (!isVibration) {
            builder.setVibrate(null);
        } else {
            defaultFlags |= Notification.DEFAULT_VIBRATE;
        }

        builder.setDefaults(defaultFlags);


        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) key, builder.build());


        setRepeat(date, repeatInterval, repeatIntervalNumber, key, contentText, priority);


    }


    // This method sets the next alarm if necessary using the AlarmHelper.
    private void setRepeat(long date, int repeatInterval, int repeatIntervalNumber,
                           final long key, String title, int priority) {

        ReminderDatabaseHelper.init(context);

        if (repeatInterval != ReminderEntity.NO_REPEATS) {

            Calendar calendar = Calendar.getInstance();

            switch (repeatInterval) {

                case 5:
                    repeatInterval = Calendar.YEAR;
                    break;

                case 4:
                    repeatInterval = Calendar.MONTH;
                    break;

                case 3:
                    repeatInterval = Calendar.WEEK_OF_MONTH;
                    break;

                case 2:
                    repeatInterval = Calendar.DAY_OF_MONTH;
                    break;

                case 1:
                    repeatInterval = Calendar.HOUR_OF_DAY;
                    break;

                case 0:
                default:
                    repeatInterval = Calendar.MINUTE;
            }


            calendar.setTimeInMillis(date);
            calendar.add(repeatInterval, repeatIntervalNumber);


            final long nextDate = calendar.getTimeInMillis();

            ReminderDatabaseManager.getInstance().updateDate(key, nextDate);

            AlarmHelper.getInstance().setAlarm(new ReminderEntity(
                    key, title, nextDate,
                    priority, 0, 0, repeatInterval, repeatIntervalNumber
            ));


            final ReminderAdapter reminderAdapter = AlarmHelper.getInstance().getReminderAdapter();

            // If the app is opened set next date using setNextDate method.
            if (reminderAdapter != null) {

                Handler mainHandler = new Handler(context.getMainLooper());

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        reminderAdapter.setNextDate(key, nextDate);
                    }
                });

            }


        } else {

            ReminderDatabaseManager manager = ReminderDatabaseManager.getInstance();
            manager.updateStatus(key, ReminderEntity.STATUS_INACTIVE);

            final ReminderAdapter reminderAdapter = AlarmHelper.getInstance().getReminderAdapter();

            // If the app is opened deactivate the reminder using deactivateReminder method.
            if (reminderAdapter != null) {

                Handler mainHandler = new Handler(context.getMainLooper());

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        reminderAdapter.deactivateReminder(key);
                    }
                });

            }


        }


    }


    @Override
    public void onDestroy() {


        if (ringtone != null) {
            ringtone.stop();
        }

        if (isVibration) {
            vibrator.cancel();
        }

        if (timer != null) {
            timer.cancel();
        }

        AlarmReceiver.completeWakefulIntent(intent);
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
