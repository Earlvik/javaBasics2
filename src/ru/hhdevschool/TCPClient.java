package ru.hhdevschool;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

/**
 * Created by earlvik on 26.11.14.
 * Simple client able to send messages from standard input and receive all broadcasted messages
 */
public class TCPClient {
    public static void main(String[] ar) {
        int serverPort = 6666;
        String address = "127.0.0.1";

        try {
            InetAddress ipAddress = InetAddress.getByName(address);
            System.out.println("Trying to connect to a socket with IP address " + address + " and port " + serverPort);
            Socket socket = new Socket(ipAddress, serverPort);
            System.out.println("Connected to the server");


            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            final InputStreamReader in = new InputStreamReader(sin);
            OutputStreamWriter out = new OutputStreamWriter(sout);

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
                            String line;
                            char ch;
                            while((ch = (char)in.read()) != -1){
                                builder.append(ch);
                            }
                            line = builder.toString();
                            if(line == null) continue;
                            System.out.println("Got message: " + line);
                        }catch (EOFException e) {
                            System.out.println("Server stopped responding");
                            return;
                        }catch (IOException e) {
                            e.printStackTrace();

                        }
                    }
                }
            }).start();

            while (true) {
                line = keyboard.readLine(); // ждем пока пользователь введет что-то и нажмет кнопку Enter.
                System.out.println("Sending this line to the server...");
                out.write(line); // отсылаем введенную строку текста серверу.
                out.flush();
                            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

}
