package SACEPICN;

import SACEPICN.Operators.*;
import com.thoughtworks.paranamer.ParameterNamesNotFoundException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** for testing functions of abstract class Operator */
class OperatorT extends OperatorA {
    public OperatorT (String q) { super(q); }
    @Override
    public Boolean checkParameters() {
        return true;
    }

    @Override
    public String genNFNQuery() {
        return "(call me ... )";
    }

}

public class OperatorTest {

    Operator op;

    final int ASSERT_MESSAGE        = 2;
    final int EXPECTED_RESULT       = 1;
    final int OPERATOR              = 1;
    final int QUERY_STRING          = 0;

    // valid operators for these tests
    final String[] validOperators = { "JOIN", "FILTER", "WINDOW" };

    // test queries for "testGetOperator"
    final String[][] queriesGetOperator = {
        { "JOIN( WINDOW, WINDOW )", "JOIN", "get valid operator" },
        { "BAAM( WINDOW, WINDOW )", null, "get invalid operator" },
        { "( WINDOW, WINDOW )", null, "no operator" },
        { "-( WINDOW, WINDOW )", null, "no operator" }
    };

    // test queries for "testGetParameters"
    final String[][] queriesGetParameters = {
        {  "JOIN( WINDOW, WINDOW )", "WINDOW", "WINDOW" },
        { "BAAM(what, don't)", "what", "don't" },
        { "JOIN([name],[FILTER(name,WINDOW(victims,22:18:36.800,22:18:44.001),3=M&4>30,name)],[FILTER(name,WINDOW(survivors,22:18:35.800,22:18:41.001),3=F&4>20,name)],[NULL])", "[name]", "[FILTER(name,WINDOW(victims,22:18:36.800,22:18:44.001),3=M&4>30,name)]", "[FILTER(name,WINDOW(survivors,22:18:35.800,22:18:41.001),3=F&4>20,name)]", "[NULL]" }
    };

    @Before
    public void setUp() {
    }

    @Test
    public void testGetOperator() throws Exception {
        for (String[] queryTest : this.queriesGetOperator) {
            assertEquals(queryTest[ASSERT_MESSAGE], queryTest[EXPECTED_RESULT], OperatorA.getOperator(queryTest[QUERY_STRING]));
        }
    }

    @Test
    public void testGetParameters() throws Exception {
        OperatorA opT = new OperatorT("");
        for (String[] queryTest : this.queriesGetParameters) {
            String[] result = opT.getParameters(queryTest[QUERY_STRING]);
            // number of parameters correct?
            assertEquals(queryTest.length, result.length + 1);
            // check each parameter
            for (int i = 0; i < result.length; i++) {
                assertEquals("parameter at position "+i+" not as expected", queryTest[i+1], result[i]);
            }
        }
    }

    @Test
    public void testParseQuery() throws Exception {
        OperatorA operator = new OperatorJoin("JOIN([name],[name],[FILTER(name,WINDOW(victims,22:18:36.800,22:18:44.001),3=M&4>30,name)],[FILTER(name,WINDOW(survivors,22:18:35.800,22:18:41.001),3=F&4>20,name)])");
        Node root = operator.parseQuery(null);
        assertNotNull(root);
    }

    @Test(expected = ParameterNamesNotFoundException.class)
    public void testParseQueryException() {
        OperatorA operator = new OperatorJoin("JOIN(JOIN(WINDOW, WINDOW), JOIN(WINDOW, WINDOW))");
        Node root = operator.parseQuery(null);
        assertNotNull(root);
    }



}
