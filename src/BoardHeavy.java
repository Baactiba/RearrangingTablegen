import java.io.*;
import java.util.*;

public class BoardHeavy {

    int[] board;
    long rep;

    public static void main (String[] args) throws IOException {

        BoardHeavy first = new BoardHeavy(10,0,0,0,1,0,2,0,10,10,0,0,10,10,10,10);
        first = new BoardHeavy(10,9,1,1,1,9,2,3,10,10,0,2,10,10,10,10);


        for (int dir = 0; dir < 4; dir++) {
            BoardHeavy[][] children = first.moveSpawn(dir);
            double sr = 0;
            if (children != null) {
                int len = children[0].length;
                for (int y = 0; y < len; y++) {
                    sr += 0.9f / len * children[0][y].defineSuccess();
                    sr += 0.1f / len * children[1][y].defineSuccess();
                }
            }
            System.out.println(sr);
        }


    }

    public BoardHeavy(long l) {
        board = new int[16];
        for (int x = 0; x < 64; x += 4)
            board[x / 4] = (int) ((l >> x) & 0b1111);
        rep = l;
    }

    public BoardHeavy(int... board) {
        this.board = board;
        rep = toLong();
    }



    public BoardHeavy moveNoSpawn(int dir) { // RULD
        int[] b2 = mergeWay(board, dir);
        if (Arrays.equals(board, b2))
            return null;
        return new BoardHeavy(b2);
    }

    public HashMap<BoardHeavy, Double> moveSpawns(int dir) {
        BoardHeavy postmerge = moveNoSpawn(dir);
        if (postmerge == null)
            return null;
        int zc = getCount(postmerge.board, 0);
        HashMap<BoardHeavy, Double> ret = new HashMap<>();
        for (int x = 0; x < 16; x++) {
            if (postmerge.board[x] == 0) {
                int[] with2 = new int[16];
                System.arraycopy(postmerge.board, 0, with2, 0, 16);
                int[] with4 = new int[16];
                System.arraycopy(postmerge.board, 0, with4, 0, 16);
                with2[x] = 1;
                with4[x] = 2;
                ret.put(new BoardHeavy(with2), 0.9 / zc);
                ret.put(new BoardHeavy(with4), 0.1 / zc);
            }
        }
        return ret;
    }

    public BoardHeavy[][] moveSpawn(int dir) {
        BoardHeavy postmerge = moveNoSpawn(dir);
        if (postmerge == null)
            return null;
        BoardHeavy[][] ret = new BoardHeavy[2][getCount(postmerge.board, 0)];
        int idx = 0;
        for (int x = 0; x < 16; x++) {
            if (postmerge.board[x] == 0) {
                int[] with2 = new int[16];
                System.arraycopy(postmerge.board, 0, with2, 0, 16);
                with2[x] = 1;
                int[] with4 = new int[16];
                System.arraycopy(postmerge.board, 0, with4, 0, 16);
                with4[x] = 2;
                ret[0][idx] = new BoardHeavy(with2);
                ret[1][idx++] = new BoardHeavy(with4);
            }
        }
        return ret;
    }

    public static int[] mergeWay(int[] board, int direction) {
        int[] ret = getRotation(board, direction);
        try {
            ret = mergeDown(ret, direction % 2);
        } catch (Exception e) { System.out.println(direction); }
        return getRotation(ret, (4 - direction) % 4);
    }

    public static int[] mergeDown(int[] board, int direction) {
        int[] r1 = new int[] {board[0], board[1], board[2], board[3]};
        int[] r2 = new int[] {board[4], board[5], board[6], board[7]};
        int[] r3 = new int[] {board[8], board[9], board[10], board[11]};
        int[] r4 = new int[] {board[12], board[13], board[14], board[15]};
        int[][] myMerges = direction == 0 ? mergesH : mergesV;
        r1 = myMerges[r1[3] + 16 * r1[2] + 256 * r1[1] + 4096 * r1[0]];
        r2 = myMerges[r2[3] + 16 * r2[2] + 256 * r2[1] + 4096 * r2[0]];
        r3 = myMerges[r3[3] + 16 * r3[2] + 256 * r3[1] + 4096 * r3[0]];
        r4 = myMerges[r4[3] + 16 * r4[2] + 256 * r4[1] + 4096 * r4[0]];
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

    static int[][] mergesH;
    static int[][] mergesV;
    public static void init() {
        mergesH = new int[65536][];
        mergesV = new int[65536][];
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++)
                for (int z = 0; z < 16; z++)
                    for (int a = 0; a < 16; a++) {
                        mergesH[a + 16 * z + 256 * y + 4096 * x] = mergeH(new int[]{x, y, z, a});
                        mergesV[a + 16 * z + 256 * y + 4096 * x] = mergeV(new int[]{x, y, z, a});
                    }

    }

    static public int[] mergeH(int[] tiles)
    {
        for (int z = 0; z < tiles.length; z++)
            for (int x = tiles.length-1; x > 0; x--)
                if (tiles[x]==0 && tiles[x-1] != 10 && tiles[x-1] != 9)
                {
                    tiles[x]=tiles[x-1];
                    tiles[x-1]=0;
                }
        for (int x = tiles.length-1; x > 0; x--)
            if (tiles[x]!=0 && tiles[x] < 9)
                if (tiles[x]==tiles[x-1])
                {
                    tiles[x-1]=0;
                    tiles[x]++;
                }
        for (int z = 0; z < tiles.length; z++)
            for (int x = tiles.length-1; x > 0; x--)
                if (tiles[x]==0 && tiles[x-1] != 10 && tiles[x-1] != 9)
                {
                    tiles[x]=tiles[x-1];
                    tiles[x-1]=0;
                }
        return tiles;
    }

    static public int[] mergeV(int[] tiles)
    {
        for (int z = 0; z < tiles.length; z++)
            for (int x = tiles.length-1; x > 0; x--)
                if (tiles[x]==0 && tiles[x-1] != 10 && tiles[x-1] != 8)
                {
                    tiles[x]=tiles[x-1];
                    tiles[x-1]=0;
                }
        for (int x = tiles.length-1; x > 0; x--)
            if (tiles[x]!=0 && tiles[x] < 10 && tiles[x] != 8)
                if (tiles[x]==tiles[x-1])
                {
                    tiles[x-1]=0;
                    tiles[x]++;
                }
        for (int z = 0; z < tiles.length; z++)
            for (int x = tiles.length-1; x > 0; x--)
                if (tiles[x]==0 && tiles[x-1] != 10 && tiles[x-1] != 8)
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
                if ((board[coord] < 10 && board[coord] != 8) && board[coord] == board[coord + 4])
                    return false;
            }
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 4; y++) {
                int coord = x + 4 * y;
                if (board[coord] < 9 && board[coord] == board[coord + 1])
                    return false;
            }
        return true;
    }

    static String[] displayAlph = new String[] {"___", " 2 ", " 4 ", " 8 ", "16 ", "32 ", "64 ", "128", "256",
            "512", "   ", "2k ", "4k ", "8k ", "16k", "32k", "65k", "131k"};

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
        return (int) ((rep >> 31) ^ rep);
    }

    public boolean equals (Object o) {
        return rep == ((BoardHeavy) o).rep;
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
        if (getCount(board, 10) == 8)
            return 1.0;
        if (isDead(board)) // If the board is dead then it's dead
            return -1.0;
        return 0.0;
    }

    static HashMap<Integer, RandomAccessFile> files = new HashMap<>();

    public float getSr() throws IOException {
        long name = toLong();
        int startSum = -512 * 5;
        for (int i : board)
            if (i < 14 && i > 0)
                startSum += 1 << (i - 1);
        startSum -= (2) / 2;
        int right = (int) (new File(startSum + ".txt").length() / 12 - 1); // in parentheses: startSum / 2
        int left = 0;

        RandomAccessFile file;
        if (files.containsKey(startSum))
            file = files.get(startSum);
        else {
            file = new RandomAccessFile( startSum + ".txt", "r");
            files.put(startSum, file);
        }
        while (left <= right) {
            int mid = (left + right) / 2;
            file.seek(mid * 12L);
            long target = file.readLong();
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
}