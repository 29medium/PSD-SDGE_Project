package Client;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.zeromq.ZMQ;

public class Controller {
    private Notification notification;
    private Scanner s;
    private ZMQ.Socket req;
    private ZMQ.Socket sub;

    public Controller(ZMQ.Socket req, ZMQ.Socket sub, Map<String, List<String>> types) {
        this.s = new Scanner(System.in);
        this.notification = new Notification(types);
        this.req = req;
        this.sub = sub;
    }

    private int lerInt(int min, int max){
        int n = -1;
        String str;
        if(max  < 100)
            str = "Insira uma opção:";
        else
            str = "Insira um valor de 0 a 100:";


        do{
            System.out.println(str);
            try {
                String line = s.nextLine();
                n = Integer.parseInt(line);
            } catch (NumberFormatException nfe) {
                System.out.println("Opção inválida");
                n = -1;
            }
        } while (n < min || n > max);

        return n;
    }

    private void command1(int command){
        int op;
        String type;
        boolean state;
        List<String> not = notification.getTypes(command);
        View.printMenu(not, "Notifications (Type State)");

        op = lerInt(0, not.size());
        if (op > 0) {
            type = notification.getType(op - 1);
            state = notification.setOpposite(command, type);
            if(state)
                this.sub.subscribe("offline-"+type);
            else
                this.sub.unsubscribe("offline-"+type);
        }
    }

    private void command2(int command){
        int op;
        String type;
        boolean state;

        state = notification.isNot2All();
        List<String> not = notification.getTypes(command);
        not.add("Todos "+state);
        View.printMenu(not, "Notifications (Type State)");

        op = lerInt(0, not.size());

        if(op == not.size()){
            if(state){
                this.sub.unsubscribe("record-");
                notification.setNot2All(false);
            } else {
                this.sub.subscribe("record-");
                notification.setNot2All(true);
            }
            
        }else if (op > 0) {
            type = notification.getType(op - 1);
            state = notification.setOpposite(command, type);
            if(state)
                this.sub.subscribe("record-"+type);
            else
                this.sub.unsubscribe("record-"+type);
        }
    }

    private void command34(int command){
        int op,value;
        List<String> values = notification.getXs(command);
        values.add("Adiconar X%");
        View.printMenu(values, "Notificações (X%)");
        op = lerInt(0, values.size());

        if(op == values.size()){
            value = lerInt(0, 100);
            if(!notification.containsX(command, value)){
                notification.setX(command, value);
                if(command == 3)
                    this.sub.subscribe("percentUp-"+value);
                else
                    this.sub.subscribe("percentDown-"+value);
            }
        }else {
            value = notification.getX(command,op-1);
            if(notification.containsX(command, value)){
                notification.removeX(command, value);
                if(command == 3)
                    this.sub.unsubscribe("percentUp-"+value);
                else
                    this.sub.unsubscribe("percentDown-"+value);
            }
        }
    }

    private void pedido14(int command){
        int op;
        List<String> values;
        if(command == 1) 
            values = notification.getTypes();
        else
            values = notification.getEvents();

        View.printMenu(values, "Pedido (Type)");
        op = lerInt(0, values.size());

        if(op > 0){
            if(command == 1){
                String type = notification.getType(op - 1);
                req.send("1,"+ type);
                String res = req.recvStr();
                System.out.println("Número de dispositivos do tipo " + type + " online: " + res);
            }else if(command == 4){
                String event = notification.getEvent(op - 1);
                req.send("4,"+ event);
                String res = req.recvStr();
                System.out.println("Número de eventos do tipo " + event + ": " + res);
            }
        }
    }

    private void pedido2(){
        System.out.println("Insira um ID de dispositivo:");
        String id = s.nextLine();

        req.send("2,"+ id);
        if(Boolean.parseBoolean(req.recvStr()))
            System.out.println("O dispositivo " + id + " está online no sistema");
        else
            System.out.println("O dispositivo " + id + " não está online no sistema");
    }

    private void pedido3(){
        req.send("3");
        String res = req.recvStr();
        System.out.println("Número de dispositivos ativos no sistema: " + res);
    }

    private void controllerNotifications() {
        int command;
        boolean on = true;

        while (on) {
            View.printMenuNotificacoes();
            command = lerInt(0, 4);

            switch (command) {
                case 1:
                    command1(command);
                    break;
                case 2:
                    command2(command);
                    break;
                case 3:
                    command34(command);
                    break;
                case 4:
                    command34(command);
                    break;
                case 0:
                    controller();
                    break;
                default:
                    break;
            }
        }
    }

    private void controllerQueries() {
        int command;
        boolean on = true;

        while (on) {
            View.printMenuPedidos();
            command = lerInt(0, 4);

            switch (command) {
                case 1:
                    pedido14(command);
                    break;
                case 2:
                    pedido2();
                    break;
                case 3:
                    pedido3();
                    break;
                case 4:
                    pedido14(command);
                    break;
                case 0:
                    controller();
                    break;
                default:
                    break;
            }
        }
    }

    public void controller() {
        int command;
        boolean on = true;

        while (on) {
            View.printMenuPrincipal();
            command = lerInt(0, 2);

            switch (command) {
                case 1:
                    controllerNotifications();
                    break;
                case 2:
                    controllerQueries();
                    break;
                case 0:
                    on=false;
                    break;
                default:
                    break;
            }
        }
    }
}
