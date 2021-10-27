import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class peerOne {
    public static void main(String[] args) throws IOException, InterruptedException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        ClientServer peer = new ClientServer();
        ArrayList<String> history = new ArrayList<>();
        BufferedWriter writer = Files.newBufferedWriter(Paths.get("result.txt"));
        boolean ack = true;
        int counter = 0;
        String role = "";
        int flag = 0;
        while (true) {
            Scanner scan = new Scanner(System.in);

            while (!ack){ //synchronization
                if (peer.serverCommand().equals("true")) {
                    ack = true;
                    break;
                }
            }
            if(counter != 0 && flag != 1) {
                peer.clientCommand("true");
                peer.getSocket().close();
                if(role.equals("server"))
                    peer.getServerSocket().close();
            }
            flag = 0;
            counter++;
            ack = false;
            System.out.println("Do you want to enter a command?");
            String status = scan.nextLine();
            if(status.equals("yes")) {
                role = "client";
                scan.next(); //telnet
                String command = scan.next();
                String leftover = scan.nextLine();
                leftover = leftover.replaceFirst("^\\s*", ""); // removing begining white spaces
                if(command.equals("upload")){
                    peer.connectClient();
                    peer.clientCommand(command);
                    peer.sendData(leftover);    // leftover here is a file path
                    history.add("telnet upload " + leftover); // saving history
                    writer.write("telnet upload " + leftover);  // saving history
                    writer.newLine();
                    ack = true;
                    flag = 1;
                    peer.getSocket().close();
                    continue;

                }
                if(command.equals("exec")){
                    peer.connectClient();
                    peer.clientCommand(command);
                    peer.clientCommand(leftover);  // leftover here is a command
                    history.add("telnet exec " + leftover);  // saving history
                    writer.write("telnet exec " + leftover);  // saving history
                    writer.newLine();
                }
                if(command.equals("send")){
                    peer.connectClient();
                    String[] splited = leftover.split(" ");
                    if(!(splited[0].equals("-e"))) {
                        peer.clientCommand(command);
                        peer.clientCommand(leftover); // leftover here is a message
                        history.add("telnet send " + leftover);  // saving history
                        writer.write("telnet send " + leftover); // saving history
                        writer.newLine();
                    }
                    else{
                        peer.clientCommand("send -e");
                        peer.getSocket().close();
                        leftover = leftover.replace("-e ", "");
                        history.add("telnet send -e " + leftover);  // saving history
                        writer.write("telnet send -e " + leftover); // saving history
                        writer.newLine();
                        peer.serverSecuredConnection(leftover);
                        ack = true;
                        flag = 1;
                        continue;

                    }
                    flag = 0;
                }

                if(command.equals("history")){
                    peer.connectClient();
                    peer.clientCommand(command);
                    BufferedReader reader = Files.newBufferedReader(Paths.get("result.txt"));
                    writer.close();

                    for(int i = 0;i < history.size();i++)
                        System.out.println(reader.readLine());

                    writer = Files.newBufferedWriter(Paths.get("result.txt"));
                    for(int i = 0;i < history.size();i++)
                        writer.write(history.get(i));

                }



               /* peer.connectClient(host, port);
                ArrayList<String> array = new ArrayList<>();
                scan.nextLine();
                while (true) {
                    String input;
                    if (!(input = scan.nextLine()).equals("")) {
                        array.add(input);
                    } else
                        break;
                }
                String request = String.join("\r\n", array);
                request = request + "\r\n" + "\r\n";
                peer.sendMessage(request);
                peer.recieveMessage();*/

            }
            else if(status.equals("no")){
                role = "server";
                peer.connectServer();
                String result = peer.serverCommand();
                if(result.equals("upload")){
                    System.out.println("where do you want to save the file?");
                    String path = scan.nextLine();
                    peer.recieveData(path);
                    ack = true;
                    flag = 1;
                    peer.getSocket().close();
                    peer.getServerSocket().close();
                    continue;
                }
                if(result.equals("exec")){
                    String command = peer.serverCommand(); // command
                    String s;
                    Process p;
                    try {
                        p = Runtime.getRuntime().exec(command);
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(p.getInputStream()));
                        while ((s = br.readLine()) != null)
                            System.out.println("line: " + s);
                        p.waitFor();
                        System.out.println ("exit: " + p.exitValue());
                        p.destroy();
                    } catch (Exception e) {}
                }
                if(result.equals("send")){
                    System.out.println(peer.serverCommand());
                }

                if(result.equals("send -e")){
                    peer.getSocket().close();
                    peer.getServerSocket().close();
                    TimeUnit.SECONDS.sleep(20);
                    peer.clientSecuredConnection();
                    ack = true;
                    flag = 1;
                    continue;


                }
                flag = 0;

                peer.clientCommand("true");
            }
            else if(status.equals("exit")){
                writer.close();
                System.exit(0);
            }
        }
    }
}

