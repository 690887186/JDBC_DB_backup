import java.io.File;
import java.util.ArrayList;

/**
 * @Author: MingchongLi
 * @Date: 2022/5/10
 * @Version: 1.0
 */

public class Main
{
    public static void main( String args[] )
    {
        // check existence
        if (new File("University.db").exists())
            Proccessor.ReadFromDB("University");
        else
            System.out.println("University.db not exist.");

        // delete backup if exist
        if (new File("University_backup.db").exists())
            new File("University_backup.db").delete();

        // return sqls in String and print them
        ArrayList<String> sqls = Proccessor.WriteToDB("University_backup",true);
        for (String sql: sqls)
            System.out.println(sql + ";");
    }
}