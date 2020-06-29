ChatOne设计文档

学习了通信方面的相关知识之后，在老师的号召下，结合大一的时候写过的Java课设，我决定写一下ChatOne这个app,我看了一下老师写的电脑版的ChatOne,是纯Java的，而且不能直接移植到Android Studio中，所以我决定按照我自己的想法进行编写。

ChatOne要求的是每个人都可以是服务器，也可以是客户端，也就是说，当进行群聊的时候，没有固定的服务器，谁被连接谁就是服务器，而连接者就是客户端，为了实现这个要求，我们需要在每个用户上都建立一个服务器和一个客户端。这里，为了操作简单，我默认每个用户都具有服务器的功能,当用户连接到某一服务器时，该用户就具有了客户端的功能。

首先，我们来在配置文件中添加一下权限，既然需要网络，我们就需要加上上网的权限和访问网络状态的权限，就像这样：
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

添加完了权限，我们就开始编写界面，界面总是与功能相对应，我们默认每个用户都是服务器，所以不需要为服务器功能设计组件，我们需要连接某一服务器使本机变为客户端，这就需要一个按钮来提示用户连接某一服务器，然后，我们还需要一个EditText来输入内容和一个按钮来发送内容，所以，我设计的界面就是这个样子：
                
可以看到，中间的部分是空出来的，这里放置了一个LinearLayout，用来显示聊天内容。

界面写好了我们接下来就开始逻辑的编写，首先，我们当然需要先初始化各组件，然后，我们要让用户成为一个服务器，首先，我们需要建立一个服务器，就像这样：
ss=new ServerSocket(8369);
8369是我设置的端口号，代表在本机的8369端口建立一个服务器。
然后，我们就开始监听是否有用户连接本服务器，监听是一个持续的过程，需要耗时，Android规定从4.0以后所有耗时的操作都不能在主线程中进行，必须在新建的线程中进行，所以我们用Handle和Runnable新建一个线程，在新建的线程中，我们需要监听是否有客户端连接，如果有客户端连接就会接收该客户端，否则就会阻塞，就像这样：
while(true){
    client=ss.accept();

    Lianjie lj=new Lianjie(client);
    allclient.add(lj);

    new Handler().post(lj);
}
当我们接收到连接的客户端时，我们新建了一个Lianjie变量并将其添加到了用户集合中，然后我们开启了这个线程，这个Lianjie变量是为了与客户端交互而设计的，Lianjie当监听到有用户发来消息后，接收该消息，将该消息显示在并将该消息转发给其他用户。

当然，作为服务器的用户也是可以聊天的，当作为服务器的用户点击发送按钮时，就会将该消息转发给所有的用户。

然后，我们再来写一下客户端的功能，当用户点击“连接其他用户”按钮时，会弹出如下提示框：
                     
在输入框中输入需要连接的用户的ip地址，查看手机ip地址的方法在https://jingyan.baidu.com/article/e3c78d6486b4273c4d85f564.html这里说的很清楚，输入需要连接的用户的ip地址之后点击连接按钮，就会触发事件监听器，就像这样：
client=new Socket(ip,8369);
dis= (DataInputStream) client.getInputStream();
dos= (DataOutputStream) client.getOutputStream();

isclient=true;
首先，我们新建一个socket来连接服务器，Socket有两个参数，第一个参数是要连接的服务器所在的ip地址，第二个参数是服务器所在的端口号，然后我们获取DataInputStream和DataOutputStream，然后设置变量isclient为true，代表当前用户已经成为了客户端。

然后，我们要给发送按钮设置事件监听器，其实之前我们已经给发送按钮设置事件监听器了，但只设置了作为服务器的用户的事件，作为客户端的用户的事件监听器并没有设置，所以这里我们需要进行判断，这就用到了isclient变量，isclient变量初始化为false，当用户成为客户端时，isclient变量变为true,当点击发送按钮时，进行判断，如果isclient变量为false,代表当前用户为服务器，就将该信息发送给所有的用户；如果isclient变量为true，代表该用户当前为客户端，点击发送按钮之后就将信息发给服务器，就像这样：
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

接下来，客户端需要接收服务器传来的信息，由于也是耗时操作，所以需要新建一个线程，在线程中监听服务器发来的信息，当接收到服务器传来的信息时，就把信息显示在自己的屏幕上：
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

这样，整个程序就写完了