import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public void writeOutput(String format, Object... args) {
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
}
