import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;

public class TextTrainer extends KeyAdapter
{
    static BoardHeavy board;

    static public void main (String[] args) {
/*        board = new BoardHeavy(1,0,0,0,0,0,0,0,15,6,3,14,15,15,1,14);
        board = new BoardHeavy(1,0,1,2,8,7,3,4,15,9,5,14,15,15,6,14);
        board = new BoardHeavy(2,2,1,0,9,8,2,1,15,10,1,14,15,15,1,14); // final 256
        board = new BoardHeavy(2,2,2,0,5,4,5,0,15,10,5,14,15,15,2,14);
        board = new BoardHeavy(0,0,1,0,2,0,0,0,7,6,8,14,15,15,3,14);
        board = new BoardHeavy(1,1,2,1,4,3,2,1,7,6,8,14,15,15,3,14);
        board = new BoardHeavy(1,0,1,2,0,0,2,4,2,4,5,14,15,15,3,14); // 9castle
        board = new BoardHeavy(0,1,0,0,0,0,0,0,15,1,2,14,15,15,1,14); // Phoenix */
//        board = new BoardHeavy(10,0,0,0,1,0,2,0,10,10,0,0,10,10,10,10); // Zangief Nonomino
//        board = new BoardHeavy(0,0,0,0,0,2,0,10,10,10,1,10,10,10,0,10); // XLB Nonomino
        board = new BoardHeavy(0,0,0,0,10,0,1,0,0,0,0,0,10,10,10,10); // XLB Nonomino
        System.out.println(board);
        new TextTrainer();
        stuffs.setText(board.toString());
    }

    static JTextArea stuffs = new JTextArea();
    static {
        stuffs.setEditable(false);
        stuffs.setFocusable(true);
        stuffs.setPreferredSize(new Dimension(1470, 630));
        stuffs.setFont(new Font(Font.MONOSPACED, Font.BOLD, 52));
    }

    public TextTrainer() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        stuffs.addKeyListener(this);
        JScrollPane thing = new JScrollPane(stuffs, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        frame.add(thing);
        frame.pack();
        frame.setVisible(true);
    }

    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (key > 36 && key < 41) {
            int dir =  switch (key) {
                case 37 -> 2;
                case 38 -> 1;
                case 39 -> 0;
                default -> 3;
            };
            if (board.moveNoSpawn(dir) == null)
                return;
            var spawns = board.moveSpawns(dir);

            for (BoardHeavy b : spawns.keySet()) {
                try {
                    spawns.put(b, spawns.get(b) * b.getSr());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            ArrayList<BoardHeavy> boards = new ArrayList<>();
            ArrayList<Double> importances = new ArrayList<>();
            for (BoardHeavy b : spawns.keySet()) {
                boards.add(b);
                importances.add(spawns.get(b));
            }
            double sum = 0;
            for (Double d : importances)
                sum += d;
            for (int x = 0; x < importances.size(); x++)
                importances.set(x, importances.get(x) / sum);
            if (sum == 0) {
                importances.clear();
                spawns = board.moveSpawns(dir);
                for (BoardHeavy b : boards)
                    importances.add(spawns.get(b));
            }
            double spawn = Math.random();
            int chosen = 0;
            while (spawn > 0) {
                spawn -= importances.get(chosen++);
            }
            board = boards.get(chosen - 1);

            Object[] stuff = getMove(board);
            ArrayList<String> moves = (ArrayList<String>) stuff[0];
            ArrayList<Double> scores = (ArrayList<Double>) stuff[1];

            StringBuilder text = new StringBuilder(MainlineFinder.getMainline(board) + "\n");

            String[] bString = board.toString().split("\n");
            for (int x = 0; x < 4; x++) {
                bString[x] += "    " + String.format(moves.get(x) + ": %.7f\n", scores.get(x));
            }
            text.append("\n");
            for (String s : bString)
                text.append(s);
            stuffs.setText(text.toString());
        }
    }
    static HashMap<BoardHeavy, Object[]> prec = new HashMap<>();
    public static Object[] getMove(BoardHeavy board) {

        if (prec.containsKey(board))
            return prec.get(board);

        ArrayList<String> moves = new ArrayList<>();
        moves.add("R");
        moves.add("U");
        moves.add("L");
        moves.add("D");
        ArrayList<Double> scores = new ArrayList<>();
        for (int x = 0; x < 4; x++) {
            HashMap<BoardHeavy, Double> map = board.moveSpawns(x);
            double sr = 0;
            try {
                for (BoardHeavy b : map.keySet()) {
                    try {
                        sr += b.getSr() * map.get(b);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } catch (Exception _) {}
            scores.add(sr);
        }

        for (int x = 0; x < scores.size() - 1; x++) {
            if (x < 0)
                x = 0;
            if (scores.get(x) < scores.get(x + 1)) {
                double temp = scores.get(x);
                scores.set(x, scores.get(x + 1));
                scores.set(x + 1, temp);
                String temp2 = moves.get(x);
                moves.set(x, moves.get(x + 1));
                moves.set(x + 1, temp2);
                x -= 2;
            }
        }
        Object[] ret = new Object[] {moves, scores, moves.getFirst(), scores.getFirst()};
        prec.put(board, ret);
        return ret;
    }
}