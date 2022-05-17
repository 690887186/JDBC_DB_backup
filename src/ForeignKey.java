/**
 * @author      MingchongLi
 * @date        2022/5/10
 * @version     1.0
 * @see         Table
 */

public class ForeignKey {
    private String tableA;
    private String tableB;
    private String columnA;
    private String columnB;

    public ForeignKey(String ta, String ca, String tb, String cb){
        tableA = ta;
        tableB = tb;
        columnA = ca;
        columnB = cb;
    }

    public String getColumnA() {
        return columnA;
    }

    public String getColumnB() {
        return columnB;
    }

    public String getTableA() {
        return tableA;
    }

    public String getTableB() {
        return tableB;
    }

    public String[] getAll(){
        String[] s = {tableA,columnA,tableB,columnB};
        return s;
    }
}
