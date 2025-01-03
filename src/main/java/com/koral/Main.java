package com.koral;

import com.yetanother.CommandLine;
import com.yetanother.CommandLineParser;
import com.yetanother.DefaultParser;
import com.yetanother.Options;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static final CommandLineParser PARSER = new DefaultParser();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd HH:mm");

    public static void main(String[] args) {

        CommandLine cmd = parseArguments(args);

        Path currentRelativePath = Paths.get("");
        List<OutputResult> outputResults;

        try {
            outputResults = getOutputResults(currentRelativePath, cmd);
            int[] columWidths = calculateColumnWidths(outputResults, cmd);
            printResults(outputResults, columWidths, cmd);

        } catch (IOException e) {
            System.out.println("Error reading files: " + e.getMessage());
            System.exit(1);
        }

//        try {
//            outputResults = list(currentRelativePath)
//                    .filter(path -> { // Фильтер для опции -a
//                        if (cmd.containsOption("a")) return true;
//                        try {
//                            return !isHidden(path); // Проверка на скрытый файл
//                        } catch (IOException e) {
//                            throw new RuntimeException(e); // Преобразовать в RuntimeException
//                        }
//                    })
//                    .map(elem -> {
//                        try {
//                            String name = String.valueOf(elem.getFileName());
//                            int read_write_exec_rules_num = (Files.isReadable(elem) ? 4 : 0) + (Files.isWritable(elem) ? 2 : 0) + (Files.isExecutable(elem) ? 1 : 0);
//                            Long size = Files.size(elem);
//                            String owner = Files.getOwner(elem).getName();
//                            FileTime lastModified = Files.getLastModifiedTime(elem);
//
//                            return new OutputResult(name, elem, size, read_write_exec_rules_num, owner, lastModified);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    })
//                    .toList();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        int mostLongSize = 0;
//        int mostLongOwner = 0;
//
//        // Поиск самого длинного имени пользователя и размера файла
//        for (OutputResult result : outputResults) {
//            String size_string = String.valueOf(result.getSize());
//            if (cmd.containsOption("h")) {
//                size_string = humanReadableByteCount(result.getSize());
//            }
//            int lengthSize = size_string.length();
//            if (lengthSize > mostLongSize) mostLongSize = lengthSize;
//
//            int lengthOwner = String.valueOf(result.getOwner()).length();
//            if (lengthOwner > mostLongOwner) mostLongOwner = lengthOwner;
//        }
//
//        for (OutputResult result : outputResults) {
//            // Преобразование в LocalDateTime
//            LocalDateTime localDateTime = LocalDateTime.ofInstant(result.getLastModified().toInstant(), ZoneId.systemDefault());
//
//            // Форматирование в нужный формат: MMM dd HH:mm
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm");
//            String formattedTime = localDateTime.format(formatter);
//
//            // Преоразовывание битов обозначающих права доступа в +- понятный человеку вид
//            String rwx_string = (isBitSet(result.getPermissions(), 2) ? "r" : " -") + (isBitSet(result.getPermissions(), 1) ? "w" : " -") + (isBitSet(result.getPermissions(), 0) ? "x" : " -");
//
//            String size_string = String.valueOf(result.getSize());
//
//            if (cmd.containsOption("h")) {
//                size_string = humanReadableByteCount(result.getSize());
//            }
//
//            System.out.printf("%s %s %" + mostLongSize + "s %s %s\r\n", rwx_string, result.getOwner(), size_string, formattedTime, result.getName());
//        }


    }

    private static List<OutputResult> getOutputResults(Path path, CommandLine cmd) throws IOException {
        boolean showHidden = cmd.containsOption("a");

        return Files.list(path)
                .filter(p -> showHidden || !isHiddenFile(p))
                .map(Main::mapToOutputResult)
                .collect(Collectors.toList());
    }

    private static boolean isHiddenFile(Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

    private static OutputResult mapToOutputResult(Path elem) {
        try {
            String name = elem.getFileName().toString();
            int permissions = calculatePermissions(elem);
            long size = Files.size(elem);
            String owner = Files.getOwner(elem).getName();
            FileTime lastModified = Files.getLastModifiedTime(elem);

            return new OutputResult(name, elem, size, permissions, owner, lastModified);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file: " + elem, e);
        }
    }

    private static int[] calculateColumnWidths(List<OutputResult> results, CommandLine cmd) {
        int maxSizeWidth = results.stream()
                .mapToInt(r -> getFileSizeString(r, cmd).length())
                .max()
                .orElse(0);

        int maxOwnerWidth = results.stream()
                .mapToInt(r -> r.getOwner().length())
                .max()
                .orElse(0);

        return new int[]{maxSizeWidth, maxOwnerWidth};
    }

    private static String getFileSizeString(OutputResult result, CommandLine cmd) {
        return cmd.containsOption("h") ? humanReadableByteCount(result.getSize()) : String.valueOf(result.getSize());
    }

    private static String formatPermissions(int permissions) {
        return (isBitSet(permissions, 2) ? "r" : "-")
                + (isBitSet(permissions, 1) ? "w" : "-")
                + (isBitSet(permissions, 0) ? "x" : "-");
    }

    private static int calculatePermissions(Path elem) throws IOException {
        return (Files.isReadable(elem) ? 4 : 0) + (Files.isWritable(elem) ? 2 : 0) + (Files.isExecutable(elem) ? 1 : 0);
    }

    private static boolean isBitSet(int number, int bitPosition) {
        return (number & (1 << bitPosition)) != 0;
    }

    private static CommandLine parseArguments(String[] args) {
        Options options = new Options();

        options.addOption("a", "a", false, "Show hidden files.");
        options.addOption("l", "l", false, "Detailed information (size, permissions, modification date).");
        options.addOption("h", "h", false, "Human-readable file sizes (KB, MB).");


        try {
            return PARSER.parse(options, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse command-line arguments", e);
        }
    }

    private static String humanReadableByteCount(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + "B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f%c", value / 1024.0, ci.current());
    }

    private static String formatTime(FileTime time) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());
        return dateTime.format(DATE_FORMATTER);
    }

    private static void printResults(List<OutputResult> results, int[] widths, CommandLine cmd) {
        for (OutputResult result : results) {
            String permissions = formatPermissions(result.getPermissions());
            String size = getFileSizeString(result, cmd);
            String owner = result.getOwner();
            String modifiedTime = formatTime(result.getLastModified());
            String name = result.getName();

            System.out.printf("%s %-" + widths[1] + "s %" + widths[0] + "s %s %s%n",
                    permissions, owner, size, modifiedTime, name);
        }
    }
}