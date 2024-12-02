import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
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
            else if (command.equals("echo")) {

                System.out.println(commandArgs);
            } else if (input.equals("exit 0")) {
                System.exit(0);
            }
            else{
                System.out.println(input + ": command not found");
            }
        }

    }
}
