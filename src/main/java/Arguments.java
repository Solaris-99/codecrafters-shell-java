import java.util.ArrayList;
import java.util.List;

public class Arguments {

    private String argString;
    private final List<String> tokens;


    public Arguments(){
        tokens = new ArrayList<>();
    }

    public Arguments(String arg) throws IllegalArgumentException {
        argString = arg;
        tokens = new ArrayList<>();
        tokenize();
    }

    public void setArgString(String argString) throws IllegalArgumentException {
        this.argString = argString;
        tokenize();
    }

    public String getArg(int ind){
        return tokens.get(ind);
    }

    public int getArgCount(){
        return tokens.size();
    }

    public List<String> getTokens() {
        return tokens;
    }

    private void tokenize() throws IllegalArgumentException {
        if(argString == null){throw new IllegalArgumentException("No arguments provided");}
        StringBuilder builder = new StringBuilder();
        boolean simpleQuotes = false;
        boolean doubleQuotes = false;
        boolean escaping = false;
        char separator = ' ';
        int length = argString.length()-1;
        int counter = 0;
        for(char c : argString.toCharArray()){


            if(escaping){
                //System.out.println("escaping, appending: "+c);
                builder.append(c);
                if(counter != length){
                    escaping = false;
                    counter++;
                    continue;
                }
            }

            if(counter == length){
                if((c == '\'' && simpleQuotes) || (c == '"' && doubleQuotes)){
                    String token = builder.toString();
                    if(!token.isBlank()){
                        tokens.add(token);
                    }
                }
                else if( !simpleQuotes && !doubleQuotes ){
                    //if we are escaping, c is already appended.
                    if (!escaping){
                        builder.append(c);
                    }

                    String token = builder.toString();
                    if(!token.isBlank()){
                        tokens.add(token);
                    }
                }
                break;
            }

            else if (c == '\'' && !doubleQuotes){
                simpleQuotes = !simpleQuotes;
            }
            else if( c == '"' && !simpleQuotes){
                doubleQuotes = !doubleQuotes;
            }
            else if( c == '\\' && !simpleQuotes ){
                //TODO: Can only escape based on the next char.

                if(doubleQuotes){
                    char nextChar = argString.charAt(counter+1);
                    if( nextChar == '\\' || nextChar == '"' || nextChar == '$' || nextChar == '\n'){
                        escaping = true;
                    }
                    else{
                        builder.append(c);
                    }

                }
                else
                {
                    escaping = true;
                }
            }
            else if(c != separator || doubleQuotes || simpleQuotes){
                builder.append(c);
            }
            if ( c == separator && !simpleQuotes && !doubleQuotes){
               String token = builder.toString();
               if(!token.isBlank()){
                   tokens.add(token);
               }
               builder = new StringBuilder();
            }

            counter++;
        }
        //System.out.println("## tokens ##");
        //System.out.println(tokens);
    }


}
