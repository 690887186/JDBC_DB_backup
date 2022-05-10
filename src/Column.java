/**
 * @Author: MingchongLi
 * @Date: 2022/5/10
 * @Version: 1.0
 */

import java.util.ArrayList;

public class Column {
    private String datatype;
    private String isNullable;
    private String columnName;
    private ArrayList<String> datas = new ArrayList<>();

    public Column(String name, String nullable, String dt){
        columnName = name;
        datatype = dt;
        isNullable = nullable;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getDatatype() {
        return datatype;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void addData(String data){
        datas.add(data);
    }

    public ArrayList<String> getDatas() {
        return datas;
    }
}
