import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
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

            chooseCommandAction();
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
            case "exit":
                run = false;
                break;
            default:
                printe(command);;
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
        }
        catch (IOException e) {
            printe("the path you specified");
        }

        currentDirFullPath = inputPath;
    }
    
    public static void printe(String arg) {
        System.out.printf("%s%s: not found\n", ErrorPrefix, arg);
    }
}
