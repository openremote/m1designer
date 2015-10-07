package org.openremote.client.shared.request;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.ShowFailureEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.openremote.shared.Constants.REST_SERVICE_CONTEXT_PATH;

@JsExport
@JsType
public abstract class RequestPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestPresenter.class);

    public String resourceLocation;

    public RequestPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addListener(RequestFailureEvent.class, event -> {
            dispatch(new ShowFailureEvent(
                event.getRequestFailure().getFailureMessage(),
                30000
            ));
        });
    }

    protected abstract class ResponseCallback<T> implements JsonCallback {

        protected final String requestText;
        protected final JsonEncoderDecoder<T> encoderDecoder;
        protected boolean dispatchedComplete = false;
        protected boolean notifyUserOnSuccess = false;
        protected boolean notifyUserOnFailure = true;

        public ResponseCallback(String requestText, JsonEncoderDecoder<T> encoderDecoder) {
            this.requestText = requestText;
            this.encoderDecoder = encoderDecoder;
        }

        @Override
        public void onSuccess(Method method, JSONValue response) {
            if (!dispatchedComplete && notifyUserOnSuccess) {
                dispatchedComplete = true;
                dispatch(new RequestCompleteEvent(requestText));
            }
        }

        @Override
        public void onFailure(Method method, Throwable exception) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request failure: " + requestText, exception);
            }
            if (!dispatchedComplete && notifyUserOnSuccess) {
                dispatchedComplete = true;
                dispatch(new RequestCompleteEvent(requestText));
            }
            RequestFailure requestFailure = new RequestFailure(
                requestText,
                method.getResponse() != null ? method.getResponse().getStatusCode() : -1,
                method.getResponse() != null ? method.getResponse().getStatusText() : null,
                exception.getMessage()
            );
            onFailure(requestFailure);
        }

        public void onFailure(RequestFailure requestFailure) {
            if (notifyUserOnFailure)
                dispatch(new RequestFailureEvent(requestFailure));
            else
                LOG.error(requestFailure.getFailureMessage());
        }

        public ResponseCallback<T> setNotifyUserOnSuccess(boolean notifyUserOnSuccess) {
            this.notifyUserOnSuccess = notifyUserOnSuccess;
            return this;
        }

        public ResponseCallback<T> setNotifyUserOnFailure(boolean notifyUserOnFailure) {
            this.notifyUserOnFailure = notifyUserOnFailure;
            return this;
        }
    }

    protected abstract class StatusResponseCallback extends ResponseCallback<Void> {

        protected final int expectedStatusCode;

        public StatusResponseCallback(String requestText, int expectedStatusCode) {
            super(requestText, null);
            this.expectedStatusCode = expectedStatusCode;
        }

        @Override
        public void onSuccess(Method method, JSONValue response) {
            super.onSuccess(method, response);
            try {
                if (method.getResponse().getStatusCode() != expectedStatusCode) {
                    throw new IllegalArgumentException("Response status code must be " + expectedStatusCode);
                }
                onResponse(method.getResponse());
            } catch (Exception ex) {
                onFailure(method, ex);
            }
        }

        protected abstract void onResponse(Response response);
    }

    protected abstract class ObjectResponseCallback<T> extends ResponseCallback<T> {
        public ObjectResponseCallback(String requestText, JsonEncoderDecoder<T> encoderDecoder) {
            super(requestText, encoderDecoder);
        }

        @Override
        public void onSuccess(Method method, JSONValue response) {
            super.onSuccess(method, response);
            try {
                JSONObject responseObject = response.isObject();
                if (responseObject == null) {
                    onFailure(method, new IllegalArgumentException("Response isn't a JSON object: " + response));
                } else {
                    onResponse(encoderDecoder.decode(response));
                }
            } catch (Exception ex) {
                onFailure(method, ex);
            }
        }

        protected abstract void onResponse(T data);
    }

    protected abstract class ListResponseCallback<T> extends ResponseCallback<T> {

        public ListResponseCallback(String requestText, JsonEncoderDecoder<T> encoderDecoder) {
            super(requestText, encoderDecoder);
        }

        @Override
        public void onSuccess(Method method, JSONValue response) {
            super.onSuccess(method, response);
            JSONArray responseArray = response.isArray();
            try {
                if (responseArray == null) {
                    throw new IllegalArgumentException("Response isn't a JSON array: " + response);
                } else {
                    List<T> list = new ArrayList<>();
                    for (int i = 0; i < responseArray.size(); i++) {
                        T t = encoderDecoder.decode(responseArray.get(i));
                        list.add(t);
                    }
                    onResponse(list);
                }
            } catch (Exception ex) {
                onFailure(method, ex);
            }
        }

        protected abstract void onResponse(List<T> data);
    }

    protected abstract class ArrayResponseCallback extends ResponseCallback {

        public ArrayResponseCallback(String requestText) {
            super(requestText, null);
        }

        @Override
        public void onSuccess(Method method, JSONValue response) {
            super.onSuccess(method, response);
            JSONArray responseArray = response.isArray();
            try {
                if (responseArray == null) {
                    throw new IllegalArgumentException("Response isn't a JSON array: " + response);
                } else {
                    onResponse(responseArray);
                }
            } catch (Exception ex) {
                onFailure(method, ex);
            }
        }

        protected abstract void onResponse(JSONArray arra);
    }

    protected Resource resource(String... pathElement) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://").append(hostname()).append(":").append(port()).append(REST_SERVICE_CONTEXT_PATH);
        if (pathElement != null) {
            for (String pe : pathElement) {
                sb.append("/").append(pe);
            }
        }
        return new Resource(sb.toString());
    }

    protected <T> void sendRequest(Method method, ResponseCallback<T> callback) {
        sendRequest(false, true, method, callback);
    }

    protected <T> void sendRequest(boolean notifyUserOnSuccess, boolean notifyUserOnFailure, Method method, ResponseCallback<T> callback) {
        callback.setNotifyUserOnSuccess(notifyUserOnSuccess);
        callback.setNotifyUserOnFailure(notifyUserOnFailure);
        method.send(callback);
    }

    protected static String hostname() {
        return Window.Location.getHostName();
    }

    protected static String port() {
        return Window.Location.getPort();
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
        if (resourceLocation != null) {
            notifyPath("resourceLocation", resourceLocation);
        } else {
            notifyPathNull("resourceLocation");
        }
    }

}
