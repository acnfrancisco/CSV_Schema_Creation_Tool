import java.util.*;
import java.lang.*;

public class Parser {

    ArrayList<Column> columns = new ArrayList<Column>();

    /*
     * Here we parse the file and split on commas. However, we must respect
     * commas that are nested in quotes. To do this we use the following regex,
     * code adapted from:
     * http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
     */
    public void parse(String inputLine, int lineNum){
        String otherThanQuote = " [^\"] ";
        String quotedString = String.format(" \" %s* \" ", otherThanQuote);
        String regex = String.format("(?x) "+ // enable comments, ignore white spaces
                ",                         "+ // match a comma
                "(?=                       "+ // start positive look ahead
                "  (                       "+ //   start group 1
                "    %s*                   "+ //     match 'otherThanQuote' zero or more times
                "    %s                    "+ //     match 'quotedString'
                "  )*                      "+ //   end group 1 and repeat it zero or more times
                "  %s*                     "+ //   match 'otherThanQuote'
                "  $                       "+ // match the end of the string
                ")                         ", // stop positive look ahead
                otherThanQuote, quotedString, otherThanQuote);

        String[] tokens = inputLine.split(regex);
        int i = 0;
        for(String t : tokens) {
            //duplicate string with whitespace stripped for numerical checks
            //String st = t.replaceAll("\\s","");
            String st2 = t.trim();

            if (lineNum == 0) { //line 0 should contain column names... so we make new columns
                columns.add(new Column(t));
            } else if(st2.isEmpty()){
                columns.get(i).setType("varchar");
                columns.get(i).lenMax(t.length());
                i++;
            } else {
                if (columns.get(i).getType().equals("UNASSIGNED")){

                    //initial check to see if column is numerical/decimal/varchar
                    if (isNumeric(st2)){
                        columns.get(i).setType("num");
                        columns.get(i).doMinMaxNum(Long.valueOf(st2));
                        columns.get(i).lenMax(t.length());
                    } else if(isDecimal(st2) || isSI(st2)){
                        columns.get(i).setType("double");
                        columns.get(i).lenMax(t.length());
                    } else {
                        columns.get(i).setType("varchar");
                        columns.get(i).lenMax(t.length());
                    }
                } else if (columns.get(i).getType().equals("varchar")){ //if already a varchar, keep it a varchar
                    columns.get(i).lenMax(t.length());
                } else if (columns.get(i).getType().equals("num")){     //if it is numerical it can still become a varchar or a double
                    if (isNumeric(st2)){
                        columns.get(i).doMinMaxNum(Long.valueOf(st2));
                        columns.get(i).lenMax(t.length());
                    } else if (isDecimal(st2) || isSI(st2)){ //if it is already a double it can only become a varchar
                        columns.get(i).setType("double");
                        columns.get(i).lenMax(t.length());
                    } else {
                        columns.get(i).setType("varchar");
                        columns.get(i).lenMax(t.length());
                    }
                } else if (columns.get(i).getType().equals("double")){
                    if (isNumeric(t) || isDecimal(t) || isSI(t)){
                        columns.get(i).lenMax(t.length());
                    } else {
                        columns.get(i).lenMax(t.length());
                        columns.get(i).setType("varchar");
                    }
                }
                i++;
            }
        }
    }


    //DEBUG
    public void printColumns() {

        for (Column item : columns) {
            System.out.println(item);
        }
        System.out.println();
    }
    //END DEBUG

    public boolean isNumeric(String s){
        String pattern= "^-?[0-9]+$";
        return s.matches(pattern);
    }

    public boolean isDecimal(String s){
        String pattern= "^-?[0-9]*[.][0-9]*$";
        return s.matches(pattern);
    }

    public boolean isSI(String s){
        String pattern= "^-?\\d*\\.\\d*E\\+\\d+$";
        return s.matches(pattern);
    }
    public ArrayList<Column> getColumns(){
        return columns;
    }
}