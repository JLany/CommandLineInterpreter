import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.util.Scanner;

public class Terminal {
    private static Parser parser = new Parser();
    private static Path currentDirFullPath = Paths.get(System.getProperty("user.dir"));
    private static final String ErrorPrefix = "-terminal: ";
    private static boolean run;

    public static void main(String[] args) {
        run = true;
        var in = new Scanner(System.in);

        while (run) {
            System.out.print(currentDirFullPath.toString() + ">");

            var input = in.nextLine();

            boolean isValidInput = parser.parse(input);

            if (isValidInput) {
                chooseCommandAction();
            }
        }

        in.close();
    }

    public static void chooseCommandAction() {
        var command = parser.getCommandName();

        if (command.isBlank()) {
            return;
        }

        String output = "";

        switch (command) {
            case "cd":
                output = cd(parser.getArgs());
                break;
            case "pwd":
                output = pwd();
                break;
            case "ls":
                output = ls(parser.getArgs());
                break;
            case "echo":
                output = echo(parser.getArgs());
                break;
            case "mkdir":
                output = mkdir(parser.getArgs());
                break;
            case "history":
                output = history();
                break;
            case "exit":
                run = false;
                break;
            default:
                printe(command);
        }

        parser.write(output);
    }

    // Commands.
    public static String pwd() {
        return String.format("%s\n", currentDirFullPath.toString());
    }

    public static String cd(List<String> args) {
        Path inputPath;

        if (args.size() < 1) {
            inputPath = Paths.get(System.getProperty("user.home"));
        } else {
            inputPath = Paths.get(args.get(0));
        }

        inputPath = currentDirFullPath.resolve(inputPath);

        try {
            inputPath = inputPath.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            printe("the path you specified");
        }

        currentDirFullPath = inputPath;
        return String.format("%s\n", currentDirFullPath.toString());
    }

    public static void printe(String arg) {
        System.out.printf("%s%s: not found\n", ErrorPrefix, arg);
    }

    public static String ls(List<String> args) {
        StringBuilder formattedOutput = new StringBuilder();
        if (args.size() == 0) {
            formattedOutput.append(String.format("%-50s %10s\n", "File Name", "Last Modified"));

            for (File file : currentDirFullPath.toFile().listFiles()) {
                formattedOutput.append(String.format("%-50s %10s\n", file.getName(), new Date(file.lastModified())));
            }
        } else if (args.size() == 1) {
            String directoryPath = currentDirFullPath.toAbsolutePath() + "/" + args.get(0);
            File directory = new File(directoryPath);

            if (directory.exists() && directory.isDirectory()) {
                formattedOutput.append(String.format("%-50s %10s\n", "File Name", "Last Modified"));

                for (File childFile : directory.listFiles()) {
                    formattedOutput.append(
                            String.format("%-50s %10s\n", childFile.getName(), new Date(childFile.lastModified())));
                }
            } else {
                formattedOutput.append("Invalid directory path or directory doesn't exist.");
            }
        } else {
            formattedOutput.append("Invalid arguments");
        }

        String output = formattedOutput.toString();
        return output;
    }

    public static String echo(List<String> args) {
        String output = "";
        for (String string : args) {
            output += string + '\n';
        }
        return output;
    }

    public static String mkdir(List<String> args) {
        String output = "";
        for (String string : args) {
            String directoryPath = string;

            if (!Paths.get(directoryPath).isAbsolute()) {
                directoryPath = currentDirFullPath.toAbsolutePath() + "/" + directoryPath;
            }

            File file = new File(directoryPath);

            if (file.isDirectory()) {
                output += "Directory already exists\n";
            } else {
                try {
                    Path path = Paths.get(directoryPath);
                    output += Files.createDirectories(path).toString() + '\n';
                } catch (IOException e) {
                    System.err.println("Failed to create directories");
                }
            }
        }

        return output;
    }

    public static String rmdir(List<String> args) {
        return "";
    }

    public static String history() {
        var commands = parser.getHistory();

        var output = new StringBuilder();
        for (int i = 0; i < commands.size(); ++i) {
            output.append(String.format("%d\t%s\n", i + 1, commands.get(i)));
        }

        return output.toString();
    }
}
