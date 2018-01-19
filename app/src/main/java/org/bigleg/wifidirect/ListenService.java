package org.bigleg.wifidirect;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.in;

/**
 * Created by jack on 2018/1/18.
 */

public class ListenService extends IntentService {
    private LocalBroadcastManager m_BroadcastManager;

    public ListenService(String name) {
        super(name);
    }

    public ListenService() {
        super("ListenService");
    }

    /**
     * 在子线程中处理长连接
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(6000);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        while(true){
            try{
                if(null != serverSocket){
                    Socket socket = serverSocket.accept();
                    InputStream inputStream = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = reader.readLine();
                    if(line.equals(clientSocketService.GET_IP_HEAD)){
                        String clientAddr = socket.getInetAddress().toString();
                        clientAddr = clientAddr.replace("/", "");
                        //将该IP加入群组IP列表
                        boolean exists = false;
                        List<String> groupIpList = WiFiDirectActivity.GroupMemIpAddr;
                        for(int i = 0; i < groupIpList.size(); i++){
                            String curIp = groupIpList.get(i);
                            if(curIp.equals(clientAddr)){
                                exists = true;
                                break;
                            }
                        }
                        if(!exists){
                            WiFiDirectActivity.GroupMemIpAddr.add(clientAddr);
                        }

                        OutputStream outputStream = socket.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                        //客户端 地址
                        bufferedWriter.write(clientSocketService.IP_HEAD + clientAddr);
                        bufferedWriter.write("\n");

                        List<WifiP2pDevice> deviceList = WiFiDirectActivity.getGroupDeviceList();
                        for(int i = 0; i < deviceList.size(); i++){
                            WifiP2pDevice device = deviceList.get(i);
                            bufferedWriter.write(clientSocketService.DEVICE_HEAD);
                            bufferedWriter.write(device.deviceName);
                            bufferedWriter.write(clientSocketService.DEVICE_SPLIT);
                            bufferedWriter.write(String.valueOf(device.isGroupOwner()));
                            bufferedWriter.write("\n");
                        }
                        //Group Owner回复信息时，Activity中的字段不对
                        //解决方法，直接将本设备设置为GroupOwner，既然向本设备请求连接，那肯定是Owner
                        //if("true".equals(WiFiDirectActivity.HostIsGroupOwner)) {                            //把自己的地址和IP也写进来，标志换成是否是组长
                            bufferedWriter.write(clientSocketService.DEVICE_HEAD);
                            bufferedWriter.write(WiFiDirectActivity.ThisDevice.deviceName);
                            bufferedWriter.write(clientSocketService.DEVICE_SPLIT);
                            bufferedWriter.write("true");
                            bufferedWriter.write("\n");
                        //}

                        //socket结束标志位
                        bufferedWriter.write(clientSocketService.IP_END);
                        bufferedWriter.write("\n");
                        bufferedWriter.flush();
                    }
                    else if(line.equals(clientSocketService.SEND_MEM_HEAD)){
                        ArrayList<String> mapList = new ArrayList<>();
                        while(true){
                            //一直读取直到结束break;
                            line = reader.readLine();
                            if(line.startsWith(clientSocketService.DEVICE_HEAD)){
                                line = line.replace(clientSocketService.DEVICE_HEAD, "");
                                mapList.add(line);
                                continue;
                            }
                            else if(line.equals(clientSocketService.SEND_MEM_END)){
                                break;
                            }
                        }

                        OutputStream outputStream = socket.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                        //socket结束标志位
                        bufferedWriter.write(clientSocketService.RECEIVE_MEM_END);
                        bufferedWriter.write("\n");
                        bufferedWriter.flush();
                        //通知界面更新组内成员列表

                        //接收到回复的地址之后，发送广播以更新地址信息
                        Intent ipIntent = new Intent(clientSocketService.RECEIVE_GROUPLIST_ACTION);
                        ipIntent.putStringArrayListExtra(clientSocketService.GROUP_MEM_LIST, mapList);
                        m_BroadcastManager = LocalBroadcastManager.getInstance(this);
                        m_BroadcastManager.sendBroadcast(ipIntent);
                    }
                }
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
}
