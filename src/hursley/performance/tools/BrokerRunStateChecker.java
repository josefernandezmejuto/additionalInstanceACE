package hursley.performance.tools;

import com.ibm.broker.config.proxy.*;

public class BrokerRunStateChecker {

    public static void main(String[] args) {

	    	// The ip address of where the broker is running
  		  	// and the port number of the queue manager listener.
        displayBrokerRunState("localhost", 2414, "");
    }

    public static void displayBrokerRunState(String hostname,
                                             int port,
                                             String qmgr) {
        BrokerProxy b = null;
        try {
            BrokerConnectionParameters bcp = new MQBrokerConnectionParameters(hostname, port, qmgr);
            b = BrokerProxy.getInstance(bcp);
            String brokerName = b.getName();
            
            System.out.println("Broker '"+brokerName+
                "' is available!");
            b.disconnect();
        } catch (ConfigManagerProxyException ex) {
            System.out.println("Broker is NOT available"+
                " because "+ex);
        }
    }
}