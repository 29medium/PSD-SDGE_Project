package Aggregator;

public class Device {
    private String id;
    private String type;
    private boolean online;
    private boolean active;

    public Device(String id, String type, boolean online, boolean active) {
        this.id = id;
        this.type = type;
        this.online = online;
        this.active = active;
    }
}
