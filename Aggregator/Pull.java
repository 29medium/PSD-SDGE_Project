package Aggregator;

import org.zeromq.ZMQ;

public class Pull implements Runnable {
    private ZMQ.Socket pull;

    public Pull(ZMQ.Socket pull) {
        this.pull = pull;
    }

    @Override
    public void run() {
        
    }
    
}
