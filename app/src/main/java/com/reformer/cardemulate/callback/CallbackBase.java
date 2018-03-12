package com.reformer.cardemulate.callback;

import com.reformer.cardemulate.callback.base.ResponseBase;
import com.reformer.cardemulate.util.GsonUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.json.JSONObject;

import java.io.IOException;


import okhttp3.Response;

/**
 * Created by Administrator on 2016-11-04.
 */
public abstract class CallbackBase extends Callback<ResponseBase> {
    @Override
    public ResponseBase parseNetworkResponse(Response response) throws IOException {
        return GsonUtils.getInstance().fromJson(response.body().string(), ResponseBase.class);
    }
}
