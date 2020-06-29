package com.example.chatone;

import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //声明各变量
    Button connectbutton;
    static LinearLayout chatarea;
    EditText inputbox;
    Button sendbutton;

    static TextView textView;


    //服务器相关变量
    ServerSocket ss;//在本机上建立的服务器
    static List<Lianjie> allclient=new ArrayList<>();//所有连接到本服务器的客户端


    //客户端相关变量
    Socket client;
    DataInputStream dis;
    DataOutputStream dos;

    Boolean isclient=false;//是否为客户端



    public void initviews(){
        connectbutton=findViewById(R.id.connectbutton);
        chatarea=findViewById(R.id.chatarea);
        inputbox=findViewById(R.id.inputbox);
        sendbutton=findViewById(R.id.sendbutton);
        textView=new TextView(this);

    }


    //成为服务器
    public void BeAServer(){

        //建立服务器线程
        Handler ServerHandle=new Handler();
        Runnable ServerRunnable=new Runnable() {
            @Override
            public void run() {

                try {

                    //在本机的8369端口建立一个服务器
                    ss=new ServerSocket(8369);

                    while(true){

                        client=ss.accept();

                        Lianjie lj=new Lianjie(client);
                        allclient.add(lj);

                        new Handler().post(lj);

                    }


                } catch (IOException e) {
                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }


            }
        };

        ServerHandle.post(ServerRunnable);

    }

    public static void addText(String message){
        textView.setText(message);
        chatarea.addView(textView);

    }


    //成为一个客户端
    public void BeAClient(){

        connectbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText iptetxt=new EditText(MainActivity.this);

                //弹出输入框提示用户输入要连接的ip地址
                AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("请输入要连接的ip地址");
                adb.setView(iptetxt);

                adb.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //连接该服务器
                        String ip=iptetxt.getText().toString();

                        try {

                            client=new Socket(ip,8369);
                            dis= (DataInputStream) client.getInputStream();
                            dos= (DataOutputStream) client.getOutputStream();

                            isclient=true;

                            //建立线程接收服务器发来的消息
                            Handler RecieveHandle=new Handler();
                            Runnable RecieveRunnable=new Runnable() {
                                @Override
                                public void run() {

                                    while(true){

                                        try {
                                            String message=dis.readUTF();

                                            //将该信息显示在屏幕上
                                            TextView textView=new TextView(MainActivity.this);
                                            textView.setText(message);
                                            chatarea.addView(textView);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }


                                }
                            };

                            RecieveHandle.post(RecieveRunnable);


                        } catch (IOException e) {
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                adb.show();

            }
        });
    }

    //给发送按钮设置事件监听器
    public void sendAction(){
        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=inputbox.getText().toString();
                if(message.isEmpty()){
                    Toast.makeText(MainActivity.this,"输入不能为空",Toast.LENGTH_SHORT).show();
                }else{

                    //如果输入不为空,判断当前用户是服务器还是客户端
                    if(isclient==false) {

                        //如果当前用户为服务器，则将信息发送给所有用户
                        for (Lianjie client : allclient) {
                            client.sendmessage(message);
                        }

                    }else{

                        //如果是客户端，就将信息发送到服务器
                        try {
                            dos.writeUTF(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }

                }
            }
        });
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initviews();

        //在本机上建立一个服务器
        //BeAServer();

        //建立一个客户端
        BeAClient();

        //给发送按钮设置事件监听器
        sendAction();






    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

