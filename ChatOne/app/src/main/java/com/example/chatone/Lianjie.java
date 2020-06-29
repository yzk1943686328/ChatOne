package com.example.chatone;

import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Lianjie implements Runnable{

    Socket client;
    DataInputStream dis;
    DataOutputStream dos;

    public Lianjie(Socket client){
        this.client=client;

        try {
            dis= (DataInputStream) client.getInputStream();
            dos= (DataOutputStream) client.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //将信息显示在服务器端的屏幕上
    public void showmessage(){

    }

    //将信息转发给其他用户
    public void sendmessage(String message){
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void run() {

        while(true){

            try {
                //接收客户端传来的信息
                String message=dis.readUTF();

                //将信息显示在自己的界面上
                MainActivity.addText(message);


                //将信息发给其他用户
                List<Lianjie> allclient=MainActivity.allclient;
                for(Lianjie client:allclient){
                    client.sendmessage(message);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }
}
