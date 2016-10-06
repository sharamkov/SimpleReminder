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


package sharamkov.androidapp.simplereminder.util;

import android.content.res.Resources;

import java.util.Locale;

import sharamkov.androidapp.simplereminder.R;


public class RepeatIntervalUtil {


    private RepeatIntervalUtil() {

    }


    public static String getDisplayRepeatInterval(int repeatInterval,
                                                  int repeatIntervalNumber, Resources resources) {

        String displayRepeatInterval;

        String defaultLang = Locale.getDefault().getLanguage();


        if ("ru".equals(defaultLang)) {

            displayRepeatInterval = getRuLangDisplayRepeatInterval(
                    repeatInterval, repeatIntervalNumber, resources);

        } else {

            String displayRepeatIntervals[] = resources.getStringArray(R.array.interval_types);

            displayRepeatInterval = displayRepeatIntervals[repeatInterval].toLowerCase(Locale.ENGLISH);

            if (repeatIntervalNumber > 1) {
                displayRepeatInterval = displayRepeatInterval + "s";
            }
        }


        return repeatIntervalNumber + " " + displayRepeatInterval;


    }


    private static String getRuLangDisplayRepeatInterval
            (int repeatInterval, int repeatIntervalNumber, Resources resources) {


        int remainder1 = repeatIntervalNumber % 100;

        int remainder2 = repeatIntervalNumber % 10;

        String[] displayRepeatIntervals;


        if ((remainder1 < 11 || remainder1 > 19) && remainder2 == 1) {

            displayRepeatIntervals = resources.getStringArray(R.array.interval_types_form1);
            return displayRepeatIntervals[repeatInterval];

        } else if ((remainder1 < 11 || remainder1 > 19) && (remainder2 >= 2 && remainder2 <= 4)) {

            displayRepeatIntervals = resources.getStringArray(R.array.interval_types_form2);
            return displayRepeatIntervals[repeatInterval];

        } else {

            displayRepeatIntervals = resources.getStringArray(R.array.interval_types_form3);
            return displayRepeatIntervals[repeatInterval];

        }


    }


}
