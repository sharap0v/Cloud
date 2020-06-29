package View;

import Controller.NioServer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ServerGuiController
{
    public ListView<String> serverLog;
    private NioServer nioServer;
    private Thread serverThread;

    @FXML
    public TextField serverPort;

    public void writeServerLog(String logMessage) {
        System.out.println(logMessage);
        serverLog.getItems().add(logMessage);
    }

    public void serverStart(ActionEvent event){
        if(nioServer==null){
        try {
            nioServer = new NioServer(Integer.parseInt(serverPort.getText()),this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverThread = new Thread(nioServer);
        serverThread.start();
        writeServerLog("ServerStart on port "+serverPort.getText());}
    }

    public void serverStop(ActionEvent event) {
        if(serverThread!=null){
            serverThread.interrupt();
            nioServer = null;
            serverThread = null;
            writeServerLog("ServerStop");
        }

    }
}
