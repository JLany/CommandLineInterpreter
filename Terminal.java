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

    public static void main(String[] args) {
        boolean run = true;

        while (run) {
            System.out.print(currentDirFullPath.toString() + ">");
            chooseCommandAction();
        }
    }

    public static void chooseCommandAction() {
        var in = new Scanner(System.in);
        var input = in.nextLine();
        if (input.isBlank()) {
            return;
        }

        boolean isValidInput = parser.parse(input);
        if (!isValidInput) {
            System.out.printf("%s%s: command not found\n", ErrorPrefix, parser.getCommandName());
            return;
        }

        var command = parser.getCommandName();
        if (command.compareTo("cd") == 0) {
            currentDirFullPath = Paths.get(System.getProperty("user.home"));
        }

        in.close();
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
