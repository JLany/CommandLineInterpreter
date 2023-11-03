import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

enum OutputDirection {
    Screen,
    Append,
    Overwrite
}

class Parser {
    private String commandName;
    private OutputDirection outputDirection;
    private Path outputFilePath;
    private List<String> args;
    private List<String> history = new ArrayList<>();

    // This method will divide the input into commandName and args
    // where "input" is the string command entered by the user.
    public boolean parse(String input) {
        args = new ArrayList<>();
        commandName = "";
        outputDirection = OutputDirection.Screen;
        outputFilePath = Paths.get("");
        boolean isValidInput = true;

        List<String> commandSections = Arrays.asList(input.trim().split(" "));

        if (commandSections.size() < 1) {
            return isValidInput;
        }

        commandName = commandSections.get(0);

        int i;
        for (i = 1; i < commandSections.size(); ++i) {
            if (commandSections.get(i).equals(">>")
                    || commandSections.get(i).equals(">")) {
                break;
            }

            args.add(commandSections.get(i));
        }

        // Continue parsing the sections of the redirection.
        if (i < commandSections.size()) {
            // Either is guarnteed to excute (mutually exclusive).
            if (commandSections.get(i).equals(">>")) {
                outputDirection = OutputDirection.Append;
            } else if (commandSections.get(i).equals(">")) {
                outputDirection = OutputDirection.Overwrite;
            }

            // Go to next command part.
            i++;

            try {
                Path inputPath = Paths.get(commandSections.get(i));
                Path fileName = inputPath.getFileName();

                outputFilePath = inputPath.toAbsolutePath().getParent()
                        .toRealPath(LinkOption.NOFOLLOW_LINKS)
                        .resolve(fileName);
            } catch (IOException e) {
                System.out.printf("Could not find a part of the path '%s'\n", commandSections.get(i));
                isValidInput = false;
            }

            // If there are more arguments after redirection section.
            if (i + 1 < commandSections.size()) {
                System.out.printf("Invalid command syntax.\n");
                isValidInput = false;
            }
        }

        history.add(input);

        // Each command should further investigate the arguments given to them
        // to verify that they meet the requirements of the command to run.
        return isValidInput;
    }

    public String getCommandName() {
        return commandName;
    }

    public List<String> getArgs() {
        return args;
    }

    public List<String> getHistory() {
        return history;
    }

    public void writef(String format, Object... args) {
        if (outputDirection.equals(OutputDirection.Screen)) {
            System.out.printf(format, args);
            return;
        }

        PrintWriter printer;
        boolean append = outputDirection.equals(OutputDirection.Append);
        try {
            printer = new PrintWriter(new FileWriter(outputFilePath.toFile(), append));
            printer.printf(format, args);
            printer.close();
        } catch (IOException e) {
            System.out.printf("Could not write to file: '%s'\n", outputFilePath.toString());
        }
    }

    public void write(String output) {
        writef(output);
    }
}


class Terminal {
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
            case "echo":
                output = echo(parser.getArgs());
                break;
            case "pwd":
                output = pwd();
                break;
            case "cd":
                output = cd(parser.getArgs());
                break;
            case "ls":
                output = ls(parser.getArgs());
                break;
            case "mkdir":
                output = mkdir(parser.getArgs());
                break;
            case "rmdir":
                output = rmdir(parser.getArgs());
                break;
            case "touch":
                output = touch(parser.getArgs());
                break;
            case "cp":
                output = cp(parser.getArgs());
                break;
            case "rm":
                output = rm(parser.getArgs());
                break;
            case "cat":
                output = cat(parser.getArgs());
                break;
            case "wc":
                output = wc(parser.getArgs());
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
        if (args.size() > 1) {
            return invalidCommandSyntax();
        }

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
        if (args.size() > 1) {
            return invalidCommandSyntax();
        }

        StringBuilder formattedOutput = new StringBuilder();
        if (args.size() == 0) {
            formattedOutput.append(String.format("%-50s %10s\n", "File Name", "Last Modified"));

            for (File file : currentDirFullPath.toFile().listFiles()) {
                formattedOutput.append(String.format("%-50s %10s\n", file.getName(), new Date(file.lastModified())));
            }
        } else if (args.size() == 1) {
            if (args.get(0).equals("-r")) {
                formattedOutput.append(String.format("%-50s %10s\n", "File Name", "Last Modified"));

                File[] files = currentDirFullPath.toFile().listFiles();
                for (int i = files.length - 1; i >= 0; i--) {
                    formattedOutput
                            .append(String.format("%-50s %10s\n", files[i].getName(),
                                    new Date(files[i].lastModified())));
                }
            } else {
                String directoryPath = currentDirFullPath.toAbsolutePath() + "/" + args.get(0);
                File directory = new File(directoryPath);

                if (directory.exists() && directory.isDirectory()) {
                    formattedOutput.append(String.format("%-50s %10s\n", "File Name", "Last Modified"));

                    for (File childFile : directory.listFiles()) {
                        formattedOutput.append(
                                String.format("%-50s %10s\n", childFile.getName(), new Date(childFile.lastModified())));
                    }
                } else {
                    formattedOutput.append("Invalid directory path or directory doesn't exist.\n");
                }
            }
        }

        return formattedOutput.toString();
    }

    public static String echo(List<String> args) {
        String output = "";
        for (String string : args) {
            output += string + '\n';
        }
        return output;
    }

    public static String mkdir(List<String> args) {
        if (args.size() < 1) {
            return invalidCommandSyntax();
        }

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
        if (args.size() != 1) {
            return invalidCommandSyntax();
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (args.get(0).equals("*")) {
            File[] files = currentDirFullPath.toFile().listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.delete()) {
                        stringBuilder.append(String.format("Deleted Directory %s Succesfully\n", file.getName()));
                    } else {
                        stringBuilder.append(String.format("Couldn't delete Directory %s\n", file.getName()));
                    }
                }
            }
        } else {
            String directoryPath = args.get(0);

            if (!Paths.get(directoryPath).isAbsolute()) {
                directoryPath = currentDirFullPath.toAbsolutePath() + "/" + directoryPath;
            }

            File file = new File(directoryPath);
            if (file.isDirectory()) {
                if (file.delete()) {
                    stringBuilder.append(String.format("Deleted Directory %s Succesfully\n", file.getName()));
                } else {
                    stringBuilder.append(String.format("Couldn't delete Directory %s\n", file.getName()));
                }
            } else {
                stringBuilder.append(String.format("%s is not a directory!\n", file.getName()));
            }
        }

        return stringBuilder.toString();
    }

    public static String touch(List<String> args) {
        if (args.size() != 1) {
            return invalidCommandSyntax();
        }

        String directoryPath = args.get(0);

        if (!Paths.get(directoryPath).isAbsolute()) {
            directoryPath = currentDirFullPath.toAbsolutePath() + "/" + directoryPath;
        }

        File file = new File(directoryPath);

        if (file.exists()) {
            return String.format("File %s already exists\n", file.getName());
        } else {
            try {
                file.createNewFile();
                return String.format("File %s created successfully\n", file.getName());
            } catch (IOException e) {
                return String.format("Failed to create file %s\n", file.getName());
            }
        }
    }

    public static String cp(List<String> args) {
        if (args.size() < 1) {
            return invalidCommandSyntax();
        }

        if (args.remove("-r")) {
            return copyDirectories(args);
        } else {
            return copyFiles(args);
        }
    }

    private static String copyDirectories(List<String> args) {
        if (args.size() != 2) {
            return invalidCommandSyntax();
        }

        String sourceDirName = args.get(0);
        String destinationDirName = args.get(1);

        Path sourcePath = Paths.get(args.get(0)).toAbsolutePath().normalize();
        Path destinationPath = Paths.get(args.get(1)).toAbsolutePath().normalize();

        int sourceParentLength = sourcePath.getParent().toString().length();

        StringBuilder output = new StringBuilder();
        output.append("\n");
        try {
            // var tree = Files.walk(Paths.get(sourceDirName).toAbsolutePath().normalize());

            // for (Path source : tree.toArray(Path[]::new)) {
            //     Path destination = destinationPath.resolve(source.toString().substring(sourceParentLength + 1));
            // }

            Files.walk(Paths.get(sourceDirName).toAbsolutePath().normalize())
                    .forEach(source -> {
                        Path destination = destinationPath.resolve(source.toString().substring(sourceParentLength + 1));

                        try {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            
                        }
                    });
        } catch (IOException e) {
            return String.format("%s: Could not access directory at: '%s'\n", parser.getCommandName(), sourceDirName);
        }

        return output.toString();
    }

    private static String copyFiles(List<String> args) {
        if (args.size() != 2) {
            return invalidCommandSyntax();
        }

        String fileContent;
        try {
            fileContent = readFileContent(args.get(0)).toString();
        } catch (IOException e) {
            return String.format("%s: Could not open file at: '%s'\n", parser.getCommandName(), args.get(0));
        }

        try {
            PrintWriter printer = new PrintWriter(
                    new FileWriter(
                            Paths.get(args.get(1)).toRealPath(LinkOption.NOFOLLOW_LINKS).toFile(), false));

            printer.print(fileContent);
            printer.close();
        } catch (IOException e) {
            return String.format("%s: Could not open/create file at: '%s'\n", parser.getCommandName(), args.get(1));
        }

        return "\n";
    }

    public static String rm(List<String> args) {
        if (args.size() != 1) {
            return invalidCommandSyntax();
        }

        String fileName = args.get(0);
        File fileToDelete = new File(currentDirFullPath.toAbsolutePath() + File.separator + fileName);

        if (fileToDelete.exists() && fileToDelete.isFile()) {
            if (fileToDelete.delete()) {
                return String.format("File %s deleted successfully\n", fileName);
            } else {
                return String.format("Failed to delete File %s\n", fileName);
            }
        } else {
            return String.format("File %s does not exist or is not a regular file\n", fileName);
        }
    }

    public static String cat(List<String> args) {
        if (args.size() < 1 || args.size() > 2) {
            return invalidCommandSyntax();
        }

        StringBuilder output = new StringBuilder();
        for (String fileName : args) {
            try {
                output.append(readFileContent(fileName));
            } catch (IOException e) {
                return String.format("%s: Could not open file at: '%s'\n", parser.getCommandName(), fileName);
            }
        }

        return output.toString();
    }

    private static StringBuilder readFileContent(String fileName) throws IOException {
        StringBuilder output = new StringBuilder();
        Scanner inputFile;

        inputFile = new Scanner(
                Paths.get(fileName)
                        .toRealPath(LinkOption.NOFOLLOW_LINKS).toFile());

        while (inputFile.hasNextLine()) {
            output.append(inputFile.nextLine()).append("\n");
        }

        inputFile.close();

        return output;
    }

    public static String wc(List<String> args) {
        if (args.size() != 1) {
            return invalidCommandSyntax();
        }

        String directoryPath = args.get(0);

        if (!Paths.get(directoryPath).isAbsolute()) {
            directoryPath = currentDirFullPath.toAbsolutePath() + "/" + directoryPath;
        }

        File file = new File(directoryPath);

        if (!file.exists()) {
            return String.format("%s: %s: No such file\n", parser.getCommandName(), directoryPath);
        }

        Scanner inputFile;
        int lineCount = 0, wordCount = 0, charCount = 0;
        try {
            inputFile = new Scanner(file);

            while (inputFile.hasNextLine()) {
                String line = inputFile.nextLine();

                lineCount++;

                if (line.isBlank())
                    continue;

                wordCount += line.split(" ").length;
                charCount += line.length();
            }

            inputFile.close();
        } catch (IOException e) {
            return String.format("%s: Could not open file at: '%s'\n", parser.getCommandName(), directoryPath);
        }

        return String.format("%d %d %d %s\n",
                lineCount, wordCount, charCount,
                Paths.get(directoryPath).getFileName().toString());
    }

    public static String history() {
        var commands = parser.getHistory();

        var output = new StringBuilder();
        for (int i = 0; i < commands.size(); ++i) {
            output.append(String.format("%d\t%s\n", i + 1, commands.get(i)));
        }

        return output.toString();
    }

    private static String invalidCommandSyntax() {
        return String.format("%s%s: Invalid command syntax\n", ErrorPrefix, parser.getCommandName());
    }
}
