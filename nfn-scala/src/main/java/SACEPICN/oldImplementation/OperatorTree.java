package SACEPICN.oldImplementation;/*
package SACEPICN.oldImplementation;

// 
//  Created by Ali on 06.02.18.
// 
import scala.Int;

import java.util.*;

import SACEPICN.Node;

import java.lang.String;
import java.lang.StringBuilder;

public class OperatorTree {

    public Map createOperatorTree(String _query) {

        //We are handling 3 types of queries:
        //1) JOIN(FILTER(WINDOW), FILTER(WINDOW)). Here the tree will be Join with two filter/window ops
        //2) JOIN(WINDOW, WINDOW). Tree: Join with 2 window ops
        //3) FILTER(WINDOW). Tree: Filter and Window deployed on one node

        //For this, we will need a parser that will be able to detect the format of the query and decide the tree.

        Stack<Node> nodeStack = new Stack();
        Node root = null;
        Node master = null; //Since root can change in multilevel trees, hence we will refer to master as the original root node.
        //Read Query ->
        //Check if query starts with JOIN:
        String query = "";
        if (_query.startsWith("JOIN")) {
            //Now we know that we have to expect either JOIN(FILTER(WINDOW), FILTER(WINDOW)) or JOIN(WINDOW, WINDOW)
            String[] JoinParams = getJoinParameters(_query);

            //Join query:
            String joinQuery = "(call 5 /node/nodeQuery/nfn_service_Join '" + JoinParams[0] + "' '[Q1]' '[Q2]' '" + JoinParams[3] + "')";

            //Push join on root node:
            root = new Node(joinQuery, Operator.JOIN, 1, JoinParams);
            if (master == null)
                master = root;
            nodeStack.push(root);

            //Now process the inner queries
            for (int i = 1; i <= 2; i++) {
                //We can either have filter or window (add more later)
                if (JoinParams[i].startsWith("FILTER")) {
                    Node node = handleFilterQuery(JoinParams[i], i);
                    root = addToGraph(nodeStack, root, node);

                } else if (JoinParams[i].startsWith("WINDOW")) {
                    Node node = handleWindowQuery(JoinParams[i], i);
                    root = addToGraph(nodeStack, root, node);
                }
                //If there is another join:
                //TO DO:
            }
        } else if (_query.startsWith("FILTER")) {
            //Query starts with Filter:
            //Possible queries: FILTER(content,WINDOW(victims,22:18:36.800,22:18:44.000),3=F&4>10,name) OR FILTER(content,WINDOW(victims,22:18:36.800,22:18:44.000),3=F&4>10,data)
            //This will be executed on one node - so the operator tree will be one node.
            Node node = handleFilterQuery(_query, 1);
            root = addToGraph(nodeStack, root, node);
            if (master == null)
                master = root;
        } else if (_query.startsWith("WINDOW")) {
            Node node = handleWindowQuery(_query, 1);
            root = addToGraph(nodeStack, root, node);
            if (master == null)
                master = root;
        }

        //At this point, we can return either the nodeStack or the master. NodeStack contains all nodes in a stack. Master contains all nodes in a tree.
        return new Map(nodeStack.size(), master);
        //return master;
        //return nodeStack;
    }
    private Node addToGraph(Stack<Node> nodeStack, Node root, Node node) {
        //Add to root:
        boolean added = false;
        while(!added) {
            if (root != null && root.left == null) {
                root.left = node;
                nodeStack.push(node);

                node.parent = root;
                added=true;
            } else if (root != null && root.right == null) {
                root.right = node;
                nodeStack.push(node);

                node.parent = root;
                added=true;
            } else if (root != null) {
                //Both left and right are full, make right the root. And repeat!
                root = root.right;
            } else if (root == null) {
                //There is no root node yet, so make a new root (this is for single operator graphs)
                root = node;
                nodeStack.push(node);
                added = true;
            }
        }

        //Traverse back to the master
        while(root.parent!=null) {
            root = root.parent;
        }
        return root;
    }

    //  Join will have 4 parameter
    //  inputType = name/data
    //  Q1 = Query or data 1
    //  Q2 = Query or data 2
    //  Options = NULL or conditions
    // 
    //  Samples:
    //  JOIN([name],[FILTER(content,WINDOW(victims,22:18:36.800,22:18:44.000),3=F&4>10,name)],[FILTER(content,WINDOW(victims,22:18:36.800,22:18:44.000),3=F&4>10,name)],[NULL])
    //  JOIN([name],[WINDOW(victims,22:18:36.800,22:18:44.000)],[WINDOW(victims,22:18:36.800,22:18:44.000)],[NULL])
    // 
    private String[] getJoinParameters(String _query) {
        String[] params = new String[4];
        char[] qryArray = _query.toCharArray();

        //Extract Parameters:
        int paramCount = -1;
        boolean note = false;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= qryArray.length - 1; i++) {
            if (qryArray[i] == ']') {
                note = false; //Stop recording
                params[paramCount] = sb.toString(); //set param in array
                sb = new StringBuilder();
            }
            if (note == true) {
                sb.append(qryArray[i]);
                continue;
            }

            if (qryArray[i] == '[') {
                note = true;
                paramCount++;
            }
        }
        return params;
    }

    // 
    // FILTER(content,WINDOW(victims,22:18:36.800,22:18:44.000),3=F&4>10,name)
    // FILTER(content,WINDOW(victims,22:18:36.800,22:18:44.000),3=F&4>10,data)
    // 
    private String[] getFilterParameters(String _query) {
        String[] params = new String[4];

        String query = _query.substring(7,_query.length() - 1);

        //Split these by ',':
        String[] param = query.split(",");
        if(param.length >= 6 ) //Filter contained a window. Merge index 1, 2, 3, 4 into one.
        {
            params[0] = param[0];
            StringBuilder sb = new StringBuilder();
            sb.append(param[1]);
            sb.append(',');
            sb.append(param[2]);
            sb.append(',');
            sb.append(param[3]);
            params[1] = sb.toString();
            params[2] = param[4];
            params[3] = param[5];
        }
        //We will skip filter on sensor at the moment because it does not make sense to filter directly on sensor.
        //else if(param.length <= 2){
        //
        //}

        return params;
    }

    // Accpeted Window Query formats:
    // WINDOW(data,victims,22:18:36.800,22:18:44.000) //Lower bound and upper bound based
    // WINDOW(name,victims,10,m) //Time interval based
    // WINDOW(data,victims,200,0) //Event based
    // 
    private String[] getWindowParameters(String _query) {
        String[] params = new String[4]; //Updated 08.08.18: New parameters = 4 (prev 3)

        String query = _query.substring(7,_query.length() - 1);

        //Split these by ',':
        String[] param = query.split(",");

        return param;
    }

    private Node handleWindowQuery(String query, int i) {
        String[] WindowParams = getWindowParameters(query);
        //#NOTE TO SELF: Add comments on all sub queries
        String windowQuery = "(call 4 /node/nodeQuery/nfn_service_Window ";//'" + WindowParams[0] + "' '" + WindowParams[1] + "' '" + WindowParams[2] + "' '" + WindowParams[3] + "')";

        for(int index = 0; index < WindowParams.length; index++){
            windowQuery += "'" + WindowParams[index] + "' ";
        }

        windowQuery = windowQuery.trim();
        windowQuery += ")";

        Node node = new Node(windowQuery, Operator.WINDOW, i, WindowParams);

        return node;
    }

    private Node handleFilterQuery(String query, int i) {
        String[] FilterParams = getFilterParameters(query);

        //Create a node and place it in the tree:
        String filterQuery = "(call 5 /node/nodeQuery/nfn_service_Filter '" + FilterParams[0] + "' '[Q1]' '" + FilterParams[2] + "' '" + FilterParams[3] + "')";

        //Handle window inside Filter:
        String windowQuery = "";
        if (FilterParams[1].startsWith("WINDOW")) {
            String[] WindowParams = getWindowParameters(FilterParams[1]);
            windowQuery = "(call 4 /node/nodeQuery/nfn_service_Window {" + WindowParams[0] + "} {" + WindowParams[1] + "} {" + WindowParams[2] + "})";
        } else {
            //Filter is done by sensor. Not handled so far.
        }

        //Append Window Query inside Filter Query (execute filter and window on the same node):
        filterQuery = filterQuery.replace("Q1", windowQuery);

        Node node = new Node(filterQuery, Operator.FILTER, i, FilterParams);

        return node;
    }
}

*/