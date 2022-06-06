package Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Notification {
    private Map<String, List<String>> types;
    private Map<String, Boolean> not1;
    private Map<String, Boolean> not2;
    private boolean not2All;
    private Set<Integer> not3;
    private Set<Integer> not4;

    public Notification(Map<String, List<String>> types) {
        this.types = types;

        not1 = new HashMap<>();
        not2 = new HashMap<>();

        for(String t : types.keySet()) {
            not1.put(t, false);
            not2.put(t, false);
        }

        not2All = false;

        not3 = new TreeSet<>();
        not4 = new TreeSet<>();
    }

    public List<String> getTypes(int noti) {
        List<String> res = new ArrayList<>();

        if (noti == 1) {
            for (String s : types.keySet())
                res.add(s+" "+not1.get(s));
        } else {
            for (String s : types.keySet())
                res.add(s+" "+not2.get(s));
        }
        return res;
    }

    public boolean getTypeNot(int command,int id){
        if(command == 1)
            return not1.get(getType(id));
        else
            return not2.get(getType(id));
    }

    public List<String> getXs(int noti) {
        Set<Integer> l;
        if (noti == 3) 
            l = not3;
        else 
            l = not4;

        List<String> res = new ArrayList<>();

        for (Integer i : l)
            res.add("Desativar " + i + "%");

        return res;
    }

    public int getX(int command,int index){
        Set<Integer> l;
        if (command == 3) 
            l = not3;
        else 
            l = not4;

        int res = -1;
        for (Integer i : l) {
            if(index == 0){
                res = i;
                break;
            }
            index--;
        }

        return res;
    }

    public void setNot2All() {
        for (String s : types.keySet())
            if (!not2.get(s))
                not2.put(s, true);
    }

    public void setActive(int noti, String id) {
        if (noti == 1)
            not1.put(id, true);
        else
            not2.put(id, true);
    }

    public void setInnactive(int noti, String id) {
        if (noti == 1)
            not1.put(id, false);
        else
            not2.put(id, false);
    }

    public boolean setOpposite(int noti, String type) {
        if (noti == 1){
            boolean r = !not1.get(type);
            not1.put(type,r);
            return r;
        }
        else{
            boolean r = !not2.get(type);
            not2.put(type,r);
            return r;
        }
    }

    public void setX(int noti, int v) {
        if (noti == 3)
            not3.add(v);
        else
            not4.add(v);
    }

    public boolean containsX(int noti, int v) {
        if (noti == 3)
            return not3.contains(v);
        else
            return not4.contains(v);
    }

    public void removeX(int noti, int v) {
        if (noti == 3){
            if (not3.contains(v))
                not3.remove(v);
        } else if (not4.contains(v))
                not4.remove(v);
    }

    public String getType(int id) {
        return (new ArrayList<>(types.keySet())).get(id);
    }

    public String getEvent(int id) {
        List<String> events = new ArrayList<>();

        for(List<String> e : types.values()) {
            events.addAll(e);
        }

        return events.get(id);
    }

    public List<String> getTypes() {
        return new ArrayList<>(types.keySet());
    }

    public List<String> getEvents() {
        List<String> events = new ArrayList<>();

        for(List<String> e : types.values()) {
            events.addAll(e);
        }

        return events;
    }

    public boolean isNot2All() {
        return not2All;
    }

    public void setNot2All(boolean not2All) {
        this.not2All = not2All;
    }
}