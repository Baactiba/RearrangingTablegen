import java.io.IOException;
public class Everything
{
    public static void main (String[] args) throws IOException, InterruptedException {
        if (args.length != 0) {
            Solve.recover(Integer.parseInt(args[0]));
        }
        else
            go(12, 1600);
    }
    public static void go(int maxTile, int maxLayer) throws IOException, InterruptedException {
//        BoardHeavy.maxTile = maxTile;
        Generate.gen_slow(new BoardHeavy(0,0,0,0,0,1,0,0,10,0,0,0,10,10,10,10), maxLayer);
        Solve.solve(maxLayer - 1);
    }
}