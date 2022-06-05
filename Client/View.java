package Client;

import java.util.ArrayList;
import java.util.List;

public class View {
    public static void printLine(int size) {
        for(int i=0; i<size; i++)
            System.out.print("-");

        System.out.println("");
    }

    public static void printMenu(List<String> menu, String message){

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

    public static void printMenuNotificacoes() {
        List<String> l = new ArrayList<>();
        l.add( "Notificações 'Sem Dispositivos Online'");
        l.add("Notificações 'Record de Dispositivos Online'");
        l.add( "Notificações 'Subida de Dispositivos Online");
        l.add("Notificações 'Descida de Dispositivos Online");
        printMenu(l,"NOTIFICAÇÕES");
    }

    public static void printMenuPrincipal(){
        List<String> l = new ArrayList<>();
        l.add("Notificações");
        l.add("Pedidos");
        printMenu(l,"MENU");
    }

    public static void printMenuPedidos(){
        List<String> l = new ArrayList<>();
        l.add("Pedidos");
        printMenu(l,"PEDIDOS");
    }
}
