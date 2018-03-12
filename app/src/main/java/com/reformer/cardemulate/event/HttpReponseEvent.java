package com.reformer.cardemulate.event;

import com.reformer.cardemulate.callback.base.ResponseBase;

/**
 * Created by Administrator on 2017-12-22.
 */

public class HttpReponseEvent {
    private String content;
    private ResponseBase responseBase;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ResponseBase getResponseBase() {
        return responseBase;
    }

    public void setResponseBase(ResponseBase responseBase) {
        this.responseBase = responseBase;
    }

    public HttpReponseEvent() {
    }

    public HttpReponseEvent(String content) {
        this.content = content;
    }

    public HttpReponseEvent(String content, ResponseBase responseBase) {
        this.content = content;
        this.responseBase = responseBase;
    }
}
