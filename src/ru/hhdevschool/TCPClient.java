package ru.hhdevschool;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by earlvik on 26.11.14.
 * Simple client able to send messages from standard input and receive all broadcasted messages
 */
public class TCPClient {

    private Selector selector;
    private SocketChannel channel;



    private String name;

    public String getName() {
        return name;
    }

    public TCPClient(){
        name = "Client "+(new Random()).nextInt();
    }

    public static void main(String[] ar) {
        int serverPort = 6666;
        //String address = "127.0.0.1";

        TCPClient client = new TCPClient();

        try {
            client.selector = Selector.open();
            client.channel = SocketChannel.open();
            client.channel.connect(new InetSocketAddress(serverPort));
            System.out.println("Trying to connect to a socket with IP address " + client.channel.getRemoteAddress()+ " and port " + serverPort);


            // Создаем поток для чтения с клавиатуры.
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            System.out.println("Type in something and press enter.");
            System.out.println();

            //Запускаем прослушиваение сообщений от сервера
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        String line="";
                        //line = in.readLine();
                        if(line == null) continue;
                        System.out.println("Got message: " + line);
                    }
                }
            }).start();

            while (true) {
                line = keyboard.readLine(); // ждем пока пользователь введет что-то и нажмет кнопку Enter.
                System.out.println("Sending this line to the server...");
                //out.println(line); // отсылаем введенную строку текста серверу.
                //out.flush();
               }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

}
