package Aggregator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Aggregator {
    private ZMQ.Socket pull;
    private ZMQ.Socket pub;
    private ZMQ.Socket rep;
    private ZMQ.Socket receiver;
    private Map<Integer,ZMQ.Socket> neighbours;
    private int port;
    private CRDT crdt;

    public Aggregator(ZMQ.Socket pull, ZMQ.Socket pub, ZMQ.Socket rep, ZMQ.Socket receiver, Map<Integer,ZMQ.Socket> neighbours, Set<String> nei, int port) {
        this.pull = pull;
        this.pub = pub;
        this.rep = rep;
        this.receiver = receiver;
        this.neighbours = neighbours;
        this.port = port;
        this.crdt = new CRDT(nei, port);
    }

    private void sendMessage(String msg) {
        for(ZMQ.Socket push : this.neighbours.values()){
            push.send(msg);
        }
    }

    private void interpreter(String msg) {
        String[] args = msg.split(",");

        if(args[0].equals("login")) {
            if(this.crdt.containsKeyDevice(this.port, args[1])) {
                Device d = this.crdt.getDevice(this.port, args[1]);
                d.setOnline(true);
                d.setActive(true);
                
                this.crdt.putDevice(this.port, args[1], d);
            } else {
                Device d = new Device(args[1], args[2], true, true);
                this.crdt.putDevice(this.port, args[1], d);
            }

            sendMessage(crdt.serializeMessage(args[1], "online", args[2]));

            String record = crdt.validateRecord(args[2]);
            if(record!=null) {
                pub.send(record);
            }
            
            String record_total = crdt.validateRecordTotal(args[2]);
            if(record_total!=null) {
                pub.send(record_total);
            }
        } else if(args[0].equals("logout")) {
            Device d = this.crdt.getDevice(this.port, args[1]);
            d.setOnline(false);
            d.setActive(false);
            String type = d.getType();

            this.crdt.putDevice(this.port, args[1], d);

            sendMessage(crdt.serializeMessage(args[1], "offline", type));

            String offline = crdt.validateOffline(type);
            if(offline!=null) {
                pub.send(offline);
            }
        } else if(args[0].equals("event")) {
            Device d = this.crdt.getDevice(this.port, args[1]);
            d.addEvent(args[2]);

            if(!d.isActive()){
                String type = d.getType();
                sendMessage(crdt.serializeMessage(args[1], "active", type));
                d.setActive(true);
            }
        
            this.crdt.putDevice(this.port, args[1], d);
        } else if(args[0].equals("inactive")) {
            Device d = this.crdt.getDevice(this.port, args[1]);
            d.setActive(false);
            String type = d.getType();
            sendMessage(crdt.serializeMessage(args[1], "inactive", type));

            this.crdt.putDevice(this.port, args[1], d);
        }
    }

    private void run(){
        // REP-REQ do cliente
        Thread t_rep = new Thread(new Reply(rep, crdt));
        t_rep.start();

        // PULL dos agregadores e PUSH para os agregadores
        Thread t_spread = new Thread(new Spread(this.receiver, this.neighbours, this.crdt));
        t_spread.start();

        // Scheduler x em x tempo fazer PUSH para os agregadores
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            for(ZMQ.Socket push : this.neighbours.values()){
                push.send(crdt.serializeState());
            }

        }, 15, 15, TimeUnit.SECONDS);

        // PULL do coletor e PUB para os clientes
        while (true) {
            byte[] msg = pull.recv();
            interpreter(new String(msg));
        }
    }

    public static Set<String> parseNeighbours(String path, int port) throws FileNotFoundException {
        File f = new File(path);
        Scanner s = new Scanner(f);
        String[] tokens;
        Set<String> res = null;

        while(s.hasNextLine()) {
            tokens = s.nextLine().split(":");
            if(Integer.parseInt(tokens[0]) == (port)) {
                res = new TreeSet<>( Arrays.asList(tokens[1].split(",")) );
            }
        }

        return res;
    }
    
    public static void main(String[] args) throws InterruptedException {
        if(args.length != 1)
            return;

        try(ZContext context = new ZContext();
            ZMQ.Socket pull = context.createSocket(SocketType.PULL);
            ZMQ.Socket pub = context.createSocket(SocketType.PUB);
            ZMQ.Socket rep = context.createSocket(SocketType.REP);
            ZMQ.Socket receiver = context.createSocket(SocketType.PULL))
        {
            int port_pull = Integer.parseInt(args[0]);
            int port_pub = port_pull + 1;
            int port_rep = port_pull + 2;
            int port_receiver = port_pull + 3;

            pull.bind("tcp://localhost:" + port_pull);
            pub.bind("tcp://localhost:" + port_pub);
            rep.bind("tcp://localhost:" + port_rep);
            receiver.bind("tcp://localhost:" + port_receiver);

            Set<String> nei = parseNeighbours("files/topology", port_pull);

            Map<Integer, ZMQ.Socket> neighbours = new HashMap<>();
            for(String s : nei) {
                try(ZMQ.Socket push = context.createSocket(SocketType.PUSH)) {
                    int port_push = Integer.parseInt(s);
                    int port_connect = port_push + 3;
                    push.connect("tcp://localhost:" + port_connect);
                    neighbours.put(port_push, push);
                }
            }
            
            (new Aggregator(pull, pub, rep, receiver, neighbours, nei, port_pull)).run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
