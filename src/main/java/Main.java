import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.io.File;

public class Main {
    static final String PATH = System.getenv("PATH");

    public static void main(String[] args) throws Exception {
        List<String> builtins = new ArrayList<>();
        builtins.add("echo");
        builtins.add("exit");
        builtins.add("type");

        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String command;
            String commandArgs = "";
            if (input.contains(" ")) {
                int delim = input.indexOf(" ");
                command = input.substring(0, delim);
                commandArgs = input.substring(delim + 1);
            } else {
                command = input;
            }

            if (input.isBlank()) {
                continue;
            }
            else if (command.equals("echo")){
                System.out.println(commandArgs);
            }
            else if (command.equals("exit")){
                System.exit(Integer.parseInt(commandArgs));
            }
            else if (command.equals("type")){
                int ind = builtins.indexOf(commandArgs);
                String out;
                if (ind > -1) {
                    out = commandArgs + " is a shell builtin";
                }
                else if(getPath(commandArgs).isPresent()){
                    out = commandArgs + " is "+ getPath(commandArgs).get();
                }
                else {
                    out = commandArgs + ": not found";
                }
                System.out.println(out);
            }
            else if (getPath(command).isPresent()){
                ProcessBuilder processBuilder = new ProcessBuilder(getPath(command).get(), commandArgs);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            else{
                System.out.println(input + ": command not found");

            }

        }//loop end
    }

    private static Optional<String> getPath(String command){
        //change back to linux delimiter
        String[] pathDirs = PATH.split(":");
        for (String path : pathDirs){
        //System.out.println("looking into: " + path);
            File dir = new File(path);
            File[] files = dir.listFiles();
            if (files != null) {
                for(File file : files){
                    if(file.canExecute()){
                        String name = file.getName();
                        //System.out.println("checking: "+name);
                        if(name.contains(command)){
                            return Optional.of(file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

}
