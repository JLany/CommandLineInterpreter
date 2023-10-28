import java.util.List;
import java.util.ArrayList;

class Parser {
    private String commandName;
    private List<String> args;

    // This method will divide the input into commandName and args
    // where "input" is the string command entered by the user.
    public boolean parse(String input) {
        commandName = "";
        args = new ArrayList<>();
        
        String[] commandSections = input.trim().split(" ");

        if (commandSections.length < 0) {
            return true;
        }

        commandName = commandSections[0];

        for (int i = 1; i < commandSections.length; ++i) {
            args.add(commandSections[i]);
        }

        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public List<String> getArgs() {
        return args;
    }
}

