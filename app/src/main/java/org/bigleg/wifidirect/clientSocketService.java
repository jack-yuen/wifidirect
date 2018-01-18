package org.bigleg.wifidirect;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.bigleg.async.http.socketio.SocketIOClient;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by jack on 2018/1/18.
 */

public class clientSocketService extends IntentService {
    public static final String RECEIVEIP_ACTION = "org.bigleg.wifidirect.RECEIVEIP";
    public static final String IP_DATA = "org.bigleg.wifidirect.IP_DATA";
    public static String GROUP_OWNER_ADDR = "org.bigleg.wifidirect.grpowner_addr";
    private LocalBroadcastManager m_BroadcastManager;
    private String m_addr;
    public clientSocketService(String name) {
        super(name);
    }
    public clientSocketService(){
        super("clientSocketService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try{
            m_addr = intent.getStringExtra(GROUP_OWNER_ADDR);
            Socket socket = new Socket(m_addr, 6000);

            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write("123412341234");
            bufferedWriter.flush();

            //InputStream inputStream = socket.getInputStream();

            int data = 33333;
            //接收到回复的地址之后，发送广播以更新地址信息
            Intent ipIntent = new Intent(RECEIVEIP_ACTION);
            ipIntent.putExtra(IP_DATA, data);
            m_BroadcastManager = LocalBroadcastManager.getInstance(this);
            m_BroadcastManager.sendBroadcast(ipIntent);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
