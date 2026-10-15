import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
public class MDTester
{
    public static void main (String[] args) throws IOException, InterruptedException {
        restore(33, 37, 8);
        Generate.merge_dedupe(37);
    }
    public static void restore(int par, int chi, int max) throws IOException {
        for (int x = 0; x < max; x++)
            Files.copy(Path.of(par + "_" + x + ".txt"), Path.of(chi + "_" + x + ".txt"), StandardCopyOption.REPLACE_EXISTING);
    }
}