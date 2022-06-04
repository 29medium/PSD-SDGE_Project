package Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Client {
    private Map<Integer,Boolean> notificacoes;
    private ZMQ.Socket sub;
    private String[] tipos = {"carro","aviao","bicicleta"};

    
    public Client(Map<Integer,Boolean> notificacoes,ZMQ.Socket sub) {
        this.notificacoes = notificacoes;
        this.sub = sub;
    }

    private void printLine(int size) {
        for(int i=0; i<size; i++)
            System.out.print("-");

        System.out.println("");
    }

    private void printMenu(String []menu, String message){

        int size, length=message.length();

        for(String linha: menu)
            if(linha.length() + 4 > length)
                length = linha.length() + 4;

        if(length < 20)
            length = 20;

        printLine(length);
        System.out.println(message);
        printLine(length);

        size = menu.length;
        for(int i = 0;i < size;i++)
            System.out.println(i+1+" | "+menu[i]);

            System.out.println("0 | Sair");
    }

    private void printMenuNotificacoes(boolean t1,boolean t2,boolean t3,boolean t4) {
        String s1,s2,s3,s4;
        if(t1)
            s1 = "Desativar notificações 'Sem Dispositivos Online'";
        else
            s1 = "Ativar notificações 'Sem Dispositivos Online'";

        if(t2)
            s2 = "Desativar notificações 'Record de Dispositivos Online'";
        else
            s2 = "Ativar notificações 'Record de Dispositivos Online'";

        if(t3)
            s3 = "Desativar notificações 'Subida de Dispositivos Online";
        else
            s3 = "Ativar notificações 'Subida de Dispositivos Online";

        if(t4)
            s4 = "Desativar notificações 'Descida de Dispositivos Online";
        else
            s4 = "Ativar notificações 'Descida de Dispositivos Online";

        printMenu((new String[]{s1,s2,s3,s4}),"NOTIFICAÇÕES");
    }

    private void printMenuPrincipal(){
        printMenu((new String[]{"Notificações","Pedidos"}),"MENU");
    }

    private void printMenuPedidos(){
        printMenu((new String[]{"Pedidos"}),"PEDIDOS");
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

    private void controlerN() {
        int command;
        boolean on = true,t;

        while (on) {
            printMenuNotificacoes(notificacoes.get(1), notificacoes.get(2), notificacoes.get(3), notificacoes.get(4));
            command = lerInt(0, 4);

            switch (command) {
                case 1:
                    t = !notificacoes.get(1);
                    if (t){
                        // input do utilizador
                        this.sub.subscribe("offline-"+tipo);
                    }
                    else{
                        this.sub.unsubscribe("offline-"+tipo)
                    }
                    notificacoes.put(1, t);
                    break;
                case 2:
                    t = !notificacoes.get(2);
                    notificacoes.put(2, t);
                    break;
                case 3:
                    t = !notificacoes.get(3);
                    notificacoes.put(3, t);
                    break;
                case 4:
                    t = !notificacoes.get(4);
                    notificacoes.put(4, t);
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
                    System.out.println("pedido");;
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
        boolean on = true,t;

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
            (new Client(notificacoes,sub)).run(args);
        }

        
    }
}
