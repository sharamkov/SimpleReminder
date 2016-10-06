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

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.util.Calendar;

import sharamkov.androidapp.simplereminder.ReminderAdapter;
import sharamkov.androidapp.simplereminder.ReminderEntity;
import sharamkov.androidapp.simplereminder.database.ReminderDatabaseManager;


public class AlarmHelper {


    private static final long MILLISECONDS_IN_A_MINUTE = 60000L;
    private static final long MILLISECONDS_IN_AN_HOUR = 3600000L;
    private static final long MILLISECONDS_IN_A_DAY = 86400000L;
    private static final long MILLISECONDS_IN_A_WEEK = 604800000L;


    public static final String FAKE = "fake://fake.fake/";


    private static final AlarmHelper instance = new AlarmHelper();

    private static Context context;
    private static AlarmManager alarmManager;

    private ReminderAdapter reminderAdapter;


    private AlarmHelper() {

    }


    public ReminderAdapter getReminderAdapter() {
        return reminderAdapter;
    }

    public void setReminderAdapter(ReminderAdapter reminderAdapter) {
        this.reminderAdapter = reminderAdapter;
    }


    public static AlarmHelper getInstance() {

        if (context == null) {
            throw new NullPointerException("Invoke " + AlarmHelper.class.getCanonicalName() +
                    ".init(Context) first");
        }
        return instance;
    }


    public static void init(Context context) {

        AlarmHelper.context = context;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    }


    @TargetApi(Build.VERSION_CODES.M)
    public void setAlarm(ReminderEntity reminderEntity) {

        Intent intent = new Intent(context, AlarmReceiver.class);

        intent.putExtra(NotificationService.KEY, reminderEntity.getKey());
        intent.putExtra(NotificationService.TITLE, reminderEntity.getTitle());
        intent.putExtra(NotificationService.DATE, reminderEntity.getDate());
        intent.putExtra(NotificationService.REPEAT_INTERVAL, reminderEntity.getRepeatInterval());
        intent.putExtra(NotificationService.REPEAT_INTERVAL_NUMBER, reminderEntity.getRepeatIntervalNumber());
        intent.putExtra(NotificationService.PRIORITY, reminderEntity.getPriority());


        // Create an identifier to make a unique PendingIntent
        Uri identifier = Uri.parse(FAKE + String.valueOf(reminderEntity.getKey()));
        intent.setData(identifier);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(AlarmHelper.context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);


        int currentVersion = Build.VERSION.SDK_INT;

        if (currentVersion >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderEntity.getDate(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderEntity.getDate(), pendingIntent);
        }

    }


    public void cancelAlarm(long key) {

        Intent intent = new Intent(context, AlarmReceiver.class);

        // Create an identifier to make a unique PendingIntent
        Uri identifier = Uri.parse(FAKE + String.valueOf(key));
        intent.setData(identifier);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);


    }


    // If the reminder date is overdue this method can calculate the nearest future date using the
    // repeat interval the user entered
    public void prepareAlarm(ReminderEntity reminderEntity, boolean updateDatabase) {

        long currentDate = System.currentTimeMillis();

        long date = reminderEntity.getDate();


        if (date <= currentDate && reminderEntity.getRepeatInterval() != ReminderEntity.NO_REPEATS) {

            long divider = 0L;

            int repeatInterval = reminderEntity.getRepeatInterval();
            int repeatIntervalNumber = reminderEntity.getRepeatIntervalNumber();


            switch (repeatInterval) {

                case 5:
                    repeatInterval = Calendar.YEAR;
                    break;
                case 4:
                    repeatInterval = Calendar.MONTH;
                    break;
                case 3:
                    repeatInterval = Calendar.WEEK_OF_MONTH;
                    divider = MILLISECONDS_IN_A_WEEK;
                    break;
                case 2:
                    repeatInterval = Calendar.DAY_OF_MONTH;
                    divider = MILLISECONDS_IN_A_DAY;
                    break;

                case 1:
                    repeatInterval = Calendar.HOUR_OF_DAY;
                    divider = MILLISECONDS_IN_AN_HOUR;
                    break;

                case 0:
                default:
                    repeatInterval = Calendar.MINUTE;
                    divider = MILLISECONDS_IN_A_MINUTE;

            }


            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(date);


            if (repeatInterval == Calendar.MONTH || repeatInterval == Calendar.YEAR) {

                do {
                    c.add(repeatInterval, repeatIntervalNumber);
                    date = c.getTimeInMillis();
                } while (date <= currentDate);


            } else {

                int quotient = (int) ((currentDate - date) / (repeatIntervalNumber * divider) + 1);

                c.add(repeatInterval, quotient * repeatIntervalNumber);
                date = c.getTimeInMillis();

            }


            reminderEntity.setDate(date);

            if (updateDatabase) {
                ReminderDatabaseManager.getInstance().updateDate(reminderEntity.getKey(), date);
            }

            setAlarm(reminderEntity);


        } else if (date > currentDate) {

            setAlarm(reminderEntity);
        }


    }


}
