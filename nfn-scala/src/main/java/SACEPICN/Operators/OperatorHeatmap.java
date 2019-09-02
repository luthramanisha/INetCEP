package SACEPICN.Operators;

import SACEPICN.NFNQueryCreator;

import java.sql.Timestamp;

public class OperatorHeatmap extends OperatorA {
    public OperatorHeatmap(String query) {
        super(query);
        this.isOperatorCreatingNode = true;
    }

    public Boolean checkParameters() {
        return true;
    }

    public String genNFNQuery() {
        NFNQueryCreator nfn = new NFNQueryCreator("(call " + (this.parameters.length+2) + " /node/nodeQuery/nfn_service_Heatmap");
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
