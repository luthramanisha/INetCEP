package SACEPICN.Operators;

import SACEPICN.NFNQueryCreator;

import java.util.HashMap;

public class OperatorSequence extends OperatorA {
    String query;
    public OperatorSequence(String query) {
        super(query);
        this.isOperatorCreatingNode = true;
        this.query = query;
    }

    /**
     * @Overriden
     */
    public Boolean checkParameters() {
        return true;
    }

    /**
     * @Overriden
     */
    public String genNFNQuery() {
        NFNQueryCreator nfn = new NFNQueryCreator("(call " + (this.parameters.length+2) + " /node/nodeQuery/nfn_service_Sequence");
        // add all parameter
        int counter = 1;
        for (int i = 0; i < this.parameters.length; i++)
        {
            if (isParamNestedQuery(i)) {
                nfn.parameters.add("[Q" + counter++ + "]");
            } else {
                nfn.parameters.add(this.parameters[i]);
            }
        }

        return nfn.getNFNQuery();
    }

}