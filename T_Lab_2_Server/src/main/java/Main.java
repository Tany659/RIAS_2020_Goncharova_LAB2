import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.*;

public class Main {
    public static int PORT = 8080;
    Logger log = Logger.getRootLogger(); //подключаем log4j


    public static void main(String[] args) {
        new Main().go();
    }

    public void go() {
        log.info("server port " + PORT);
        try {
            ServerSocket server = new ServerSocket(PORT); //порт на котором запустится сервер
            System.out.println("Server start.");

            while (true) {
                Socket clientSocket = server.accept();  //принимаем запрос на подключения от клиента
                Thread thread = new Thread(new ClientReader(clientSocket));
                log.info("client accept " + clientSocket.toString());
                log.info("client ID " + thread.getId());
                thread.start(); //запускаем поток
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
        }
    }

    class ClientReader implements Runnable {

        BufferedReader breader;
        Socket clienSocket;

        ClientReader(Socket clientSocket) {
            try {
                clienSocket = clientSocket;
                breader = new BufferedReader(new InputStreamReader(clienSocket.getInputStream())); //создаем потк чтения из сокета клиента
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void run() {
            String message;
            try {
                while (!clienSocket.isClosed()) { //слушаем пока клиент не напишит
                    message = breader.readLine();
                    String[] comand =  message.split(" ");
                    switch (comand[0]){
                        case "disconnect":
                            log.debug("client diconnect" + clienSocket.toString());
                            tellEveryone("disconnect");
                            breader.close();
                            clienSocket.close();
                            break;
                        case "send":
                            log.info("client send "+message.substring(5));
                            tellEveryone(message.substring(5));
                            break;
                        case "logLevel":
                            log.info("logLevel set" + comand[1]);
                            switch (comand[1]){
                                case "off":
                                    log.setLevel(Level.OFF);
                                    break;
                                case "fatal":
                                    log.setLevel(Level.FATAL);
                                    break;
                                case "error":
                                    log.setLevel(Level.ERROR);
                                    break;
                                case "warn":
                                    log.setLevel(Level.WARN);
                                    break;
                                case "info":
                                    log.setLevel(Level.INFO);
                                    break;
                                case "debug":
                                    log.setLevel(Level.DEBUG);
                                    break;
                                case "trace":
                                    log.setLevel(Level.TRACE);
                                    break;
                                case "all":
                                    log.setLevel(Level.ALL);
                                    break;
                                default:
                            }
                            break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void tellEveryone(String message) {
            try {
                PrintWriter pw = new PrintWriter(clienSocket.getOutputStream());
                pw.println(message); //отправляем клиентy
                pw.flush();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}