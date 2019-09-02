package SACEPICN.Operators;

import SACEPICN.NFNQueryCreator;

import java.sql.Timestamp;

public class OperatorJoin extends OperatorA {
    public OperatorJoin(String query) {
        super(query);
        this.isOperatorCreatingNode = true;
    }

    @Override
    public Boolean checkParameters() {
        // first parameter
        int index = 0;
        if (!isParamFormatNameOnIndex(index)) return false;
        // second parameter
        index = 1;
        if (!isParamFormatNameOnIndex(index)) return false;

        // third and fourth parameter
        for (int i = 2; i <= 3; i++)
        {
            Boolean min = false;
            // "at least one has to be true"
            min |= isParamFilterQueryOnIndex(i);
            min |= isParamWindowQueryOnIndex(i);
            min |= isParamJoinQueryOnIndex(i);
            min |= isParamPredict1QueryOnIndex(i);
            min |= isParamPredict2QueryOnIndex(i);
            min |= isParamHeatmapQueryOnIndex(i);
            if (!min) return false;
        }

        // each parameter is correct
        return true;
    }

    @Override
    public String genNFNQuery() {
        NFNQueryCreator nfn = new NFNQueryCreator("(call " + (this.parameters.length+2) + " /node/nodeQuery/nfn_service_Join");
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