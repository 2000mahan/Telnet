import com.sun.net.ssl.internal.ssl.Provider;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class ClientServer
{
    private String host;
    private int port;
    private Socket socket;
    private ServerSocket serverSocket;
    private byte[] buf;
    private String received;
    private DataInputStream input;
    private DataOutputStream output;
    private FileInputStream fis;
    private FileOutputStream fos;
    private String fileName;


    public ClientServer(){

    }

    public void connectClient() throws IOException {
        InetAddress ip = InetAddress.getByName("localhost");
        this.socket = new Socket(ip, 5060);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    public void connectServer() throws IOException {
        this.serverSocket = new ServerSocket(5060);
        serverSocket.setSoTimeout(5000);
        try {
            serverSocket.setSoTimeout(50000);
            this.socket = serverSocket.accept();
            System.out.println("a client is connected : " + socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    public void serverSecuredConnection(String message) throws IOException, InterruptedException {
        //The Port number through which this server will accept client connections
        int port = 35786;
        /*Adding the JSSE (Java Secure Socket Extension) provider which provides SSL and TLS protocols
        and includes functionality for data encryption, server authentication, message integrity,
        and optional client authentication.*/
        Security.addProvider(new Provider());
        //specifing the keystore file which contains the certificate/public key and the private key
        System.setProperty("javax.net.ssl.keyStore","myKeyStore.jks");
        //specifing the password of the keystore file
        System.setProperty("javax.net.ssl.keyStorePassword","123456");
        //This optional and it is just to show the dump of the details of the handshake process
        System.setProperty("javax.net.debug","all");

            //SSLServerSocketFactory establishes the ssl context and and creates SSLServerSocket
            SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
            //Create SSLServerSocket using SSLServerSocketFactory established ssl context
            SSLServerSocket sslServerSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(port);
            System.out.println("Echo Server Started & Ready to accept Client Connection");
            //Wait for the SSL client to connect to this server
            SSLSocket sslSocket = (SSLSocket)sslServerSocket.accept();
            //Create InputStream to recive messages send by the client
            DataInputStream inputStream = new DataInputStream(sslSocket.getInputStream());
            //Create OutputStream to send message to client
            DataOutputStream outputStream = new DataOutputStream(sslSocket.getOutputStream());
            outputStream.writeUTF(message);
            //Keep sending the client the message you recive unless he sends the word "close"
            TimeUnit.SECONDS.sleep(10);
            inputStream.close();
            outputStream.close();
            sslSocket.close();
            sslServerSocket.close();



    }

    public void clientSecuredConnection() throws IOException {
        //The Port number through which the server will accept this clients connection
        int serverPort = 35786;
        //The Server Address
        String serverName = "localhost";
        /*Adding the JSSE (Java Secure Socket Extension) provider which provides SSL and TLS protocols
        and includes functionality for data encryption, server authentication, message integrity,
        and optional client authentication.*/
        Security.addProvider(new Provider());
        //specifing the trustStore file which contains the certificate & public of the server
        System.setProperty("javax.net.ssl.trustStore","myTrustStore.jts");
        //specifing the password of the trustStore file
        System.setProperty("javax.net.ssl.trustStorePassword","123456");
        //This optional and it is just to show the dump of the details of the handshake process
        System.setProperty("javax.net.debug","all");
            //SSLSSocketFactory establishes the ssl context and and creates SSLSocket
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            //Create SSLSocket using SSLServerFactory already established ssl context and connect to server
            SSLSocket sslSocket = (SSLSocket)sslsocketfactory.createSocket(serverName,serverPort);
            //Create OutputStream to send message to server
            DataOutputStream outputStream = new DataOutputStream(sslSocket.getOutputStream());
            //Create InputStream to read messages send by the server
            DataInputStream inputStream = new DataInputStream(sslSocket.getInputStream());
            //read the first message send by the server after being connected
            System.out.println(inputStream.readUTF());
            inputStream.close();
            outputStream.close();
            sslSocket.close();

    }

    public void clientCommand(String command) throws IOException {
        output.writeUTF(command);
       // output.close();
        output.flush();
    }

    public String serverCommand() throws IOException {
        String result = input.readUTF();
       // input.close();
        return result;
    }

    public void sendMessage(String request) throws IOException {
        byte [] bytes = request.getBytes();
        for(int i = 0;i < bytes.length;i++)
            output.writeByte(bytes[i]);
       // output.close();
        output.flush();

    }

    public void recieveMessage() throws IOException {
        byte[] data = new byte[4096];
        int counter = 0;
        while(true){
            try{
                data[counter] = input.readByte();
                counter++;
            }
            catch(EOFException e) {
              //  input.close();
                break;
            }
        }
        String result = new String(data, "UTF-8");
        System.out.println(result);

    }

    public void sendData(String filePath) throws IOException {
        File myFile = new File(filePath);
        FileInputStream in = new FileInputStream(myFile);
        byte[] data = new byte[999999];
        int counter = 0;
        while ((counter = in.read(data)) >= 0) {
            output.write(data, 0, counter);
        }
        in.close();
        output.close();
    }

    public void recieveData(String savingPath) throws IOException {
        File myFile = new File(savingPath);
        FileOutputStream ou = new FileOutputStream(myFile);
        byte[] data = new byte[999999];
        int counter = 0;
        while ((counter = input.read(data)) >= 0) {
            ou.write(data, 0, counter);
        }
        ou.close();

    }

    public ArrayList<Integer> findOpenPorts(String host, int start, int end){
        ArrayList<Integer> freePorts = new ArrayList<>();
        for(int i = start;i <= end;i++){
            try {
                Socket s = new Socket(host, i);
                freePorts.add(i);
                s.close();
            } catch (IOException e) {
                //pass
            }
        }

        return freePorts;
    }

    public byte[] getBuf() {
        return buf;
    }

    public DataInputStream getInput() {
        return input;
    }

    public DataOutputStream getOutput() {
        return output;
    }

    public FileInputStream getFis() {
        return fis;
    }

    public int getPort() {
        return port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getHost() {
        return host;
    }

    public String getReceived() {
        return received;
    }

    public FileOutputStream getFos() {
        return fos;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    public void setFis(FileInputStream fis) {
        this.fis = fis;
    }

    public void setFos(FileOutputStream fos) {
        this.fos = fos;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setInput(DataInputStream input) {
        this.input = input;
    }

    public void setOutput(DataOutputStream output) {
        this.output = output;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setReceived(String received) {
        this.received = received;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

}

