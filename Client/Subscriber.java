package Client;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

// Classe com thread que corre o socket sub
public class Subscriber implements Runnable{
    private ZMQ.Socket sub;

    // Construtor da classe subscriber
    public Subscriber(ZMQ.Socket sub) {
        this.sub = sub;
    }

    // Método que corre a thread
    public void run() {
        try {
            while(true) {
                byte[] msg = this.sub.recv();
                View.printNotification(new String(msg));
            }   
        } catch(ZMQException ignored) {}
    }
}