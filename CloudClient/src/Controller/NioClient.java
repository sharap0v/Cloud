package Controller;

import lib.Library;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;


import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.Iterator;

public class NioClient implements Runnable{
    private int port;
    private String ip;
    private String password;
    private String userName;
    private SocketChannel clientChannel;
    private Selector selector;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(128);
    public static String clientMessage;
    public boolean fileReception =false;
    public String transferFileName = null;

    public NioClient(String port, String ip, String password, String userName) {
        this.port = Integer.parseInt(port);
        this.ip = ip;
        this.password = password;
        this.userName = userName;
    }

    @Override
    public void run() {
        try {
            clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(ip,port));
            selector = Selector.open();
            clientChannel.register(selector, SelectionKey.OP_CONNECT);
            SelectionKey key;
            Iterator<SelectionKey> iterator;
            while (!Thread.interrupted()){
                int eventsCount = selector.select();
                iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()){
                    key = iterator.next();

                    //System.out.println("1");
                    if (!key.isValid()) {
                        System.out.println("break");
                        break;
                    }
                    if(key.isValid() & key.isConnectable()){
                        System.out.println("Соединение");
                        handleConnection(key);
                    }
                    if(key.isReadable()){
                       System.out.println("read");
                       handleRead(key);
                    }
                    if(key.isWritable()){
                       //System.out.println("write");
                        handleWrite(key);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        if(clientMessage!=null){
        SocketChannel channel = ((SocketChannel) key.channel());
        String[] msg = clientMessage.split(Library.DELIMITER);
        String msgType = msg[0];
        switch (msgType){
            case Library.COPY_FILE_TO_SERVER:
                channel.write(ByteBuffer.wrap(clientMessage.getBytes()));
                channel.register(selector, SelectionKey.OP_READ);
                transferFileName = msg[1];
                clientMessage=null;
                break;
        }}
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        StringBuilder message = new StringBuilder();
        //channel.read(byteBuffer);
        System.out.println(byteBuffer);
        int read = 0;
        byteBuffer.rewind();
        while ((read = channel.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            byte[] bytes = new byte[byteBuffer.limit()];
            byteBuffer.get(bytes);
            message.append(new String(bytes));
            byteBuffer.rewind();
        }
        System.out.println(message);
        String[] msg = message.toString().split(Library.DELIMITER);
        String msgType = msg[0];
        System.out.println("1"+msg[0]);
        if (Library.FILE_READY.equals(msgType)) {
            channel.register(selector, SelectionKey.OP_WRITE);
            sendFile(channel,transferFileName);
        }
    }

    private void handleConnection(SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        if(channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.write(ByteBuffer.wrap(Library.getAuthRequest(userName, password).getBytes()));
        channel.register(selector, SelectionKey.OP_WRITE);
    }
    private void sendFile(SocketChannel channel,String fileName) throws IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get(fileName));
            fileChannel.transferTo(0,fileChannel.size(),channel);
        System.out.println("Файл передан");
    }
}
