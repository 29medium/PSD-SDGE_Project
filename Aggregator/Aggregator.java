package Aggregator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

// Classe que representa um agregador
public class Aggregator {
    private ZMQ.Socket pull;
    private ZMQ.Socket pub;
    private ZMQ.Socket rep;
    private ZMQ.Socket pullAggregator;
    private ZMQ.Socket pushAggregator;
    private Set<Integer> neighbours;
    private int port;
    private CRDT crdt;
    private boolean changed;

    // Constutor da classe Agregador
    public Aggregator(ZMQ.Socket pull, ZMQ.Socket pub, ZMQ.Socket rep, ZMQ.Socket pullAggregator, ZMQ.Socket pushAggregator, Set<Integer> neighbours, int port) {
        this.pull = pull;
        this.pub = pub;
        this.rep = rep;
        this.pullAggregator = pullAggregator;
        this.pushAggregator = pushAggregator;
        this.neighbours = neighbours;
        this.port = port;
        this.crdt = new CRDT(port);
        this.changed = false;
    }

    // Método que envia uma mensagem sobre o estado de um dispositivo a todos os vizinhos
    private void sendMessage(String msg) {
        for(Integer i : this.neighbours){
            pushAggregator.send(msg);
        }
    }

    // Método responsável por tratar as mensagens recebidas dos coletores
    private void interpreter(String msg) {
        String[] args = msg.split(",");

        if(args[0].equals("login")) {
            if(this.crdt.containsKeyDevice(this.port, args[1])) {
                Device d = this.crdt.getDevice(this.port, args[1]);
                d.setOnline(true);
                d.setActive(true);
            } else {
                Device d = new Device(args[1], args[2], true, true);
                this.crdt.putDevice(this.port, args[1], d);
            }
            // Envia mensagem a avisar que o estado de um dispositivo foi alterado
            sendMessage(crdt.serializeMessage(args[1], "online", args[2]));

            // Verifica se novo record de tipo foi atingido e publica se for o caso 
            String record = crdt.validateRecord(args[2]);
            if(record!=null)
                pub.send(record);
            
            // Verifica se novo record total foi atingido e publica se for o caso 
            String record_total = crdt.validateRecordTotal(args[2]);
            if(record_total!=null)
                pub.send(record_total);

            // Verifica se percentagem foi ultrapassada e publica se for o caso
            for (String percent : crdt.validatePercentage())
                pub.send(percent);

        } else if(args[0].equals("logout")) {
            Device d = this.crdt.getDevice(this.port, args[1]);
            d.setOnline(false);
            d.setActive(false);
            String type = d.getType();

            // Envia mensagem a avisar que o estado de um dispositivo foi alterado
            sendMessage(crdt.serializeMessage(args[1], "offline", type));

            // Verifica se o numero de dispositivos offline do tipo é zero e publica se for o caso
            String offline = crdt.validateOffline(type);
            if(offline!=null)
                pub.send(offline);
            
            // Verifica se percentagem foi ultrapassada e publica se for o caso
            for (String percent : crdt.validatePercentage())
                pub.send(percent);

        } else if(args[0].equals("event")) {
            Device d = this.crdt.getDevice(this.port, args[1]);
            d.addEvent(args[2]);

            changed = true;

            if(!d.isActive()){
                String type = d.getType();
                d.setActive(true);

                // Envia mensagem a avisar que o estado de um dispositivo foi alterado
                sendMessage(crdt.serializeMessage(args[1], "active", type));
            }
        } else if(args[0].equals("inactive")) {
            Device d = this.crdt.getDevice(this.port, args[1]);
            d.setActive(false);
            String type = d.getType();
            
            // Envia mensagem a avisar que o estado de um dispositivo foi alterado
            sendMessage(crdt.serializeMessage(args[1], "inactive", type));
        }
    }

    // Método que executa os sockets em diferentes threads e schedulers 
    private void run(){
        // Thread responsável pelos pedidos REP-REQ do cliente
        Thread t_rep = new Thread(new Reply(rep, crdt));
        t_rep.start();

        // Thread responsável pelo PULL dos agregadores e PUSH para os agregadores
        Thread t_spread = new Thread(new Spread(pub, pullAggregator, pushAggregator, neighbours, crdt));
        t_spread.start();

        // Scheduler de 5 em 5 segundos fazer PUSH para os agregadores
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            if(changed) {
                for(Integer i : neighbours){
                    pushAggregator.send(crdt.serializeState());
                }
                changed = false;
            }
        }, 5, 5, TimeUnit.SECONDS);


        // Thread principal responsável por PULL do coletor e PUB para os clientes
        while (true) {
            byte[] msg = pull.recv();
            interpreter(new String(msg));
        }
    }

    // Função que dá parse à topologia e guarda as portas dos vizinhos
    public static Set<Integer> parseNeighbours(String path, int port) throws FileNotFoundException {
        File f = new File(path);
        Scanner s = new Scanner(f);
        String[] tokens;
        Set<Integer> res = new TreeSet<>();

        while(s.hasNextLine()) {
            tokens = s.nextLine().split(":");
            if(Integer.parseInt(tokens[0]) == (port)) {
                String[] args = (tokens[1].split(","));
                for(String str : args) {
                    res.add(Integer.parseInt(str));
                }
            }
        }

        return res;
    }
    
    // Função main do agregador
    public static void main(String[] args) throws InterruptedException {
        if(args.length != 1)
            return;

        // Criação do contexto e de todos os sockets necessários
        try(ZContext context = new ZContext();
            ZMQ.Socket pull = context.createSocket(SocketType.PULL);
            ZMQ.Socket pub = context.createSocket(SocketType.PUB);
            ZMQ.Socket rep = context.createSocket(SocketType.REP);
            ZMQ.Socket pushAggregator = context.createSocket(SocketType.PUSH);
            ZMQ.Socket pullAggreagtor = context.createSocket(SocketType.PULL))
        {
            int port_pull = Integer.parseInt(args[0]);
            int port_pub = port_pull + 1;
            int port_rep = port_pull + 2;
            int port_receiver = port_pull + 3;

            // Bind dos sockets às respetivas portas
            pull.bind("tcp://localhost:" + port_pull);
            pub.bind("tcp://localhost:" + port_pub);
            rep.bind("tcp://localhost:" + port_rep);
            pullAggreagtor.bind("tcp://localhost:" + port_receiver);

            Set<Integer> neighbours = parseNeighbours("files/topology", port_pull);

            // Connect do socket push aos seus vizinhos
            for(Integer p : neighbours) {
                int port_connect = p + 3;
                pushAggregator.connect("tcp://localhost:" + port_connect);
            }
            
            // Criação da classe agregador
            (new Aggregator(pull, pub, rep, pullAggreagtor, pushAggregator, neighbours, port_pull)).run();
            
        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro de topoligia não encontrado");
        }  catch ( ZMQException ignored ) {}
    }
}
