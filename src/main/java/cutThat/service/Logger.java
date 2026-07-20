package cutThat.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE_NAME = "app_activity.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void log(String message){
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String formattedMessage = String.format("[%s] %s", timestamp, message);

        System.out.println(formattedMessage);
        writeToFile(formattedMessage);
    }

    public static synchronized void error(String message,Throwable throwable){
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String errorMessage = String.format("[%s] [ERROR] %s", timestamp, message);

        System.err.println(errorMessage);
        if(throwable != null){
            throwable.printStackTrace();
        }
        try(PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE_NAME,true))) {
            out.println(errorMessage);
            if(throwable != null){
                throwable.printStackTrace(out);
            }
            out.println("--------------------------------------------------");
        } catch (IOException e){
            System.out.println("Не удалось записать ошибку в лог-файл" + e.getMessage());
        }
    }

    private static void writeToFile(String message){
        try(PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_NAME,true))){
            writer.println(message);
        }catch (IOException e){
            System.err.println("Не удалось записать в лог-файл: " + e.getMessage());
        }
    }
}
