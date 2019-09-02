package SACEPICN;

import java.util.ArrayList;
import java.sql.Timestamp;

public class NFNQueryCreator {

    private String call;
    public ArrayList<String> parameters;
    private Timestamp _timestamp = new Timestamp(System.currentTimeMillis());

    public NFNQueryCreator(String call) {
        this.call = call;
        this.parameters = new ArrayList<>();
    }

    public String appendNFNParameter(String f)
    {
        String call = f;
        // add each parameter as string
        call += " '" + _timestamp.getTime() + "'";
        for (String p : this.parameters)
        {
            call += " '" + p + "'";
        }

        // close 
        call += ")";

        return call;
    }

    public String getNFNQuery() {
        return appendNFNParameter(call);
    }

    // SPECIAL FOR WINDOW OPERATOR

    public String appendNFNWindowParameter(String f)
    {
        String call = f;
        // add each parameter as string
        call += " {" + _timestamp.getTime() + "}";
        for (String p : this.parameters)
        {
            call += " {" + p + "}";
        }

        // close 
        call += ")";

        return call;
    }

    public String getNFNQueryWindow() {
        return appendNFNWindowParameter(call);
    }

    public String getFlatNFNQueryWindow() {
        return appendNFNParameter(call);
    }


}