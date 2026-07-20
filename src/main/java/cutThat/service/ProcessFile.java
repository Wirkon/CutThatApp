package cutThat.service;

import cutThat.ui.DropZone;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Queue;


public class ProcessFile {
    public static boolean isNvidiaSupported = false;
    public static boolean isAmdSupported = false;
    public static boolean isIntelSupported = false;
    private static String defaultCodec = "libx264";
    public static void detectHardware() {
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("win")) return;

            try {
                Process process = new ProcessBuilder("powershell", "-NoProfile", "-Command",
                        "Get-CimInstance Win32_VideoController | Select-Object -ExpandProperty Name")
                        .start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    StringBuilder gpuNames = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        gpuNames.append(line).append(" ");
                    }

                    String gpuString = gpuNames.toString().toLowerCase();
                    isNvidiaSupported = gpuString.contains("nvidia");
                    isAmdSupported = gpuString.contains("amd") || gpuString.contains("radeon");
                    isIntelSupported = gpuString.contains("intel");

                    if (isNvidiaSupported) defaultCodec = "h264_nvenc";
                    else if (isAmdSupported) defaultCodec = "h264_amf";
                    else if (isIntelSupported) defaultCodec = "h264_qsv";
                    else defaultCodec = "libx264";
                    Logger.log(String.format("Оборудование определено: NVIDIA=%b, AMD=%b, INTEL=%b, Авто-кодек: %s",
                            isNvidiaSupported, isAmdSupported, isIntelSupported,defaultCodec));
                }
            } catch (Exception e) {
                Logger.error("Ошибка определения видеокарты: ",e);
                defaultCodec = "libx264";
            }


    }

        public static void processDroppedFile(Queue<File> fileQueue, DropZone dropZone, int currentIndex, int totalIndex, String selectedCodec) {
            Logger.log("=== МЕТОД ВЫЗВАН ===");
            Logger.log("Выбранный кодек в UI: [" + selectedCodec + "]");
            Logger.log("Количество файлов загруженных в очередь: " + fileQueue.size());
            if (fileQueue.isEmpty()) {
                return;
            }

            Task<Void> batchCompressionTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    int current = currentIndex;
                    String encoder = switch (selectedCodec) {
                        case "NVIDIA NVENC" -> "h264_nvenc";
                        case "AMD AMF" -> "h264_amf";
                        case "INTEL QSV" -> "h264_qsv";
                        case "CPU(x264)" -> "libx264";
                        default -> defaultCodec;
                    };


                    while (!fileQueue.isEmpty()) {
                        File file = fileQueue.poll();
                        if (file == null) continue;


                        dropZone.updateCountLabel(current, totalIndex);
                        dropZone.showEncodingProgress();

                        String parentDir = file.getParent();
                        String compressedName = "compressed_" + "encoder_" + encoder + "_" +file.getName();
                        File outputFile = new File(parentDir, compressedName);

                        try {
                            FFmpegCompressor.compress(file, outputFile, dropZone::updateProgress, encoder);
                            Logger.log("Видео успешно сжато и сохранено по пути: " + outputFile.getAbsolutePath());
                        } catch (Exception exception) {
                            Logger.error("Ошибка при сжатии файла " + file.getName() + ": ",exception);
                        }

                        current++;
                    }
                    return null;
                }
            };

            batchCompressionTask.setOnSucceeded(event -> {
                dropZone.showSuccess();
            });

            batchCompressionTask.setOnFailed(event -> {
                Throwable exception = event.getSource().getException();
                if (exception != null) {
                    Logger.error("Критическая ошибка пула сжатия: ", exception);
                }
                dropZone.showSuccess();
            });
            Thread thread = new Thread(batchCompressionTask);
            thread.setDaemon(true);
            thread.start();
        }


    }
