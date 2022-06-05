package Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Client {
    private Notification notification;
    private ZMQ.Socket sub;

    
    public Client(ZMQ.Socket sub) {
        this.notification = new Notification();
        this.sub = sub;
    }

    public boolean lerSN(){
        Scanner s = new Scanner(System.in);
        String line;

        do{
           System.out.println("Pretende ativar ou desativar notificações? (S/N):");
            line = s.nextLine();
        } while (!line.toUpperCase().equals("S") && !line.toUpperCase().equals("N"));

        return line.toUpperCase().equals("S");
    }

    private int lerInt(int min, int max){
        Scanner s = new Scanner(System.in);
        int n = -1;

        do{
            System.out.println("Insira uma opção:");;
            try {
                String line = s.nextLine();
                n = Integer.parseInt(line);
            } catch (NumberFormatException nfe) {
                System.out.println("Opção inválida");;
                n = -1;
            }
        } while (n < min || n > max);

        return n;
    }

    private void sub(int command){
        boolean r = lerSN();
        if (r){
            List<String> types = notification.getNotInnactives(command);
            printMenu(types,"TIPOS");
            int op = lerInt(0,types.size());
            String type = notification.getType(op);
            notification.setActive(command, type);
            if(op>0)
                this.sub.subscribe("offline-"+type);
        } else{
            List<String> types = notification.getNotActives(command);
            printMenu(types,"TIPOS ATIVOS");
            int op = lerInt(0,types.size());
            String type = notification.getType(op);
            notification.setInnactive(command, type);
            if(op>0)
                this.sub.unsubscribe("offline-"+notification.getType(op));
        }
    }

    private void controlerN() {
        int command;
        boolean on = true;

        while (on) {
            printMenuNotificacoes();
            command = lerInt(0, 4);

            switch (command) {
                case 1:
                    sub(command);
                    break;
                case 2:
                    sub(command);
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
            printMenuPedidos();
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
            printMenuPrincipal();
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
                    break;
                default:
                    break;
            }
        }
    }

    public void run(String[] args) {
        controler(); 
    }

    public static void main(String[] args) throws InterruptedException {
        Map<Integer,Boolean> notificacoes = new HashMap<>();
        notificacoes.put(1,false);
        notificacoes.put(2,false);
        notificacoes.put(3,false);
        notificacoes.put(4,false);
        
        try(ZContext context = new ZContext();
        ZMQ.Socket sub = context.createSocket(SocketType.SUB)) {
            sub.connect("tcp://localhost:" + args[0]);
            Thread reader = new Thread(new ClientReader(sub));
            reader.start();
            (new Client(sub)).run(args);
        }  
    }

    private void printLine(int size) {
        for(int i=0; i<size; i++)
            System.out.print("-");

        System.out.println("");
    }

    private void printMenu(List<String> menu, String message){

        int size = 0, length=message.length();

        for(String linha: menu){
            if(linha.length() + 4 > length)
                length = linha.length() + 4;
        }

        if(length < 20)
            length = 20;

        printLine(length);
        System.out.println(message);
        printLine(length);

        size = menu.size();
        for(int i = 0;i < size;i++)
            System.out.println(i+1+" | "+menu.get(i));

            System.out.println("0 | Sair");
    }

    private void printMenuNotificacoes() {
        List<String> l = new ArrayList<>();
        l.add( "Notificações 'Sem Dispositivos Online'");
        l.add("Notificações 'Record de Dispositivos Online'");
        l.add( "Notificações 'Subida de Dispositivos Online");
        l.add("Notificações 'Descida de Dispositivos Online");
        printMenu(l,"NOTIFICAÇÕES");
    }

    private void printMenuPrincipal(){
        List<String> l = new ArrayList<>();
        l.add("Notificações");
        l.add("Pedidos");
        printMenu(l,"MENU");
    }

    private void printMenuPedidos(){
        List<String> l = new ArrayList<>();
        l.add("Pedidos");
        printMenu(l,"PEDIDOS");
    }
}
