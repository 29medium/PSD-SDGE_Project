package Aggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CRDT {
    // Mapa de dispositivos
    private Map<Integer,Map<String,Device>> devices;
    // Vetor de versões
    private Map<Integer,Integer> versions;
    private Map<String,Integer> record;
    private int record_total;
    private int percentage_online;
    private int own_port;
    
    // Construtor da classe CRDT
    public CRDT(int own_port){
        this.devices = new HashMap<>();
        this.devices.put(own_port, new HashMap<>());

        this.versions = new HashMap<>();
        this.versions.put(own_port, 0);

        this.record = new HashMap<>();

        this.record_total = 0;
        this.percentage_online = 0;
        this.own_port = own_port;
    }

    // Método que incrementa a versão
    public void incVersion() {
        this.versions.put(own_port, this.versions.get(own_port) + 1);
    }

    // Método que coloca um dispositivo no map
    public void putDevice(int port, String id, Device value){        
        this.devices.putIfAbsent(port, new HashMap<>());
        this.devices.get(port).put(id, value);
    }

    // Método que vai buscar um dispositivo ao map
    public Device getDevice(int port, String id){
        if (this.devices.containsKey(port))
            return this.devices.get(port).get(id);
        else
            return null;
    }

    // Método que verifica se key está no map de dispositivos
    public boolean containsKeyDevice(int port, String id){
        return this.devices.containsKey(port) && this.devices.get(port).containsKey(id);
    }

    // Método que vai buscar o map de dispositivos
    public Map<String,Device> getDeviceMap(int port){
        return this.devices.get(port);
    }

    // Método que troca o map de dispositivo pela nova versão
    public void replaceDevices(int port, Map<String,Device> devices){
        this.devices.remove(port);
        this.devices.put(port, devices);
    }

    // Método que valida se todos os dispositivos de um tipo da zona estão offline
    public String validateOffline(String type) {
        int online = (int) this.devices.get(this.own_port).values().stream().filter(x -> x.getType().equals(type) && x.isOnline()).count();
        if(online == 0) {
            return "offline-" + type;
        }
        return null;
    }

    // Método que verifica se o record do número de dispositivos online de um dado tipo da zona foi atingido 
    public String validateRecord(String type) {
        if(this.record.containsKey(type)) {
            int current = (int) this.devices.get(this.own_port).values().stream().filter(x -> x.getType().equals(type) && x.isOnline()).count();
            if(current > this.record.get(type)){
                this.record.put(type,current);
                return "record-" + type;
            }
        } else {
            this.record.put(type, 1);
            return "record-" + type;
        }

        return null;
    }

    // Método que verifica se o record do número de dispositvos da zona foi atingido
    public String validateRecordTotal(String type) {
        int current_total = (int) this.devices.get(this.own_port).values().stream().filter(x -> x.isOnline()).count();
        if(current_total>record_total) {
            record_total = current_total;
            return "record";
        }

        return null;
    }

    // Método que verifica se a percentagem de dispositivos online da zona face ao total do sistema ultrapassou uma certa percentagem
    public List<String> validatePercentage() {
        int zone_online = (int) devices.get(own_port).values().stream().filter(x -> x.isOnline()).count();
        int total_online = devices.values().stream().map(x -> (int) x.values().stream().filter(y -> y.isOnline()).count()).mapToInt(Integer::intValue).sum();
        int new_percentage = total_online==0 ? 0 : zone_online / total_online * 10;

        List<String> res = new ArrayList<>();

        if(new_percentage > percentage_online) {
            for(int i = 1; i <= new_percentage && i < 10; i++)
                res.add("percentUp-" + i + "0");
        } else if(new_percentage < percentage_online) {
            for(int i = 9 ; i>=new_percentage && i > 0; i--){
                res.add("percentDown-" + i + "0");
            }
        }

        percentage_online = new_percentage;

        return res;
    }

    // Método que devolve o número de dispositivos online de um dado tipo no sistema
    public String devicesOnline(String type) {
        int count = 0;

        for(Map<String, Device> m : devices.values()){
            count += m.values().stream().filter(x -> x.getType().equals(type) && x.isOnline()).count();
        }

        return String.valueOf(count);
    }

    // Método que verifica se um dispositivo está online no sistema
    public String deviceOnline(String id) {
        boolean res = false;

        for(Map<String,Device> m : devices.values()) {
            if(m.containsKey(id)) {
                Device d = m.get(id);
                if(d.isOnline())
                    res = true;
            }
        }
        
        return String.valueOf(res);
    }

    // Método que devolve o número de dispositivos ativos no sistema
    public String devicesActive() {
        int count = 0;

        for(Map<String, Device> m : devices.values()){
            count += m.values().stream().filter(x -> x.isActive()).count();
        }

        return String.valueOf(count);
    }

    // Método que devolve o número de eventos ocorridos de um dado tipo no sistema
    public String events(String type) {
        int count = 0;

        for(Map<String, Device> m : devices.values()){
            for(Device e : m.values()) {
                count += e.getNumEvents(type);
            }
        }

        return String.valueOf(count);
    }

    // Método que serializa a mensagem sobre o estado de um dispositivo (online/offline e ativo/inativo)
    public String serializeMessage(String user, String state, String type){
        // Incrementar a versão antes de serealizar
        incVersion();

        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(this.versions.get(own_port)))
            .append(",")
            .append(String.valueOf(this.own_port))
            .append(",")
            .append("msg,")
            .append(state).append(";").append(user).append(";").append(type);  
        return sb.toString();      
    }

    // Método que serializa o estado do agregador para ser enviado aos agregadores vizinhos
    public String serializeState(){
        // Incrementar a versão antes de serealizar
        incVersion();

        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(this.versions.get(own_port)))
            .append(",")
            .append(String.valueOf(this.own_port))
            .append(",")
            .append("state,");

        for (Map.Entry<String,Device> device : this.devices.get(this.own_port).entrySet()) {
            sb.append(device.getKey()).append("~").append(device.getValue().serialize()).append(";");
        }
        
        String s = sb.toString();
        return s.substring(0, s.length() - 1);
    }
    
    // Método que deserializa o estado de um dispositivo ou total de um agregaodr vizinho
    public boolean deserialize(String s) {        
        String []args = s.split(",");
        int ver = Integer.parseInt(args[0]);
        int source = Integer.parseInt(args[1]);

        // Se for a primeira mensagem que recebe de um nodo, adiciona ao vetor de versões
        versions.putIfAbsent(source, 0);

        // Verifica se a versão é antiga, de modo a não adicionar estados antigos ou repetidos
        if (args.length != 4 || source==own_port || ver <= this.versions.get(source))
            return false;

        // Verifica se a mensagem é do estado do agregador ou de um dispositivos
        if(args[2].equals("state")){
            String []state = args[3].split(";");
            Map<String,Device> newState = new HashMap<>();

            for (String dev : state){
                String []devArgs = dev.split("~");
                newState.put(devArgs[0],new Device(devArgs[1]));
            }

            replaceDevices(source, newState);
        }

        else if (args[2].equals("msg")){
            String[] msg = args[3].split(";");
            
            if(msg[0].equals("online")){
                if(this.containsKeyDevice(source, msg[1])) {
                    Device d = getDevice(source, msg[1]);
                    d.setOnline(true);
                    d.setActive(true);
                } else {
                    Device d = new Device(msg[1], msg[2], true, true);
                    this.putDevice(source, msg[1], d);
                }
            }
            else if(msg[0].equals("offline")){
                Device d = getDevice(source, msg[1]);
                d.setOnline(false);
                d.setActive(false);
            }
            else if(msg[0].equals("active")){
                Device d = getDevice(source, msg[1]);
                d.addEvent(msg[2]);
                d.setActive(true);
            
            }
            else if(msg[0].equals("inactive")){
                Device d = getDevice(source, msg[1]);
                d.setActive(false);
            }
        }

        // Adiciona nova versão ao vetor de versões
        this.versions.put(source, ver);    

        return true;    
    }
}
