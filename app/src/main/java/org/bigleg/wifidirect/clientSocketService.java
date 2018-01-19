package org.bigleg.wifidirect;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by jack on 2018/1/18.
 */

public class clientSocketService extends IntentService {
    public static final String RECEIVEIP_ACTION = "org.bigleg.wifidirect.RECEIVEIP";
    public static final String IP_DATA = "org.bigleg.wifidirect.IP_DATA";
    public static final String GROUP_MEM_LIST = "org.bigleg.wifidirect.grp_mem_lst";

    public static String GROUP_OWNER_ADDR = "org.bigleg.wifidirect.grpowner_addr";
    public static final String GET_IP_HEAD = "org.bigleg.wifidirect.get_ip";
    public static final String IP_END = "org.bigleg.wifidirect.ip_end";

    public static final String IP_HEAD = "org.bigleg.wifidirect.ip_head";
    public static final String DEVICE_HEAD = "org.bigleg.wifidirect.device_head";
    public static final String DEVICE_SPLIT = "=device_split=";

    public static final String SEND_MEM_HEAD ="org.bigleg.wifidirect.send_mem_head";
    public static final String SEND_MEM_END ="org.bigleg.wifidirect.send_mem_head";
    public static final String RECEIVE_MEM_END ="org.bigleg.wifidirect.receive_mem_head";//客户端接收成员列表结束，服务器需要关闭

    public static final String RECEIVE_GROUPLIST_ACTION = "org.bigleg.wifidirect.RECEIVE_GRP_LIST";

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
            bufferedWriter.write(GET_IP_HEAD);
            bufferedWriter.write("\n");
            bufferedWriter.flush();

            String clientAddr = "";
            ArrayList<String> deviceList = new ArrayList<>();

            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while(true) {
                String line = bufferedReader.readLine();
                if(line!= null && line.startsWith(IP_HEAD)){
                    line = line.replace(IP_HEAD, "");
                    clientAddr = line;
                    continue;
                }
                if(line!= null && line.startsWith(DEVICE_HEAD)){
                    line = line.replace(DEVICE_HEAD, "");
                    deviceList.add(line);
                    continue;
                }
                //服务器发送完成，关闭连接
                if(line.equals(IP_END)){
                    //关闭之前告诉监听器也关闭?
                    socket.close();
                    break;
                }
            }
            //接收到回复的地址之后，发送广播以更新地址信息
            Intent ipIntent = new Intent(RECEIVEIP_ACTION);
            ipIntent.putExtra(IP_DATA, clientAddr);
            ipIntent.putStringArrayListExtra(GROUP_MEM_LIST, deviceList);
            m_BroadcastManager = LocalBroadcastManager.getInstance(this);
            m_BroadcastManager.sendBroadcast(ipIntent);
            //this.stopSelf();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
