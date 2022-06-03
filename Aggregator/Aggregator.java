package Aggregator;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Aggregator {
    public static void main(String[] args) throws InterruptedException {

        try(ZContext context = new ZContext();
            ZMQ.Socket pub = context.createSocket(SocketType.PUB)) 
        {
            //ZMQ.Socket pull = context.createSocket(SocketType.PULL);
            //pull.connect("tcp://localhost:" + args[0]);
            pub.bind("tcp://localhost:" + args[0]);

            while(true) {
                Thread.sleep(500);

                pub.send("INFO");
            }
        }
    }
}
