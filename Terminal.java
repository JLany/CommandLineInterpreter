import java.nio.file.Path;
import java.nio.file.Files;
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

            if (!isValidInput) {
                System.out.printf("%s%s: command not found\n", ErrorPrefix, parser.getCommandName());
                continue;
            }

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
                currentDirFullPath = Paths.get(System.getProperty("user.home"));
                break;
            case "exit":
                run = false;
                break;
            default:
                System.out.printf("%s%s: command not found", ErrorPrefix, command);
        } 
        
    }

    // Commands.
    public static String pwd() {
        return "";
    }

    // public static String ls(String arg) {
    //     try {
    //         currentDirFullPath = Paths.get(System.getProperty("user.dir"));
    //         List<String> entries = getFilesAndDirectories(currentDirFullPath);

    //         return "";
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }

    //     return "";
    // }

    // private static List<String> getFilesAndDirectories(Path directory) throws IOException {
    //     try (DirectoryStream<Path> stream = 
    //             Files.newDirectoryStream(directory,
    //                     (entry) -> Files.isDirectory(entry) || Files.isRegularFile(entry))) {
    //         return stream
    //             .map(Path::getFileName)
    //             .map(Path::toString)
    //             .sorted()
    //             .collect(Collectors.toList());
    //     }
    // }
}
