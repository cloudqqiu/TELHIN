package ExtendLinking;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;

public class ConstraintClass {
    public HashSet<Pair<Integer, Integer>> ml_pair;
    public HashSet<Pair<Integer, Integer>> cl_pair;
    public double[] wei;

    public ConstraintClass(double[] w, HashSet<Pair<Integer, Integer>> ml, HashSet<Pair<Integer, Integer>> cl) {
        wei = w;
        ml_pair = ml;
        cl_pair = cl;
    }
}
