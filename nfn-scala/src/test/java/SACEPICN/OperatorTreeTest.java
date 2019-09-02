package SACEPICN;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OperatorTreeTest {


    OperatorTree opTree;

    @Before
    public void setup() {
        this.opTree = new OperatorTree();
    }

    @After
    public void tearDown() {
        this.opTree = null;
    }

    @Test
    public void testCreateOperatorTree() throws Exception {
        Map result = opTree.createOperatorTree("JOIN([name],[name],[FILTER(name,WINDOW(victims,22:18:36.800,22:18:44.001),3=M&4>30,name)],[FILTER(name,WINDOW(survivors,22:18:35.800,22:18:41.001),3=F&4>20,name)])");
        assertNotNull(result);
        assertEquals(5, result._stackSize);

        result = opTree.createOperatorTree("JOIN([name],[name],[FILTER(name,WINDOW(victims,22:18:36.800,22:18:44.001),gender=M&age>30&id=9&food=z&basichygiene<eff,name)],[FILTER(name,WINDOW(survivors,22:18:35.800,22:18:41.001),gender=F&4>20,name)])");
        assertNotNull(result);
        assertEquals(5, result._stackSize);

        //Hinzugefügt von Johannes
        result = opTree.createOperatorTree("JOIN([name],[name],[FILTER(name,WINDOW(name,victims,5,M),3=M&4>30,name)],[FILTER(name,WINDOW(name,victims,5,M),3=M&4>30,name)])");
        assertNotNull(result);
        assertEquals(5, result._stackSize);

        result = opTree.createOperatorTree("PREDICT1({name},{name},{2m},{JOIN([name],[name],[WINDOW(name,plug0,5,M)],[WINDOW(name,plug1,5,M)])})");
        assertNotNull(result);
        assertEquals(4, result._stackSize);

        result = opTree.createOperatorTree("PREDICT1(name,name,2m,JOIN(name,name,WINDOW(name,plug0,5,M),WINDOW(name,plug1,5,M)))");
        assertNotNull(result);
        assertEquals(4, result._stackSize);

        result = opTree.createOperatorTree("JOIN(name,name,PREDICT2(name,name,2m,WINDOW(name,plug0,5,M)),PREDICT2(name,name,2m,WINDOW(name,plug1,5,M)))");
        assertNotNull(result);
        assertEquals(5, result._stackSize);

        result = opTree.createOperatorTree("FILTER(name,PREDICT1(name,name,2m,JOIN(name,name,WINDOW(name,plug0,5,M),WINDOW(name,plug1,5,M))),6>20,name)");
        assertNotNull(result);
        assertEquals(5, result._stackSize);

        result = opTree.createOperatorTree("FILTER(name,JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,16:22:00.000,16:23:00.000)),PREDICT2(name,name,30s,WINDOW(name,plug1,16:22:00.000,16:23:00.000))),5>58,name)");
        assertNotNull(result);
        assertEquals(6, result._stackSize);

        result = opTree.createOperatorTree("JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,16:22:00.000,16:23:00.000)),PREDICT2(name,name,30s,WINDOW(name,plug1,16:22:00.000,16:23:00.000)))");
        assertNotNull(result);
        assertEquals(5, result._stackSize);

        result = opTree.createOperatorTree("HEATMAP(name,name,0.0015,8.8215389251709,8.7262659072876,51.7832946777344,51.8207664489746,JOIN(name,name,WINDOW(name,gps1,5,M),WINDOW(name,gps2,5,M)))");
        assertNotNull(result);
        assertEquals(4, result._stackSize);
        
        //Ende
        result = opTree.createOperatorTree("JOIN([name],[name],[FILTER(name,FILTER(name,WINDOW(victims,22:18:36.800,22:18:44.001),3=M&4>30,name),3=M&4>30,name)],[FILTER(name,WINDOW(survivors,22:18:35.800,22:18:41.001),3=F&4>20,name)])");
        assertNotNull(result);
        assertEquals(6, result._stackSize);
        
        result = opTree.createOperatorTree("PREDICT1(name,2m,JOIN([name],[name],[WINDOW(plug1,18:22:00.000,18:27:00.000)],[WINDOW(plug2,18:22:00.000,18:27:00.000)],[NULL]))");
        assertNotNull(result);
        assertEquals(4, result._stackSize);

        result = opTree.createOperatorTree("PREDICT2(name,2m,WINDOW(plug1,18:22:00.000,18:27:00.000))");
        assertNotNull(result);
        assertEquals(2, result._stackSize);

        result = opTree.createOperatorTree("PREDICT1(name,2m,WINDOW(plug1,18:22:00.000,18:27:00.000))");
        assertNotNull(result);
        assertEquals(2, result._stackSize);

        result = opTree.createOperatorTree("SEQUENCE(name,name,plug1,plug2)");
        assertNotNull(result);
        assertEquals(1, result._stackSize);

        result = opTree.createOperatorTree("AGGREGATOR(name,name,MAX,WINDOW(plug1,18:22:00.000,18:27:00.000))");
        assertNotNull(result);
        assertEquals(2, result._stackSize);

        result = opTree.createOperatorTree("WINDOW(name,victims,4,S)");
        assertNotNull(result);
        assertEquals(1, result._stackSize);

        result = opTree.createOperatorTree("FILTER(name,WINDOW(name,victims,4,S),3=M&4>30,name)");
        assertNotNull(result);
        assertEquals(2, result._stackSize);

        result = opTree.createOperatorTree("JOIN(name,name,FILTER(name,WINDOW(name,victims,4,S),3=M&4>30,name),FILTER(name,WINDOW(name,victims,4,S),3=M&4>30,name))");
        assertNotNull(result);
        assertEquals(5, result._stackSize);

        result = opTree.createOperatorTree("JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,1,S)),PREDICT2(name,name,30s,WINDOW(name,plug1,1,S)))");
        assertNotNull(result);
        assertEquals(5, result._stackSize);

        result = opTree.createOperatorTree("FILTER(name,JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,1,S)),PREDICT2(name,name,30s,WINDOW(name,plug1,1,S))),6>50,name)");
        assertNotNull(result);
        assertEquals(6, result._stackSize);

        result = opTree.createOperatorTree("HEATMAP(name,name,0.0015,8.7262659072876,8.8215389251709,51.7832946777344,51.8207664489746,JOIN(name,name,WINDOW(name,gps1,2,S),WINDOW(name,gps2,2,S)))");
        assertNotNull(result);
        assertEquals(4, result._stackSize);

        // here a wrong query testing, for missing closing parantheis/brackets/braces
        // result = opTree.createOperatorTree("JOIN([name],[FILTER(name,FILTER(name,WINDOW(victims,22:18:36.800,22:18:44.001),3=M&4>30,name),3=M&4>30,name)],[FILTER(name,WINDOW(survivors,22:18:35.800,22:18:41.001),3=F&4>20,name)],[NULL])");
        // assertNotNull(result);
        // assertEquals(result._stackSize, 2);
    }




    // @Test
    // public void parseNode() throws Exception {
    // }

    // @Test
    // public void getOperator() throws Exception {
    // }

    

    // @Test
    // public void getNestedQueries() throws Exception {
    // }

    // @Test
    // public void isOperatorEvaluatingParams() throws Exception {
    // }
}