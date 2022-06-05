package Aggregator;

import org.zeromq.ZMQ;

public class Reply implements Runnable {
    private ZMQ.Socket rep;

    public Reply(ZMQ.Socket rep) {
        this.rep = rep;
    }

    private void interpreter(String msg) {
        String[] args = msg.split(",");

        // Numero de dispositivos online no sistema
        if(args[0].equals("1")) {

        }
        // Se um dispositivo esta online
        else if(args[0].equals("2")) {

        }
        // Numero de dispositivos ativos no sistema
        else if(args[0].equals("3")) {

        }
        // Numero de eventos de um dado tipo ocorridos no sistema
        else if(args[0].equals("4")) {

        }
    }
    
    public void run() {
        while(true) {
            byte[] msg = rep.recv();
            interpreter(new String(msg));
        }
    }
}
