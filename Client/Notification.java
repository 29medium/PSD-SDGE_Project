package Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notification {
    private List<String> types;
    private Map<String,Boolean> not1;
    private Map<String,Boolean> not2;
    private boolean not2Todos;
    private List<Float> not3;
    private List<Float> not4;

    public Notification() {
        types = new ArrayList<>();
        types.add("car");
        types.add("light");
        types.add("drone");

        not1 = new HashMap<>();
        not1.put("car", false);
        not1.put("light", false);
        not1.put("drone", false);

        not2 = new HashMap<>();
        not2.put("car", false);
        not2.put("light", false);
        not2.put("drone", false);

        not2Todos = false;
        
        not3 = new ArrayList<>();
        not4 = new ArrayList<>();
    }

    public List<String> getNotActives(int noti){
        List<String> res = new ArrayList<>();

        if(noti == 1) {
            for(String s : types)
                if(not1.get(s))
                    res.add(s);
        } else {
            for(String s : types)
                if(not2.get(s))
                    res.add(s);
        }
        return res;
    }

    public List<String> getNotInnactives(int noti){
        List<String> res = new ArrayList<>();

        if(noti == 1) {
            for(String s : types)
                if(!not1.get(s))
                    res.add(s);
        } else {
            for(String s : types)
                if(!not2.get(s))
                    res.add(s);
        }
        return res;
    }

    public List<String> getXs(int noti){
        int size;
        List<Float> l;
        if(noti == 3){
            size = not3.size();
            l = not3;
        }
        else {
            size = not4.size();
            l = not4;
        }

        List<String> res = new ArrayList<>();

        for(Float i:l)
            res.add("" + i);

        return res;
    }

    public void setNot2All() {
        for(String s : types)
            if(!not2.get(s))
                not2.put(s,true);
    }

    public void setActive(int noti, String id){
        if(noti == 1)
            not1.put(id, true);
        else
            not2.put(id, true);
    }

    public void setInnactive(int noti, String id){
        if(noti == 1)
            not1.put(id, true);
        else
            not2.put(id, true);
    }

    public void setX(int noti,float v){
        if(noti == 3)
            not3.add(v);
        else
            not4.add(v);
    }

    public void removeX(int noti,float v){
        if(noti == 3)
            if(not3.contains(v))
                not3.remove(v);
        else
            if(not4.contains(v))
                not4.remove(v);
    }

    public String getType(int id){
        return types.get(id);
    }
}