package org.bigleg.wifidirect;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jack on 2018/1/19.
 * groupOwner在组内成员列表变化时，向所有成员发送成员列表
 */

public class ownerSendMemListSocketService extends IntentService {
    public ownerSendMemListSocketService(String name) {
        super(name);
    }
    public ownerSendMemListSocketService() {
        super("ownerSendMemListSocketService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try{
            //owner的groupChanged里要写入下面这个信息
            List<String> ipList = intent.getStringArrayListExtra(clientSocketService.GROUP_MEM_LIST);
            List<WifiP2pDevice> devList = WiFiDirectActivity.getGroupDeviceList();//这些是要写入的
            for(int i = 0; i < ipList.size(); i++){
                String curIp = ipList.get(i);
                Thread curThread = new SendGrpmemThread(curIp, devList);
                curThread.run();
            }
            //this.stopSelf();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private class SendGrpmemThread extends Thread{
        private String m_ip;
        private List<WifiP2pDevice> m_devList;
        public SendGrpmemThread(String ip, List<WifiP2pDevice> devList){
            m_ip = ip;
            m_devList = devList;
        }
        @Override
        public void run(){
            try {
                Socket socket = new Socket(m_ip, 6000);
                OutputStream outputStream = socket.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(clientSocketService.SEND_MEM_HEAD);
                bufferedWriter.write("\n");
                for (int i = 0; i < m_devList.size(); i++){
                    WifiP2pDevice device = m_devList.get(i);
                    bufferedWriter.write(clientSocketService.DEVICE_HEAD);
                    bufferedWriter.write(device.deviceName);
                    bufferedWriter.write(clientSocketService.DEVICE_SPLIT);
                    bufferedWriter.write("false");
                    bufferedWriter.write("\n");
                }
                //写自身
                String curHostName = WiFiDirectActivity.ThisDevice.deviceName;
                bufferedWriter.write(clientSocketService.DEVICE_HEAD);
                bufferedWriter.write(curHostName);
                bufferedWriter.write(clientSocketService.DEVICE_SPLIT);
                bufferedWriter.write("true");
                bufferedWriter.write("\n");

                bufferedWriter.write(clientSocketService.SEND_MEM_END);
                bufferedWriter.write("\n");
                bufferedWriter.flush();

                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while(true) {
                    String line = bufferedReader.readLine();
                    //服务器（client）接收完成，关闭连接
                    if(line.equals(clientSocketService.RECEIVE_MEM_END)){
                        socket.close();
                        break;
                    }
                }
                //TODO ??
                //this.destroy();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
