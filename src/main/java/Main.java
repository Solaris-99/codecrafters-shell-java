import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        List<String> builtins = new ArrayList<>();
        builtins.add("echo");builtins.add("exit");builtins.add("type");

        while (true){
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String command;
            String commandArgs = "";
            if(input.contains(" ")){
                int delim = input.indexOf(" ");
                command = input.substring(0, delim);
                commandArgs = input.substring(delim+1);
            }else{
                command = input;
            }


            if(input.isBlank()){continue;}
            else{
                switch (command){
                    case "echo":
                        System.out.println(commandArgs);
                        break;
                    case "exit":
                        System.exit(Integer.parseInt(commandArgs));
                    case "type":
                        int ind = builtins.indexOf(commandArgs);
                        String out;
                        if(ind > -1){
                            out = commandArgs + " is a shell builtin";
                        }else{
                            out = commandArgs + ": not found";
                        }
                        System.out.println(out);
                        break;
                    default:
                        System.out.println(input + ": command not found");

                }
            }

        }

    }
}
