import java.util.ArrayList;
import java.sql.*;

public class QueryBuilder {

    // class constructor
    public QueryBuilder() {
    }

    public String createTable(String tblName, ArrayList<Column> columns) {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append("CREATE TABLE IF NOT EXISTS " + "`" +tblName + "`" + " (" + NEW_LINE);
        for (int i=0; i<columns.size(); ++i){
            if (columns.get(i).getType().equals("UNASSIGNED")){
                columns.get(i).setType("varchar");
                columns.get(i).setLen(20);
            }

            result.append("`" + columns.get(i).getName()+"`" + " ");

            if (columns.get(i).getType().equals("varchar")){
                result.append("VARCHAR(");
                result.append(columns.get(i).getLen().toString());
                result.append(") " + NEW_LINE);
            } else if(columns.get(i).getType().equals("num")) {
                if (columns.get(i).getMin() < 0){
                    result.append("UNSIGNED ");
                    //negative nums
                    Long tempMax = Math.max(Math.abs(columns.get(i).getMin()), columns.get(i).getMax());
                    if      (tempMax < 128)                  result.append("TINYINT");
                    else if (tempMax < 32768)                result.append("SMALLINT");
                    else if (tempMax < 8388608)              result.append("MEDIUMINT");
                    else if (tempMax < 2147483648L)          result.append("INT");
                    else if (tempMax < 9223372036854775808D) result.append("BIGINT");
                } else {
                    //non-negative nums
                    Long tempMax = columns.get(i).getMax();
                    if      (tempMax < 256)                  result.append("TINYINT");
                    else if (tempMax < 65536)                result.append("SMALLINT");
                    else if (tempMax < 1677216)              result.append("MEDIUMINT");
                    else if (tempMax < 4294967295L)          result.append("INT");
                    else if (tempMax < 18446744073709551615D)result.append("BIGINT");
                }
            } else if (columns.get(i).getType().equals("double")) {
                result.append("DOUBLE");
            }
            if(i != columns.size()-1){
                result.append("," + NEW_LINE);
            }
        }
        result.append(");");


        return result.toString();
    }
}
