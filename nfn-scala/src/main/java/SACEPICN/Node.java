package SACEPICN;

/**
 * Created by Ali on 06.02.18.
 */

public class Node {

    public String _value; //Result
    public String _query; //Query
    public Operator _type; //Operator Type
    public int _id; //ID
    public String[] _parameters; //Parameters
    public boolean _Cprocessed = false; //Operator Placement overlay - Processed or not?
    public boolean _Vprocessed = false; //Operator Deployment overlay - Processed or not?
    public String _executionNode; //Node name where this query was or will be processed
    public String _executionNodePort; //Node port where this query was or will be processed
    public Node left, right, parent;

    //Create
    public Node(
              String qry, Operator type, int id, String[] parameters ) {
        _query = qry;
        _type = type;
        _id = id;
        _parameters = parameters;
        left = right = null;
    }
}