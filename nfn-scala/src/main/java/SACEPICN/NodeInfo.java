package SACEPICN;

/**
 * Created by Ali on 06.02.18.
 */

import myutil.FormattedOutput;

public class NodeInfo {
    public String NI_NodeName = null;
    public String NI_Sensor = null;
    public Latency[] NI_Latency = null;
    public Double NI_Battery = null;

    public NodeInfo(String data) {
        String[] intermData = data.split("\\|");
        if (intermData.length > 3) {
            //Node Data is okay! Process it
            NI_NodeName = intermData[0];
            NI_Sensor = intermData[1];
            NI_Latency = getLatencyfromData(intermData[2]);
            NI_Battery = FormattedOutput.parseDouble(intermData[3].toString());
        }
    }
    //To manage no-existant nodes (6/3/18)
    public NodeInfo(){
    }

    private Latency[] getLatencyfromData(String data) {
        Latency[] Latencies = null;
        if(data.contains(",")) {
            String[] intermData = data.split("\\,");
            if (intermData.length > 0) {
                //Latency has data! Process it
                Latencies = new Latency[intermData.length];
                for (int index = 0; index < intermData.length; index++) {
                    Latency lat = new Latency();
                    //Do another split on Node=Val
                    String[] individualLatency = intermData[index].split("\\=");
                    if (individualLatency.length > 0) {
                        //We are good to go! Node latencies are in proper format.
                        lat.Lat_Node = individualLatency[0];
                        lat.Lat_Latency = FormattedOutput.parseDouble(individualLatency[1].toString());
                    }
                    Latencies[index] = lat;
                }
            }
        }
        else {
            //Only one node..
            Latencies = new Latency[1];
            String[] individualLatency = data.split("\\=");
            if (individualLatency.length > 1) {
                Latency lat = new Latency();
                lat.Lat_Node = individualLatency[0];
                lat.Lat_Latency = FormattedOutput.parseDouble(individualLatency[1].toString());
                Latencies[0] = lat;
            } else {
                Latency lat = new Latency();
                lat.Lat_Node = individualLatency[0];
                lat.Lat_Latency = FormattedOutput.parseDouble(("-1").toString());
                Latencies[0] = lat;
            }
        }
        return Latencies;
    }
}