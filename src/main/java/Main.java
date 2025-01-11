import java.util.*;
import java.io.*;

public class Main {
    static final String PATH = System.getenv("PATH");
    static final PrintStream STDOUT = System.out;
    static final PrintStream STDERR = System.err;

    public static void main(String[] args) throws Exception {
        List<String> builtins = new ArrayList<>();
        builtins.add("echo");
        builtins.add("pwd");
        builtins.add("exit");
        builtins.add("type");
        builtins.add("cd");

        while (true) {
            System.setOut(STDOUT);
            System.setErr(STDERR);
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
                case "append" ->{
                    System.out.append("appended text");
                    System.out.println(commandArgs.getArg(1));
                }
                case "type" -> {
                    String arg0 = commandArgs.getArg(1);
                    int ind = builtins.indexOf(arg0);
                    String out;
                    if (ind > -1) {
                        out = arg0 + " is a shell builtin";
                        System.out.println(out);
                    } else if (getPath(arg0).isPresent()) {
                        out = arg0 + " is " + getPath(arg0).get();
                        System.out.println(out);
                    } else {
                        System.err.println(arg0 + ": not found");
                    }
                }
                default -> {
                    Optional<String> execPath = getPath(commandArgs.getArg(0));
                    if (execPath.isPresent()) {

                        //System.out.println("## Command ##");
                        //System.out.println(commandArr);

                        ProcessBuilder processBuilder = new ProcessBuilder(commandArgs.getTokens());
                        Process process = processBuilder.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                        String err = errReader.readLine();
                        if(err != null){
                            System.err.println(err);
                        }

                    }
                    else {
                        System.err.println(input + ": command not found");
                    }
                }
            }//switch end

        }//loop end
    }

    private static Optional<String> getPath(String command){
        String[] pathDirs = PATH.split(File.pathSeparator);
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
            stdOutIndex = tokens.indexOf("1>");
        }
        int stdErrIndex = tokens.indexOf("2>");

        int stdOutAppend = tokens.indexOf(">>");
        if(stdOutAppend < 0 ){
            stdOutAppend = tokens.indexOf("1>>");
        }
        int stdErrAppend = tokens.indexOf("2>>");


        if(stdOutIndex >= 0 || stdOutAppend >= 0){
            boolean appending = false;
            int opInd = stdOutIndex;
            if ( stdOutAppend >= 0 ){
                appending = true;
                opInd = stdOutAppend;
            }
            String filePath;
            String pathArg = arguments.getArg(opInd+1);
            if(pathArg.startsWith(".")){
                filePath = new File(System.getProperty("user.dir"),pathArg).getPath();
            }
            else{
                filePath = new File(pathArg).getPath();
            }


            try {
                if(appending){
                    String text = readFile(filePath);
                    System.setOut(new PrintStream(filePath));
                    System.out.append(text);
                }
                else{
                    System.setOut(new PrintStream(filePath));
                }
            }
            catch (IOException e) {
                System.err.printf("%s: %s: No such file or directory%n",arguments.getArg(0),filePath);
            }
            tokens.remove(opInd + 1);
            tokens.remove(opInd);
        }
        if(stdErrIndex >= 0 || stdErrAppend >= 0){
            boolean appending = false;
            int opInd = stdErrIndex;
            if ( stdErrAppend >= 0 ){
                appending = true;
                opInd = stdErrAppend;
            }
            String filePath;
            String pathArg = arguments.getArg(opInd+1);
            if(pathArg.startsWith(".")){
                filePath = new File(System.getProperty("user.dir"),pathArg).getPath();
            }
            else{
                filePath = new File(pathArg).getPath();
            }

            try {
                if(appending){
                    String text = readFile(filePath);
                    System.setErr(new PrintStream(filePath));
                    System.err.append(text);
                }
                else{
                    System.setErr(new PrintStream(filePath));;
                }

            }
            catch (IOException e) {
                System.err.printf("%s: %s: No such file or directory%n",arguments.getArg(0),filePath);
            }
            tokens.remove(opInd + 1);
            tokens.remove(opInd);
        }
    }

    private static String readFile(String path) throws IOException {
        FileReader fr = new FileReader(path);
        StringBuilder builder = new StringBuilder();
        int i;
        while ((i=fr.read()) != -1){
            builder.append((char) i );
        }
        return builder.toString();
    }
}
