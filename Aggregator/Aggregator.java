package Aggregator;

import java.util.HashMap;
import java.util.Map;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Aggregator {
    public static void main(String[] args) throws InterruptedException {
        Map<String,Device> devices = new HashMap<>();

        try(ZContext context = new ZContext();
            ZMQ.Socket pull = context.createSocket(SocketType.PULL);
            ZMQ.Socket pub = context.createSocket(SocketType.PUB)) 
        {
            pull.bind("tcp://localhost:" + args[0]);
            pub.bind("tcp://localhost:" + args[1]);

            Thread t_pull = new Thread(new Pull(pull, pub, devices)); 

            t_pull.start();
        }
    }
}
