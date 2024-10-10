import java.util.ArrayList;
import java.util.List;

public class commandFactory {
    private static final List<String> cmdHistory = new ArrayList<>();

    // Store the command and its arguments into a history array
    public static void buildCommand(String[] cmdPlusArgs) {
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
            case "cd" -> commandFunctions.cd(cmd);
            case "mkdir" -> commandFunctions.mkdir(cmd);
            case "rmdir" -> commandFunctions.rmdir(cmd);
            case "mv" -> commandFunctions.mv(cmd);
            case "cp" -> commandFunctions.cp(cmd);
            case "cat" -> commandFunctions.cat(cmd);
            case "more" -> commandFunctions.more(cmd);
            case "rm" -> commandFunctions.rm(cmd);
            case "date" -> commandFunctions.date(cmd);
            default -> System.out.println("myShell> Command not found. myShell closing.\n");
        }
        
    }
}
