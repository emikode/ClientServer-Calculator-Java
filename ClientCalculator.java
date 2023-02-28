import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientCalculator {
    public static void main(String[] args) {
            System.out.println("Connessione al server...");
            try(Socket connessioneServer = new Socket("localhost",5000)){
                String IPServer = connessioneServer.getRemoteSocketAddress().toString();
                String IPLocale = connessioneServer.getLocalAddress().toString();
                System.out.println("Connessione col server stabilita:\nIP Server remoto: " + IPServer + "\nIP Client: " + IPLocale);

                BufferedReader streamInput = new BufferedReader(new InputStreamReader(connessioneServer.getInputStream()));
                PrintWriter invioDati = new PrintWriter(new OutputStreamWriter(connessioneServer.getOutputStream()),true);

                Scanner inputStreamReader = new Scanner(System.in);

                System.out.println("Inserisci l'espressione da calcolare: ");
                String espressioneInput = inputStreamReader.nextLine();

                System.out.println("Invio dell'espressione al server...");
                invioDati.println(espressioneInput);

                System.out.println("Attesa risposta server...");
                String rispostaServer = streamInput.readLine();

                System.out.println("Risposta server ricevuta: " + rispostaServer);
            
                connessioneServer.close();
                inputStreamReader.close();
            }catch(Exception e){
                //Scrive sull'error stream (Visualizzazione in colore rosso nella shell di IntelliJ)
                System.err.println("Errore durante la connessione al server: " + e.getMessage());
            }
    }
}