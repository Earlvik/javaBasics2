package ru.hhdevschool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/***
 * Runnable class to receive messages from one client and send them to others in separate thread
 */
class ClientHandler implements Runnable{

    DataInputStream in;
    DataOutputStream out;
    static List<ClientHandler> handlers = new ArrayList<ClientHandler>();

    public ClientHandler(Socket clientSocket) throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());
        handlers.add(this);
    }
    @Override
    public void run() {
        String line;
        while(true){
            try{
            try {
                line = in.readUTF();
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
            handler.out.writeUTF(line);
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
