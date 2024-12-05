import java.util.*;
import java.io.*;

public class Main {
    static final String PATH = System.getenv("PATH");

    public static void main(String[] args) throws Exception {
        List<String> builtins = new ArrayList<>();
        builtins.add("echo");
        builtins.add("pwd");
        builtins.add("exit");
        builtins.add("type");
        builtins.add("cd");

        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String command;
            Arguments commandArgs = new Arguments();
            if (input.isBlank()) {
                continue;
            }
            else if (input.contains(" ")) {
                int delim = input.indexOf(" ");
                command = input.substring(0, delim);
                String commandArgsString = input.substring(delim + 1);
                commandArgs.setArgString(commandArgsString);
            } else {
                command = input;
            }


            switch (command) {
                case "cd" -> changeDir(commandArgs.getArg(0));
                case "pwd" -> {
                    String cwd = System.getProperty("user.dir");
                    System.out.println(cwd);
                }
                case "echo" -> {
                    for(int i = 0; i < commandArgs.getArgCount(); i++){
                        System.out.print(commandArgs.getArg(i));
                    }
                    System.out.println();
                }
                case "exit" -> System.exit(Integer.parseInt(commandArgs.getArg(0)));
                case "type" -> {
                    String arg0 = commandArgs.getArg(0);
                    int ind = builtins.indexOf(arg0);
                    String out;
                    if (ind > -1) {
                        out = arg0 + " is a shell builtin";
                    } else if (getPath(arg0).isPresent()) {
                        out = arg0 + " is " + getPath(arg0).get();
                    } else {
                        out = arg0 + ": not found";
                    }
                    System.out.println(out);
                }
                default -> {
                    Optional<String> execPath = getPath(command);
                    if (execPath.isPresent()) {
                        List<String> commandArr = new ArrayList<>();
                        commandArr.add(execPath.get());
                        commandArr.addAll(commandArgs.getTokens());
                        ProcessBuilder processBuilder = new ProcessBuilder(commandArr);
                        processBuilder.redirectErrorStream(true);
                        Process process = processBuilder.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                    else {
                        System.out.println(input + ": command not found");
                    }
                }
            }//switch end

        }//loop end
    }

    private static Optional<String> getPath(String command){
        String[] pathDirs = PATH.split(":");
        for (String path : pathDirs){
            File dir = new File(path);
            File[] files = dir.listFiles();
            if (files != null) {
                for(File file : files){
                    if(file.canExecute()){
                        String name = file.getName();
                        if(name.equals(command)){
                            return Optional.of(file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static void changeDir(String path){
        try{
            File dir;
            if(path.startsWith(".")){
                dir = new File(System.getProperty("user.dir"),path);
            }
            else if(path.equals("~")){
                dir = new File(System.getenv("HOME"));
            }
            else{
                dir = new File(path);
            }

            if(!dir.exists()){
                System.out.printf("cd: %s: No such file or directory%n", path);
                return;
            }
            System.setProperty("user.dir", dir.getCanonicalPath());
        }
        catch (IOException e){
            System.out.printf("cd: %s: cannot be opened: %s%n", path, e.getMessage());
        }

    }

}
