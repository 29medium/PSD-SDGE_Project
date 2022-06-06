package Aggregator;

import java.util.Map;

import org.zeromq.ZMQ;

public class Spread implements Runnable {
    private ZMQ.Socket receiver;
    private Map<Integer,ZMQ.Socket> neighbours;
    private CRDT crdt;
    
    public Spread(ZMQ.Socket receiver, Map<Integer,ZMQ.Socket> neighbours, CRDT crdt) {
        this.receiver = receiver;
        this.neighbours = neighbours;
        this.crdt = crdt;
    }

    public void run() {
        while(true) {
            byte[] msg = receiver.recv();

            crdt.deserialize(new String(msg));

            for(ZMQ.Socket push : neighbours.values()) {
                push.send(msg);
            }
        }
    }
}
