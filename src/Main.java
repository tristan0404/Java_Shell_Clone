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

            if (c == '\\' && i + 1 < command.length() && !quotes) {
                i++;
                char next = command.charAt(i);
                if (dblQuotes) {
                    if (next == '\\' || next == '"' || next == '$') {
                        input.append(next);
                    }
                    else{
                        input.append("\\");
                        input.append(next);
                    }
                }
                else{
                    input.append(next);
                }
                continue;
            }
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
//region Exit Command
            if (String.join(" ", command).equals("exit 0")) {
                System.exit(0);
            }
            //endregion
            //region Echo Command
            else if (command[0].equals("echo")) {
                StringBuilder builder = new StringBuilder();
                CommandInfo cmdInfo = CommandInfo.parseDirection(command);
                String[] actual =  cmdInfo.input;
                String output = cmdInfo.output;
                boolean appendOutput = cmdInfo.appendOutput;

                for (int i = 1; i < actual.length; i++) {
                    if (i > 1) {
                        builder.append(" ");
                    }
                    builder.append(actual[i]);
                }
                //region stdout handling
                if (output != null) {
                    File outputFile = new File(output);
                    if (!outputFile.isAbsolute()) {
                        outputFile = new File(currDir, output);
                    }

                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, appendOutput))) {
                        writer.write(builder.toString());
                        writer.newLine();
                    }
                    catch (IOException e) {
                        System.out.println("Error" + e.getMessage());
                    }
                }//endregion
                else {
                    System.out.println(builder);
                }
            }
            //endregion
            //region Type Command
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
            //endregion
            //region CD Command
            else if (command[0].equals("cd")) {
                if (command.length < 2) {
                    currDir = System.getenv("HOME");
                    if (currDir == null) {
                        currDir = System.getProperty("user.home");
                    }
                } else if (command[1].equals("~")) {
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
            //endregion
            //region PWD Command
            else if (command[0].equals("pwd")) {
                System.out.println(currDir);
            }
            //endregion
            //region Command Handler
            else {
                CommandInfo cmdInfo = CommandInfo.parseDirection(command);
                String[] actual =  cmdInfo.input;
                String output = cmdInfo.output;
                String error = cmdInfo.error;
                boolean appendOutput = cmdInfo.appendOutput;
                boolean appendError = cmdInfo.appendError;

                //region Path search
                String pathEnv = System.getenv("PATH");
                String[] paths = pathEnv.split(":");
                String fullPath = null;

                if(paths == null || paths.length == 0) {
                    System.out.println("No PATH found");
                }

                for (String path : paths) {
                    File file = new File(path, actual[0]);
                    if (file.exists() && file.canExecute()) {
                        fullPath = file.getAbsolutePath();
                        break;
                    }
                }
                if (fullPath == null) {
                    System.out.println(String.join(" ", actual) + ": command not found");
                    continue;
                }
                //endregion

                try {
                    List<String> commandList = new ArrayList<>();
                    //commandList.add(fullPath);
                    commandList.add(actual[0]);
                    for (int i = 1; i < actual.length; i++) {
                        commandList.add(actual[i]);
                    }
                    ProcessBuilder pb = new ProcessBuilder(commandList);
                    Map<String, String> env = pb.environment();
                    String dir = new File(fullPath).getParent();
                    env.put("PATH", dir + ":" + env.get("PATH"));
                    pb.directory(new File(currDir));
                    Process p = pb.start();
                    p.waitFor();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    //region stdout handling
                    if (output != null) {
                        File outputFile = new File(output);
                        if (!outputFile.isAbsolute()) {
                            outputFile = new File(currDir, output);
                        }
                        File parent = outputFile.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, appendOutput))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            System.out.println(line);
                        }
                    }else {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }

                    }
                    //endregion
                    //region stderr handling
                    if (error != null) {
                        File errorFile = new File(error);
                        if (!errorFile.isAbsolute()) {
                            errorFile = new File(currDir, error);
                        }
                        File parent = errorFile.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(errorFile, appendError))) {
                            String line;
                            while ((line = errorReader.readLine()) != null) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                    }else {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                    //endregion
                } catch (Exception e) {
                    System.out.println(String.join(" ", actual) + ": command not found");
                }
            }
            //endregion
        }

    }
}
