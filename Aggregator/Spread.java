package Aggregator;

import java.util.Set;

import org.zeromq.ZMQ;

// classe que trata da receção de mensagens de outros agregadores e faz também o envio
public class Spread implements Runnable {
    private ZMQ.Socket pub;
    private ZMQ.Socket pullAggregator;
    private ZMQ.Socket pushAggregator;
    private Set<Integer> neighbours;
    private CRDT crdt;
    
    // Construtor da classe spread
    public Spread(ZMQ.Socket pub, ZMQ.Socket pullAggregator,ZMQ.Socket pushAggregator, Set<Integer> neighbours, CRDT crdt) {
        this.pub = pub;
        this.pullAggregator = pullAggregator;
        this.pushAggregator = pushAggregator;
        this.neighbours = neighbours;
        this.crdt = crdt;
    }

    // Método que corre a thread
    public void run() {
        while(true) {
            // recebe do socket PULL
            byte[] msg = pullAggregator.recv();

            // verifica se mensagem é válida e se alterou o estado
            if(crdt.deserialize(new String(msg))) {
                // se alterou o estado, envia para todos os seus vizinhos a mensagem serializada
                for(Integer i : neighbours) {
                    pushAggregator.send(msg);
                }
            }
            
            // Verfica se tem algo para notificar face ao aumento ou diminuição de utilizadores online
            for (String percent : crdt.validatePercentage())
                // se sim, faz publish da mensagem correspondente
                pub.send(percent);
        }
    }
}
