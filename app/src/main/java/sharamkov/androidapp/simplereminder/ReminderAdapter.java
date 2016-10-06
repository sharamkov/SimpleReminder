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

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import sharamkov.androidapp.simplereminder.database.ReminderDatabaseManager;
import sharamkov.androidapp.simplereminder.dialog.EditReminderDialog;
import sharamkov.androidapp.simplereminder.dialog.RemoveReminderDialog;
import sharamkov.androidapp.simplereminder.notification.AlarmHelper;
import sharamkov.androidapp.simplereminder.util.DateTimeUtil;
import sharamkov.androidapp.simplereminder.util.RepeatIntervalUtil;


public class ReminderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<ReminderEntity> reminderEntities;
    private Activity activity;


    public interface OnLocationChangeListener {

        int getLocation(ReminderEntity reminderEntity);

        void onLocationChange(int newLocation);

    }


    public interface OnSearchListener {

        void onSearch(String searchCriterion);

    }


    public ReminderAdapter(Activity activity) {
        this.activity = activity;
        reminderEntities = new ArrayList<>();
    }


    private int checkLocation(ReminderEntity reminderEntity) {

        return ((OnLocationChangeListener) activity).getLocation(reminderEntity);

    }


    // This method is used to change the reminder location in the reminder list when the reminder
    // status, state or date is changed
    private void changeLocation(int currentLocation, ReminderEntity reminderEntity) {

        reminderEntities.remove(currentLocation);

        int newLocation = checkLocation(reminderEntity);

        if (currentLocation != newLocation) {

            notifyItemRemoved(currentLocation);
            addReminderEntity(newLocation, reminderEntity, true);

        } else {

            reminderEntities.add(currentLocation, reminderEntity);
            notifyItemChanged(currentLocation);

        }


    }


    // This method works to set the next date when the notification appears and the app is opened
    public void setNextDate(long key, long nextDate) {

        for (int location = 0; location < getItemCount(); location++) {

            ReminderEntity reminderEntity = reminderEntities.get(location);

            if (key == reminderEntity.getKey()) {

                reminderEntity.setDate(nextDate);
                changeLocation(location, reminderEntity);

                return;
            }
        }
    }


    public void updateReminderEntity(ReminderEntity reminderEntity, int location) {

        reminderEntities.set(location, reminderEntity);
        changeLocation(location, reminderEntity);

    }


    // This method works to deactivate the reminder when the notification appears and the app is opened
    public void deactivateReminder(long key) {

        for (int location = 0; location < getItemCount(); location++) {

            ReminderEntity reminderEntity = reminderEntities.get(location);

            if (key == reminderEntity.getKey()) {

                reminderEntity.setStatus(ReminderEntity.STATUS_INACTIVE);
                changeLocation(location, reminderEntity);

                return;
            }
        }
    }


    public ReminderEntity getReminderEntity(int location) {
        return reminderEntities.get(location);
    }


    public void addReminderEntity(int location, ReminderEntity reminderEntity, boolean isNotify) {

        reminderEntities.add(location, reminderEntity);

        if (isNotify) {
            notifyItemInserted(location);
            scrollToLocation(location);
        }

    }


    public ReminderEntity removeReminderEntity(int location) {

        ReminderEntity reminderEntity = reminderEntities.remove(location);
        notifyItemRemoved(location);
        return reminderEntity;

    }


    public void searchReminderEntities(String searchCriterion) {

        reminderEntities.clear();
        ((OnSearchListener) activity).onSearch(searchCriterion);
        notifyDataSetChanged();

    }


    private void scrollToLocation(int newLocation) {
        ((OnLocationChangeListener) activity).onLocationChange(newLocation);
    }


    public void scrollToLocation(long key) {

        for (int i = 0; i < reminderEntities.size(); i++) {

            if (reminderEntities.get(i).getKey() == key) {

                scrollToLocation(i);
                break;

            }

        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View reminderEntryView = inflater.inflate(R.layout.reminder_entry, parent, false);

        ImageView reminderStatus = (ImageView) reminderEntryView.findViewById(R.id.reminder_status);
        TextView reminderTitle = (TextView) reminderEntryView.findViewById(R.id.reminder_title);
        TextView reminderDate = (TextView) reminderEntryView.findViewById(R.id.reminder_date);
        TextView reminderRepeat = (TextView) reminderEntryView.findViewById(R.id.reminder_repeat);

        return new ReminderEntityViewHolder(reminderEntryView, reminderStatus, reminderTitle,
                reminderDate, reminderRepeat);

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final ReminderEntity reminderEntity = reminderEntities.get(position);

        final ReminderEntityViewHolder reminderEntityViewHolder = (ReminderEntityViewHolder) holder;

        final View itemView = holder.itemView;
        final Resources resources = itemView.getResources();


        // Set the reminder title
        reminderEntityViewHolder.reminderTitle.setText(reminderEntity.getTitle());

        // Set the reminder repeat
        if (reminderEntity.getRepeatInterval() == ReminderEntity.NO_REPEATS
                || reminderEntity.getRepeatIntervalNumber() == 0) {
            reminderEntityViewHolder.reminderRepeat.setText(resources.getString(R.string.no_repeats));
        } else {

            String displayRepeatInterval = RepeatIntervalUtil.getDisplayRepeatInterval(
                    reminderEntity.getRepeatInterval(),
                    reminderEntity.getRepeatIntervalNumber(),
                    resources);

            reminderEntityViewHolder.reminderRepeat.setText(String.format(resources.getString(R.string.repeat_interval), displayRepeatInterval));

        }


        if (reminderEntity.getStatus() == ReminderEntity.STATUS_ACTIVE) {

            // Set the text color
            reminderEntityViewHolder.reminderTitle.setTextColor(
                    ContextCompat.getColor(activity, R.color.sr_primary_text_default_material_light));
            reminderEntityViewHolder.reminderDate.setTextColor(
                    ContextCompat.getColor(activity, R.color.sr_secondary_text_default_material_light));
            reminderEntityViewHolder.reminderRepeat.setTextColor(
                    ContextCompat.getColor(activity, R.color.sr_secondary_text_default_material_light));


            // Set the status image and the full date (date and time)
            reminderEntityViewHolder.reminderDate.setText(
                    DateTimeUtil.getDateAndTime(activity, reminderEntity.getDate()));
            reminderEntityViewHolder.reminderStatus.setImageResource(R.drawable.ic_alarm_white_48dp);


            // Check if the reminder is enabled or disabled
            if (reminderEntity.getState() == ReminderEntity.STATE_ENABLED) {

                // Set the image color for enabled reminder according to the reminder priority
                if (reminderEntity.getPriority() == ReminderEntity.PRIORITY_NORMAL) {
                    reminderEntityViewHolder.reminderStatus
                            .setColorFilter(ContextCompat.getColor(activity, R.color.priority_normal));
                } else {
                    reminderEntityViewHolder.reminderStatus
                            .setColorFilter(ContextCompat.getColor(activity, R.color.priority_high));
                }

            } else {

                // Set the image color for disabled reminder
                reminderEntityViewHolder.reminderStatus
                        .setColorFilter(ContextCompat.getColor(activity, R.color.sr_primary_text_disabled_material_light));
            }


            setReminderStatusOnClickListener(reminderEntityViewHolder, reminderEntity, itemView);


        } else {

            // Set the text color
            reminderEntityViewHolder.reminderTitle.setTextColor(
                    ContextCompat.getColor(activity, R.color.sr_primary_text_disabled_material_light));
            reminderEntityViewHolder.reminderDate.setTextColor(
                    ContextCompat.getColor(activity, R.color.sr_secondary_text_disabled_material_light));
            reminderEntityViewHolder.reminderRepeat.setTextColor(
                    ContextCompat.getColor(activity, R.color.sr_secondary_text_disabled_material_light));

            // Set the status image and the image color
            reminderEntityViewHolder.reminderStatus
                    .setImageResource(R.drawable.ic_alarm_off_white_48dp);
            reminderEntityViewHolder.reminderStatus.setColorFilter(
                    ContextCompat.getColor(activity, R.color.sr_primary_text_disabled_material_light)
            );


            // Display the date if it's been set otherwise display "No date" message
            if (reminderEntity.getDate() == 0) {
                reminderEntityViewHolder.reminderDate.setText(resources.getString(R.string.no_date));
            } else {
                reminderEntityViewHolder.reminderDate.setText(DateTimeUtil.getDateAndTime(activity, reminderEntity.getDate()));
            }


            reminderEntityViewHolder.reminderStatus.setOnClickListener(null);

        }


        // Set the listener to show EditReminderDialog
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditReminderDialog.getNewInstance(reminderEntity)
                        .show(activity.getFragmentManager(), "EditReminderDialogFragment");
            }
        });


        // Set the listener to show RemoveReminderDialog
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                RemoveReminderDialog removeReminderDialog = new RemoveReminderDialog();

                Bundle arguments = new Bundle();
                arguments.putInt(RemoveReminderDialog.LOCATION, holder.getLayoutPosition());
                removeReminderDialog.setArguments(arguments);

                removeReminderDialog.show(activity.getFragmentManager(), "RemoveReminderDialogFragment");

                return true;
            }
        });


    }


    private void setReminderStatusOnClickListener(final ReminderEntityViewHolder reminderEntityViewHolder,
                                                  final ReminderEntity reminderEntity, final View itemView) {

        // Set the listener for status image to change the reminder status/state
        reminderEntityViewHolder.reminderStatus.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (reminderEntity.getState() == ReminderEntity.STATE_DISABLED) {

                            reminderEntity.setState(ReminderEntity.STATE_ENABLED);


                            // Check if the reminder has no repeat and is out of date
                            if (reminderEntity.getRepeatInterval() == ReminderEntity.NO_REPEATS
                                    && reminderEntity.getDate() <= Calendar.getInstance().getTimeInMillis()) {

                                reminderEntity.setStatus(ReminderEntity.STATUS_INACTIVE);

                                changeLocation(reminderEntityViewHolder.getLayoutPosition(), reminderEntity);

                                ReminderDatabaseManager.getInstance().updateState(
                                        reminderEntity.getKey(), reminderEntity.getState());

                                ReminderDatabaseManager.getInstance().updateStatus(
                                        reminderEntity.getKey(), reminderEntity.getStatus()
                                );


                                Toast.makeText(itemView.getContext(), R.string.enable_inactive_reminder_toast_message,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }


                            // Set the image color for enabled reminder according to the reminder priority
                            if (reminderEntity.getPriority() == ReminderEntity.PRIORITY_NORMAL) {
                                reminderEntityViewHolder.reminderStatus
                                        .setColorFilter(ContextCompat.getColor(activity, R.color.priority_normal));
                            } else {
                                reminderEntityViewHolder.reminderStatus
                                        .setColorFilter(ContextCompat.getColor(activity, R.color.priority_high));
                            }


                            AlarmHelper.getInstance().prepareAlarm(reminderEntity, true);

                            Toast.makeText(itemView.getContext(), R.string.enable_active_reminder_toast_message,
                                    Toast.LENGTH_SHORT).show();


                        } else {

                            reminderEntity.setState(ReminderEntity.STATE_DISABLED);

                            // Set the image color for disabled reminder
                            reminderEntityViewHolder.reminderStatus
                                    .setColorFilter(ContextCompat.getColor(activity, R.color.sr_primary_text_disabled_material_light));

                            AlarmHelper.getInstance().cancelAlarm(reminderEntity.getKey());

                            Toast.makeText(itemView.getContext(), R.string.disable_reminder_toast_message,
                                    Toast.LENGTH_SHORT).show();
                        }


                        changeLocation(reminderEntityViewHolder.getLayoutPosition(), reminderEntity);


                        ReminderDatabaseManager.getInstance().updateState(
                                reminderEntity.getKey(), reminderEntity.getState());

                    }
                }
        );


    }


    @Override
    public int getItemCount() {
        return reminderEntities.size();
    }


    private static class ReminderEntityViewHolder extends RecyclerView.ViewHolder {

        ImageView reminderStatus;
        TextView reminderTitle;
        TextView reminderDate;
        TextView reminderRepeat;

        public ReminderEntityViewHolder(View itemView, ImageView reminderStatus,
                                        TextView reminderTitle, TextView reminderDate,
                                        TextView reminderRepeat) {
            super(itemView);
            this.reminderStatus = reminderStatus;
            this.reminderTitle = reminderTitle;
            this.reminderDate = reminderDate;
            this.reminderRepeat = reminderRepeat;
        }
    }


}
