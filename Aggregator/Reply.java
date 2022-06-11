package Aggregator;

import org.zeromq.ZMQ;

// Classe com thread que corre o socket reply
public class Reply implements Runnable {
    private ZMQ.Socket rep;
    private CRDT crdt;

    // Construtor da classe reply
    public Reply(ZMQ.Socket rep, CRDT crdt) {
        this.rep = rep;
        this.crdt = crdt;
    }

    // Método que intrepreta a mensagem do cliente e envia a resposta
    private void interpreter(String msg) {
        String res = null;
        String[] args = msg.split(",");

        if(args[0].equals("1")) {
            res = crdt.devicesOnline(args[1]);
        }
        else if(args[0].equals("2")) {
            res = crdt.deviceOnline(args[1]);
        }
        else if(args[0].equals("3")) {
            res = crdt.devicesActive();
        }
        else if(args[0].equals("4")) {
            res = crdt.events(args[1]);
        }

        rep.send(res);
    }
    
    // Método que corre a thread
    public void run() {
        while(true) {
            byte[] msg = rep.recv();
            interpreter(new String(msg));
        }
    }
}
