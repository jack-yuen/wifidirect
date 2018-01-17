package org.bigleg.async.http.socketio;

import org.json.JSONArray;

public interface Acknowledge {
    void acknowledge(JSONArray arguments);
}
