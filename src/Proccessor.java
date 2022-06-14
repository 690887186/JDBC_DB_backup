import java.io.*;
import java.sql.*;
import java.util.ArrayList;

/**
 * @author      MingchongLi
 * @date        2022/6/15
 * @version     1.5
 * @see         Main
 * @see         Table
 *
 */

public class Proccessor {
    public static ArrayList<String> sqls = new ArrayList<>();
    private static ArrayList<Table> tablesQueue = new ArrayList<>();

    /**
     * Read from a .db file and store in classes
     *
     * @param db DB name
     */
    public static void ReadFromDB(String db) {
        int i = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db + ".db");
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (resultSet.next()) new Table(resultSet.getString("TABLE_NAME"));


            for (Table table : Table.Tables) {
                ResultSet columns = databaseMetaData.getColumns(null, null, table.getName(), null);
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String datatype = columns.getString("TYPE_NAME");
                    String isNullable = columns.getString("IS_NULLABLE");

                    Column column = new Column(columnName, isNullable, datatype);
                    table.addColumn(column);
                    ResultSet datas = connection.createStatement().executeQuery("SELECT `" + columnName + "` FROM `" + table.getName() + "`");
                    while (datas.next()){
                        if (datatype.equals("INTEGER")){
                            if (datas.getString(1) == null || datas.getString(1).equals("\\N")) column.addData("null");
                            else column.addData(datas.getInt(1));
                        }else if (datatype.equals("BLOB")) column.addData(datas.getBytes(1));
                        else column.addData(datas.getString(1));

                    }
                }
                ResultSet PK = databaseMetaData.getPrimaryKeys(null, null, table.getName());

                while (PK.next()) table.addPrimaryKey(PK.getString("COLUMN_NAME"));

                ResultSet FK = databaseMetaData.getImportedKeys(null, null, table.getName());
                while (FK.next())
                    table.addForeignKey(FK.getString("PKTABLE_NAME"), FK.getString("PKCOLUMN_NAME"), FK.getString("FKTABLE_NAME"), FK.getString("FKCOLUMN_NAME"));

                ResultSet IN = databaseMetaData.getIndexInfo(null, null, table.getName(), false, false);
                while (IN.next()) table.addIndex(IN.getString("COLUMN_NAME"), IN.getString("NON_UNIQUE"));
            }

            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return;
    }

    /**
     * Backup to files
     *
     * @param db        DB name
     * @param backupDB  back up to a .db file?
     * @param backupSql back up to a .sql file?
     * @return sqls in Strings
     */
    public static ArrayList<String> Backup(String db, boolean backupDB, boolean backupSql, boolean backupBat) {
        for (Table table : Table.Tables) tablesQueue.add(table);

        while(tablesQueue.size() > 0) CreateTableRecursively(tablesQueue.get(0));

        InsertDatas();
        System.out.println("end insert");

        if (backupSql) BackupSql(db);
        System.out.println("end sql");

        if (backupDB) BackupDB(db);
        System.out.println("end db");

        if (backupBat) BackupBat(db);
        System.out.println("end bat");

        return sqls;
    }

    /**
     * Generate CREATE instructions and add them in sqls.
     *
     */
    public static void CreateTableRecursively(Table table) {
        // check foreign keys
        for (ForeignKey fk : table.getForeignKeys()){
            // search by String name
            for (Table fkTable : tablesQueue){
                if (fkTable.getName().equals(fk.getTableA()) && !table.getName().equals(fk.getTableA())){
                    // recursively create the table which has foreign keys of it.
                    CreateTableRecursively(fkTable);
                    break;
                }
            }
        }

        String sql = "CREATE TABLE `" + table.getName() + "`(";
        for (Column column : table.getColumns())
            sql += "`" + column.getColumnName() + "` "
                    + column.getDatatype()
                    + (column.getIsNullable().equals("N") ? " NOT NULL," : ",");


        if (table.getPrimaryKeys().size() != 0) {
            sql += "PRIMARY KEY (";
            for (String str : table.getPrimaryKeys()) sql += str + ",";
            sql = sql.substring(0, sql.length() - 1) + "),";
        }

        for (ForeignKey fk : table.getForeignKeys())
            sql += "FOREIGN KEY (" +
                    "`" + fk.getColumnB() + "`" +
                    ") REFERENCES " +
                    "`" + fk.getTableA() + "`(" +
                    "`" + fk.getColumnA() + "`),";

        sql = sql.substring(0, sql.length() - 1) + ")";

        sqls.add("DROP TABLE IF EXISTS `" + table.getName()+ "`");

        sqls.add(sql);

        if (table.getIndexes() != null)
            sqls.add("CREATE " + table.getIndexes().getNonUnique() + "INDEX "
                    + table.getIndexes().getIndexName() + " ON `"
                    + table.getName()
                    + "` (" + table.getIndexes().getColumnName() + " ASC)");

        tablesQueue.remove(tablesQueue.indexOf(table));
    }

    /**
     * Generate INSERT instructions and add them in sqls.
     */

    public static void InsertDatas() {
        for (Table table : Table.Tables) {
            for (int i = 0; i < table.getColumns().get(0).getDatas().size(); i++) {
                String sql = "INSERT INTO `" + table.getName() + "` VALUES(";
                for (Column column : table.getColumns()) {
                    if (column.getDatatype().startsWith("VARCHAR")) sql += "\"" + column.getDatas().get(i).getS() + "\",";
                    else if (column.getDatatype().startsWith("BLOB")) sql += "\"" + column.getDatas().get(i).getB() +"\",";
                    else if (column.getDatatype().startsWith("INTEGER")) sql += column.getDatas().get(i).getI() + ",";
                    else sql += "\"" + column.getDatas().get(i).getS() + "\",";
                }
                sql = sql.substring(0, sql.length() - 1) + ")";
                sqls.add(sql);
            }
        }
    }

    public static void BackupDB(String db) {
        // delete backup if exist
        new File(db + ".db").delete();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + db + ".db");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Statement statement = connection.createStatement();
            for (String sql : sqls) statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void BackupSql(String db) {
        new File(db + ".sql").delete();
        File file = new File(db + ".sql");

        try {
            OutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            for (String sql : sqls) outputStreamWriter.append(sql + ";\n");
            outputStreamWriter.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param db
     */
    public static void BackupBat(String db) {
        new File("do.bat").delete();
        File do_file = new File("do.bat");
        new File(db + ".bat").delete();
        File db_file = new File(db + ".bat");

        try {
            OutputStream outputStream = new FileOutputStream(do_file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.append("For /L %%i in (1,1,10) do (sqlite3 "+ db + ".db<"+ db + ".bat)");
            outputStreamWriter.close();
            outputStream.close();

            outputStream = new FileOutputStream(db_file);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            for (String sql : sqls) outputStreamWriter.append(sql + ";\n");
            outputStreamWriter.close();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
