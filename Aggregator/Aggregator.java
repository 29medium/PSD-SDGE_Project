package Aggregator;

import java.util.HashMap;
import java.util.Map;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Aggregator {
    private ZMQ.Socket pull;
    private ZMQ.Socket pub;
    private Map<String,Device> devices;
    private Map<String,Integer> record;
    int record_total;

    public Aggregator(ZMQ.Socket pull, ZMQ.Socket pub, Map<String,Device> devices) {
        this.pull = pull;
        this.pub = pub;
        this.devices = devices;
        this.record_total = 0;
    }

    private void intreperter(String msg) {
        System.out.println(msg);
        String[] args = msg.split("|");

        if(args[0]=="login") {
            if(devices.containsKey(args[1])) {
                Device d = devices.get(args[1]);
                d.setOnline(true);
                d.setActive(true);
                
                devices.put(args[1], d);
            } else {
                Device d = new Device(args[1], args[2], true, true);
                devices.put(args[1], d);
            }

            if(record.containsKey(args[2])) {
                int current = (int) devices.values().stream().filter(x -> x.getType().equals(args[2]) || x.isOnline()).count();
                if(current > record.get(args[2])){
                    record.put(args[2], current);
                    pub.send("record-" + args[2]);
                    System.out.println("record-" + args[2]);
                }
            } else {
                record.put(args[2], 1);
                pub.send("record-" + args[2]);
                System.out.println("record-" + args[2]);
            }

            int current_total = (int) devices.values().stream().filter(x -> x.isOnline()).count();
            if(current_total>record_total) {
                record_total = current_total;
                pub.send("record");
                System.out.println("record");
            }
        } else if(args[0]=="logout") {
            Device d = devices.get(args[1]);
            d.setOnline(false);
            d.setActive(false);
            devices.put(args[1], d);

            if(!devices.values().stream().filter(x -> x.getType().equals(args[2])).anyMatch(x -> x.getOnline())){
                pub.send("offline-" + args[2]);
                System.out.println("offline-" + args[2]);
            }
        } else if(args[0]=="event") {
            Device d = devices.get(args[1]);
            d.addEvent(args[2]);
            if(!d.isActive())
                d.setActive(true);
            devices.put(args[1], d);
        } else if(args[0]=="inactive") {
            Device d = devices.get(args[1]);
            d.setActive(false);
            devices.put(args[1], d);
        }
    }

    private void run(){
        while (true) {
            byte[] msg = pull.recv();
            intreperter(new String(msg));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Map<String,Device> devices = new HashMap<>();

        try(ZContext context = new ZContext();
            ZMQ.Socket pull = context.createSocket(SocketType.PULL);
            ZMQ.Socket pub = context.createSocket(SocketType.PUB)) 
        {
            pull.bind("tcp://localhost:" + args[0]);
            pub.bind("tcp://localhost:" + args[1]);
            
            (new Aggregator(pull, pub, devices)).run();;
        }
    }
}
