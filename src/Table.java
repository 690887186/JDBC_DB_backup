/**
 * @author      MingchongLi
 * @date        2022/5/10
 * @version     1.0
 * @see         Proccessor
 */


import java.util.ArrayList;

public class Table {
    public static ArrayList<Table> Tables = new ArrayList<>();

    private String name;
    private ArrayList<Column> columns = new ArrayList<>();
    private ArrayList<ForeignKey> foreignKeys= new ArrayList<>();
    private ArrayList<String> primaryKeys = new ArrayList<>();
    private Index indexes = null;

    public Table(String name){
        this.name = name;
        Tables.add(this);
    }

    public void addColumn(String name, String nullable, String dt){
        columns.add(new Column(name, nullable, dt));
    }

    public void addColumn(Column c){
        columns.add(c);
    }

    public void addPrimaryKey(String primaryKey) {
        primaryKeys.add(primaryKey);
    }

    public void addForeignKey(String ta, String tb, String ca, String cb) {
        foreignKeys.add(new ForeignKey(ta,tb,ca,cb));
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public ArrayList<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public ArrayList<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void addIndex(String columnName, String nonUnique){
        if (indexes == null)
            indexes = new Index(columnName, nonUnique, name + "_index");
        else indexes.addColumn(columnName);
    }

    public Index getIndexes() {
        return indexes;
    }

    public String getName() {
        return name;
    }
}
