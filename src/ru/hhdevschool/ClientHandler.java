package ru.hhdevschool;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/***
 * Runnable class to receive messages from one client and send them to others in separate thread
 */
class ClientHandler implements Runnable{

    InputStreamReader in;
    OutputStreamWriter out;
    static List<ClientHandler> handlers = new java.util.concurrent.CopyOnWriteArrayList<ClientHandler>();

    public ClientHandler(Socket clientSocket) throws IOException {
        in = new InputStreamReader(clientSocket.getInputStream());
        out = new OutputStreamWriter(clientSocket.getOutputStream());
        handlers.add(this);
    }
    @Override
    public void run() {

        while(true){
            try{
            try {
                StringBuilder builder = new StringBuilder();
                String line;
                char ch;
                while((ch = (char)in.read()) != -1){
                    builder.append(ch);
                }
                line = builder.toString();
                if(line == null) continue;
                System.out.println("One of the clients just sent me this line : " + line);
                broadcast(line);
            } catch(EOFException e) {
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
            handler.out.write(line);
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
