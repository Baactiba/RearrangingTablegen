import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Generate
{
    static public void main (String[] args) throws IOException, InterruptedException {
        if (args.length == 0)
//            gen_slow(new BoardHeavy(14,0,0,1,14,0,0,0,15,0,0,0,15,0,0,0), 100); 9castle
//            gen_slow(new BoardHeavy(0,0,1,0,9,3,1,8,15,10,1,14,15,15,2,14), 100); // Phoenix
            gen_slow(new BoardHeavy(10,9,1,1,8,7,2,3,10,10,0,2,10,10,10,10), 70); // Zangief Nonomino
        else {
            THREADS = Integer.parseInt(args[0]);
            MERGETHREADS = Integer.parseInt(args[1]);
//            BoardHeavy.maxTile = Integer.parseInt(args[2]);
            if (args.length > 4)
                sl = Integer.parseInt(args[4]);
            gen_slow(new BoardHeavy(14,0,0,1,14,0,0,0,15,0,0,0,15,0,0,0), Integer.parseInt(args[3]));
        }
    }

    static int THREADS = 16;
    static int MERGETHREADS = 8;
    static int sl = 0;

    static public void gen_slow(BoardHeavy start, int maxLayer) throws IOException, InterruptedException {
        new DataOutputStream(new FileOutputStream("0.txt")).writeLong(start.rep);
        for (int x = sl; x < maxLayer; System.out.println("Generating " + (x++ + 1) + "\n")) {
//            if (new File(x + ".txt").length() == 0) {
//                System.out.println("Breaking at " + x + " ig");
//                maxLayer = x;
//                break;
//            }
            AtomicInteger completed = new AtomicInteger(0);
            for (int tn = 0; tn < THREADS; tn++)
                new GenThread(x, tn, new File(x + ".txt").length(), completed).start();
            while (completed.get() < THREADS) {}
            System.out.println("Merging and deduplicating...\n");
            merge_dedupe(x + 1);
        }
        merge_dedupe(maxLayer);
        merge_dedupe(maxLayer + 1);
        new File(maxLayer + ".txt").delete();
        new File((maxLayer + 1) + ".txt").delete();
    }

    final static Object synchronizer = new Object();
    final static AtomicInteger ip = new AtomicInteger(0);
    final static ArrayList<ArrayList<String>> jobs = new ArrayList<>();
    final static ArrayList<AtomicInteger> layerIp = new ArrayList<>();
    static {
        for (int x = 0; x < 10; x++) {
            jobs.add(new ArrayList<>());
            layerIp.add(new AtomicInteger(0));
        }
    }

    static public void merge_dedupe(int layer) throws InterruptedException {

        ip.set(0);
        for (ArrayList<String> l : jobs)
            l.clear();
        for (int x = 0; x < 99999; x++) {
            File f = new File(layer + "_" + x + ".txt");
            if (f.exists())
                jobs.getFirst().add(f.getName());
            else
                break;
        }

        ArrayList<Merger> threads = new ArrayList<>();
        for (int t = 0; t < MERGETHREADS; t++) {
            Merger m = new Merger();
            m.start();
            threads.add(m);
        }
        for (Merger m : threads)
            m.join();
        System.out.println(Merger.lrf.length() / 1048576 + " MiB\n");
        Merger.lrf.renameTo(new File(layer + ".txt"));
    }

    static synchronized Object[] getJob() {
        synchronized(synchronizer) {
            for (int x = 0; x < jobs.size() - 1; x++) {
                int sz = jobs.get(x).size();
                f: if (sz == 1 && layerIp.get(x).get() == 0) {
                    for (int l = x + 1; l < jobs.size(); l++)
                        if (!jobs.get(l).isEmpty())
                            break f;
                    jobs.get(x + 1).add(jobs.get(x).removeFirst()); // If there is only one job then move it to next layer
                }
                else if (sz > 1) {
                    layerIp.get(x + 1).incrementAndGet();
                    return new Object[]{x, jobs.get(x).removeFirst(), jobs.get(x).removeFirst()};
                }
            }
            if (ip.get() == 0) {
                boolean fixed = false;
                for (int x = 0; x < jobs.size(); x++)
                    for (int y = 0; y < jobs.get(x).size(); y++) {
                        if (x > 1)
                            jobs.get(1).add(jobs.get(x).removeFirst());
                        if (!jobs.get(y).isEmpty()) {
                            System.out.println("ITS BROKEN BRO " + jobs.get(x));
                            pJobs();
                            fixed = true;
                        }
                    }
                if (fixed)
                    pJobs();
                if (!fixed)
                    return null;      // Ends threads. Quick fix unresolved merges
            }
            return new Object[2]; // If empty but there are threads in progress, signal thread to wait
        }
    }
    public static void pJobs() {
        System.out.println();
        boolean started = false;
        for (int x = jobs.size() - 1; x >= 0; x--) {
            if (!jobs.get(x).isEmpty())
                started = true;
            if (started) {
                System.out.println(x + " " + jobs.get(x));
            }
        }
        System.out.println();
    }
}

class Merger extends Thread {

    static AtomicInteger tempNames = new AtomicInteger(0);
    static File lrf;

    public void run() {
        Object[] job;
        while (true) {
            job = Generate.getJob();
            if (job == null)
                return;
            else if (job.length == 2) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException _) { }
            }
            else {
                Generate.ip.incrementAndGet();
                File next = job(new File((String) job[1]), new File((String) job[2]), (int) job[0] == 0);
                synchronized(Generate.synchronizer) {
                    Generate.ip.decrementAndGet();
                    Generate.layerIp.get((int) job[0] + 1).decrementAndGet();
                    Generate.jobs.get((int) job[0] + 1).add(next.getName());
                    lrf = next;
                }
            }
        }
    }
    public File job(File f1, File f2, boolean sort) {
        File ret = new File("temp" + tempNames.getAndIncrement() + ".txt");
//        System.out.println("Merging " + f1.getName() + " and " + f2.getName() + " to " + ret.getName());
        try {
            if (sort) {
                f1 = sort(f1);
                f2 = sort(f2);
            }
            mergeUnique(f1, f2, ret);
            f1.delete();
            f2.delete();
        } catch (IOException _) {}
        return ret;
    }
    public static File sort (File f1) throws IOException {
        long valCt = f1.length() / 8;
        long[] stuff = new long[(int) valCt];
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f1), 262144));
        for (int i = 0; i < stuff.length; i++) {
            stuff[i] = in.readLong();
        }
        Arrays.sort(stuff);
        File ret = new File("temp" + tempNames.getAndIncrement() + ".txt");
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(ret), 262144));
        for (long l : stuff)
            dos.writeLong(l);
        in.close();
        dos.close();
        f1.delete();
        return ret;
    }
    public static void mergeUnique(File f1, File f2, File out) throws IOException {
        try (
                DataInputStream dis1 = new DataInputStream(new BufferedInputStream(new FileInputStream(f1), 262144));
                DataInputStream dis2 = new DataInputStream(new BufferedInputStream(new FileInputStream(f2), 262144));
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out), 262144))
        ) {
            boolean has1 = false, has2 = false;
            long cur1 = 0, cur2 = 0;
            long lastWritten = Long.MIN_VALUE;
            boolean haveLast = false;

            // Try to load first values
            try { cur1 = dis1.readLong(); has1 = true; } catch (EOFException ignored) {}
            try { cur2 = dis2.readLong(); has2 = true; } catch (EOFException ignored) {}

            while (has1 || has2) {
                long next;

                if (has1 && has2) {
                    if (cur1 < cur2) {
                        next = cur1;
                        try { cur1 = dis1.readLong(); } catch (EOFException e) { has1 = false; }
                    } else if (cur2 < cur1) {
                        next = cur2;
                        try { cur2 = dis2.readLong(); } catch (EOFException e) { has2 = false; }
                    } else { // equal
                        next = cur1;
                        try { cur1 = dis1.readLong(); } catch (EOFException e) { has1 = false; }
                        try { cur2 = dis2.readLong(); } catch (EOFException e) { has2 = false; }
                    }
                } else if (has1) {
                    next = cur1;
                    try { cur1 = dis1.readLong(); } catch (EOFException e) { has1 = false; }
                } else { // only has2
                    next = cur2;
                    try { cur2 = dis2.readLong(); } catch (EOFException e) { has2 = false; }
                }

                if (!haveLast || next != lastWritten) {
                    dos.writeLong(next);
                    lastWritten = next;
                    haveLast = true;
                }
            }
            dos.flush();
        }
    }
}
class GenThread extends Thread {
    int layer;
    int threadNum;
    long length;
    AtomicInteger completed;
    public GenThread(int layer, int threadNum, long length, AtomicInteger completed) {
        this.layer = layer;
        this.threadNum = threadNum;
        this.length = length;
        this.completed = completed;
    }
    public void run() {
        try {
            long boardCt = length / 8;
            long startBd = (long) ((((double) threadNum) / Generate.THREADS) * boardCt);
            long endBd = (long) ((((double) (threadNum + 1)) / Generate.THREADS) * boardCt);
            FileInputStream fis = new FileInputStream(layer + ".txt");
            fis.skipNBytes(8 * startBd);
            DataInputStream dis = new DataInputStream(new BufferedInputStream(fis, 262144));
            if (layer == Generate.sl)
                threadNum -= Generate.THREADS;
            DataOutputStream two = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream((layer + 1) + "_" + (threadNum + Generate.THREADS) + ".txt"), 262144));
            if (layer == Generate.sl)
                threadNum += Generate.THREADS;
            DataOutputStream four = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream((layer + 2) + "_" + threadNum + ".txt"), 262144));
            for (; startBd < endBd; startBd++) {
                BoardHeavy b = new BoardHeavy(dis.readLong());
                for (int move = 0; move < 4; move++) {
                    BoardHeavy[][] plus = b.moveSpawn(move);
                    if (plus != null) {
                        for (BoardHeavy a : plus[0])
                            if (a.defineSuccess() == 0.0) {
                                two.writeLong(a.rep);
                            }
                        for (BoardHeavy a : plus[1])
                            if (a.defineSuccess() == 0.0)
                                four.writeLong(a.rep);
                    }
                }
            }
            two.close();
            four.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        completed.incrementAndGet();
    }
}