/*
    @author: zukic07
*/
package SACEPICN.Operators;

import SACEPICN.Node;
import SACEPICN.Operator;
import SACEPICN.OperatorTree;
import com.thoughtworks.paranamer.ParameterNamesNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;

/**
 * This abstract Operator class has to be extended by any new Operator.
 * It's task is to executes a generic parsing strategy and create
 * a new Node for the OperatorTree class. 
 * 
 * To create a new Operator the following tasks have to be done:
 *      - set variable <isOperatorCreatingNode> to true if in each case
 *        the resulting nfn query will not be merged with another Operator
 *        (the OperatorFilter is currently the only one, if its followed by OperatorWindow)
 *      - connect in <validOperators> the operator name with it's appropriate (enum) Operator type
 *      - in the static function <createOperator(...)>, add the case for your new Operator
 *      - override <genNFNQuery()> function (similar to existing Operators)
 *      - override <checkParameters> function (similar to existing Operators)
 */
public abstract class OperatorA {

    public Boolean isOperatorCreatingNode;
    private String query;
    public String[] parameters;

    /**
     * Register here Operators. Add their names to activate them
     * as valid, or remove to deactivate them
     */
    public static HashMap<String, Operator> validOperators = new HashMap<>();
    static {
        validOperators.put("FILTER", Operator.FILTER);
        validOperators.put("JOIN", Operator.JOIN);
        validOperators.put("WINDOW", Operator.WINDOW);
        validOperators.put("PREDICT1", Operator.PREDICT1);
        validOperators.put("PREDICT2", Operator.PREDICT2);
        validOperators.put("HEATMAP", Operator.HEATMAP);
        validOperators.put("AGGREGATOR", Operator.AGGREGATION);
        validOperators.put("SEQUENCE", Operator.SEQUENCE);
    }

    /**
     * create appropriate Operator
     * 
     * @param operator
     * @param query
     * @return
     */
    public static OperatorA createOperator(String operator, String query) {
        OperatorA op;
        switch (operator) {
            case "FILTER" : op = new OperatorFilter(query); break;
            case "JOIN" : op = new OperatorJoin(query); break;
            case "WINDOW" : op = new OperatorWindow(query); break;
            case "PREDICT1" : op = new OperatorPredict1(query); break;
            case "PREDICT2" : op = new OperatorPredict2(query); break;
            case "HEATMAP" : op = new OperatorHeatmap(query); break;
            case "AGGREGATOR" : op = new OperatorAggregator(query); break;
            case "SEQUENCE" : op = new OperatorSequence(query); break;
            default: op = null;
        }

        return op;
    }

    /**
     * CONSTRUCTOR
     */
    public OperatorA(String query) {
        this.query = query;

        try {
            this.parameters = getParameters(query);
        } catch (Exception e) {
            // query parameter has wrong closing order (braces, brackets or paratheses)
        }
    }

    /**
     * Set specific checks and return if passes or fails
     * 
     * @param queryParameter
     * @return
     */
    public abstract Boolean checkParameters();
    
    public abstract String genNFNQuery();

    //public abstract String genNFNQuery(String processing);

    public String genFlatNFNQuery() { return null;};

    /**
     * parse a Query and return appropriate node with nfn_query
     * @return Node 
     */
    public Node parseQuery(Node parent) throws ParameterNamesNotFoundException {
        // tempQuery = removeOuterOperator(query, operator);
        Node thisNode;

        if (!checkParameters()) {
            throw new ParameterNamesNotFoundException("Detected wrong parameters. For further imformation look into logs");
        };
        
        // not creating new node for nested query
        String op = OperatorA.getOperator(this.query);
        thisNode = new Node(genNFNQuery(), OperatorA.validOperators.get(op), OperatorTree.getID(), this.parameters);
        thisNode.parent = parent;

        String[] nestedQueries = getNestedQueries(this.parameters);
        if (nestedQueries.length == 0) {
            // this is a leaf node
            return thisNode;
        }
        
        // nested queries
        if (nestedQueries.length > 0)
        {
            String operatorLeft = getOperator(nestedQueries[0]);
            OperatorA nextOp = createOperator(operatorLeft, nestedQueries[0]);
            if (!isOperatorCreatingNode && operatorLeft.equals("WINDOW")) {
                thisNode._query = thisNode._query.replace("Q1", nextOp.genFlatNFNQuery());
            }
            else {
                thisNode.left = nextOp.parseQuery(thisNode);
            }

            if (nestedQueries.length == 2)
            {
                String operatorRight = getOperator(nestedQueries[1]);
                nextOp = createOperator(operatorRight, nestedQueries[1]);
                if (!isOperatorCreatingNode && operatorRight.equals("WINDOW")) {
                    thisNode._query = thisNode._query.replace("Q2", nextOp.genNFNQuery());
                }
                else {
                    thisNode.right = nextOp.parseQuery(thisNode);
                }
            }
        }
        

        return thisNode;
    }

    /**
     * split only all comma separated parameters, without
     * any validity checks. 
     * 
     * 
     * @param query
     * @param operator
     * @return
     */
    public String[] getParameters(String query) throws Exception {
        Stack<Character> charStack = new Stack<Character>();
        char[] tempQuery = removeOuterOperator(query).toCharArray();
        ArrayList<String> arr = new ArrayList<>();

        int start = 0;
        for (int i = 0; i < tempQuery.length; i++) {
            switch (tempQuery[i]) {
                case '(' : charStack.push(')'); break;
                case '[' : charStack.push(']'); break;
                case '{' : charStack.push('}'); break;
                case ')' :
                case ']' :
                case '}' :
                    if (charStack.peek().equals(tempQuery[i])) {
                        charStack.pop();
                    } else {
                        // TODO Error case
                        throw new Exception("wrong closing Char");
                    }
                    break;
                case ',' :
                    if (charStack.isEmpty()) {
                        // remove whitespaces at beginning and end
                        while (String.valueOf(tempQuery[start]).matches("\\s")) start++;
                        int end = i-1;
                        while (String.valueOf(tempQuery[end]).matches("\\s")) end--;
                        arr.add(String.valueOf(tempQuery, start, ((end+1)-start)));
                        start = i + 1;
                    }
                    break;
            }
        }
        while (String.valueOf(tempQuery[start]).matches("\\s")) start++;
        int end = tempQuery.length-1;
        while (String.valueOf(tempQuery[end]).matches("\\s")) end--;
        arr.add(String.valueOf(tempQuery, start, (end+1)-start));

        String[] result = new String[arr.size()];
        for (int i = 0; i < arr.size(); i++) result[i] = (arr.get(i));
        return result;
    }

    /**
     * remove outer operator:
     *  "op(params...)" => "params..."
     * 
     * @param query current full query
     * @param operator current outer operator
     * @return only parameters as string (raw)
     */
    private String removeOuterOperator(String query) {
        // remove []
        if (query.startsWith("[") && query.endsWith("]")
        || query.startsWith("{") && query.endsWith("}")) {
            query = query.substring(1, query.length()-1);
        }
        int index = query.indexOf('(');
        String step1;
        if (index > -1) {
            step1 = query.substring(index+1);
            return step1.substring(0, step1.length()-1);
        } else {
            // no outer operator
            return query;
        }
    }

    private String removeBracketsBraces(String query) {
        // remove []
        if (query.startsWith("[") && query.endsWith("]")
        || query.startsWith("{") && query.endsWith("}")) {
            return query.substring(1, query.length()-1);
        }
        return query;
    }



    // * //////////////////////////////////////////////////////////////////////////
    // * //////////////////////////////////////////////////////////////////////////
    // *                      Parameter Check FUNCTIONS
    // * //////////////////////////////////////////////////////////////////////////
    // * //////////////////////////////////////////////////////////////////////////

    private void isParamIndexCheck(Integer index) {
        if (index == null || this.parameters.length < (index + 1) ) {
            throw new ParameterNamesNotFoundException("no parameter found on index " + index);
        }
    }

    public Boolean isParamIntegerOnIndex(Integer index) {
        isParamIndexCheck(index);
        String value = parameters[index];
        try {
            Integer.decode(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };

    public Boolean isParamFilterQueryOnIndex(Integer index) {
        isParamIndexCheck(index);
        String operator = getOperator(this.parameters[index]);
        if (operator == null) return false;
        if (operator.equals("FILTER")) return true;
        return false;
    }

    public Boolean isParamWindowQueryOnIndex(Integer index) {
        isParamIndexCheck(index);
        String operator = getOperator(this.parameters[index]);
        if (operator == null) return false;
        if (operator.equals("WINDOW")) return true;
        return false;
    }

    public Boolean isParamJoinQueryOnIndex(Integer index) {
        isParamIndexCheck(index);
        String operator = getOperator(this.parameters[index]);
        if (operator == null) return false;
        if (operator.equals("JOIN")) return true;
        return false;
    }

    public Boolean isParamPredict2QueryOnIndex(Integer index) {
        isParamIndexCheck(index);
        String operator = getOperator(this.parameters[index]);
        if (operator == null) return false;
        if (operator.equals("PREDICT2")) return true;
        return false;
    }

    public Boolean isParamPredict1QueryOnIndex(Integer index) {
        isParamIndexCheck(index);
        String operator = getOperator(this.parameters[index]);
        if (operator == null) return false;
        if (operator.equals("PREDICT1")) return true;
        return false;
    }

    public Boolean isParamHeatmapQueryOnIndex(Integer index) {
        isParamIndexCheck(index);
        String operator = getOperator(this.parameters[index]);
        if (operator == null) return false;
        if (operator.equals("HEATMAP")) return true;
        return false;
    }

    public Boolean isParamNestedQuery(Integer index) {
        isParamIndexCheck(index);
        if (getOperator(this.parameters[index]) != null) {
            return true;
        }

        // no nested query
        return false;
    }

    /* parameter is "name" or "data" */
    public Boolean isParamFormatNameOnIndex(Integer index) {
        isParamIndexCheck(index);
        return (
            removeBracketsBraces(this.parameters[index].toLowerCase()).equals("name") 
                || removeBracketsBraces(this.parameters[index].toLowerCase()).equals("data")
                );
    };

    public Boolean isParamBoolExp(Integer index) {
        String comparison = "(\\w+)([<>=]|[<][=]|[>][=])(\\w+)";
        return this.parameters[index].matches( comparison + "([|&]" + comparison + ")*" );
    }

    // * //////////////////////////////////////////////////////////////////////////
    // * //////////////////////////////////////////////////////////////////////////
    // *                            STATIC FUNCTIONS
    // * //////////////////////////////////////////////////////////////////////////
    // * //////////////////////////////////////////////////////////////////////////
    final static String operatorPattern = "[a-zA-Z0-9]+[(]{1}.*[)]{1}";

    /**
     * return Operator as string of current query
     * @param query
     * @return
     */
    public static String getOperator(String q) {
        String operator = null;
        String query = q;

        // remove []
        if (query.startsWith("[") && query.endsWith("]")
        || query.startsWith("{") && query.endsWith("}")) {
            query = query.substring(1, query.length()-1);
        }

        // does begin of query match pattern?
        if (query.matches(operatorPattern)) {
            // get all charachters until first occurence of "("
            String step1 = query.substring(0, query.indexOf("("));
            // is operator valid?
            if (OperatorA.validOperators.containsKey(step1)) {
                // valid operator, set for return
                operator = step1;
            }
        }

        return operator;
        
    }



    /**
     * return all parameters that are nested queries
     * @return String[] nestedQueries
     */
    public static String[] getNestedQueries(String[] params) {
        ArrayList<String> arr = new ArrayList<>();

        for (int i = 0; i < params.length; i++)
        {
            String op = getOperator(params[i]);
            if (op != null && OperatorA.validOperators.containsKey(op))
            {
                arr.add(params[i]);
            }
        }
        String[] result = new String[arr.size()];
        for (int i = 0; i < arr.size(); i++) result[i] = arr.get(i);
        return result;
    }
}
