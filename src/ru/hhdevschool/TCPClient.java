package ru.hhdevschool;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by earlvik on 26.11.14.
 * Simple client able to send messages from standard input and receive all broadcasted messages
 */
public class TCPClient {
    private SocketChannel channel;
    private ByteBuffer messageBuffer = ByteBuffer.allocate(256);



    private String name;

    public String getName() {
        return name;
    }

    public TCPClient(){
        name = "Client "+(new Random()).nextInt(20);
    }

    @Override
    public String toString(){
        return name;
    }

    public static void main(String[] ar) throws IOException {
        int serverPort = 6666;


        final TCPClient client = new TCPClient();

        try {
            client.channel = SocketChannel.open();
            client.channel.configureBlocking(false);
            client.channel.connect(new InetSocketAddress(serverPort));
            System.out.println("Trying to connect to a socket with address " + client.channel.getRemoteAddress());
            while(!client.channel.finishConnect());
            System.out.println("Connection established");

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
                        try {
                            StringBuilder builder = new StringBuilder();
                            while( client.channel.read(client.messageBuffer) > 0){
                                client.messageBuffer.flip();
                                byte[] bytes = new byte[client.messageBuffer.limit()];
                                client.messageBuffer.get(bytes);
                                builder.append(new String(bytes));
                                client.messageBuffer.clear();
                                if(builder.toString().contains("\n")) break;
                            }
                            if(builder.length() == 0) continue;
                            String message = builder.toString();
                            System.out.println("Got message: "+message);
                        } catch (IOException e) {
                            System.out.println("Server has disconnected");
                            break;
                        }
                    }
                }
            }).start();

            while (true) {
                line = keyboard.readLine(); // ждем пока пользователь введет что-то и нажмет кнопку Enter.
                System.out.println("Sending this line to the server...");
                ByteBuffer messageBuffer = ByteBuffer.wrap(line.getBytes());
                client.channel.write(messageBuffer);
                messageBuffer.rewind();
            }
        } catch (Exception x) {
            x.printStackTrace();
        }finally{
            client.channel.close();
        }
    }

}