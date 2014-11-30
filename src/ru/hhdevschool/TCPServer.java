package ru.hhdevschool;

import ru.hhdevschool.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by earlvik on 26.11.14.
 */
public class TCPServer {
    List<Socket> clients = new ArrayList<Socket>();
        public static void main(String[] args)    {
            int port = 6666;
            try {
                ServerSocket ss = new ServerSocket(port);
                System.out.println("Waiting for a client...");
               while(true) {
                    Socket socket = ss.accept();
                    System.out.println("Got a client");
                    System.out.println();
                    new Thread(new ClientHandler(socket)).start();
                }
            } catch(Exception x) { x.printStackTrace(); }
            finally {
               ClientHandler.CloseAll();

            }
        }
}


