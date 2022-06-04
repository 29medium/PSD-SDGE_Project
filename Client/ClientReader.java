package Client;

import org.zeromq.ZMQ;

public class ClientReader implements Runnable{
    private ZMQ.Socket sub;

    public ClientReader(ZMQ.Socket sub) {
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