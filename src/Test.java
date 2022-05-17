import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        ArrayList<String> strs = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            strs.add("num " + i);

        for (String str : strs){
            System.out.println(str);
            if (str.equals("num 5")) {
                strs.remove(1);
            }
        }

        for (String str : strs)
            System.out.println(str);
    }
}
