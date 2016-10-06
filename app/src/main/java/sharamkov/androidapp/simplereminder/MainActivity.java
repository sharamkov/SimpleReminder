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


package sharamkov.androidapp.simplereminder;


import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;
import java.util.Locale;

import sharamkov.androidapp.simplereminder.database.ReminderDatabaseHelper;
import sharamkov.androidapp.simplereminder.database.ReminderDatabaseManager;
import sharamkov.androidapp.simplereminder.dialog.AddReminderDialog;
import sharamkov.androidapp.simplereminder.dialog.EditReminderDialog;
import sharamkov.androidapp.simplereminder.dialog.RemoveReminderDialog;
import sharamkov.androidapp.simplereminder.notification.AlarmHelper;
import sharamkov.androidapp.simplereminder.notification.NotificationService;


public class MainActivity extends AppCompatActivity implements AddReminderDialog.OnReminderAddedListener,
        RemoveReminderDialog.OnReminderRemoveListener, ReminderAdapter.OnLocationChangeListener,
        EditReminderDialog.OnReminderUpdateListener, ReminderAdapter.OnSearchListener {


    private RecyclerView.LayoutManager layoutManager;
    private ReminderAdapter reminderAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // When the user starts the application stop NotificationService if it's running
        stopService(new Intent(getApplicationContext(), NotificationService.class));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        setSupportActionBar(toolbar);

        // RecyclerView is used as a reminder list
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        reminderAdapter = new ReminderAdapter(this);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setAdapter(reminderAdapter);
        recyclerView.setLayoutManager(layoutManager);

        // FAB is used to open AddReminderDialog to add a new reminder into the RecyclerView
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AddReminderDialog addReminderDialog = new AddReminderDialog();
                addReminderDialog.show(getFragmentManager(), "AddReminderDialogFragment");
            }
        });

        // SearchView is used to search reminders in the RecyclerView according to the user's search criterion
        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchCriterion) {

                reminderAdapter.searchReminderEntities(searchCriterion);

                return false;
            }
        });


        ReminderDatabaseHelper.init(getApplicationContext());
        getRemindersFromDatabase(null);

        AlarmHelper.init(getApplicationContext());
        AlarmHelper.getInstance().setReminderAdapter(reminderAdapter);


        // Set default locale to Locale.ENGLISH to make sure
        // com.wdullaer.materialdatetimepicker.date.DatePickerDialog and
        // com.wdullaer.materialdatetimepicker.time.TimePikerDialog are displayed in English lang.
        if (!"ru".equals(Locale.getDefault().getLanguage())) {
            Locale.setDefault(Locale.ENGLISH);
        }

        // If the notification is clicked scroll to the reminder using the reminder key
        long key = getIntent().getLongExtra(NotificationService.KEY, 0L);
        if (key != 0L) {
            reminderAdapter.scrollToLocation(key);
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem preferences = menu.add(R.string.menu_item_preferences);
        preferences.setIntent(new Intent(this, ReminderPreferencesActivity.class));

        return super.onCreateOptionsMenu(menu);
    }


    // This method works when the user clicks the notification and the app is already opened
    @Override
    protected void onNewIntent(Intent intent) {

        stopService(new Intent(getApplicationContext(), NotificationService.class));

        long key = getIntent().getLongExtra(NotificationService.KEY, 0L);
        if (key != 0L) {
            reminderAdapter.scrollToLocation(key);
        }

        super.onNewIntent(intent);
    }


    @Override
    public void onReminderAdded(ReminderEntity newReminder, boolean saveToDatabase, boolean isNotify) {

        // Check disabled reminders without repeats and inactivate overdue ones
        if (newReminder.getState() == ReminderEntity.STATE_DISABLED
                && newReminder.getRepeatInterval() == ReminderEntity.NO_REPEATS) {

            if (newReminder.getDate() <= System.currentTimeMillis()) {

                newReminder.setStatus(ReminderEntity.STATUS_INACTIVE);
                // All inactive reminders have the enabled state:
                newReminder.setState(ReminderEntity.STATE_ENABLED);

                ReminderDatabaseManager.getInstance().updateStatus(
                        newReminder.getKey(), newReminder.getStatus()
                );
                ReminderDatabaseManager.getInstance().updateState(
                        newReminder.getKey(), newReminder.getState()
                );
            }

        }

        // Add the reminder to the reminder list (RecyclerView) according to the reminder location
        int location = getLocation(newReminder);
        reminderAdapter.addReminderEntity(location, newReminder, isNotify);

        // Save the reminder to the database if it's just be added using AddReminderDialog
        if (saveToDatabase) {
            ReminderDatabaseManager.getInstance().insertReminder(newReminder);
        }

    }


    /*
     This method defines the reminder location in the reminder list (RecyclerView) according to the following logic:
     first, active and enabled reminders according to their date in ascending order, then active but disabled reminders
     according to their date in ascending order, then inactive reminders according to their date in ascending order
     */
    public int getLocation(ReminderEntity newReminder) {

        int location = reminderAdapter.getItemCount();

        switch (newReminder.getStatus()) {


            case ReminderEntity.STATUS_ACTIVE:


                if (newReminder.getState() == ReminderEntity.STATE_ENABLED) {

                    long newReminderDate = newReminder.getDate();

                    for (int i = 0; i < reminderAdapter.getItemCount(); i++) {

                        ReminderEntity reminderEntity = reminderAdapter.getReminderEntity(i);

                        if (reminderEntity.getStatus() == ReminderEntity.STATUS_INACTIVE
                                || reminderEntity.getState() == ReminderEntity.STATE_DISABLED
                                || newReminderDate <= reminderEntity.getDate()) {

                            location = i;
                            break;
                        }

                    }

                } else {

                    long newReminderDate = newReminder.getDate();

                    for (int i = 0; i < reminderAdapter.getItemCount(); i++) {

                        ReminderEntity reminderEntity = reminderAdapter.getReminderEntity(i);

                        if (reminderEntity.getStatus() == ReminderEntity.STATUS_INACTIVE
                                || reminderEntity.getState() == ReminderEntity.STATE_DISABLED
                                && newReminderDate <= reminderEntity.getDate()) {

                            location = i;
                            break;
                        }

                    }
                }

                break;


            case ReminderEntity.STATUS_INACTIVE:

                long newReminderDate = newReminder.getDate();

                for (int i = 0; i < reminderAdapter.getItemCount(); i++) {

                    ReminderEntity reminderEntity = reminderAdapter.getReminderEntity(i);

                    if (reminderEntity.getStatus() == ReminderEntity.STATUS_INACTIVE
                            && newReminderDate <= reminderEntity.getDate()) {

                        location = i;
                        break;
                    }

                }

                break;

        }

        return location;

    }


    @Override
    public void onLocationChange(int newLocation) {

        layoutManager.scrollToPosition(newLocation);
    }


    private void getRemindersFromDatabase(String searchCriterion) {

        // Get all reminders from the database if the searchCriterion is null
        // otherwise get reminders according to the searchCriterion
        List<ReminderEntity> reminderEntities =
                ReminderDatabaseManager.getInstance().getRemindersByCriterion(searchCriterion);

        for (ReminderEntity reminderEntity : reminderEntities) {
            onReminderAdded(reminderEntity, false, false);
        }


    }


    @Override
    public void onReminderRemove(int location) {

        ReminderEntity reminderEntity = reminderAdapter.removeReminderEntity(location);
        ReminderDatabaseManager.getInstance().deleteReminder(reminderEntity.getKey());

        if (reminderEntity.getStatus() == ReminderEntity.STATUS_ACTIVE && reminderEntity.getState() == ReminderEntity.STATE_ENABLED) {
            AlarmHelper.getInstance().cancelAlarm(reminderEntity.getKey());
        }

    }


    @Override
    public void onReminderUpdate(ReminderEntity reminderEntity) {

        for (int location = 0; location < reminderAdapter.getItemCount(); location++) {

            if (reminderEntity.getKey() == reminderAdapter.getReminderEntity(location).getKey()) {

                reminderAdapter.updateReminderEntity(reminderEntity, location);

            }

        }

        ReminderDatabaseManager.getInstance().updateReminder(reminderEntity);

    }


    @Override
    public void onSearch(String searchCriterion) {
        getRemindersFromDatabase(searchCriterion);
    }


}
