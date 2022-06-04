package Aggregator;

import org.zeromq.ZMQ;

public class Publisher implements Runnable {
    private ZMQ.Socket pub;

    public Publisher(ZMQ.Socket pub) {
        this.pub = pub;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pub.send("INFO");
        }
    }
    
}
