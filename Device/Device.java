package Device;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;


public class Device {

    public static Map<String,List<String>> parseEvents(String path) throws FileNotFoundException {
        File f = new File(path);
        Scanner s = new Scanner(f);
        String[] tokens;
        Map<String,List<String>> res = new HashMap<>();

        while(s.hasNextLine()) {
            tokens = s.nextLine().split(":");
            String[] events = tokens[1].split(",");
            res.put(tokens[0], Arrays.asList(events));
        }
        s.close();
        return res;
    }
    public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException, InterruptedException {

        if (args.length != 4){
            System.out.println("Argumentos inválidos");
            return;
        }
        int port = Integer.parseInt(args[0]);
        String id = args[1];
        String passwd = args[2];
        String type = args[3];
        
        Socket s = new Socket("localhost", port);
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

        out.write(("login " + id + " " + passwd + " " + type + "\n").getBytes());
        out.flush();

        byte[] arr = new byte[4096];
        int size = in.read(arr, 0, 4096);

        if(size > 0) {
            Map<String,List<String>> events = parseEvents("files/devices");
            
            int i = 10;
            while(i>0) {
                Thread.sleep(1000);
                String e = events.get(type).get(new Random().nextInt(events.size()));
                out.write(("event " + e + "\n").getBytes());
                out.flush();
                i--;
            }
        }
        s.close();
    }
}
