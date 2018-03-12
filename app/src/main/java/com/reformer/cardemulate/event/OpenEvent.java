package com.reformer.cardemulate.event;

/**
 * Created by Administrator on 2017-12-28.
 */

public class OpenEvent {
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OpenEvent() {
    }

    public OpenEvent(String content) {
        this.content = content;
    }
}
