import it.unimi.dsi.fastutil.longs.Long2BooleanRBTreeMap;
import java.io.DataOutputStream;
import java.io.*;
import java.util.ArrayList;

public class Solve
{

    public static void main (String[] args) throws IOException, InterruptedException {
        solve(560);
    }

    public static void recover(int mostRecentSolvedLayer) throws IOException, InterruptedException {
        SolvedLayer p2 = new SolvedLayer(new File((mostRecentSolvedLayer + 1) + ".txt"));
        SolvedLayer p1 = new SolvedLayer(new File(mostRecentSolvedLayer + ".txt"));
        SolveLayer cur = new SolveLayer(new File((mostRecentSolvedLayer - 1) + ".txt"));
        for (int x = mostRecentSolvedLayer - 1; x > 0; x--) {
            System.out.println("Solving " + x);
            solveLayer(cur, p1, p2);
            p2.transcend(new File((x + 2) + ".txt"));
            p2 = p1;
            p1 = cur.ascend();
            cur = new SolveLayer(new File((x - 1) + ".txt"));
            System.out.println(chsr);
            chsr = 0.0;
        }
    }

    public static void solve(int maxLayer) throws IOException, InterruptedException {
        SolvedLayer p2 = new SolvedLayer(new File("ThisIsNotA_____fileLmfao"));
        SolvedLayer p1 = new SolvedLayer(new File("ThisIsNotA_____fileLmfao"));
        SolveLayer cur = new SolveLayer(new File(maxLayer + ".txt"));
        for (int x = maxLayer; x > 0; x--) {
            System.out.println("Solving " + x);
            solveLayer(cur, p1, p2);
            p2.transcend(new File((x + 2) + ".txt"));
            p2 = p1;
            p1 = cur.ascend();
            cur = new SolveLayer(new File((x - 1) + ".txt"));
            System.out.println(chsr);
            chsr = 0.0;
        }
    }

    static double chsr = 0.0;

    private static void solveLayer(SolveLayer cur, SolvedLayer p1, SolvedLayer p2) throws InterruptedException {
        int len = cur.layer.length;
        ArrayList<SolvingThread> threads = new ArrayList<>();
        for (int x = 0; x < Generate.THREADS; x++) {
            SolvingThread m = new SolvingThread((int) ((double) x / Generate.THREADS * len),
                    (int) ((double) (x + 1) / Generate.THREADS * len),
                    cur, p1, p2);
            m.start();
            threads.add(m);
        }
        for (SolvingThread m : threads)
            m.join();
    }

}
class SolveLayer {
    SolveNode[] layer;
    public SolveLayer(File f) {
        try {
            long len = f.length();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f), 262144));
            layer = new SolveNode[(int) (len / 8)];
            for (int i = 0; i < layer.length; i++)
                layer[i] = new SolveNode(dis.readLong());
        } catch (IOException e) {
            layer = new SolveNode[0];
        }
    }
    public SolvedLayer ascend() {
        return new SolvedLayer(layer);
    }
}
class SolvedLayer {
    long[] layer;
    float[] sr;
    Long2BooleanRBTreeMap others = new Long2BooleanRBTreeMap();
    static final Object synchronizer = "Hi";
    public SolvedLayer(File f) {
        try {
            long len = f.length();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f), 262144));
            layer = new long[(int) (len / 12)];
            sr = new float[layer.length];
            for (int i = 0; i < layer.length; i++) {
                layer[i] = dis.readLong();
                sr[i] = dis.readFloat();
            }
        } catch (IOException e) {
            layer = new long[0];
            sr = new float[0];
        }
    }
    public SolvedLayer(SolveNode[] layer) {
        this.layer = new long[layer.length];
        sr = new float[layer.length];
        for (int i = 0; i < layer.length; i++) {
            this.layer[i] = layer[i].name;
            sr[i] = layer[i].getSr();
        }
    }
    float getSr(long name) {
        int left = 0;
        int right = layer.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (layer[mid] == name)
                return sr[mid];
            else if (layer[mid] > name)
                right = mid - 1;
            else
                left = mid + 1;
        }
/*        synchronized(synchronizer) {
            if (others.containsKey(name)) {
                if (others.get(name))
                    return 1f;
                return 0f;
            }
            if (new BoardHeavy(name).defineSuccess() == 1.0) {
                others.put(name, true);
                return 1f;
            }
            others.put(name, false);
            return 0f;
        } */
        if (new BoardHeavy(name).defineSuccess() == 1.0) {
            return 1f;
        }
        return 0f;
    }
    public void transcend(File out) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out), 262144));
        for (int i = 0; i < layer.length; i++) {
            if (sr[i] > 0.001) { // TODO deletion threshold
                dos.writeLong(layer[i]);
                dos.writeFloat(sr[i]);
            }
            if (sr[i] > Solve.chsr)
                Solve.chsr = sr[i];
        }
        dos.close();
    }
}
class SolveNode {
    long name;
    float[] sr;
    public SolveNode(long name) {
        this.name = name;
        sr = new float[4];
    }
    public float getSr() {
        float max = sr[0];
        if (sr[1] > max)
            max = sr[1];
        if (sr[2] > max)
            max = sr[2];
        if (sr[3] > max)
            max = sr[3];
        return max;
    }
}
class SolvingThread extends Thread {
    int i1;
    int i2;
    SolveLayer cur;
    SolvedLayer p1;
    SolvedLayer p2;
    public SolvingThread(int i1, int i2, SolveLayer cur, SolvedLayer p1, SolvedLayer p2) {
        this.i1 = i1;
        this.i2 = i2;
        this.cur = cur;
        this.p1 = p1;
        this.p2 = p2;
    }
    public void run() {
        for (int x = i1; x < i2; x++) {
            for (int dir = 0; dir < 4; dir++) {
                BoardHeavy[][] children = new BoardHeavy(cur.layer[x].name).moveSpawn(dir);
                if (children != null) {
                    int len = children[0].length;
                    for (int y = 0; y < len; y++) {
                        cur.layer[x].sr[dir] += 0.9f / len * p1.getSr(children[0][y].rep);
                        cur.layer[x].sr[dir] += 0.1f / len * p2.getSr(children[1][y].rep);
                    }
                }
            }
        }
    }
}