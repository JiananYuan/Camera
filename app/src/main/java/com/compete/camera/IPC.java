package com.compete.camera;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class IPC {

    static private ByteArrayOutputStream bout;
    static private Socket socket = null;
    static private BufferedOutputStream buf = null;

    private static void sendImgMsg(Bitmap bitmap) {
        // 字节数组输出流
        bout = new ByteArrayOutputStream();
        // bitmap 压缩到字节数组输出流里面
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bout);
        try {
            buf.write(bout.toByteArray());
            buf.close(); socket.close(); bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send(Bitmap bmp, String host, int port) {
        if (host == null || host.equals("")) {
            return ;
        }
        new Thread(()->{
            try {
                socket = new Socket(host, port);
                buf = new BufferedOutputStream(new DataOutputStream(socket.getOutputStream()));
                sendImgMsg(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
