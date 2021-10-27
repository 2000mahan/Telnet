import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        while (true) {
            Scanner scan = new Scanner(System.in);
            System.out.println("what do you want to do?");
            String task = scan.nextLine();
            if (task.equals("send request")) {
                scan.next(); // telnet
                String host = scan.next(); // host
                int port = scan.nextInt(); // port
                client.connect(host, port);
                scan.nextLine();
                if (port == 80) {
                    ArrayList<String> array = new ArrayList<>();
                    while (true) {
                        String input;
                        if (!(input = scan.nextLine()).equals("")) {
                            array.add(input);
                        } else
                            break;
                    }
                    String request = String.join("\r\n", array);
                    request = request + "\r\n" + "\r\n";
                    client.sendMessage(request);
                    client.recieveMessage();

                }
                if (port == 25) {
                    ArrayList<String> array = new ArrayList<>();
                    int count = 0;
                    while (true) {
                        String input = scan.nextLine();
                        if (!input.equals("QUIT")) {

                            if (!array.isEmpty() && array.get(array.size() - 1).equals("DATA") && count >= 3) {
                                scan.nextLine();
                                array.add(input + "\n" + "." + "\n");
                            } else
                                array.add(input + "\n");


                            client.sendMessage(array.get(count));
                            client.recieveMessage();
                            count++;

                        } else {
                            client.sendMessage(input + "\n");
                            client.recieveMessage();
                            break;
                        }


                    }


                }

            }
            else if(task.equals("find open ports")){
                String host = scan.next();
                int start = scan.nextInt();
                int end = scan.nextInt();
                scan.nextLine();
                ArrayList<Integer> freePorts = client.findOpenPorts(host, start, end);
            }
        }
    }
}
