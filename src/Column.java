public class Column {

    //Name of the column as read in from the first row of the CSV
    private String name;

    //Type of the column, this will be determined after parsing the entire document
    private String type;

    //used to keep track of the max and min values in a column.
    //only used for cloumns with strictly numerical entries.
    private long maxVal;
    private long minVal;
    private int len;

    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");
		
        result.append(this.getClass().getName() + " Object {" + NEW_LINE);
        result.append(" Name: " + name + NEW_LINE);
        result.append(" Type: " + type + NEW_LINE);
        result.append(" Length: " + len + NEW_LINE);
        result.append(" Maximum Value: " + maxVal + NEW_LINE );
        result.append(" Minimum Value: " + minVal + NEW_LINE);
        result.append("}");
        return result.toString();
    }

    //BEGIN CONSTRUCTORS
    public Column(String newName){
        name = newName;
        type = "UNASSIGNED";
        maxVal = 0;
        minVal = 0;
    }
    public Column(){
        name = "UNASSIGNED";
        type = "UNASSIGNED";
        maxVal = 0;
        minVal = 0;
    }
    //END CONSTRUCTORS

    //BEGIN GETTERS
    public String   getName(){
        return name;
    }
    public String   getType(){
        return type;
    }
    public Integer  getLen()	{ return (Integer) len;  }
    public Long     getMin()    { return minVal;
    }
    public Long     getMax(){
        return maxVal;
    }
    //END GETTERS

    //BEGIN SETTERS
    public void setName(String newName){
        name = newName;
    }
    public void setType(String newType){
        type = newType;
    }
    public void setLen(int len1){
        len = len1;
    }
    public void setMin(Long val){
        minVal = val;
    }
    public void setMax(Long val){
        maxVal = val;
    }
    public void doMinMaxNum(Long val){
        if (minVal > val) minVal = val;
        if (maxVal < val) maxVal = val;
    }
    public void lenMax(int val){
        if (len < val) len = val;
    }
    //END SETTERS

}
