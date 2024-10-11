import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class commandFactory {
    private static final List<String> cmdHistory = new ArrayList<>();

    // Store the command and its arguments into a history array
    public static void buildCommand(String[] cmdPlusArgs) throws IOException {
        // Store the command and its arguments into the history list
        StringBuilder command = new StringBuilder();
        for (String arg : cmdPlusArgs) {
            command.append(arg).append(" ");
        }
        cmdHistory.add(command.toString().trim().toLowerCase());

        // breakdown command and argument(s)
        String cmd = cmdPlusArgs[0].toLowerCase();
        String[] args = new String[cmdPlusArgs.length - 1];
        System.arraycopy(cmdPlusArgs,1,args,0,args.length);
        
        // check what the command is and call the proper function passing along any arguments
        switch (cmd) {
            case "exit" -> commandFunctions.exit(cmd);
            case "history" -> commandFunctions.history(cmdHistory);
            case "ls" -> commandFunctions.ls(cmd);
            case "pwd" -> commandFunctions.pwd(cmd);
            case "cd" -> commandFunctions.cd(cmdHistory.get(cmdHistory.size() - 1)); // pass the last command in the history list
            case "cat" -> commandFunctions.cat(cmdHistory.get(cmdHistory.size() - 1)); // pass the last command in the history list
            case "date" -> commandFunctions.date(cmd);
            default -> System.out.println("myShell> Command not found. myShell closing.\n");
        }
        
    }
}
