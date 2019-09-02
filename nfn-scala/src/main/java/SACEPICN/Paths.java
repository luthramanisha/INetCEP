package SACEPICN;

/**
 * Created by Ali on 06.02.18.
 */

public class
Paths {
    public int hopCount = 0;
    public String hopStringRepresentation = null;
    public double cumulativePathCost = 0.0;
    public String[] pathNodes = null;
    public double[] cumulativePathEnergy = null;
    public double[] cumulativePathBDP = null;
    public scala.collection.mutable.HashMap<String, String> hopWeights_Energy = null;
    public scala.collection.mutable.HashMap<String, String> hopWeights_BDP = null;
}
