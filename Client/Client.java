package Client;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Client {
    public static void main(String[] args) throws InterruptedException {

        try(ZContext context = new ZContext();
            ZMQ.Socket sub = context.createSocket(SocketType.SUB)) 
        {
            //ZMQ.Socket pull = context.createSocket(SocketType.PULL);
            //pull.connect("tcp://localhost:" + args[0]);
            sub.connect("tcp://localhost:" + args[0]);
            
            if (args.length == 1)
                sub.subscribe("".getBytes());
            else for (int i = 1; i < args.length; i++)
                sub.subscribe(args[i].getBytes());

            while(true) {
                byte[] msg = sub.recv();
                System.out.println(new String(msg));
            }
        }
    } 
}
