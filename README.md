# Java Client-Server Calculator:

---

> Simple Client-Server calculator, written in Java using sockets.
> 

---

## Code:

### Client:

```java
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
```

### Server:

```java
import java.io.*;
import java.net.*;
import java.util.*;

public class ServerCalcolatrice{
    static int portaDelServer = 5000;

    public static <T> Stack<T> reverseStack(Stack<T> stack){
        Stack<T> reversedStack = new Stack<T>();

        while(!stack.empty()){
            reversedStack.push(stack.pop());
        }

        return reversedStack;
    }

    public static void elabora(Socket clientSocket){
        try{
            //InputStreamReader, utilizza una tecnica di lettura sequenziale, per cui legge dal client un byte alla volta.
            //BufferedReader invece, utilizza una tecnica di "Buffering",
            //per cui legge l'intera stringa in RAM (carattere per carattere, invece di leggere per bytes),
            //velocizzando le future operazioni di lettura.
            //BufferedReader non può leggere il buffer di input direttamente, quindi utilizziamo InputStreamReader
            //come "interfaccia" per poi permettere a BufferedReader la lettura per caratteri.
            BufferedReader inputStreamClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //PrintWriter permette di formattare i dati per la scrittura (partendo dai dati primitivi di Java),OutputStreamWriter è un wrapper della classe OutputStream che permette di scrivere stringhe e caratteri, al posto di bytes codificati.
            PrintWriter outputStreamClient = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()),true);

            //ora, possiamo utilizzare queste due interfacce per le operazioni di I/O:

            System.out.println("In attesa del messaggio da parte del client...");
            String messaggioClient = inputStreamClient.readLine();

            System.out.println("Messaggio ricevuto: " + messaggioClient);

            //Esempio possibile stringa ricevuta: 15*2-3+8/6-2

            Double rispostaClient=0.0;
            Stack<Double> numeri= new Stack<Double>();
            Stack<Character> operandi = new Stack<Character>();
            
            char operando;
            Double num;

            String[] parsedElements = messaggioClient.split("(?<=[-+*^/\\(\\)])|(?=[-+*^/\\(\\)])");            

            //Riempio i due stack "numeri" e "operandi", ed eseguo già divisioni e moltiplicazioni.
            numeri.push(Double.parseDouble(parsedElements[0]));

                    for(int i=1;i<=parsedElements.length-2;i+=2){
                        try{
                            operando = parsedElements[i].toCharArray()[0];
                            num = Double.parseDouble(parsedElements[i+1]);

                            if(operando=='*'){
                                numeri.push(numeri.pop()*num);
                            }else if(operando=='/'){
                                numeri.push(numeri.pop()/num);
                            }else if(operando=='^'){
                                numeri.push(Math.pow(numeri.pop(),num));
                            }else{
                                numeri.push(num);
                                operandi.push(operando);
                            }
                        }catch(Exception e){
                            System.out.println("Espressione formattata male.");
                        }
                    }
            
            numeri = reverseStack(numeri);
            operandi = reverseStack(operandi);

            /*System.out.println("Contenuto stack numeri:");
            while(!numeri.empty()){
                System.out.println(numeri.pop());
            }

            System.out.println("Contenuto stack operandi:");
            while(!operandi.empty()){
                System.out.println(operandi.pop());
            }*/
            
            //addizioni e sottrazioni

            rispostaClient = numeri.pop();

            while(!numeri.empty() && !operandi.empty()){
                operando = operandi.pop();
                num = numeri.pop();
                if(operando == '+'){
                    rispostaClient += num;
                }else if(operando == '-'){
                    rispostaClient -= num;
                }
            }

            System.out.println("Invio risposta al client...");
            outputStreamClient.println(rispostaClient);

            System.out.println("Risposta inviata con successo.");

        }catch(Exception e){
            System.err.println("Errore durante l'elaborazione dei dati: " + e.getMessage());
        }

    }

    public static void main(String args[]) {

        //implementazione "portable" del paradigma client-server, è inserito in un blocco
        //try-catch, perchè genera un eccezione se il server non può esser reso
        //risponibile sulla porta specificata (per un qualsiasi motivo).
        try(ServerSocket serverSocket = new ServerSocket(portaDelServer)){
            System.out.println("Il Server è disponibile e in ascolto sulla porta: " + portaDelServer + " , IP:" + serverSocket.getLocalSocketAddress());

            while(true){
                //accetta richieste in entrata, in un oggetto di tipo "Socket".
                try(Socket clientSocket = serverSocket.accept()){
                    String indirizzoIPclient = clientSocket.getRemoteSocketAddress().toString();
                    //memorizza l'indirizzo IP del client a cui il server è connesso, restituisce NULL se la connessione non è andata a buon fine.

                    System.out.println("Connesso con il client, IP: " + indirizzoIPclient);

                    elabora(clientSocket);

                    System.out.println("Chiusura connessione client.");
                    clientSocket.close();
                }catch (Exception e){
                    System.err.println("Errore durante la connessione col client: " + e.getMessage());
                }

            }

        }catch(Exception e){
            System.err.println("Errore durante l'esecuzione del codice server" + e.getMessage());
        }
    }
}
```

---

## Technologies Used

- **Java**

---

## Usage

Execute the **server** code **first**, than you can execute the **client** code.

Write your math expression as **strings**, in **infix-notation**.

The server will automatically **parse** the expression, and calculate the result.

Supports **[* / + - % ^]** operands (**multiply**, **divide**, **add**, **subtract**, **modulus**, **pow**).

---

## Contact

Coded by **@emikode** - feel free to contact me!

---
