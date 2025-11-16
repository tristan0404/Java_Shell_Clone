import java.io.*;
import java.util.*;
import java.util.Scanner;

public class Main {
    private static List<String> parseCommand(String command) {
        List<String> elements = new ArrayList<>();
        StringBuilder input = new StringBuilder();
        boolean quotes = false;
        boolean dblQuotes = false;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (c == '\'' && !dblQuotes) {
                quotes = !quotes;
            }else if (c == '\"' && !quotes) {
                dblQuotes = !dblQuotes;
            }
            else if (c == ' ' && !quotes && !dblQuotes) {
                if (input.length() > 0) {
                    elements.add(input.toString());
                    input = new StringBuilder();
                }
            }
            else{
                input.append(c);
            }
        }
        if (input.length() > 0) {
            elements.add(input.toString());
        }
        return elements;
    }
    public static void main(String[] args) throws Exception {

        String currDir = System.getProperty("user.dir");
        while(true) {
            System.out.print("$ ");
            Scanner sc = new Scanner(System.in);
            List<String> list = parseCommand(sc.nextLine());
            String[] command = list.toArray(new String[0]);

            if (String.join(" ", command).equals("exit 0")) {
                System.exit(0);
            } else if (command[0].equals("echo")) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < command.length; i++) {
                    if (i > 1) {
                        builder.append(' ');
                    }
                    builder.append(command[i]);
                }
                System.out.println(builder);
            }
            else if (command[0].equals("type")) {
                if (command[1].equals("echo") || command[1].equals("type")|| command[1].equals("exit") || command[1].equals("pwd")) {
                    System.out.println(command[1] +" is a shell builtin");
                }else{
                    String pathEnv = System.getenv("PATH");
                    String[] paths = pathEnv.split(":"); // Split PATH by colon (:)
                    boolean found = false;

                    for (String dir : paths) {
                        File file = new File(dir, command[1]); // e.g. /usr/bin/grep
                        if (file.exists()) {
                            if (file.canExecute()) {
                                System.out.println(command[1] + " is " + file.getAbsolutePath());
                                found = true;
                                break;
                            } else {
                            }
                        }
                    }
                    if (!found) {
                        System.out.println(command[1]+ ": not found");
                    }
                }
            }
            else if (command[0].equals("cd")) {
                if (command[1].equals("~")) {
                    currDir = System.getenv("HOME");
                    if (currDir == null) {
                        currDir = System.getProperty("user.home");
                    }
                }
                else {
                    File newDir =  new File(command[1]);
                    if (newDir.isAbsolute()) {
                        if (newDir.exists() && newDir.isDirectory()) {
                            currDir = command[1];
                        }
                        else  {
                            System.out.println("cd: " + command[1] + ": No such file or directory");
                        }
                    } else {
                        newDir = new File(currDir, command[1]);
                        currDir = newDir.toPath().normalize().toString();
                    }

                }
            }
            else if (command[0].equals("pwd")) {
                System.out.println(currDir);
            }
            else {
                String pathEnv = System.getenv("PATH");
                String[] paths = pathEnv.split(":");
                String fullPath = null;

                if(paths == null || paths.length == 0) {
                    System.out.println("No PATH found");
                }

                for (String path : paths) {
                    File file = new File(path, command[0]);
                    if (file.exists() && file.canExecute()) {
                        fullPath = file.getAbsolutePath();
                        break;
                    }
                }
                try {
                    List<String> commandList = new ArrayList<>();
                    //commandList.add(fullPath);
                    commandList.add(command[0]);
                    for (int i = 1; i < command.length; i++) {
                        commandList.add(command[i]);
                    }
                    ProcessBuilder pb = new ProcessBuilder(commandList);
                    Map<String, String> env = pb.environment();
                    String dir = new File(fullPath).getParent();
                    env.put("PATH", dir + ":" + env.get("PATH"));
                    Process p = pb.start();
                    p.waitFor();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (Exception e) {
                    System.out.println(String.join(" ", command) + ": command not found");
                }
            }
        }

    }
}
