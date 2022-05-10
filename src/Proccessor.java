/**
 * @Author: MingchongLi
 * @Date: 2022/5/10
 * @Version: 1.0
 */

import java.sql.*;
import java.time.chrono.ThaiBuddhistChronology;
import java.util.ArrayList;

public class Proccessor {
    public static void ReadFromDB(String db) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db + ".db");
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
            while (resultSet.next()) {
                new Table(resultSet.getString("TABLE_NAME"));
            }
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
                        column.addData(datas.getString(1));
                }
                ResultSet PK = databaseMetaData.getPrimaryKeys(null, null, table.getName());
                while (PK.next())
                    table.addPrimaryKey(PK.getString("COLUMN_NAME"));

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

    public static ArrayList<String> WriteToDB(String db, Boolean backup) {
        ArrayList<String> sqls = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db + ".db");
            CreateTables(sqls, connection);
            InsertDatas(sqls, connection);
            if (backup)
                Backup(connection,sqls);
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return sqls;
    }

    public static void CreateTables(ArrayList<String> sqls, Connection connection){
        for (Table table : Table.Tables) {
            String sql = "CREATE TABLE " + table.getName() + "(";
            for (Column column : table.getColumns()) {
                sql += column.getColumnName() + " " + column.getDatatype() + ",";
            }

            if (table.getPrimaryKeys().size() != 0) {
                sql += "PRIMARY KEY (";
                for (String str : table.getPrimaryKeys())
                    sql += str + ",";
                sql = sql.substring(0, sql.length() - 1) + "),";
            }


            for (ForeignKey fk : table.getForeignKeys())
                sql += "FOREIGN KEY (" + fk.getColumnB() + ") REFERENCES " + fk.getTableA() + "(" + fk.getColumnA() + "),";

            sql = sql.substring(0, sql.length() - 1) + ")";
            sqls.add(sql);
        }
    }

    public static void InsertDatas(ArrayList<String> sqls, Connection connection){
        for (Table table : Table.Tables){
            for (int i = 0; i < table.getColumns().get(0).getDatas().size(); i++) {
                String sql = "INSERT INTO " + table.getName() + " VALUES(";
                for (Column column : table.getColumns()) {
                    sql += "\" " + column.getDatas().get(i) + "\",";
                }
                sql = sql.substring(0, sql.length() - 1) + ")";
                sqls.add(sql);
            }
        }
    }

    public static void Backup(Connection connection, ArrayList<String> sqls){
        try {
            Statement statement = connection.createStatement();
            for (String sql : sqls)
                statement.executeUpdate(sql);

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }


    }
}
