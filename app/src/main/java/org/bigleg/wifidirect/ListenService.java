package org.bigleg.wifidirect;

import android.app.IntentService;
import android.content.Intent;
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
                    String clientAddr = socket.getInetAddress().toString();

                    OutputStream outputStream = socket.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    bufferedWriter.write(clientAddr);
                    bufferedWriter.flush();
                }
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
}
