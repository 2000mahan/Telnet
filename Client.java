import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client
{
    private String host;
    private int port;
    private Socket socket;
    private byte[] buf;
    private String received;
    private DataInputStream input;
    private DataOutputStream output;
    private FileInputStream fis;
    private FileOutputStream fos;
    private long fileSize;
    private String fileName;


    public Client(){

    }

    public void connect(String host, int port) throws IOException {
        InetAddress ip = InetAddress.getByName(host);
        this.host = host;
        this.port = port;
        this.socket = new Socket(ip, port);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());

    }

    public void sendMessage(String request) throws IOException {

        byte [] bytes = request.getBytes();
        for(int i = 0;i < bytes.length;i++)
            output.writeByte(bytes[i]);


    }

    public void recieveMessage() throws IOException {
        byte[] data = new byte[4096];
        int counter = 0;
        socket.setSoTimeout(600);
        while(true){
            try{
                data[counter] = input.readByte();
                counter++;
            }
            catch(SocketTimeoutException ex) {
                break;
            }
            catch (IOException e){
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
                SocketAddress sockaddr = new InetSocketAddress(host, i);
                Socket s = new Socket();
                int timeout = 5000;   // 5000 millis = 5 seconds
                s.connect(sockaddr, timeout);
                freePorts.add(i);
                System.out.println(i);
            } catch (IOException e) {

            }
        }

        return freePorts;
    }

}

