import java.util.ArrayList;
import java.util.List;

public class CommandInfo {
    String[] input;
    String output;

    CommandInfo(String[] input, String output){
        this.input = input;
        this.output = output;
    }
    public static CommandInfo parseDirection(String[] command){
        String output = null;
        List<String> list = new ArrayList<>();
        for(int i = 0; i < command.length; i++){
            if(command[i].equals(">") || command[i].equals("1>")){
                if(i + 1 < command.length){
                    output=command[i+1];
                    i++;
                }
            }
            else{
                list.add(command[i]);
            }
        }
        return new CommandInfo(list.toArray(new String[0]), output);
    }
}
