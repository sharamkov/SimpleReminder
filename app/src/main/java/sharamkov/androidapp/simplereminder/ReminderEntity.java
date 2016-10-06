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


public class ReminderEntity {

    public static final int PRIORITY_NORMAL = 0;
    public static final int PRIORITY_HIGH = 1;

    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE = 1;

    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;

    public static final int NO_REPEATS = -1;


    private long key;
    private String title;
    private long date;
    private int priority;
    private int status;
    private int state;
    private int repeatInterval;
    private int repeatIntervalNumber;


    public ReminderEntity() {
        state = STATE_ENABLED;
        repeatInterval = NO_REPEATS;
    }

    public ReminderEntity(long key, String title, long date, int priority, int status,
                          int state, int repeatInterval, int repeatIntervalNumber) {

        this.key = key;
        this.title = title;
        this.date = date;
        this.priority = priority;
        this.status = status;
        this.state = state;
        this.repeatInterval = repeatInterval;
        this.repeatIntervalNumber = repeatIntervalNumber;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public int getRepeatIntervalNumber() {
        return repeatIntervalNumber;
    }

    public void setRepeatIntervalNumber(int repeatIntervalNumber) {
        this.repeatIntervalNumber = repeatIntervalNumber;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }
}
