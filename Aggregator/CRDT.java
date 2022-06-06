package Aggregator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CRDT {
    private Map<Integer,Map<String,Device>> devices;
    private Map<String,Integer> record;
    private Map<Integer,Integer> versions;
    int record_total;
    int percentage_online;
    int own_port;
    
    public CRDT(Set<Integer> neighbours, int own_port){
        this.devices = new HashMap<>();
        this.record = new HashMap<>();
        this.versions = new HashMap<>();

        this.devices.put(own_port, new HashMap<>());
        this.versions.put(own_port, 0);

        this.record_total = 0;
        this.percentage_online = 0;
        this.own_port = own_port;
    }

    public void incVersion() {
        int ver = (versions.get(own_port)) + 1;
        versions.put(own_port, ver);
    }

    public void putDevice(int port, String id, Device value){        
        this.devices.putIfAbsent(port, new HashMap<>());
        this.devices.get(port).put(id, value);
    }

    public Device getDevice(int port, String id){
        if (this.devices.containsKey(port))
            return this.devices.get(port).get(id);
        else
            return null;
    }

    public boolean containsKeyDevice(int port, String id){
        return this.devices.containsKey(port) && this.devices.get(port).containsKey(id);
    }

    public Map<String,Device> getDeviceMap(int port){
        return this.devices.get(port);
    }

    public void replaceDevices(int port, Map<String,Device> devices){
        this.devices.put(port, devices);
    }


    public String serializeMessage(String user, String state, String type){
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

    public String serializeState(){
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
    
    public boolean deserialize(String s) {        
        String []args = s.split(",");
        int ver = Integer.parseInt(args[0]);
        int source = Integer.parseInt(args[1]);

        if(!versions.containsKey(source))
            versions.put(source, 0);

        System.out.println(s);
        System.out.println();
        System.out.println("Versions nova " + ver + "- versions velha " + this.versions.get(source));
        System.out.println();

        if (args.length != 4 || source==own_port || ver <= this.versions.get(source))
            return false;

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
            // chamar serialize msg
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

        this.versions.put(source, ver);    
        return true;    
    }

    public String validateOffline(String type) {
        int online = (int) this.devices.get(this.own_port).values().stream().filter(x -> x.getType().equals(type) && x.isOnline()).count();
        if(online == 0) {
            return "offline-" + type;
        }
        return null;
    }

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

    public String validateRecordTotal(String type) {
        int current_total = (int) this.devices.get(this.own_port).values().stream().filter(x -> x.isOnline()).count();
        if(current_total>record_total) {
            record_total = current_total;
            return "record";
        }

        return null;
    }

    public String validatePercentage() {
        int zone_online = (int) devices.get(own_port).values().stream().filter(x -> x.isOnline()).count();
        int total_online = devices.values().stream().map(x -> (int) x.values().stream().filter(y -> y.isOnline()).count()).mapToInt(Integer::intValue).sum();
        int new_percentage = total_online==0 ? 0 : zone_online / total_online * 100;

        String res = null;

        if(new_percentage/10 > percentage_online/10) {
            res = "percentUp-" + new_percentage/10 + "0";
        } else if(new_percentage/10 > percentage_online/10) {
            res = "percentDown-" + new_percentage/10 + "0";
        }

        percentage_online = new_percentage;

        return res;
    }

    public String devicesOnline(String type) {
        int count = 0;

        for(Map<String, Device> m : devices.values()){
            count += m.values().stream().filter(x -> x.getType().equals(type) && x.isOnline()).count();
        }

        return String.valueOf(count);
    }

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

    public String devicesActive() {
        int count = 0;

        for(Map<String, Device> m : devices.values()){
            count += m.values().stream().filter(x -> x.isActive()).count();
        }

        return String.valueOf(count);
    }

    public String events(String type) {
        int count = 0;

        for(Map<String, Device> m : devices.values()){
            for(Device e : m.values()) {
                count += e.getNumEvents(type);
            }
        }

        return String.valueOf(count);
    }
}
