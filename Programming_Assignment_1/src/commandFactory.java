
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class commandFactory {

    public static final List<String> cmdHistory = new ArrayList<>();

    public static void buildCommand(String cmdPlusArgs) throws IOException {
        // Check for rerun command (!n) or history command
        if (cmdPlusArgs.startsWith("!")) {
            commandFunctions.reRunLastNCommands(cmdPlusArgs, cmdHistory);
            return; // Early exit, no need to log this command
        } else if (cmdPlusArgs.equals("history")) {
            commandFunctions.history(cmdHistory);
            return; // Early exit, no need to log this command
        }

        // Store the command and its arguments into the history array
        StringBuilder command = new StringBuilder();
        String[] cmdPlusArgsArray = cmdPlusArgs.split(" ");

        for (String arg : cmdPlusArgsArray) {
            command.append(arg).append(" ");
        }
        cmdHistory.add(command.toString().trim().toLowerCase());

        // Breakdown command and argument(s)
        String cmd = cmdPlusArgsArray[0].toLowerCase();
        String[] args = new String[cmdPlusArgsArray.length - 1];
        System.arraycopy(cmdPlusArgsArray, 1, args, 0, args.length);

        // Check what the command is and call the proper function passing along any arguments
        switch (cmd) {
            case "exit" ->
                commandFunctions.exit(cmd);
            case "ls" ->
                commandFunctions.ls(cmd);
            case "pwd" ->
                commandFunctions.pwd(cmd);
            case "cd" ->
                commandFunctions.cd(cmdHistory.get(cmdHistory.size() - 1)); // pass the last command in the history list
            case "cat" ->
                commandFunctions.cat(cmdHistory.get(cmdHistory.size() - 1)); // pass the last command in the history list
            case "date" ->
                commandFunctions.date(cmd);
            default ->
                System.out.println("myShell> Command not found. myShell closing.\n");
        }
    }
}
