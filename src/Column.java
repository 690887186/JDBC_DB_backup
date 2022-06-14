/**
 * @author MingchongLi
 * @date 2022/6/15
 * @version 1.1
 * @see Table
 */

import java.sql.Blob;
import java.util.ArrayList;

public class Column {
    private String datatype;
    private String isNullable;
    private String columnName;
    private ArrayList<Data> datas = new ArrayList<>();

    public Column(String name, String nullable, String dt) {
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

    public void addData(String data) {
        datas.add(new Data(data));
    }

    public void addData(int data){
        datas.add(new Data(data));
    }

    public void addData(double data){
        datas.add(new Data(data));
    }

    public void addData(float data){
        datas.add(new Data(data));
    }
    public void addData(byte[] data){
        datas.add(new Data(data));
    }
    public void addData(char data){
        datas.add(new Data(data));
    }

    public ArrayList<Data> getDatas() {
        return datas;
    }
}
