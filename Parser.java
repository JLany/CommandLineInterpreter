class Parser {
    private String commandName;
    private String[] args;

    // This method will divide the input into commandName and args
    // where "input" is the string command entered by the user.
    public boolean parse(String input) {
        commandName = input.trim().split(" ")[0];

        return false;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

