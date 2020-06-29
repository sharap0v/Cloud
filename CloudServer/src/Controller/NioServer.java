package Controller;

import View.ServerGuiController;
import lib.Library;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class NioServer implements Runnable {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(256);
    private int port;
    private static int clientCount = 0;
    private ServerGuiController serverGuiController;
    private String fileName;
    public boolean fileReception =false;
    private String receptionFileName = null;

    public NioServer(int port,ServerGuiController serverGuiController) throws IOException {
        this.port = port;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.serverGuiController = serverGuiController;
    }

    @Override
    public void run() {
        System.out.println("ServerStart on port "+port);
        try {
            Iterator<SelectionKey> iterator;
            SelectionKey key;
            while (!Thread.interrupted()) {
                int eventsCount = selector.select();
                System.out.println("Selected " + eventsCount + " events.");
                iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        handleAccess(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
            serverSocketChannel.close();
            selector.close();
            System.out.println("Server Stop");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAccess(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel)key.channel()).accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        StringBuilder message = new StringBuilder();
        int read = 0;
        byteBuffer.rewind();
        if(!fileReception){
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
        switch (msgType){
            case Library.AUTH_REQUEST:
                System.out.println(key.attachment());
                if(msg[1].equalsIgnoreCase("user")&&msg[2].equalsIgnoreCase("password")){
                    channel.register(selector, SelectionKey.OP_READ,msg[1]);
                    if(Files.notExists(Paths.get(msg[1]))){
                        Files.createDirectory(Paths.get(msg[1]));
                    }
                }else {
                    System.out.println("vsehana");
                    channel.close();
                }
                System.out.println(key.attachment());
                break;
            case Library.COPY_FILE_TO_SERVER:
                System.out.println(key.attachment());
                System.out.println(msg[1]);
                channel.write(ByteBuffer.wrap((Library.FILE_READY+"±1").getBytes()));
                receptionFileName = msg[1];
                System.out.println(msg[1]);
                System.out.println("копирование файла на сервер");
                fileReception = true;
                break;
        }}else {
            acceptFile(key, receptionFileName);
        }
    }

    private void handleWrite(SelectionKey key) {
    }

    private void acceptFile(SelectionKey key,String fileName){
        byteBuffer.rewind();
        try (FileChannel fileChannel = FileChannel.open(Paths.get(key.attachment()+"/"+fileName),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            fileChannel.transferFrom((SocketChannel) key.channel(), 0, Long.MAX_VALUE);
                } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Файл принят");
        fileReception = false;
        receptionFileName = null;
    }


}
