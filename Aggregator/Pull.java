package Aggregator;

import java.util.Map;

import org.zeromq.ZMQ;

public class Pull implements Runnable {
    private ZMQ.Socket pull;
    private ZMQ.Socket pub;
    private Map<String,Device> devices;

    public Pull(ZMQ.Socket pull, ZMQ.Socket pub, Map<String,Device> devices) {
        this.pull = pull;
        this.pub = pub;
        this.devices = devices;
    }

    private void intreperter(String msg) {
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
        } else if(args[0]=="logout") {
            Device d = devices.get(args[1]);
            d.setOnline(false);
            d.setActive(false);
            devices.put(args[1], d);
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

    @Override
    public void run() {
        while (true) {
            byte[] msg = pull.recv();
            intreperter(new String(msg));
            pub.send(new String(msg));
        }
    }
}
