package Client;

import java.util.List;
import java.util.Scanner;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Client {
    private Scanner s;
    private Notification notification;
    private ZMQ.Socket sub;

    public Client(ZMQ.Socket sub) {
        this.s = new Scanner(System.in);
        this.notification = new Notification();
        this.sub = sub;
    }

    public boolean lerSN(String st){
        String line;

        do{
            System.out.println(st);
            line = s.nextLine();
        } while (!line.toUpperCase().equals("S") && !line.toUpperCase().equals("N"));

        return line.toUpperCase().equals("S");
    }

    private int lerInt(int min, int max){
        int n = -1;

        do{
            System.out.println("Insira uma opção:");
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

    private void sub1(int command){
        int op;
        String type;
        boolean state;
        List<String> not = notification.getTypes(command);
        View.printMenu(not, "Notifications:");

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

    private void sub2(int command){
        int op;
        String type;
        boolean state;

        if (!notification.isNot2All()) {
            state = lerSN("Pretende ativar notificações para qualquer tipo? (S/N):");
            if (state) {
                this.sub.subscribe("record-");
                notification.setNot2All(true);
            }
        }

        if (!notification.isNot2All()){

        List<String> not = notification.getTypes(command);
        View.printMenu(not, "Notifications:");

        op = lerInt(0, not.size());
        if (op > 0) {
            type = notification.getType(op - 1);
            state = notification.setOpposite(command, type);
                if(state)
                    this.sub.subscribe("record-"+type);
                else
                    this.sub.unsubscribe("record-"+type);
            }
        }
    }

    private void sub(int command){
        List<String> types;
        int op;
        String type;
        boolean r = lerSN("Pretende ativar ou desativar notificações? (S/N):");
        if (r){
            if(command == 2) {
                if (!notification.isNot2All()) {
                    r = lerSN("Pretende ativar notificações para qualquer tipo? (S/N):");
                    if (r) {
                        this.sub.subscribe("record-");
                        notification.setNot2All(true);
                    }
                } else
                    System.out.println("Notificações ativas para record de qualquer tipo!");
            }  else if(command == 1 || !r) {
                types = notification.getNotInnactives(command);
                View.printMenu(types, "TIPOS");
                op = lerInt(0, types.size());
                if (op > 0) {
                    type = notification.getType(op - 1);
                    notification.setActive(command, type);
                    if(command == 1)
                        this.sub.subscribe("offline-"+type);
                    else
                        this.sub.subscribe("record-"+type);
                }
            }
        } else {
            if(command == 2 && notification.isNot2All()) {
                r = lerSN("Pretende desativar notificações para qualquer tipo? (S/N):");
                if (r) {
                    this.sub.unsubscribe("record-");
                    notification.setNot2All(false);
                }
            } else {
                types = notification.getNotActives(command);
                View.printMenu(types, "TIPOS ATIVOS");
                op = lerInt(0, types.size());
                if (op > 0) {
                    type = notification.getType(op - 1);
                    notification.setInnactive(command, type);
                    if(command == 1)
                        this.sub.unsubscribe("offline-"+type);
                    else
                        this.sub.unsubscribe("record-"+type);
                }
            }
        }
    }

    private void controlerN() {
        int command;
        boolean on = true;

        while (on) {
            View.printMenuNotificacoes();
            command = lerInt(0, 4);

            switch (command) {
                case 1:
                    sub1(command);
                    break;
                case 2:
                    sub2(command);
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 0:
                    controler();
                    break;
                default:
                    break;
            }
        }
    }

    private void controlerP() {
        int command;
        boolean on = true;

        while (on) {
            View.printMenuPedidos();
            command = lerInt(0, 2);

            switch (command) {
                case 1:
                    System.out.println("pedido 1");
                    break;
                case 2:
                    System.out.println("pedido 2");;
                    break;
                case 0:
                    controler();
                    break;
                default:
                    break;
            }
        }
    }

    private void controler() {
        int command;
        boolean on = true;

        while (on) {
            View.printMenuPrincipal();
            command = lerInt(0, 2);

            switch (command) {
                case 1:
                    controlerN();
                    break;
                case 2:
                    controlerP();
                    break;
                case 0:
                    on=false;
                    s.close();
                    break;
                default:
                    break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        try(ZContext context = new ZContext();
            ZMQ.Socket sub = context.createSocket(SocketType.SUB)) {
            sub.connect("tcp://localhost:" + args[0]);

            Thread reader = new Thread(new ClientReader(sub));
            reader.start();

            (new Client(sub)).controler();
        }
    }
}