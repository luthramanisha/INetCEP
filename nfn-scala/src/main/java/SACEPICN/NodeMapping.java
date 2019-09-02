package SACEPICN;

/**
 * Created by Ali on 06.02.18.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

//We can also make this class final (essentially static)
public class NodeMapping {

    public HashMap<String, String> nodeMapPortName = null;
    public HashMap<String, String> nodeMapNamePort = null;

    public HashMap<String, String> nodeMapPortIP = null;
    public HashMap<String, String> nodeMapNameIP = null;

    public NodeMapping() {
        String systemEnv = System.getenv("HOME") + "/MA-Ali";//"/manisha/gitlab/ws18_aoc2_lab"; // "/MA-Ali";
        //String systemEnv = System.getenv("HOME") + "/manisha/gitlab/ws18_aoc2_lab"; // "/MA-Ali";

        if (nodeMapPortName == null && nodeMapNamePort == null) {
            String configFile = systemEnv + "/nodeData/nodeInformation";
            String line = null;

            nodeMapPortName = new HashMap<String, String>();
            nodeMapNamePort = new HashMap<String, String>();

            nodeMapPortIP = new HashMap<String, String>();
            nodeMapNameIP = new HashMap<String, String>();

            try {
                FileReader fileReader =
                        new FileReader(configFile);

                BufferedReader bufferedReader =
                        new BufferedReader(fileReader);

                while ((line = bufferedReader.readLine()) != null) {
                    String[] lineBreak = line.split("-");
                    if (lineBreak.length > 1) {
                        //Adding node mapping to object:
                        nodeMapNamePort.put(lineBreak[0], lineBreak[1]); //NodeX/900X
                        nodeMapPortName.put(lineBreak[1], lineBreak[0]); //900X/NodeX

                        nodeMapPortIP.put(lineBreak[1], lineBreak[2]);
                        nodeMapNameIP.put(lineBreak[0], lineBreak[2]);
                    }
                }
                bufferedReader.close();
            } catch (FileNotFoundException ex) {

            } catch (IOException ex) {

            }
        }
    }

    //Get Port by Node Name
    public String getPort(String name) {
        if (nodeMapNamePort != null) return nodeMapNamePort.get(name);
        else {
            NodeMapping obj = new NodeMapping();
            return obj.nodeMapNamePort.get(name);
        }
    }

    //Get Name by Node Port
    public String getName(String port) {
        if (nodeMapPortName != null) return nodeMapPortName.get(port);
        else {
            NodeMapping obj = new NodeMapping();
            return obj.nodeMapPortName.get(port);
        }
    }

    //Get IP by Node Name
    public String getIPbyName(String name) {
        if (nodeMapNameIP != null) return nodeMapNameIP.get(name);
        else {
            NodeMapping obj = new NodeMapping();
            return obj.nodeMapNameIP.get(name);
        }
    }
    //Get IP by Node Port
    public String getIPbyPort(String port) {
        if (nodeMapPortIP != null) return nodeMapPortIP.get(port);
        else {
            NodeMapping obj = new NodeMapping();
            return obj.nodeMapPortIP.get(port);
        }
    }
}