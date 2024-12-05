import java.util.ArrayList;
import java.util.List;

public class Arguments {

    private String argString;
    private final List<String> tokens;


    public Arguments(){
        tokens = new ArrayList<>();
    }

    public Arguments(String arg) throws Exception {
        argString = arg;
        tokens = new ArrayList<>();
        tokenize();
    }

    public void setArgString(String argString) throws Exception {
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

    private void tokenize() throws Exception {
        if(argString == null){throw new Exception("No arguments provided");}
        StringBuilder builder = new StringBuilder();
        boolean simpleQuotes = false;
        boolean doubleQuotes = false;
        boolean escaping = false;
        char separator = ' ';
        int length = argString.length()-1;
        int counter = 0;
        // abc\ def
        for(char c : argString.toCharArray()){
            if(escaping){
                //System.out.println("escaping, appending: "+c);
                builder.append(c);
                escaping = false;
                counter++;
                continue;
            }
            else if (c == '\'' && !doubleQuotes){
                simpleQuotes = !simpleQuotes;
            }
            else if( c == '"' && !escaping && !simpleQuotes){
                doubleQuotes = !doubleQuotes;
            }
            else if( c == '\\' && !simpleQuotes ){
                //TODO: Can only escape based on the next char.
                if(doubleQuotes){
                    char nextChar = argString.charAt(counter+1);
                    if( nextChar == '\\' || nextChar == '"' || nextChar == '$'){
                        escaping = true;
                    }
                }else{
                    escaping = true;
                }

            }
            else if(c != separator || (c == separator && (doubleQuotes || simpleQuotes) )){
                //append whitespace IF AND ONLY IF quoting is taking place
                //dobleQuotes -> true && simpleQuotes == true then append whitespace
                //otherwhise do nothing, ONLY IF whitespace
                //System.out.println("appending: " + c);

                builder.append(c);
            }
            if ( (c == separator && !simpleQuotes && !doubleQuotes) || counter == length){
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
