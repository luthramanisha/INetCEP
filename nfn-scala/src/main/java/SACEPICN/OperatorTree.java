package SACEPICN;

import SACEPICN.Operators.OperatorA;
import com.thoughtworks.paranamer.ParameterNamesNotFoundException;

public class OperatorTree {

    private static int id = 0;
    private Integer treeDepth = 0;

    /**
     * default constructor
     */

    public OperatorTree() {  /* add something for distributed procesing here!*/ }

    /**
     * counter, that avoids negative values
     */
    public static int getID() {
        id++;
        if (id < 0) id = 0; // only positive IDs
        return id;
    }

    /**
     * get appropriatate (enum) Operator type
     * 
     * @return Operator
     */
    public static Operator getOperatorType(String operator) {
        Operator op = OperatorA.validOperators.get(operator);
        if (op == null)
            return null; // TODO some exception
        else
            return op;
    }

    /**
     * create the whole Operator Tree after syntactical and (minimum) 
     * semantic checks and after parsing the query 
     * 
     * @param query
     * @return Map an operator tree data structure
     */
    public Map createOperatorTree(String query) {
        String operator = OperatorA.getOperator(query);
        if (operator == null) {
            // * ? some logging?
            return getErrorMap();
        }
        OperatorA op = OperatorA.createOperator(operator, query);

        Node root = null;

        try {
            root = op.parseQuery(null);
        } catch (ParameterNamesNotFoundException e) {
            // return this as default Map for faulty queries
            return getErrorMap();
        }
            
        // tree depth
        depthCalc(root);

        // TODO adding some semantic checks, e.g.
        // hasJoinNestedWindow(root);
        // isEachWindowLeaf(root);
        // etc.
        
        return new Map(treeDepth, root);
    }

    /**
     * depth first search for detecting the maximum operator tree depth
     * 
     */
    private void depthCalc(Node nodeIt) {
        // // keep max depth
        // this.treeDepth = Integer.max(this.treeDepth, depth);
        // // go recursively
        // if (nodeIt.left != null) depthCalc(nodeIt.left, depth + 1);
        // if (nodeIt.right != null) depthCalc(nodeIt.right, depth + 1);
        if (nodeIt.parent == null) treeDepth = 1;
        else {
            treeDepth++;
        }
        if (nodeIt.left != null) depthCalc(nodeIt.left);
        if (nodeIt.right != null) depthCalc(nodeIt.right);
    }

    /**
     * this Map is returned in Error cases
     * @return
     */
    private Map getErrorMap() {
        return new Map(0, new Node(null, Operator.NONE, 0, null));
    }

}
