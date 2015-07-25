package org.openremote.beta.client.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPresenter.class);

    protected abstract class ResponseCallback<T> implements JsonCallback {

        public final JsonEncoderDecoder<T> encoderDecoder;
        public final Function success;

        public ResponseCallback(JsonEncoderDecoder<T> encoderDecoder, Function success) {
            this.encoderDecoder = encoderDecoder;
            this.success = success;
        }

        @Override
        public void onFailure(Method method, Throwable exception) {
            LOG.error("Request error: " + exception);
        }
    }

    protected abstract class ObjectResponseCallback<T> extends ResponseCallback<T> {
        public ObjectResponseCallback(JsonEncoderDecoder<T> encoderDecoder, Function success) {
            super(encoderDecoder, success);
        }

        @Override
        public void onSuccess(Method method, JSONValue response) {
            JSONObject responseObject = response.isObject();
            if (responseObject == null) {
                onFailure(method, new IllegalArgumentException("Response isn't a JSON object: " + response));
            } else {
                onResponse(encoderDecoder.decode(response));
                success.call();
            }
        }

        protected abstract void onResponse(T data);
    }

    protected abstract class ListResponseCallback<T> extends ResponseCallback<T> {

        public ListResponseCallback(JsonEncoderDecoder<T> encoderDecoder, Function success) {
            super(encoderDecoder, success);
        }

        @Override
        public void onSuccess(Method method, JSONValue response) {
            JSONArray responseArray = response.isArray();
            if (responseArray == null) {
                onFailure(method, new IllegalArgumentException("Response isn't a JSON array: " + response));
            } else {
                List<T> list = new ArrayList<>();
                for (int i = 0; i < responseArray.size(); i++) {
                    T t = encoderDecoder.decode(responseArray.get(i));
                    list.add(t);
                }
                onResponse(list);
                success.call();
            }
        }

        protected abstract void onResponse(List<T> data);
    }

    protected Resource resource(String base, String... pathElement) {
        Resource resource = new Resource(GWT.getHostPageBaseURL() + base);
        if (pathElement != null) {
            for (String pe : pathElement) {
                resource = resource.resolve(pe);
            }
        }
        return resource;
    }

}
