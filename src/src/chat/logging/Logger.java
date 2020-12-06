package src.chat.logging;

import java.io.*;
import java.net.Socket;

public class Logger {
    private static int i;
    private File file;
    private BufferedReader reader;
    private BufferedWriter writer;
    private DataOutputStream out;
    private String name;

    public Logger(Socket socket, String name) {
        try {
            file = new File("C://Users//Карина//IdeaProjects//homework7_level2//LogFile"
                    + name +".txt");
               if (!file.exists()){
                   boolean create = file.createNewFile();
                }
            out = new DataOutputStream(socket.getOutputStream());
                writer =new BufferedWriter(new FileWriter(file, true));
                reader = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addLog(String message) throws IOException {
        try{

            writer.write(message);
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void showLog() throws IOException {
        int amountOfLine1;
        if(!file.exists()) {
            System.out.println("Нет такого файла");
            return;
        }
        int amountOfLine=0;
        try {

            out.writeUTF("--Начало истории чата--");
            reader.markSupported();
            while (reader.ready()){
                reader.readLine();
                amountOfLine++;
            }
            reader.close();
            reader = new BufferedReader(new FileReader(file));

            if(amountOfLine>100){
                amountOfLine = amountOfLine-100;
                amountOfLine1=0;
                while (reader.ready()){
                    if (amountOfLine1>amountOfLine){
                    out.writeUTF(reader.readLine());}
                    amountOfLine1++;
                }
            }else {
                while (reader.ready()){
                    out.writeUTF(reader.readLine());
                }
            }

            out.writeUTF("--Конец истории чата--");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
