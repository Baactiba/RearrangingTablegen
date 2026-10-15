import java.io.*;
import java.util.*;

public class Board2432 {

    int[] board;
    long rep;
    static int maxTile = 11;

    public static void main (String[] args) throws IOException {

//        Board2432 first = new Board2432(2,0,0,0,0,0,0,0,15,6,3,14,15,15,1,14);
//        Board2432 first = new Board2432(15,6,3,14,15,15,1,14,2,0,1,0,0,0,0,0);
//        System.out.println(first);
//        for (Board2432 b : first.moveSpawns(3).keySet())
//            System.out.println(b);
        RandomAccessFile raf = new RandomAccessFile("2.txt", "r");
        for (int i = 0; i < raf.length(); i += 12) {
            raf.seek(i);
            Board2432 b = new Board2432(raf.readLong());
            if (!b.inFormation())
                System.out.println(b);
        }

//        Board2432 first = new Board2432(1,0,0,0,3,2,1,0,15,6,3,14,15,15,1,14);
//        System.out.println(first.getSr());

    }

    public Board2432(long l) {
        board = new int[16];
        for (int x = 0; x < 64; x += 4)
            board[x / 4] = (int) ((l >> x) & 0b1111);
        rep = l;
    }

    public Board2432(int... board) {
        this.board = board;
        rep = toLong();
    }



    public Board2432 moveNoSpawn(int dir) { // RULD
        int[] b2 = mergeWay(board, dir);
        if (b2[8] + b2[12] + b2[13] + b2[11] + b2[15] != 75)
            return null;
        if (Arrays.equals(board, b2))
            return null;
        return new Board2432(b2);
    }

    public HashMap<Board2432, Double> moveSpawns(int dir) {
        Board2432 postmerge = moveNoSpawn(dir);
        if (postmerge == null)
            return null;
        int zc = getCount(postmerge.board, 0);
        HashMap<Board2432, Double> ret = new HashMap<>();
        for (int x = 0; x < 16; x++) {
            if (postmerge.board[x] == 0) {
                int[] with2 = new int[16];
                System.arraycopy(postmerge.board, 0, with2, 0, 16);
                int[] with4 = new int[16];
                System.arraycopy(postmerge.board, 0, with4, 0, 16);
                with2[x] = 1;
                with4[x] = 2;
                ret.put(new Board2432(with2), 0.9 / zc);
                ret.put(new Board2432(with4), 0.1 / zc);
            }
        }
        return ret;
    }

    public Board2432[][] moveSpawn(int dir) {
        Board2432 postmerge = moveNoSpawn(dir);
        if (postmerge == null)
            return null;
        Board2432[][] ret = new Board2432[2][getCount(postmerge.board, 0)];
        int idx = 0;
        for (int x = 0; x < 16; x++) {
            if (postmerge.board[x] == 0) {
                int[] with2 = new int[16];
                System.arraycopy(postmerge.board, 0, with2, 0, 16);
                with2[x] = 1;
                int[] with4 = new int[16];
                System.arraycopy(postmerge.board, 0, with4, 0, 16);
                with4[x] = 2;
                ret[0][idx] = new Board2432(with2);
                ret[1][idx++] = new Board2432(with4);
            }
        }
        return ret;
    }

    public static int[] mergeWay(int[] board, int direction) {
        int[] ret = getRotation(board, direction);
        try {
            ret = mergeDown(ret);
        } catch (Exception e) { System.out.println(direction); }
        return getRotation(ret, (4 - direction) % 4);
    }

    public static int[] mergeDown(int[] board) {
        int[] r1 = new int[] {board[0], board[1], board[2], board[3]};
        int[] r2 = new int[] {board[4], board[5], board[6], board[7]};
        int[] r3 = new int[] {board[8], board[9], board[10], board[11]};
        int[] r4 = new int[] {board[12], board[13], board[14], board[15]};
        r1 = merges[r1[3] + 16 * r1[2] + 256 * r1[1] + 4096 * r1[0]];
        r2 = merges[r2[3] + 16 * r2[2] + 256 * r2[1] + 4096 * r2[0]];
        r3 = merges[r3[3] + 16 * r3[2] + 256 * r3[1] + 4096 * r3[0]];
        r4 = merges[r4[3] + 16 * r4[2] + 256 * r4[1] + 4096 * r4[0]];
        return new int[] {r1[0], r1[1], r1[2], r1[3],
                r2[0], r2[1], r2[2], r2[3],
                r3[0], r3[1], r3[2], r3[3],
                r4[0], r4[1], r4[2], r4[3]};
    }

    public static int[] getRotation(int[] board, int rot) {
        return switch(rot) {
            case 0 -> new int[] {board[0], board[1], board[2], board[3], board[4], board[5], board[6], board[7],
                    board[8], board[9], board[10], board[11], board[12], board[13], board[14], board[15]};
            case 1 -> new int[] {board[12], board[8], board[4], board[0], board[13], board[9], board[5], board[1],
                    board[14], board[10], board[6], board[2], board[15], board[11], board[7], board[3]};
            case 2 -> new int[] {board[15], board[14], board[13], board[12], board[11], board[10], board[9], board[8],
                    board[7], board[6], board[5], board[4], board[3], board[2], board[1], board[0]};
            case 3 -> new int[] {board[3], board[7], board[11], board[15], board[2], board[6], board[10], board[14],
                    board[1], board[5], board[9], board[13], board[0], board[4], board[8], board[12]};
            case 4 -> new int[] {board[0], board[4], board[8], board[12], board[1], board[5], board[9], board[13],
                    board[2], board[6], board[10], board[14], board[3], board[7], board[11], board[15]};
            case 5 -> new int[] {board[15], board[11], board[7], board[3], board[14], board[10], board[6], board[2],
                    board[13], board[9], board[5], board[1], board[12], board[8], board[4], board[0]};
            case 6 -> new int[] {board[12], board[13], board[14], board[15], board[8], board[9], board[10], board[11],
                    board[4], board[5], board[6], board[7], board[0], board[1], board[2], board[3]};
            case 7 -> new int[] {board[3], board[2], board[1], board[0], board[7], board[6], board[5], board[4],
                    board[11], board[10], board[9], board[8], board[15], board[14], board[13], board[12]};
            default -> null;
        };
    }

    static {
        init();
    }

    static int[][] merges;
    public static void init() {
        merges = new int[65536][];
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++)
                for (int z = 0; z < 16; z++)
                    for (int a = 0; a < 16; a++)
                        merges[a + 16 * z + 256 * y + 4096 * x] = merge(new int[]{x, y, z, a});

    }

    static public int[] merge (int[] tiles)
    {
        for (int z = 0; z < tiles.length; z++)
            for (int x = tiles.length-1; x > 0; x--)
                if (tiles[x]==0)
                {
                    tiles[x]=tiles[x-1];
                    tiles[x-1]=0;
                }
        for (int x = tiles.length-1; x > 0; x--)
            if (tiles[x]!=0 && tiles[x] < 14)
                if (tiles[x]==tiles[x-1])
                {
                    tiles[x-1]=0;
                    tiles[x]++;
                }
        for (int z = 0; z < tiles.length; z++)
            for (int x = tiles.length-1; x > 0; x--)
                if (tiles[x]==0)
                {
                    tiles[x]=tiles[x-1];
                    tiles[x-1]=0;
                }
        return tiles;
    }

    static boolean isDead(int[] board) {
        for (int i : board)
            if (i == 0)
                return false;
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 3; y++) {
                int coord = x + 4 * y;
                if (board[coord] < 14 && board[coord] == board[coord + 4])
                    return false;
            }
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 4; y++) {
                int coord = x + 4 * y;
                if (board[coord] < 14 && board[coord] == board[coord + 1])
                    return false;
            }
        return true;
    }

    static String[] displayAlph = new String[] {"___", " 2 ", " 4 ", " 8 ", "16 ", "32 ", "64 ", "128", "256",
            "512", "1k ", "2k ", "4k ", "8k ", "16k", "32k", "65k", "131k"};

    static public String pArr(int[] board) {
        StringBuilder hi = new StringBuilder(49);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++)
                hi.append(displayAlph[board[4 * x + y]]).append(" ");
            hi.append("\n");
        }
        return hi + "\n";
    }

    public String toString() {
        return pArr(board);
    }

    static int getCount(int[] board, int val) {
        int ret = 0;
        for (int i : board)
            if (val == i)
                ret++;
        return ret;
    }

    public int hashCode() {
/*        int ret = 0;
        for (int x = 0; x < 8; x++)
            ret |= board[x] << (4 * x);
        return ret; */
        return (int) ((rep >> 31) ^ rep);
    }

    public boolean equals (Object o) {
/*        if (o instanceof Board2432 b) {
            for (int x = 0; x < 16; x++)
                if (b.board[x] != board[x])
                    return false;
            return true;
        }
        return false; */
        return rep == ((Board2432) o).rep;
    }

    public int sum() {
        int ret = 0;
        for (int i : board)
            if (i != 0 && i < 14)
                ret += 1 << i;
        return ret;
    }

    public long toLong() {
        long ret = 0;
        for (int x = 0; x < 16; x++)
            ret |= ((long) board[x] << (4 * x));
        return ret;
    }

    public double defineSuccess() { // 1.0 if success, -1.0 if dead or doomed, 0.0 if unsolved
        if (isDead(board)) // If the board is dead then it's dead
            return -1.0;
        for (int i : board)
            if (i >= maxTile && i < 14)
                return 1.0;
        return 0.0; // Unsolved
    }

    static HashMap<Integer, RandomAccessFile> files = new HashMap<>();

    public float getSr() throws IOException {
        long name = toLong();
        int sum = 0;
        for (int i : board)
            if (i < 14 && i > 0)
                sum += 1 << (i - 1);
        sum -= 38;
        System.out.println(sum);
        int right = (int) (new File((sum) + ".txt").length() / 12 - 1); // in parentheses: startSum / 2
        int left = 0;

        RandomAccessFile file;
        if (files.containsKey(sum))
            file = files.get(sum);
        else {
            file = new RandomAccessFile((sum) + ".txt", "r");
            files.put(sum, file);
        }
        while (left <= right) {
            int mid = (left + right) / 2;
            file.seek(mid * 12L);
            long target = file.readLong();
            System.out.println(new Board2432(target));
            if (target == name) {
                return file.readFloat();
            }
            else if (target > name)
                right = mid - 1;
            else
                left = mid + 1;
        }
        if (defineSuccess() == 1)
            return 1f;
        return 0f;
    }

    public boolean inFormation() {
        return board[8] + board[12] + board[13] + board[11] + board[15] == 75;
    }
}