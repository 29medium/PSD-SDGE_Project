package Client;

import org.zeromq.ZMQ;

public class Subscriber implements Runnable{
    private ZMQ.Socket sub;

    public Subscriber(ZMQ.Socket sub) {
        this.sub = sub;
    }

    @Override
    public void run() {

        while(true) {
            byte[] msg = this.sub.recv();
            System.out.println(new String(msg));
        }
    }
}