package My.tomcat.util;

public class MutableInteger {
    protected int value = 0;
    public MutableInteger() {}
    public MutableInteger(int val) {
        this.value = val;
    }

    public int get() { return value;}
    public void set(int val) {this.value = val;}
}
