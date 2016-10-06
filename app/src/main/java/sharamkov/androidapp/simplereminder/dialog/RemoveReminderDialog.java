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
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.TextView;

import sharamkov.androidapp.simplereminder.R;


public class RemoveReminderDialog extends DialogFragment {

    public static final String LOCATION = "location";

    public interface OnReminderRemoveListener {
        void onReminderRemove(int location);
    }

    private OnReminderRemoveListener onReminderRemoveListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onReminderRemoveListener = (OnReminderRemoveListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final int location = getArguments().getInt(LOCATION);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        TextView customTitle = new TextView(getActivity());

        customTitle.setText(getString(R.string.delete_reminder_dialog_title));
        customTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.sr_primary_text_default_material_light));
        customTitle.setPadding(0, 30, 0, 30);
        customTitle.setTextSize(20);
        customTitle.setGravity(Gravity.CENTER);

        dialogBuilder.setCustomTitle(customTitle);


        dialogBuilder.setPositiveButton(getString(R.string.delete_reminder_dialog_positive_button),

                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        onReminderRemoveListener.onReminderRemove(location);
                        dialog.dismiss();

                    }
                });


        dialogBuilder.setNegativeButton(getString(R.string.delete_reminder_dialog_negative_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        return dialogBuilder.create();

    }
}
