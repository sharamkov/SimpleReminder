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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import sharamkov.androidapp.simplereminder.R;


public class RepeatIntervalNumberDialog extends DialogFragment {

    // The number of repeat intervals the user has entered
    private String enteredNumber;


    private RepeatIntervalNumberDialogListener repeatIntervalNumberDialogListener;

    public interface RepeatIntervalNumberDialogListener {

        void onDialogPositiveClick(String enteredNumber);

        void onDialogNegativeClick();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        dialogBuilder.setTitle(R.string.repeat_interval_number_dialog_title);

        View intervalNumberView = getActivity().getLayoutInflater().inflate(
                R.layout.repeat_interval_number_dialog, null);

        final EditText intervalNumberEditText = (EditText) intervalNumberView.findViewById(R.id.interval_number_edit_text);

        if (enteredNumber != null) {
            intervalNumberEditText.setText(enteredNumber);
            intervalNumberEditText.setSelection(intervalNumberEditText.length());
        }

        dialogBuilder.setView(intervalNumberView);


        dialogBuilder.setPositiveButton(getResources().getString(R.string.positive_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        enteredNumber = intervalNumberEditText.getText().toString();
                        repeatIntervalNumberDialogListener.onDialogPositiveClick(enteredNumber);

                        dialog.dismiss();
                    }
                }
        );


        dialogBuilder.setNegativeButton(getResources().getString(R.string.negative_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });


        final AlertDialog alertDialog = dialogBuilder.create();


        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            //This method controls state (enabled/disabled) of RepeatIntervalNumberDialog "OK" button
            //depending on whether the user has entered the number or not.
            @Override
            public void onShow(DialogInterface dialog) {

                final Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);

                if (intervalNumberEditText.length() == 0) {
                    positiveButton.setEnabled(false);
                } else {
                    positiveButton.setEnabled(true);
                }

                intervalNumberEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (intervalNumberEditText.length() == 0) {
                            positiveButton.setEnabled(false);
                        } else {
                            positiveButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }

        });


        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


        return alertDialog;

    }


    @Override
    public void onPause() {
        super.onPause();

        // RepeatIntervalNumberDialog won't be recreated after screen rotation
        dismiss();
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        repeatIntervalNumberDialogListener.onDialogNegativeClick();

    }


    public void setEnteredIntervalNumber(String enteredNumber) {
        this.enteredNumber = enteredNumber;
    }


    public static RepeatIntervalNumberDialog getNewInstance(RepeatIntervalNumberDialogListener
                                                                    repeatIntervalNumberDialogListener) {

        RepeatIntervalNumberDialog repeatIntervalNumberDialog = new RepeatIntervalNumberDialog();

        repeatIntervalNumberDialog.repeatIntervalNumberDialogListener = repeatIntervalNumberDialogListener;

        return repeatIntervalNumberDialog;
    }
}
