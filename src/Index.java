/**
 * @author      MingchongLi
 * @date        2022/5/17
 * @version     1.0
 * @see         Table
 */

public class Index {
    private String columnName;
    private String nonUnique;
    private String indexName;

    public Index(String columnName, String nonUnique, String indexName) {
        this.columnName = "`" + columnName + "`";
        this.nonUnique = nonUnique.equals("0") ? "" : "UNIQUE ";
        this.indexName = "`" + indexName + "`";
    }

    public void addColumn(String columnName) {
        this.columnName += ",`" + columnName + "`";
    }

    public String getColumnName() {
        return columnName;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getNonUnique() {
        return nonUnique;
    }
}
