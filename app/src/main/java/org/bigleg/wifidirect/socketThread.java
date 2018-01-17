package org.bigleg.wifidirect;

import android.net.wifi.p2p.WifiP2pDevice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by jack on 2018/1/17.
 */

public class socketThread extends Thread {
    private WifiP2pDevice m_dev;
    List<WifiP2pDevice> m_groupList;
    public socketThread(WifiP2pDevice dev, List<WifiP2pDevice> groupList){
        m_dev = dev;
        m_groupList = groupList;
    }
    @Override
    public void run(){
        Socket socket = null;
        try {
            socket = new Socket(m_dev.deviceAddress, 6000);
            OutputStream outputStream = socket.getOutputStream();
            byte[] buffer = new byte[4];
            buffer[0] = 'x';
            buffer[1] = 'x';
            buffer[2] = 'x';
            buffer[3] = 'x';
            outputStream.write(buffer, 0, 3);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
