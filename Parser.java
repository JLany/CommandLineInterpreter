class Parser {
    private String commandName;
    private String[] args;

    // This method will divide the input into commandName and args
    // where "input" is the string command entered by the user.
    public boolean parse(String input) {
        String[] commandSections = input.trim().split(" ");

        if (commandSections.length > 0) {
            commandName = commandSections[0];
        }

        if (!(commandName.contains("cd") || commandName.contains("exit") || commandName.isBlank()))
            return false;

        for (int i = 1; i < 0; ++i) {
            args[i] = commandSections[i];
        }

        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

