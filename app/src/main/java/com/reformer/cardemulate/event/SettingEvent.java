package com.reformer.cardemulate.event;

/**
 * Created by Administrator on 2017-12-28.
 */

public class SettingEvent {
    public final static String NFC = "nfc";
    public final static String SHAKE = "shake";
    public final static String FEEL = "feel";

    private String obj;
    private boolean checked;

    public String getObj() {
        return obj;
    }

    public void setObj(String obj) {
        this.obj = obj;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public SettingEvent(String obj, boolean checked) {
        this.obj = obj;
        this.checked = checked;
    }
}
