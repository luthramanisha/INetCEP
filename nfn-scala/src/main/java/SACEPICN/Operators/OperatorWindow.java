package SACEPICN.Operators;

import SACEPICN.NFNQueryCreator;

import java.sql.Timestamp;

public class OperatorWindow extends OperatorA {
    public OperatorWindow(String query) {
        super(query);
        this.isOperatorCreatingNode = true;
    }

    @Override
    public Boolean checkParameters() {
        // first param
        Integer index = 0;
        if (!isParamFormatNameOnIndex(index));

        return true;
    }

    @Override
    public String genNFNQuery() {
        NFNQueryCreator nfn = prepareNfn();
        return nfn.getNFNQuery();
    }

    @Override
    public String genFlatNFNQuery() {
        NFNQueryCreator nfn = prepareNfn();
        return nfn.getNFNQueryWindow();
    }

    private NFNQueryCreator prepareNfn() {
        NFNQueryCreator nfn = new NFNQueryCreator("(call " + (this.parameters.length+2) + " /node/nodeQuery/nfn_service_Window");
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

        return nfn;
    }

}