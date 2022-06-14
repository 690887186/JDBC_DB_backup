/**
 * @author      MingchongLi
 * @date        2022/6/15
 * @version     1.0
 * @see         Column
 */

public class Data {
    private int i;
    private float f;
    private double d;
    private byte[] b;
    private String s;
    private char c;

    public Data(int x){
        i = x;
    }

    public Data(float x){
        f = x;
    }

    public Data(double x){
        d = x;
    }
    public Data(byte[] x){
        b = x;
    }

    public Data(String x){
        if (x != null) s = x.replace("\"","'");
    }

    public Data(char x){
        c = x;
    }

    public String getB() {
        String str = "0x";
        for (byte by : b) str += Integer.toHexString( by & 0xFF);
        return str;
    }

    public char getC() {
        return c;
    }

    public double getD() {
        return d;
    }

    public float getF() {
        return f;
    }

    public int getI() {
        return i;
    }

    public String getS() {
        return s;
    }
}
