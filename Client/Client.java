package Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

// Classe que representa um cliente
public class Client {
    private Controller controller;
    private ZMQ.Socket sub;
    private ZMQ.Socket req;

    // Contrutor da classe cliente
    public Client(ZMQ.Socket sub, ZMQ.Socket req, Map<String, List<String>> types) {
        this.sub = sub;
        this.req = req;
        this.controller = new Controller(req, sub, types);
    }

    // Método que incializa a thread do subscriber e corre o controlador
    private void run() {
        // Thread responsável pelo PUB-SUB
        Thread t_sub = new Thread(new Subscriber(sub));
        t_sub.start();
        
        // Começar o controlador
        controller.controller();
    }

    // Função que dá parse aos tipos de dispositvos e de eventos
    public static Map<String, List<String>> parseTypes(String path) throws FileNotFoundException {
        File f = new File(path);
        Scanner s = new Scanner(f);
        String[] tokens;
        Map<String, List<String>> res = new HashMap<>();

        while(s.hasNextLine()) {
            tokens = s.nextLine().split(":");
            res.put(tokens[0], new ArrayList<>( Arrays.asList(tokens[1].split(",")) ));
        }

        return res;
    }

    // Função main do clinete
    public static void main(String[] args) throws InterruptedException {
        if(args.length != 1)
            return;

        // Criação do contexto e dos sockets
        try(ZContext context = new ZContext();
            ZMQ.Socket sub = context.createSocket(SocketType.SUB);
            ZMQ.Socket req = context.createSocket(SocketType.REQ)) {

            int port_sub = Integer.parseInt(args[0]);
            int port_req = port_sub + 1;

            // Conect dos sockets ao agregador
            sub.connect("tcp://localhost:" + port_sub);
            req.connect("tcp://localhost:" + port_req);

            Map<String, List<String>> types = parseTypes("files/devices");

            // Criação da classe cliente
            (new Client(sub,req,types)).run();

        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro de dispositivos não encontrado");
        } catch ( ZMQException ignored ) {}
    }
}