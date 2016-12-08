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


package sharamkov.androidapp.simplereminder.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import sharamkov.androidapp.simplereminder.ReminderEntity;


public class ReminderDatabaseManager {

    private static ReminderDatabaseManager instance;

    private final SQLiteDatabase writableDatabase;
    private final SQLiteDatabase readableDatabase;


    private ReminderDatabaseManager(SQLiteOpenHelper helper) {
        writableDatabase = helper.getWritableDatabase();
        readableDatabase = helper.getReadableDatabase();
    }


    public static ReminderDatabaseManager getInstance() {

        if (instance == null) {
            instance = new ReminderDatabaseManager(ReminderDatabaseHelper.getInstance());
        }

        return instance;

    }


    public int getRepeatIntervalByKey(long key) {

        int repeatInterval = 0;

        Cursor cursor = readableDatabase.query(ReminderDatabaseHelper.TABLE, new String[]{ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL},
        ReminderDatabaseHelper.COLUMN_KEY + "=" + key, null, null, null, null);

        if(cursor.moveToFirst()){
            int repeatIntervalColumnIndex = cursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL);
            repeatInterval = cursor.getInt(repeatIntervalColumnIndex);
        }

        return repeatInterval;
    }


    public void insertReminder(ReminderEntity reminderEntity) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(ReminderDatabaseHelper.COLUMN_KEY, reminderEntity.getKey());
        contentValues.put(ReminderDatabaseHelper.COLUMN_TITLE, reminderEntity.getTitle());
        contentValues.put(ReminderDatabaseHelper.COLUMN_DATE, reminderEntity.getDate());
        contentValues.put(ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL, reminderEntity.getRepeatInterval());
        contentValues.put(ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL_NUMBER, reminderEntity.getRepeatIntervalNumber());
        contentValues.put(ReminderDatabaseHelper.COLUMN_PRIORITY, reminderEntity.getPriority());
        contentValues.put(ReminderDatabaseHelper.COLUMN_STATUS, reminderEntity.getStatus());
        contentValues.put(ReminderDatabaseHelper.COLUMN_STATE, reminderEntity.getState());
        contentValues.put(ReminderDatabaseHelper.COLUMN_SEARCH, reminderEntity.getTitle().toLowerCase());

        writableDatabase.insert(ReminderDatabaseHelper.TABLE, null, contentValues);

    }


    public void updateReminder(ReminderEntity reminderEntity) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(ReminderDatabaseHelper.COLUMN_TITLE, reminderEntity.getTitle());
        contentValues.put(ReminderDatabaseHelper.COLUMN_DATE, reminderEntity.getDate());
        contentValues.put(ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL, reminderEntity.getRepeatInterval());
        contentValues.put(ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL_NUMBER, reminderEntity.getRepeatIntervalNumber());
        contentValues.put(ReminderDatabaseHelper.COLUMN_PRIORITY, reminderEntity.getPriority());
        contentValues.put(ReminderDatabaseHelper.COLUMN_STATUS, reminderEntity.getStatus());
        contentValues.put(ReminderDatabaseHelper.COLUMN_STATE, reminderEntity.getState());
        contentValues.put(ReminderDatabaseHelper.COLUMN_SEARCH, reminderEntity.getTitle().toLowerCase());

        long key = reminderEntity.getKey();

        writableDatabase.update(ReminderDatabaseHelper.TABLE, contentValues,
                ReminderDatabaseHelper.COLUMN_KEY + "=" + key, null);


    }


    public List<ReminderEntity> getRemindersByCriterion(String searchCriterion) {

        List<ReminderEntity> reminderEntities = new ArrayList<>();

        String selection;
        String[] selectionArgs;

        if (searchCriterion == null) {
            selection = null;
            selectionArgs = null;
        } else {
            // Prepare a selection and SelectionArgs for case insensitive search on the COLUMN_SEARCH column
            searchCriterion = searchCriterion.toLowerCase(Locale.ENGLISH);
            selection = ReminderDatabaseHelper.COLUMN_SEARCH + " LIKE ?";
            selectionArgs = new String[]{"%" + searchCriterion + "%"};
        }


        String[] columns = new String[]{
                ReminderDatabaseHelper.COLUMN_KEY,
                ReminderDatabaseHelper.COLUMN_TITLE,
                ReminderDatabaseHelper.COLUMN_DATE,
                ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL,
                ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL_NUMBER,
                ReminderDatabaseHelper.COLUMN_PRIORITY,
                ReminderDatabaseHelper.COLUMN_STATUS,
                ReminderDatabaseHelper.COLUMN_STATE
        };

        Cursor cursor = readableDatabase.query(ReminderDatabaseHelper.TABLE,
                columns, selection, selectionArgs, null, null, null);


        if (cursor.moveToFirst()) {

            int keyColumnIndex = cursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_KEY);
            int titleColumnIndex = cursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_TITLE);
            int dateColumnIndex = cursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_DATE);
            int priorityColumnIndex = cursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_PRIORITY);
            int statusColumnIndex = cursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_STATUS);
            int stateColumnIndex = cursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_STATE);
            int repeatIntervalColumnIndex = cursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL);
            int repeatIntervalNumberColumnIndex = cursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_REPEAT_INTERVAL_NUMBER);


            do {
                reminderEntities.add(

                        new ReminderEntity(
                                cursor.getLong(keyColumnIndex),
                                cursor.getString(titleColumnIndex),
                                cursor.getLong(dateColumnIndex),
                                cursor.getInt(priorityColumnIndex),
                                cursor.getInt(statusColumnIndex),
                                cursor.getInt(stateColumnIndex),
                                cursor.getInt(repeatIntervalColumnIndex),
                                cursor.getInt(repeatIntervalNumberColumnIndex)


                        )
                );

            } while (cursor.moveToNext());
        }

        cursor.close();

        return reminderEntities;

    }


    public void deleteReminder(long key) {

        writableDatabase.delete(ReminderDatabaseHelper.TABLE, ReminderDatabaseHelper.COLUMN_KEY
                + "=" + key, null);

    }


    public void updateState(long key, int state) {
        update(key, ReminderDatabaseHelper.COLUMN_STATE, state);
    }


    public void updateStatus(long key, int status) {
        update(key, ReminderDatabaseHelper.COLUMN_STATUS, status);
    }


    public void updateDate(long key, long date) {
        update(key, ReminderDatabaseHelper.COLUMN_DATE, date);
    }


    private void update(long key, String column, long newValue) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(column, newValue);

        writableDatabase.update(ReminderDatabaseHelper.TABLE, contentValues,
                ReminderDatabaseHelper.COLUMN_KEY + "=" + key, null);
    }


}
