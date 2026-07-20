package cutThat.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFmpegCompressor {
    private static final String FFMPEG_PATH = Paths.get("bin","ffmpeg.exe").toAbsolutePath().toString();

    public interface ProgressListener{
        void onProgressUpdate(double progress, String timeRemaining);
    }

    public static void compress(File inputFile, File outputFile,ProgressListener listener,String encoder) throws IOException, InterruptedException {
        Process process = getProcess(inputFile, outputFile, encoder);
        Pattern durationPattern = Pattern.compile("Duration:\\s*(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");
        Pattern timePattern = Pattern.compile("time=(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");
        Pattern speedPattern = Pattern.compile("speed=\\s*(\\d+\\.?\\d*)x");

        double totalDurationSeconds = 0;

        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Logger.log("[FFmpeg Log]" + line);

                if(totalDurationSeconds == 0) {
                    Matcher durationMatcher = durationPattern.matcher(line);
                    if(durationMatcher.find()) {
                        long hours = Long.parseLong(durationMatcher.group(1));
                        long minutes = Long.parseLong(durationMatcher.group(2));
                        long seconds = Long.parseLong(durationMatcher.group(3));
                        totalDurationSeconds += hours * 3600 + minutes * 60 + seconds;
                    }
                }

                Matcher timeMatcher = timePattern.matcher(line);
                if(timeMatcher.find() && totalDurationSeconds > 0) {
                    long hours = Long.parseLong(timeMatcher.group(1));
                    long minutes = Long.parseLong(timeMatcher.group(2));
                    long seconds = Long.parseLong(timeMatcher.group(3));
                    double currentSeconds = hours * 3600 + minutes * 60 + seconds;
                    double progress = Math.min(1.0, currentSeconds / totalDurationSeconds);
                    double speed = 1.0;
                    Matcher speedMatcher = speedPattern.matcher(line);
                    if(speedMatcher.find()) {
                        speed = Double.parseDouble(speedMatcher.group(1));
                    }
                    double remainingSeconds = (totalDurationSeconds - currentSeconds) / (speed > 0 ? speed : 1.0);
                    int remMin = (int) (remainingSeconds / 60);
                    int remSec = (int) (remainingSeconds % 60);

                    String timeRemainingStr = String.format("Осталось примерно: %02d:%02d (Скорость: %.2fx)", remMin, remSec, speed);
                    if (listener != null) {
                        listener.onProgressUpdate(progress, timeRemainingStr);
                    }
                }
            }
        }
        int exitCode = process.waitFor();
        if(exitCode != 0) {
            Logger.error("[FFmpeg Log]" + exitCode,new RuntimeException());
        }
    }

    private static Process getProcess(File inputFile, File outputFile, String encoder) throws IOException {
        File ffmpeg = new File(FFMPEG_PATH);
        if (!ffmpeg.exists()) {
            Logger.error("FFmpeg не найдет по пути: " + FFMPEG_PATH, new IOException());
        }


        List<String> command = new ArrayList<>();
        command.add(FFMPEG_PATH);
        command.add("-y");
        command.add("-i");
        command.add(inputFile.getAbsolutePath());

        command.add("-vcodec");
        command.add(encoder);
        command.add("-pix_fmt");
        command.add("yuv420p");
        if(encoder.contains("libx264")){
            command.add("-crf"); command.add("28");
            command.add("-preset"); command.add("fast");
        } else if (encoder.contains("nvenc")) {
            command.add("-rc"); command.add("constqp");
            command.add("-cq"); command.add("28");
        }
        command.add("-acodec");
        command.add("aac");
        command.add(outputFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        return process;
    }
}
