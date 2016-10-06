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


package sharamkov.androidapp.simplereminder.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import sharamkov.androidapp.simplereminder.R;
import sharamkov.androidapp.simplereminder.ReminderEntity;
import sharamkov.androidapp.simplereminder.notification.AlarmHelper;
import sharamkov.androidapp.simplereminder.util.DateTimeUtil;


public class EditReminderDialog extends DialogFragment implements DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener {


    private static final String KEY = "Key";
    private static final String TIME_IN_MILLS = "TimeInMills";
    private static final String TITLE_TEXT = "Title text";
    private static final String PRIORITY = "Priority";
    private static final String REPEAT_INTERVAL = "Repeat Interval";
    private static final String REPEAT_INTERVAL_NUMBER = "Repeat interval number";
    private static final String REPEAT_INTERVAL_TYPE_POSITION = "RepeatIntervalTypePosition";

    private TextInputLayout
            titleTextInputLayout,
            intervalTypeTextInputLayout,
            intervalNumberTextInputLayout,
            dateTextInputLayout,
            timeTextInputLayout;

    private EditText
            titleEditText,
            dateEditText,
            timeEditText,
            intervalTypeEditText,
            intervalNumberEditText;


    private CheckBox priority;
    private CheckBox repeat;

    private Calendar calendar = Calendar.getInstance();

    private OnReminderUpdateListener onReminderUpdateListener;

    // This field is used to store the position of the repeat interval type (day, hour, week, etc)
    // in the repeat interval type list which the user have selected using RepeatIntervalTypeDialog
    private int repeatIntervalTypePosition;


    public EditReminderDialog() {
        calendar.set(Calendar.SECOND, 0);
    }


    public interface OnReminderUpdateListener {

        void onReminderUpdate(ReminderEntity reminderEntity);

    }


    public static EditReminderDialog getNewInstance(ReminderEntity reminderEntity) {

        EditReminderDialog editReminderDialog = new EditReminderDialog();

        Bundle arguments = new Bundle();

        arguments.putLong(KEY, reminderEntity.getKey());
        arguments.putString(TITLE_TEXT, reminderEntity.getTitle());
        arguments.putLong(TIME_IN_MILLS, reminderEntity.getDate());
        arguments.putInt(PRIORITY, reminderEntity.getPriority());
        arguments.putInt(REPEAT_INTERVAL, reminderEntity.getRepeatInterval());
        arguments.putInt(REPEAT_INTERVAL_NUMBER, reminderEntity.getRepeatIntervalNumber());

        editReminderDialog.setArguments(arguments);

        return editReminderDialog;

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onReminderUpdateListener = (OnReminderUpdateListener) activity;
    }


    @Override
    public void onStart() {
        super.onStart();

        // Remove the space that had been entered automatically when the
        // user opened one of the dialogs to set a date, time or repeat
        // and then rotated the screen before the date, time or repeat was set
        if (" ".equals(intervalTypeEditText.getText().toString())) {
            intervalTypeEditText.setText(null);
        } else if (" ".equals(intervalNumberEditText.getText().toString())) {
            intervalNumberEditText.setText(null);
        } else if (" ".equals(dateEditText.getText().toString())) {
            dateEditText.setText(null);
        } else if (" ".equals(timeEditText.getText().toString())) {
            timeEditText.setText(null);
        }

        titleEditText.setSelection(titleEditText.length());

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(TIME_IN_MILLS, calendar.getTimeInMillis());
        outState.putInt(REPEAT_INTERVAL_TYPE_POSITION, repeatIntervalTypePosition);

        if (titleEditText.length() != 0) {
            outState.putString(TITLE_TEXT, titleEditText.getText().toString());
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            calendar.setTimeInMillis(savedInstanceState.getLong(TIME_IN_MILLS));
            repeatIntervalTypePosition = savedInstanceState.getInt(REPEAT_INTERVAL_TYPE_POSITION);

            String title = savedInstanceState.getString(TITLE_TEXT);

            if (title != null) {
                titleEditText.setText(title);
            }

        } else {

            Bundle arguments = getArguments();

            titleEditText.setText(arguments.getString(TITLE_TEXT));

            long timeInMills = arguments.getLong(TIME_IN_MILLS);

            if (timeInMills != 0) {
                calendar.setTimeInMillis(timeInMills);
                dateEditText.setText(DateTimeUtil.getDate(getActivity(), timeInMills));
                timeEditText.setText(DateTimeUtil.getTime(getActivity(), timeInMills));
            }

            if (arguments.getInt(PRIORITY) == ReminderEntity.PRIORITY_HIGH) {
                priority.setChecked(true);
            }

            int repeatInterval = arguments.getInt(REPEAT_INTERVAL);

            if (repeatInterval != ReminderEntity.NO_REPEATS) {

                repeat.setChecked(true);

                String[] intervalTypes = getResources().getStringArray(R.array.interval_types);


                repeatIntervalTypePosition = repeatInterval;
                String intervalType = intervalTypes[repeatInterval];

                intervalTypeEditText.setText(intervalType);
                intervalNumberEditText.setText(
                        String.valueOf(arguments.getInt(REPEAT_INTERVAL_NUMBER)));


            }
        }

        return super.onCreateView(inflater, container, savedInstanceState);

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.reminder_dialog, null);

        dialogBuilder.setTitle(R.string.edit_reminder_dialog_title);
        dialogBuilder.setView(dialogView);

        titleTextInputLayout = (TextInputLayout) dialogView.findViewById(R.id.title_input);
        dateTextInputLayout = (TextInputLayout) dialogView.findViewById(R.id.date_input);
        timeTextInputLayout = (TextInputLayout) dialogView.findViewById(R.id.time_input);


        titleEditText = titleTextInputLayout.getEditText();
        dateEditText = dateTextInputLayout.getEditText();
        timeEditText = timeTextInputLayout.getEditText();


        priority = (CheckBox) dialogView.findViewById(R.id.priority);
        repeat = (CheckBox) dialogView.findViewById(R.id.repeat);


        intervalTypeTextInputLayout = (TextInputLayout) dialogView.findViewById(R.id.interval_type);
        intervalNumberTextInputLayout = (TextInputLayout) dialogView.findViewById(R.id.interval_number);

        intervalTypeEditText = intervalTypeTextInputLayout.getEditText();
        intervalNumberEditText = intervalNumberTextInputLayout.getEditText();


        titleTextInputLayout.setHint(getResources().getString(R.string.title_hint));
        dateTextInputLayout.setHint(getResources().getString(R.string.date_hint));
        timeTextInputLayout.setHint(getResources().getString(R.string.time_hint));
        intervalNumberEditText.setHint(getResources().getString(R.string.number_of_intervals_hint));
        intervalTypeEditText.setHint(getResources().getString(R.string.interval_type_hint));


        intervalTypeEditText.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.sr_secondary_text_disabled_material_light));
        intervalNumberEditText.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.sr_secondary_text_disabled_material_light));


        intervalTypeEditText.setEnabled(false);
        intervalNumberEditText.setEnabled(false);


        // Enable the repeat option if repeat CheckBox is checked otherwise disable the repeat option
        repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    intervalTypeEditText.setEnabled(true);
                    intervalNumberEditText.setEnabled(true);

                    intervalTypeTextInputLayout.setHint(intervalTypeEditText.getHint());
                    intervalTypeEditText.setHint(null);

                    intervalNumberTextInputLayout.setHint(intervalNumberEditText.getHint());
                    intervalNumberEditText.setHint(null);

                } else {

                    intervalTypeEditText.setText(null);
                    intervalNumberEditText.setText(null);

                    intervalTypeEditText.setEnabled(false);
                    intervalNumberEditText.setEnabled(false);

                    intervalTypeEditText.setHint(intervalTypeTextInputLayout.getHint());
                    intervalTypeTextInputLayout.setHint(null);

                    intervalNumberEditText.setHint(intervalNumberTextInputLayout.getHint());
                    intervalNumberTextInputLayout.setHint(null);

                    intervalTypeEditText.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.sr_secondary_text_disabled_material_light));
                    intervalNumberEditText.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.sr_secondary_text_disabled_material_light));

                }
            }
        });


        dateEditText.setOnClickListener(this);
        timeEditText.setOnClickListener(this);
        intervalTypeEditText.setOnClickListener(this);
        intervalNumberEditText.setOnClickListener(this);


        dialogBuilder.setPositiveButton(R.string.positive_button, this);
        dialogBuilder.setNegativeButton(R.string.negative_button, this);

        AlertDialog addReminderDialog = dialogBuilder.create();

        addReminderDialog.setOnShowListener(this);

        return addReminderDialog;

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.date_input_edit_text:
                createDatePickerDialog();
                break;

            case R.id.time_input_edit_text:
                createTimePickerDialog();
                break;

            case R.id.interval_type_edit_text:
                createRepeatIntervalTypeDialog();
                break;

            case R.id.interval_number_edit_text:
                createRepeatIntervalNumberDialog();
                break;

        }

    }


    @Override
    public void onClick(DialogInterface dialog, int which) {

        switch (which) {

            // Create a reminder if the user has clicked "OK" button
            case DialogInterface.BUTTON_POSITIVE:

                ReminderEntity reminderEntity = new ReminderEntity();

                reminderEntity.setKey(getArguments().getLong(KEY));


                reminderEntity.setTitle(titleEditText.getText().toString());


                if (intervalTypeEditText.length() != 0) {

                    if (intervalNumberEditText.length() != 0) {

                        int repeatIntervalNumber = Integer.parseInt(intervalNumberEditText.getText().toString());

                        if (repeatIntervalNumber == 0) {
                            repeatIntervalTypePosition = ReminderEntity.NO_REPEATS;
                        }

                        reminderEntity.setRepeatIntervalNumber(repeatIntervalNumber);

                    } else {
                        reminderEntity.setRepeatIntervalNumber(1);
                    }

                    reminderEntity.setRepeatInterval(repeatIntervalTypePosition);
                }


                if (priority.isChecked()) {
                    reminderEntity.setPriority(ReminderEntity.PRIORITY_HIGH);
                }


                if ((dateEditText.length() != 0 || timeEditText.length() != 0)) {

                    reminderEntity.setDate(calendar.getTimeInMillis());

                    if (reminderEntity.getDate() > System.currentTimeMillis()) {

                        reminderEntity.setStatus(ReminderEntity.STATUS_ACTIVE);
                        AlarmHelper.getInstance().setAlarm(reminderEntity);

                    } else if (reminderEntity.getRepeatInterval() != ReminderEntity.NO_REPEATS) {

                        reminderEntity.setStatus(ReminderEntity.STATUS_ACTIVE);
                        // If the user has set a date that is before the current date prepareAlarm method
                        // will calculate the nearest future date using the repeat interval
                        AlarmHelper.getInstance().prepareAlarm(reminderEntity, false);

                    } else {

                        reminderEntity.setStatus(ReminderEntity.STATUS_INACTIVE);
                        AlarmHelper.getInstance().cancelAlarm(reminderEntity.getKey());
                    }

                } else {
                    reminderEntity.setStatus(ReminderEntity.STATUS_INACTIVE);
                    AlarmHelper.getInstance().cancelAlarm(reminderEntity.getKey());
                }


                if (reminderEntity.getStatus() == ReminderEntity.STATUS_INACTIVE) {
                    reminderEntity.setState(ReminderEntity.STATE_ENABLED);
                }

                onReminderUpdateListener.onReminderUpdate(reminderEntity);

                dialog.dismiss();
                break;


            // Cancel the dialog if user has clicked "Cancel" button
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.cancel();
                break;

        }

    }


    // This method controls a state (enabled/disabled) of AddReminderDialog "OK" button
    // depending on whether the user has entered the title or not.
    @Override
    public void onShow(DialogInterface dialog) {
        timeEditText.setSelection(timeEditText.length());

        final Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);

        if (titleEditText.length() == 0) {
            positiveButton.setEnabled(false);
            titleTextInputLayout.setError(getResources().getString(R.string.title_error));
        }

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (titleEditText.length() != 0) {
                    positiveButton.setEnabled(true);
                    titleTextInputLayout.setError(null);
                } else {
                    positiveButton.setEnabled(false);
                    titleTextInputLayout.setError(getResources().getString(R.string.title_error));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    // Show a dialog to select a date
    private void createDatePickerDialog() {

        // Enter a space to avoid a visual effect of floating
        // label appearance when the date is already set.
        if (dateEditText.length() == 0) {
            dateEditText.setText(" ");
        }


        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(

                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        dateEditText.setText(DateTimeUtil.getDate(getActivity(), calendar.getTimeInMillis()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                // If the user hasn't picked a date set text to null
                // to hide a floating label and show a hint
                if (" ".equals(dateEditText.getText().toString())) {
                    dateEditText.setText(null);
                }
            }
        });

        // DatePickerDialog won't be recreated after screen rotation
        datePickerDialog.dismissOnPause(true);


        datePickerDialog.setOkText(R.string.positive_button);
        datePickerDialog.setCancelText(R.string.negative_button);


        datePickerDialog.setAccentColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));

        datePickerDialog.show(getFragmentManager(), "DatePickerDialogFragment");
    }

    // Show a dialog to select time
    private void createTimePickerDialog() {

        if (timeEditText.length() == 0) {
            timeEditText.setText(" ");
        }


        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {

                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);

                        timeEditText.setText(DateTimeUtil.getTime(getActivity(), calendar.getTimeInMillis()));

                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(getActivity())
        );


        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                if (" ".equals(timeEditText.getText().toString())) {
                    timeEditText.setText(null);
                }
            }
        });

        // TimePickerDialog won't be recreated after screen rotation
        timePickerDialog.dismissOnPause(true);

        timePickerDialog.setOkText(getResources().getString(R.string.positive_button));
        timePickerDialog.setCancelText(getResources().getString(R.string.negative_button));

        timePickerDialog.setAccentColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));

        timePickerDialog.show(getFragmentManager(), "TimePickerDialogFragment");
    }


    // Show a dialog to select a repeat interval type (day, week, hour, etc)
    private void createRepeatIntervalTypeDialog() {


        if (intervalTypeEditText.length() == 0) {
            intervalTypeEditText.setText(" ");

        }

        RepeatIntervalTypeDialog repeatIntervalTypeDialog = RepeatIntervalTypeDialog.getNewInstance(

                new RepeatIntervalTypeDialog.RepeatIntervalTypeDialogListener() {
                    @Override
                    public void onDialogPositiveClick(int selectedIntervalTypePosition
                            , String selectedIntervalTypeName) {

                        repeatIntervalTypePosition = selectedIntervalTypePosition;
                        intervalTypeEditText.setText(selectedIntervalTypeName);

                    }

                    @Override
                    public void onDialogNegativeClick() {
                        if (" ".equals(intervalTypeEditText.getText().toString())) {
                            intervalTypeEditText.setText(null);
                        }
                    }


                });


        String selectedType = intervalTypeEditText.getText().toString();


        if (!" ".equals(selectedType)) {
            repeatIntervalTypeDialog.setSelectedIntervalTypePosition(repeatIntervalTypePosition);
        }


        repeatIntervalTypeDialog.show(getFragmentManager(), "RepeatIntervalTypeDialogFragment");

    }


    // Show a dialog to enter the number of repeat intervals. For example if a repeat interval type is "Day"
    // and the number of repeat intervals is "3" then the notification fires every three days
    private void createRepeatIntervalNumberDialog() {

        if (intervalNumberEditText.length() == 0) {
            intervalNumberEditText.setText(" ");
        }

        RepeatIntervalNumberDialog repeatIntervalNumberDialog = RepeatIntervalNumberDialog.getNewInstance(
                new RepeatIntervalNumberDialog.RepeatIntervalNumberDialogListener() {
                    @Override
                    public void onDialogPositiveClick(String enteredNumber) {
                        if (enteredNumber != null) {

                            if (!enteredNumber.startsWith("0")) {
                                intervalNumberEditText.setText(enteredNumber);
                            } else {
                                intervalNumberEditText.setText("0");
                            }

                        }
                    }

                    @Override
                    public void onDialogNegativeClick() {
                        if (" ".equals(intervalNumberEditText.getText().toString())) {
                            intervalNumberEditText.setText(null);
                        }
                    }
                }
        );


        String enteredNumber = intervalNumberEditText.getText().toString();

        if (!" ".equals(enteredNumber)) {
            repeatIntervalNumberDialog.setEnteredIntervalNumber(enteredNumber);
        }

        repeatIntervalNumberDialog.show(getFragmentManager(), "RepeatIntervalNumberDialogFragment");
    }

}
