package ru.hhdevschool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by earlvik on 26.11.14.
 */
public class TCPServer {

    private final ByteBuffer welcomeBuffer = ByteBuffer.wrap("You are connected to the server!".getBytes());
    private Selector clientSelector;
    private ServerSocketChannel serverSocketChannel;
    private ByteBuffer messageBuffer = ByteBuffer.allocate(256);
    private int port;
    private Set<SocketChannel> socketSet = Collections.synchronizedSet(new HashSet<SocketChannel>());

    public TCPServer(int port) throws IOException{
        this.port = port;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        clientSelector =Selector.open();
        serverSocketChannel.register(clientSelector, SelectionKey.OP_ACCEPT);
    }

    public TCPServer() throws IOException {
        this(6666);
    }

        public static void main(String[] args) throws IOException {
                final TCPServer server = new TCPServer();
                server.start();
        }


    public void start() throws IOException {
        System.out.println("Started server at port "+this.port);

        Iterator<SelectionKey> iterator;
        SelectionKey key;
        while(this.serverSocketChannel.isOpen()) {
            this.clientSelector.select();
            iterator = this.clientSelector.selectedKeys().iterator();
            while(iterator.hasNext()) {
                key = iterator.next();
                iterator.remove();

                if(key.isAcceptable()) this.accept(key);
                if(key.isReadable()) this.read(key);
            }
        }
        this.serverSocketChannel.close();
        this.clientSelector.close();
    }
    private void accept(SelectionKey key) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel)key.channel()).accept();
        String address = (new StringBuilder( socketChannel.socket().getInetAddress().toString() )).append(":")
                .append(socketChannel.socket().getPort()).toString();

        socketChannel.configureBlocking(false);
        socketChannel.register(clientSelector, SelectionKey.OP_READ, address);
        socketChannel.write(welcomeBuffer);
        welcomeBuffer.rewind();
        socketSet.add(socketChannel);
        System.out.println("Connected client at: "+address);

    }

    private void read(final SelectionKey key) throws IOException {
        final SocketChannel socketChannel = (SocketChannel)key.channel();
        StringBuilder builder = new StringBuilder();
        messageBuffer.clear();
        int read = -1;
        try {
            while ((read = socketChannel.read(messageBuffer)) > 0) {
                messageBuffer.flip();
                byte[] bytes = new byte[messageBuffer.limit()];
                messageBuffer.get(bytes);
                builder.append(new String(bytes));
                messageBuffer.clear();
            }
            final String message;
            if (read < 0) {
                message = key.attachment() + " has left the chat\n";
                socketSet.remove(socketChannel);
                socketChannel.close();
            } else {
                message = key.attachment() + ": " + builder.toString();
            }
            System.out.println("Got message: " + message);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        broadcast(message, socketChannel);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch(IOException e){
            System.out.println("Client has disconnected: "+key.attachment());
        }

    }

    private void broadcast(String message, SocketChannel sender) throws IOException {
        ByteBuffer messageBuffer = ByteBuffer.wrap(message.getBytes());
        for(SocketChannel socketChannel: socketSet){
            if(socketChannel!=sender){
                socketChannel.write(messageBuffer);
                messageBuffer.rewind();
            }
        }
    }
}



