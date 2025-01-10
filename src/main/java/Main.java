import java.util.*;
import java.io.*;

public class Main {
    static final String PATH = System.getenv("PATH");
    static final PrintStream STDOUT = System.out;

    public static void main(String[] args) throws Exception {
        List<String> builtins = new ArrayList<>();
        builtins.add("echo");
        builtins.add("pwd");
        builtins.add("exit");
        builtins.add("type");
        builtins.add("cd");

        while (true) {
            System.setOut(STDOUT);
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if(input.isBlank()){
                continue;
            }
            Arguments commandArgs = new Arguments(input);
            setOutput(commandArgs);
            String command = commandArgs.getArg(0);

            switch (command) {
                case "cd" -> changeDir(commandArgs.getArg(1));
                case "pwd" -> {
                    String cwd = System.getProperty("user.dir");
                    System.out.println(cwd);
                }
                case "echo" -> {
                    List<String> tokens = commandArgs.getTokens();
                    int tokenSize = tokens.size();
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i <  tokenSize; i++ ){
                        builder.append(tokens.get(i));
                        if(i+1 < tokenSize){
                            builder.append(" ");
                        }
                    }
                    System.out.println(builder);
                }
                case "exit" -> System.exit(Integer.parseInt(commandArgs.getArg(1)));
                case "type" -> {
                    String arg0 = commandArgs.getArg(1);
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
                    Optional<String> execPath = getPath(commandArgs.getArg(0));
                    if (execPath.isPresent()) {

                        //System.out.println("## Command ##");
                        //System.out.println(commandArr);

                        ProcessBuilder processBuilder = new ProcessBuilder(commandArgs.getTokens());
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
        //for windows: split by ;
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

    private static void setOutput(Arguments arguments){
        List <String> tokens = arguments.getTokens();
        int stdOutIndex = tokens.indexOf(">");
        if(stdOutIndex < 0 ){
            tokens.indexOf("1>");
        }
        if(stdOutIndex > 0){
            String filePath = arguments.getArg(stdOutIndex+1);
            try {
                System.setOut(new PrintStream(filePath));
            }
            catch (FileNotFoundException e) {
                System.out.printf("%s: %s: No such file or directory%n",arguments.getArg(0),filePath);
            }
            tokens.remove(stdOutIndex);
            tokens.remove(stdOutIndex + 1);
        }

    }

}
