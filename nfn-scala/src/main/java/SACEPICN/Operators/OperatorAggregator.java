package SACEPICN.Operators;

import SACEPICN.NFNQueryCreator;

import java.util.HashMap;

public class OperatorAggregator extends OperatorA {
    String query;
    public OperatorAggregator(String query) {
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

    public String genNFNQuery() {
        NFNQueryCreator nfn = new NFNQueryCreator("(call " + (this.parameters.length+2) + " /node/nodeQuery/nfn_service_Aggregator");
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