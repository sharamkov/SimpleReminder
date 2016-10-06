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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import sharamkov.androidapp.simplereminder.ReminderEntity;
import sharamkov.androidapp.simplereminder.database.ReminderDatabaseHelper;
import sharamkov.androidapp.simplereminder.database.ReminderDatabaseManager;


public class AlarmRestorer extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        // Restore all alarms that have been canceled because of the device was turned off or rebooted
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            AlarmHelper.init(context);
            AlarmHelper alarmHelper = AlarmHelper.getInstance();


            ReminderDatabaseHelper.init(context);
            List<ReminderEntity> reminderEntities =
                    ReminderDatabaseManager.getInstance().getRemindersByCriterion(null);


            for (ReminderEntity reminderEntity : reminderEntities) {

                if (reminderEntity.getStatus() == ReminderEntity.STATUS_ACTIVE &&
                        reminderEntity.getState() == ReminderEntity.STATE_ENABLED) {

                    alarmHelper.prepareAlarm(reminderEntity, true);

                }

            }
        }


    }
}
