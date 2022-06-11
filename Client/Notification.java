package Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
// Classe que guarda o estado das notificações ativas
public class Notification {
    //map <type,list events> 
    private Map<String, List<String>> types;
    //map <type, state> notification1
    private Map<String, Boolean> notification1;
    //map <type, state> notification2
    private Map<String, Boolean> notification2;
    //notification2 all types active
    private boolean notification2All;
    //set <active percent> notification3
    private Set<Integer> notification3;
    //set <active percent> notification4
    private Set<Integer> notification4;

    // Construtor da classe Notification
    public Notification(Map<String, List<String>> types) {
        this.types = types;

        notification1 = new HashMap<>();
        notification2 = new HashMap<>();

        // Iniciliza todos os tipos a false
        for(String t : types.keySet()) {
            notification1.put(t, false);
            notification2.put(t, false);
        }

        notification2All = false;

        notification3 = new TreeSet<>();
        notification4 = new TreeSet<>();
    }

    // Método que retorna todos os tipos e o seu estado
    public List<String> getTypes(int noti) {
        List<String> res = new ArrayList<>();

        if (noti == 1) {
            for (String s : types.keySet())
                res.add(s+" "+notification1.get(s));
        } else {
            for (String s : types.keySet())
                res.add(s+" "+notification2.get(s));
        }
        return res;
    }

    // Método que retorna uma lista dos tipos ativos
    public List<String> getXUsedList(int noti) {
        Set<Integer> l;
        if (noti == 3) 
            l = notification3;
        else 
            l = notification4;

        List<String> res = new ArrayList<>();

        for (Integer i : l)
            res.add("Desativar " + i + "%");

        return res;
    }

    // Método que retorna a lista da percentagens inatvias, para noti 3 ou 4
    public List<String> getXUnusedList(int command){
        Set<Integer> l;
        List<String> values = new ArrayList<>();
        values.add("10");values.add("20");values.add("30");
        values.add("40");values.add("50");values.add("60");
        values.add("70");values.add("80");values.add("90");

        if(command == 3)
            l = notification3;
        else
            l = notification4;

        for (Integer i : l) 
            values.remove(""+i);

        return values;
    }

    // Método que retorna a percentagem
    public int getX(int command,int index){
        Set<Integer> l;
        if (command == 3) 
            l = notification3;
        else 
            l = notification4;

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

    // Metodo que inverte o estado de uma subscrição
    public boolean setOpposite(int noti, String type) {
        boolean r;
        if (noti == 1){
            r = !notification1.get(type);
            notification1.put(type,r);
        }
        else{
            r = !notification2.get(type);
            notification2.put(type,r);
        }
        return r;
    }

    // Metodo que adiciona o valor v ao noti 3 ou 4
    public void setX(int noti, int v) {
        if (noti == 3)
            notification3.add(v);
        else
            notification4.add(v);
    }

    // Método que verifica se o noti 3 ou 4 contem o valor v 
    public boolean containsX(int noti, int v) {
        if (noti == 3)
            return notification3.contains(v);
        else
            return notification4.contains(v);
    }

    // Método que remove o valor v do noti 3 ou 4
    public void removeX(int noti, int v) {
        if (noti == 3){
            if (notification3.contains(v))
                notification3.remove(v);
        } else if (notification4.contains(v))
                notification4.remove(v);
    }

    // Método que retorna o tipo correspondente a um id
    public String getType(int id) {
        return (new ArrayList<>(types.keySet())).get(id);
    }

    // Método que retorna um evento correspondente a um id
    public String getEvent(int id) {
        List<String> events = new ArrayList<>();

        for(List<String> e : types.values()) {
            events.addAll(e);
        }

        return events.get(id);
    }

    // Método que retorna a lista com todos os tipos
    public List<String> getTypes() {
        return new ArrayList<>(types.keySet());
    }

    // Método que retorna a lista com todos os eventos
    public List<String> getEvents() {
        List<String> events = new ArrayList<>();

        for(List<String> e : types.values()) {
            events.addAll(e);
        }

        return events;
    }

    // Método que retorna o estado isNot2All
    public boolean isNot2All() {
        return notification2All;
    }

    // Método que muda o estado do notification 2
    public void setNot2All(boolean not2All) {
        this.notification2All = not2All;
    }
}