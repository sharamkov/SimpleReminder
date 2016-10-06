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

import sharamkov.androidapp.simplereminder.R;


public class RepeatIntervalTypeDialog extends DialogFragment {

    // The position of the repeat interval type in the repeat interval type list the user has selected
    private int selectedPosition;


    private RepeatIntervalTypeDialogListener repeatIntervalTypeDialogListener;

    public interface RepeatIntervalTypeDialogListener {

        void onDialogPositiveClick(int selectedIntervalTypePosition, String selectedIntervalTypeName);

        void onDialogNegativeClick();
    }


    @Override
    public void onPause() {
        super.onPause();

        // RepeatIntervalTypeDialog won't be recreated after screen rotation
        dismiss();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        dialogBuilder.setTitle(R.string.repeat_interval_type_dialog_title);

        final String[] intervalTypes = getActivity().getResources().getStringArray(R.array.interval_types);

        dialogBuilder.setSingleChoiceItems(intervalTypes, selectedPosition, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedPosition = which;
            }
        });


        dialogBuilder.setPositiveButton(
                getResources().getString(R.string.positive_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        repeatIntervalTypeDialogListener.onDialogPositiveClick(selectedPosition, intervalTypes[selectedPosition]);
                        dialog.dismiss();
                    }
                }
        );


        dialogBuilder.setNegativeButton(
                getResources().getString(R.string.negative_button),

                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );

        return dialogBuilder.create();


    }


    public void setSelectedIntervalTypePosition(int selectedIntervalTypePosition) {
        selectedPosition = selectedIntervalTypePosition;
    }


    public static RepeatIntervalTypeDialog getNewInstance(RepeatIntervalTypeDialogListener
                                                                  repeatIntervalTypeDialogListener) {

        RepeatIntervalTypeDialog repeatIntervalTypeDialog = new RepeatIntervalTypeDialog();
        repeatIntervalTypeDialog.repeatIntervalTypeDialogListener = repeatIntervalTypeDialogListener;

        return repeatIntervalTypeDialog;

    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        repeatIntervalTypeDialogListener.onDialogNegativeClick();

    }

}
