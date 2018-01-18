package org.bigleg.wifidirect;

import android.net.wifi.p2p.WifiP2pDevice;

import org.bigleg.async.AsyncDatagramSocket;
import org.bigleg.async.AsyncNetworkSocket;
import org.bigleg.async.AsyncSSLSocket;
import org.bigleg.async.AsyncSocket;
import org.bigleg.async.http.AsyncHttpClient;
import org.bigleg.async.http.socketio.Acknowledge;
import org.bigleg.async.http.socketio.SocketIOClient;
import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jack on 2018/1/17.
 */

public class socketThread extends Thread {
    private String m_addr;
    List<WifiP2pDevice> m_groupList;
    public socketThread(String addr, List<WifiP2pDevice> groupList){
        m_addr = addr;
        m_groupList = groupList;
    }
    @Override
    public void run(){
        try {
            //Socket s = new Socket(m_addr, 6000);
//            AsyncDatagramSocket socket = new AsyncDatagramSocket();
//            byte[] buffer = new byte[4];
//            buffer[0] = 'x';
//            buffer[1] = 'x';
//            buffer[2] = 'x';
//            buffer[3] = 'x';
//            ByteBuffer bb = ByteBuffer.wrap(buffer);
//            socket.send(m_dev.deviceAddress, 6000, bb);
            //final TriggerFuture trigger = new TriggerFuture();
            SocketIOClient client = SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), m_addr+":6000", null).get();
            client.emit("hello", new Acknowledge() {
                @Override
                public void acknowledge(JSONArray arguments) {
                    //trigger.trigger("hello".equals(arguments.optString(0)));
                    System.out.println(arguments.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
