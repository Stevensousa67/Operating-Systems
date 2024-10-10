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
            case "cd" -> System.out.println("myShell> cd command found. myShell closing.\n");
            case "mkdir" -> System.out.println("myShell> mkdir command found. myShell closing.\n");
            case "rmdir" -> System.out.println("myShell> rmdir command found. myShell closing.\n");
            case "mv" -> System.out.println("myShell> mv command found. myShell closing.\n");
            case "cp" -> System.out.println("myShell> cp command found. myShell closing.\n");
            case "cat" -> System.out.println("myShell> cat command found. myShell closing.\n");
            case "more" -> System.out.println("myShell> more command found. myShell closing.\n");
            case "rm" -> System.out.println("myShell> rm command found. myShell closing.\n");
            case "args" -> System.out.println("myShell> args command found. myShell closing.\n");
            case "date" -> System.out.println("myShell> date command found. myShell closing.\n");
            case "help" -> System.out.println("myShell> help command found. myShell closing.\n");
            default -> System.out.println("myShell> Command not found. myShell closing.\n");
        }
        
    }
}
