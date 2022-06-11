package Aggregator;

import java.util.HashMap;
import java.util.Map;

// classe que representa um device
public class Device {
    private String id;
    private String type;
    private boolean online;
    private boolean active;
    private Map<String, Integer> events;

    // Inicialização do Device por argumentos
    public Device(String id, String type, boolean online, boolean active) {
        this.id = id;
        this.type = type;
        this.online = online;
        this.active = active;
        this.events = new HashMap<>();
    }
    
    // Inicialização do Device por uma String serializada
    public Device(String serialized){
        this.deserialize(serialized);
    }

    // método que retorna o id
    public String getId() {
        return this.id;
    }
    // método que altera o id
    public void setId(String id) {
        this.id = id;
    }
    // método que retorna o tipo
    public String getType() {
        return this.type;
    }

    // método que altera o tipo
    public void setType(String type) {
        this.type = type;
    }

    // metodo que retorsa de device se encontra online
    public boolean isOnline() {
        return this.online;
    }

    // metodo que altera o estado de online
    public void setOnline(boolean online) {
        this.online = online;
    }
    // método que retorna se dispositivo se encontra ativo
    public boolean isActive() {
        return this.active;
    }

    // metodo que altera o estado de atividade
    public void setActive(boolean active) {
        this.active = active;
    }

    // metodo que insere um novo evento / incrementa o counter
    public void addEvent(String event) {
        if(events.containsKey(event)) {
            int counter = events.get(event);
            counter++;
            events.replace(event, counter);
        } else {
            events.put(event, 1);
        }
    }

    // método que retorna quandos eventos ocorreram de um determinado tipo
    public int getNumEvents(String event) {
        if(events.containsKey(event)) {
            return events.get(event);
        } else
            return 0;
    }

    // método que serializa o Device
    //id:type:online:active:event1_int<event2_int<event3_int
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(id)
          .append(":")
          .append(type)
          .append(":")
          .append(String.valueOf(online))
          .append(":")
          .append(String.valueOf(active))
          .append(":");

        for(Map.Entry<String, Integer> e : this.events.entrySet()) {
            sb.append(e.getKey()).append("%").append(String.valueOf(e.getValue())).append("!");
        }

        String s = sb.toString();
        return s.substring(0, s.length() - 1);
    }

    // método que deserializa o Device através de uma String
    private void deserialize(String serialized){
        String[] args = serialized.split(":");

        if(args.length != 5)
            return;

        this.id = args[0];
        this.type = args[1];
        this.online = Boolean.parseBoolean(args[2]);
        this.active = Boolean.parseBoolean(args[3]);

        this.events = new HashMap<>();
        String[] tokens = args[4].split("!");
        for(String t : tokens) {
            String[] entry = t.split("%");
            this.events.put(entry[0],Integer.parseInt(entry[1]));
        }
    }
}
