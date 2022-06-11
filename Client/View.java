package Client;

import java.util.ArrayList;
import java.util.List;

// Classe que representa a vista dos clientes
public class View {
    // Função que imprime uma linha
    public static void printLine(int size) {
        for(int i=0; i<size; i++)
            System.out.print("-");

        System.out.println("");
    }

    // Função que imprime uma tabela e o respetivo titulo
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

    // Função que imprime o menu de notificações
    public static void printNotificationsMenu() {
        List<String> l = new ArrayList<>();
        l.add("Notificações 'Sem Dispositivos Online'");
        l.add("Notificações 'Record de Dispositivos Online'");
        l.add("Notificações 'Subida de Dispositivos Online");
        l.add("Notificações 'Descida de Dispositivos Online");
        printMenu(l,"NOTIFICAÇÕES");
    }

    // Função que imprime o menu principal
    public static void printMainMenu(){
        List<String> l = new ArrayList<>();
        l.add("Notificações");
        l.add("Pedidos");
        printMenu(l,"MENU");
    }

    // Função que imprime o menu de pedidos
    public static void printRequestsMenu(){
        List<String> l = new ArrayList<>();
        l.add("Número de dispositivos de um dado tipo online no sistema");
        l.add("Verificar se um dispositivo está online no sistema");
        l.add("Número de dispositivos ativos no sistema");
        l.add("Número de eventos de um dado tipo ocorridos no sistema");
        printMenu(l,"PEDIDOS");
    }

    // Função que imprime uma notificação
    public static void printNotification(String msg) {
        System.out.println("\nNotificação: " + msg + "\n");
    }

    // Função que imprime a resposta ao pedido 1
    public static void printRequest1(String type, String res) {
        System.out.println("Número de dispositivos do tipo " + type + " online: " + res);
    }

    // Função que imprime a resposta ao pedido 2
    public static void printRequest2(String id, Boolean res) {
        if(res)
            System.out.println("O dispositivo " + id + " está online no sistema");
        else
            System.out.println("O dispositivo " + id + " não está online no sistema");
    }

    // Função que imprime a resposta ao pedido 3
    public static void printRequest3(String res) {
        System.out.println("Número de dispositivos ativos no sistema: " + res);
    }

    // Função que imprime a resposta ao pedido 4
    public static void printRequest4(String event, String res) {
        System.out.println("Número de eventos do tipo " + event + ": " + res);
    }

    // Função que imprime inserir opção
    public static void printInsertOption() {
        System.out.println("Insira uma opção:");
    }

    // Função que imprime opção invalida
    public static void printInvalidOption() {
        System.out.println("Opção inválida");
    }

    // Função que imprime inserir id
    public static void printInsertId() {
        System.out.println("Insira um ID de dispositivo:");
    }
}
