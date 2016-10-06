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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


public class ReminderDatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String name = "SimpleReminderDatabase";
    private static final int version = 1;


    // A table to store reminders
    public static final String TABLE = "SimpleReminderTable";


    // The table columns
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_REPEAT_INTERVAL = "repeat_interval";
    public static final String COLUMN_REPEAT_INTERVAL_NUMBER = "repeat_interval_number";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_STATE = "state";


    // The COLUMN_SEARCH is a workaround to perform a case insensitive search
    // for non-ASCII characters
    public static final String COLUMN_SEARCH = "search";


    // A Script to create the table
    private static final String TABLE_CREATION_SCRIPT =
            "CREATE TABLE " + TABLE + "("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                    + COLUMN_KEY + " INTEGER, "
                    + COLUMN_TITLE + " TEXT NOT NULL, "
                    + COLUMN_DATE + " INTEGER, "
                    + COLUMN_REPEAT_INTERVAL + " INTEGER, "
                    + COLUMN_REPEAT_INTERVAL_NUMBER + " INTEGER, "
                    + COLUMN_PRIORITY + " INTEGER, "
                    + COLUMN_STATUS + " INTEGER, "
                    + COLUMN_STATE + " INTEGER, "
                    + COLUMN_SEARCH + " TEXT NOT NULL);";


    private static ReminderDatabaseHelper instance;


    private ReminderDatabaseHelper(Context context) {
        super(context, name, null, version);
    }


    public static void init(Context context) {
        if (instance == null) {
            instance = new ReminderDatabaseHelper(context);
        }
    }


    static ReminderDatabaseHelper getInstance() {

        if (instance == null) {
            throw new NullPointerException("Invoke " + ReminderDatabaseHelper.class.getCanonicalName()
                    + ".init(Context) first");
        }

        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATION_SCRIPT);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
