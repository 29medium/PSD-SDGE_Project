package Aggregator;

import java.util.HashMap;
import java.util.Map;

public class Device {
    private String id;
    private String type;
    private boolean online;
    private boolean active;
    private Map<String, Integer> events;

    public Device(String id, String type, boolean online, boolean active) {
        this.id = id;
        this.type = type;
        this.online = online;
        this.active = active;
        this.events = new HashMap<>();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOnline() {
        return this.online;
    }

    public boolean getOnline() {
        return this.online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean getActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addEvent(String event) {
        if(events.containsKey(event)) {
            int counter = events.get(event);
            counter++;
            events.put(event, counter);
        } else {
            events.put(event, 1);
        }
    }
}
