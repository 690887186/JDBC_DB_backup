import java.io.File;
import java.util.ArrayList;


/**
 * @author      MingchongLi
 * @date        2022/5/11
 * @version     1.1
 */


public class Main
{
    /**
     * @param args [0]: DB name, ex: University
     *             [1]: backup a DB file, true / false
     *             [2]: backup a sql file, true / false
     *             defualt: University true true
     */
    public static void main(String args[])
    {
        String DBName = "LSH";
        boolean backupDB = true;
        boolean backupSql = true;

        if (args.length != 0)
            DBName = args[0];
        if (args.length > 1)
            backupDB = args[1].equals("true") ? true : false;
        if (args.length > 2)
            backupSql = args[2].equals("true") ? true : false;

        // check existence
        if (new File(DBName + ".db").exists())
            Proccessor.ReadFromDB(DBName);
        else
            System.out.println(DBName + ".db not exist.");

        // return sqls in String and print them
        ArrayList<String> sqls = Proccessor.Backup(DBName + "_backup",backupDB,backupSql);
        for (String sql: sqls)
            System.out.println(sql + ";");
    }
}