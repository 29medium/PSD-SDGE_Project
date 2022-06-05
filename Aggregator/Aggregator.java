package Aggregator;

import java.util.HashMap;
import java.util.Map;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Aggregator {
    private ZMQ.Socket pull;
    private ZMQ.Socket pub;
    private ZMQ.Socket rep;
    private Map<String,Device> devices;
    private Map<String,Integer> record;
    int record_total;

    public Aggregator(ZMQ.Socket pull, ZMQ.Socket pub, ZMQ.Socket rep, Map<String,Device> devices) {
        this.pull = pull;
        this.pub = pub;
        this.rep = rep;
        this.devices = devices;
        this.record = new HashMap<>();
        this.record_total = 0;
    }

    private void validateOffline(String type) {
        int online = (int) devices.values().stream().filter(x -> x.getType().equals(type) && x.getOnline()).count();
        if(online == 0) {
            pub.send("offline-" + type);
        }
    }

    private void validateRecord(String type) {
        if(record.containsKey(type)) {
            int current = (int) devices.values().stream().filter(x -> x.getType().equals(type) && x.isOnline()).count();
            if(current > record.get(type)){
                record.put(type, current);
                pub.send("record-" + type);
            }
        } else {
            record.put(type, 1);
            pub.send("record-" + type);
        }

        int current_total = (int) devices.values().stream().filter(x -> x.isOnline()).count();
        if(current_total>record_total) {
            record_total = current_total;
            pub.send("record");
        }
    }

    private void interpreter(String msg) {
        String[] args = msg.split(",");

        if(args[0].equals("login")) {
            if(devices.containsKey(args[1])) {
                Device d = devices.get(args[1]);
                d.setOnline(true);
                d.setActive(true);
                
                devices.put(args[1], d);
            } else {
                Device d = new Device(args[1], args[2], true, true);
                devices.put(args[1], d);
            }

            validateRecord(args[2]);
        } else if(args[0].equals("logout")) {
            Device d = devices.get(args[1]);
            d.setOnline(false);
            d.setActive(false);
            String type = d.getType();
            devices.put(args[1], d);

            validateOffline(type);
        } else if(args[0].equals("event")) {
            Device d = devices.get(args[1]);
            d.addEvent(args[2]);
            if(!d.isActive())
                d.setActive(true);
            devices.put(args[1], d);
        } else if(args[0].equals("inactive")) {
            Device d = devices.get(args[1]);
            d.setActive(false);
            devices.put(args[1], d);
        }
    }

    private void run(){
        while (true) {
            byte[] msg = pull.recv();
            interpreter(new String(msg));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if(args.length != 1)
            return;

        Map<String,Device> devices = new HashMap<>();

        try(ZContext context = new ZContext();
            ZMQ.Socket pull = context.createSocket(SocketType.PULL);
            ZMQ.Socket pub = context.createSocket(SocketType.PUB);
            ZMQ.Socket rep = context.createSocket(SocketType.REP))
        {
            int port_pull = Integer.parseInt(args[0]);
            int port_pub = port_pull + 1;
            int port_rep = port_pull + 2;

            pull.bind("tcp://localhost:" + port_pull);
            pub.bind("tcp://localhost:" + port_pub);
            rep.bind("tcp://localhost:" + port_rep);

            Thread t_rep = new Thread(new Reply(rep));
            t_rep.start();
            
            (new Aggregator(pull, pub, rep, devices)).run();;
        }
    }
}
