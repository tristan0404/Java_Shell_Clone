import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.terminal.*;

import java.io.File;
import java.util.List;

class ShellCompleter implements Completer {
    private String currDir;
    private String[] builtIns = {"echo", "exit", "type", "pwd", "cd"};

    public ShellCompleter(String currDir) {
        this.currDir = currDir;
    }
    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String word = line.word();

        if(line.wordIndex() == 0){
            for(String builtin : builtIns){
                if(builtin.startsWith(word)){
                    candidates.add(new Candidate(builtin));
                }
            }
        }

        String pathEnv = System.getenv("PATH");
        if(pathEnv != null){
            String[] paths = pathEnv.split(":");
            for(String path : paths){
                File dir = new File(path);
                if(dir.exists() && dir.isDirectory()){
                    File[] files = dir.listFiles();
                    if(files != null){
                        for(File file : files){
                            if(file.canExecute() && file.getName().startsWith(word)){
                                candidates.add(new Candidate(file.getName()));
                            }
                        }
                    }
                }
            }
        }
        else{
            fileCompleter(word, candidates);
        }
    }

    private void fileCompleter(String word, List<Candidate> candidates){
        File currentDir;
        String filePrefix;

        if (word.contains("/")){
            int lastSlash = word.lastIndexOf("/");
            String dirPath = word.substring(0, lastSlash + 1);
            filePrefix = word.substring(lastSlash + 1);

            File dir = new File(dirPath);
            if (!dir.isAbsolute()){
                currentDir = new File(currDir,dirPath);
            }
            else{
                currentDir = dir;
            }
        }
        else{
            currentDir = new File(currDir);
            filePrefix = word;
        }
        if(currentDir.exists() && currentDir.isDirectory()){
            File[] files = currentDir.listFiles();
            if(files != null){
                for(File file : files){
                    if(file.getName().startsWith(filePrefix)){
                        String completion = word.substring(0, word.length()-filePrefix.length())+file.getName();
                        if(file.isDirectory()){
                            completion += "/";
                        }
                        candidates.add(new Candidate(completion,file.getName(),null, null, null, null, true));
                    }
                }
            }
        }
    }
}
