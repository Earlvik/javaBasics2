package ru.hhdevschool;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/***
 * Runnable class to receive messages from one client and send them to others in separate thread
 */
class ClientHandler implements Runnable{

    BufferedReader in;
    PrintWriter out;
    static List<ClientHandler> handlers = new java.util.concurrent.CopyOnWriteArrayList<ClientHandler>();

    public ClientHandler(Socket clientSocket) throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(),true);
        handlers.add(this);
    }
    @Override
    public void run() {
        while(true){
           try{
            try {
                String line;
                line = in.readLine();
                if(line == null) continue;
                System.out.println("One of the clients just sent me this line : " + line);
                broadcast(line);
            } catch(SocketException e) {
                System.out.println("One of clients disconnected");
                handlers.remove(this);
                in.close();
                out.close();
                return;
            }
            }catch(IOException e) {
                e.printStackTrace();

            }
        }

    }

    private synchronized void broadcast(String line) throws IOException {
        for(ClientHandler handler:handlers){
            if(handler == this) continue;
            handler.out.println(line);
            handler.out.flush();
        }
    }

    static public void CloseAll(){
        for(ClientHandler handler:handlers){
            try {
                handler.in.close();
                handler.out.close();
            }catch(IOException e ){
                e.printStackTrace();
            }
        }
    }
}
