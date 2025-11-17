import java.util.ArrayList;
import java.util.List;

public class CommandInfo {
    String[] input;
    String output;
    String error;

    CommandInfo(String[] input, String output, String error) {
        this.input = input;
        this.output = output;
        this.error = error;
    }
    public static CommandInfo parseDirection(String[] command){
        String output = null;
        String error = null;
        List<String> list = new ArrayList<>();

        for(int i = 0; i < command.length; i++){
            if(command[i].equals(">") || command[i].equals("1>")){
                if(i + 1 < command.length){
                    output=command[i+1];
                    i++;
                }
            }else if(command[i].equals("2>")){
                if(i + 1 < command.length){
                    error =command[i+1];
                    i++;
                }
            }
            else{
                list.add(command[i]);
            }
        }
        return new CommandInfo(list.toArray(new String[0]), output, error);
    }
}
