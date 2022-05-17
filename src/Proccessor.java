/**
 * @Author: MingchongLi
 * @Date: 2022/5/11
 * @Version: 1.1
 */

import java.io.*;
import java.sql.*;
import java.time.chrono.ThaiBuddhistChronology;
import java.util.ArrayList;

public class Proccessor {
    /**
     * Read from a .db file and store in classes
     *
     * @param db DB name
     */
    public static void ReadFromDB(String db) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db + ".db");
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
            while (resultSet.next()) new Table(resultSet.getString("TABLE_NAME"));

            for (Table table : Table.Tables) {
                ResultSet columns = databaseMetaData.getColumns(null, null, table.getName(), null);
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String datatype = columns.getString("TYPE_NAME");
                    String isNullable = columns.getString("IS_NULLABLE");
                    Column column = new Column(columnName, isNullable, datatype);
                    table.addColumn(column);
                    ResultSet datas = connection.createStatement().executeQuery("SELECT " + columnName + " FROM " + table.getName());

                    while (datas.next())
                        column.addData((datatype.equals("INTEGER") && datas.getString(1).equals("\\N")) ? "null" : datas.getString(1));

                }
                ResultSet PK = databaseMetaData.getPrimaryKeys(null, null, table.getName());

                while (PK.next()) table.addPrimaryKey(PK.getString("COLUMN_NAME"));

                ResultSet FK = databaseMetaData.getImportedKeys(null, null, table.getName());
                while (FK.next())
                    table.addForeignKey(FK.getString("PKTABLE_NAME"), FK.getString("PKCOLUMN_NAME"), FK.getString("FKTABLE_NAME"), FK.getString("FKCOLUMN_NAME"));

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
    public static ArrayList<String> Backup(String db, boolean backupDB, boolean backupSql) {
        ArrayList<String> sqls = new ArrayList<>();
        CreateTables(sqls);
        InsertDatas(sqls);

        if (backupSql)
            BackupSql(db, sqls);
        if (backupDB)
            BackupDB(db, sqls);
        return sqls;
    }

    /**
     * Generate CREATE instructions and add them in sqls.
     *
     * @param sqls the ArrayList<String> to be added.
     */
    public static void CreateTables(ArrayList<String> sqls) {
        for (Table table : Table.Tables) {
            String sql = "CREATE TABLE " + table.getName() + "(";
            for (Column column : table.getColumns())
                sql += column.getColumnName() + " " + column.getDatatype() + (column.getIsNullable().equals("N") ? " NOT NULL," : ",");


            if (table.getPrimaryKeys().size() != 0) {
                sql += "PRIMARY KEY (";
                for (String str : table.getPrimaryKeys()) sql += str + ",";
                sql = sql.substring(0, sql.length() - 1) + "),";
            }

            for (ForeignKey fk : table.getForeignKeys())
                sql += "FOREIGN KEY (" + fk.getColumnB() + ") REFERENCES " + fk.getTableA() + "(" + fk.getColumnA() + "),";

            sql = sql.substring(0, sql.length() - 1) + ")";
            sqls.add(sql);
        }
    }

    /**
     * Generate INSERT instructions and add them in sqls.
     *
     * @param sqls the ArrayList<String> to be added.
     */
    public static void InsertDatas(ArrayList<String> sqls) {
        for (Table table : Table.Tables) {
            for (int i = 0; i < table.getColumns().get(0).getDatas().size(); i++) {
                String sql = "INSERT INTO " + table.getName() + " VALUES(";
                for (Column column : table.getColumns()) {
                    if (column.getDatatype().startsWith("VARCHAR")) sql += "\"" + column.getDatas().get(i) + "\",";
                    else sql += column.getDatas().get(i) + ",";
                }
                sql = sql.substring(0, sql.length() - 1) + ")";
                sqls.add(sql);
            }
        }
    }

    public static void BackupDB(String db, ArrayList<String> sqls) {
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

    public static void BackupSql(String db, ArrayList<String> sqls) {
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
}
