import java.util.*;
public class MainlineFinder
{

    public static void main (String[] args) {
        BoardHeavy test = new BoardHeavy(1,2,1,1,9,8,5,5,15,10,5,14,15,15,7,14);
        System.out.println(getMainline(test));
    }

    static public String getMainline(BoardHeavy space) {
        if (!TextTrainer.prec.containsKey(space))
            TextTrainer.prec.clear();
        ArrayList<ArrayList<Info>> boardChances = new ArrayList<>();
        ArrayList<Info> layer1 = new ArrayList<>();
        layer1.add(new Info(space));
        boardChances.add(layer1);
        double threshold = (double) TextTrainer.getMove(space)[3] / 2;
        for (int x = 0; x < 200; x++) {
            ArrayList<Info> nextLayer = new ArrayList<>();
            for (Info i : boardChances.getLast())
                for (String s : new String[] {"R", "U", "L", "D"}) {
                    Info i2 = i.add(s);
                    if (i2.getSr() > threshold)
                        nextLayer.add(i2);
                }
            if (nextLayer.isEmpty())
                break;
            boardChances.add(nextLayer);
        }
        Info target = boardChances.getLast().getFirst();
        return "Main line: " + target.name + String.format(" (%.2f%%)", 50 * target.getSr() / threshold);
    }
}
class Info
{
    String name;
    HashMap<BoardHeavy, Double> contents;
    public Info(BoardHeavy space) {
        name = "";
        contents = new HashMap<>();
        contents.put(space, 1.0);
    }
    public Info(String name) {
        this.name = name;
    }
    public Info add(String dir) {
        Info i2 = new Info(name + dir);
        HashMap<BoardHeavy, Double> childChances = new HashMap<>();
        for (BoardHeavy b : contents.keySet()) {
            if (TextTrainer.getMove(b)[2].equals(dir)) {
                double parChc = contents.get(b);
                HashMap<BoardHeavy, Double> children = b.moveSpawns(switch (dir) {
                    case "L" -> 2;
                    case "U" -> 1;
                    case "R" -> 0;
                    default -> 3;
                });
                if (children != null)
                    for (BoardHeavy c : children.keySet()) {
                        if (!childChances.containsKey(c))
                            childChances.put(c, 0.0);
                        childChances.put(c, childChances.get(c) + parChc * children.get(c));
                    }
            }
        }
        i2.contents = childChances;
        return i2;
    }
    public double getSr() {
        double ret = 0;
        for (BoardHeavy b : contents.keySet())
            ret += contents.get(b) * (double) TextTrainer.getMove(b)[3];
        return ret;
    }
}