package Device;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

// classe que define um protótipo de um device
public class Device {
    // método que devolve lista de eventos por tipo
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
        // inicialização dos argumentos
        int port = Integer.parseInt(args[0]);
        String id = args[1];
        String passwd = args[2];
        String type = args[3];
        
        // inicialização de sockets e buffers de leitura/escrita
        Socket s = new Socket("localhost", port);
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

        // inicialização de um counter só para efeitos de teste e verificação
        Map<String,Integer> counter = new HashMap<>();

        // pedido de login
        out.write(("login " + id + " " + passwd + " " + type + "\n").getBytes());
        out.flush();

        byte[] arr = new byte[4096];
        // receber a resposta
        int size = in.read(arr, 0, 4096);

        if(size > 0) {
            // obter lista de eventos disponíveis por cada tipo
            Map<String,List<String>> events = parseEvents("files/devices");
            
            int i = 0;
            // iteração de 100 vezes para enviar eventos aleatórios dentro do mesmo tipo
            while(i<100) {
                Thread.sleep(100);
                // obter um random evento dentro do tipo do dispositivo
                String e = events.get(type).get(new Random().nextInt(events.get(type).size()));

                // inicializar o counter com 0 caso não haja esse evento como key
                counter.putIfAbsent(e, 0);
                // aumentar o counter do evento
                counter.put(e,counter.get(e)+1);
                // enviar evento pelo socket TCP para o coletor
                out.write(("event " + e + "\n").getBytes());
                out.flush();
                i++;
            }
            // print do counter para verificação
            for(Map.Entry<String,Integer> entry : counter.entrySet()){
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }
        }
        // fecho do socket
        s.close();
    }
}
