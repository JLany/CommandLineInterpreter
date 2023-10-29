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
        
        switch (command) {
            case "cd":
                cd(parser.getArgs());
                break;
            case "pwd":
                System.out.printf("%s\n", pwd());
                break;
            case "ls":
                ls(parser.getArgs());
                break;
            case "echo":
                echo(parser.getArgs());
                break;
            case "mkdir":
                mkdir(parser.getArgs());
                break;
            case "exit":
                run = false;
                break;
            default:
                printe(command);
        }
    }

    // Commands.
    public static String pwd() {
        return currentDirFullPath.toString();
    }

    public static void cd(List<String> args) {
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
    }

    public static void printe(String arg) {
        System.out.printf("%s%s: not found\n", ErrorPrefix, arg);
    }

    public static void ls(List<String> args) {
        if (args.size() == 0) {
            System.out.printf("%-50s %10s\n", "Name", "Last Modified");
            for (File file : currentDirFullPath.toFile().listFiles()) {
                System.out.printf("%-50s %10s", file.getName(), new Date(file.lastModified()));
                System.out.println();
            }
        } else if (args.size() == 1) {
            String directoryPath = currentDirFullPath.toAbsolutePath() + "/" + args.get(0);
            File directory = new File(directoryPath);

            if (directory.exists() && directory.isDirectory()) {
                System.out.printf("%-50s %10s\n", "Name", "Last Modified");
                for (File childFile : directory.listFiles()) {
                    System.out.printf("%-50s %10s", childFile.getName(), new Date(childFile.lastModified()));
                    System.out.println();
                }
            } else {
                System.out.println("Invalid directory path or directory doesn't exist.");
            }
        } else {
            System.out.println("Invalid arguments");
        }
    }

    public static void echo(List<String> args) {
        for (String string : args) {
            System.out.println(string);
        }
    }

    public static void mkdir(List<String> args) {
        for (String string : args) {
            String directoryPath = string;

            if (!Paths.get(directoryPath).isAbsolute()) {
                directoryPath = currentDirFullPath.toAbsolutePath() + "/" + directoryPath;
            }

            File file = new File(directoryPath);

            if (file.isDirectory()) {
                System.out.println("Directory already exists");
            } else {
                try {
                    Path path = Paths.get(directoryPath);
                    Files.createDirectories(path);
                } catch (IOException e) {
                    System.err.println("Failed to create directories");
                }
            }
        }
    }
}
