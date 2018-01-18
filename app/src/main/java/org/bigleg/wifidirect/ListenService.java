package org.bigleg.wifidirect;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.Nullable;

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
import java.util.List;

import static java.lang.System.in;

/**
 * Created by jack on 2018/1/18.
 */

public class ListenService extends IntentService {

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
        while(true){
            try {
                ServerSocket serverSocket = new ServerSocket(6000);
                while (true){
                    Socket socket = serverSocket.accept();
                    InputStream inputStream = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = reader.readLine();
                    if(line.equals(clientSocketService.GET_IP_HEAD)){
                        String clientAddr = socket.getInetAddress().toString();
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
                            bufferedWriter.write(device.deviceAddress);
                            bufferedWriter.write(clientSocketService.DEVICE_SPLIT);
                            bufferedWriter.write(device.status + "");
                            bufferedWriter.write(clientSocketService.DEVICE_SPLIT);
                            bufferedWriter.write(device.primaryDeviceType);
                            bufferedWriter.write(clientSocketService.DEVICE_SPLIT);
                            bufferedWriter.write(String.valueOf(device.isGroupOwner()));
                            bufferedWriter.write("\n");
                        }
                        //TODO 把自己的地址和IP也写进来，标志换成是否是组长
                        //socket结束标志位
                        bufferedWriter.write(clientSocketService.IP_END);
                        bufferedWriter.write("\n");
                        bufferedWriter.flush();
                    }
                }
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
}
