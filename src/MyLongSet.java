public class MyLongSet {
    static final int[] SIZES = {7, 13, 29, 53, 97, 193, 389, 769, 1543, 3079, 6151, 12289, 24593, 49157, 98317};
    static final double LOAD_FACTOR = 0.3324;

    int size = 0;
    long[] stuff = new long[29];
    static int probes = 0;

    public static void main (String args[]) {
        MyLongSet set = new MyLongSet();
        for (int x = 0; x < 31; x++)
            set.add((int) Math.pow(1.3, x));
        System.out.println(set);
        System.out.println(set.size);
    }


    public boolean add(long x) {
        if ((double) size / stuff.length > LOAD_FACTOR) {
            size = 0;
            int curSize = stuff.length;
            for (int i : SIZES)
                if (i > curSize) {
                    curSize = i;
                    break;
                }
            long[] old = stuff;
            stuff = new long[curSize];
            for (long adding : old)
                if (adding != 0)
                    add(adding);
        }

        int h1 = (int) (x % stuff.length);
        int h2 = 1 + (int) (x % (stuff.length - 1));
        if (h1 < 0) h1 += stuff.length;
        if (h2 < 0) h2 += stuff.length;
        int index = h1;
        while (stuff[index] != 0) {
            probes++;
            if (stuff[index] == x)
                return false;
            index = (index + h2) % stuff.length;
        }
        probes++;
        stuff[index] = x;
        size++;
        return true;
    }
    public boolean contains(long x) {
        int h1 = (int) (x % stuff.length);
        int h2 = (int) (1 + (x % (stuff.length - 1)));
        if (h1 < 0) h1 += stuff.length;
        if (h2 < 0) h2 += stuff.length;
        int index = h1;
        while (stuff[index] != 0) {
            if (stuff[index] == x)
                return true;
            index = (index + h2) % stuff.length;
        }
        return false;
    }
    public String toString() {
        StringBuilder ret = new StringBuilder("{");
        for (long l : stuff)
            if (l != 0)
                ret.append(l).append(", ");
        return ret.append('}').toString();
    }
    public MyLongSet clone() {
        MyLongSet ret = new MyLongSet();
        ret.size = size;
        ret.stuff = new long[stuff.length];
        System.arraycopy(stuff, 0, ret.stuff, 0, stuff.length);
        return ret;
    }
}