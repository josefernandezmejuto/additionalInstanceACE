package hursley.performance.tools;

import com.ibm.broker.config.proxy.ApplicationProxy;
import com.ibm.broker.config.proxy.BrokerConnectionParameters;
import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyRequestFailureException;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;
import com.ibm.broker.config.proxy.IntegrationNodeConnectionParameters;
import com.ibm.broker.config.proxy.LogEntry;
import com.ibm.broker.config.proxy.MessageFlowProxy;
import com.ibm.broker.config.proxy.RestApiProxy;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

public class GetAdditionalInstances {
	public static void main(String[] args) {
		String host = null;
		int port = 0;
		int numberOfAdditionalInstances = 0;
		int messageFlowsChanges = 0;
		int messageFlowsNotChanged = 0;
		String server = null;
		String application = null;
		String messageflow = null;
		String restapi = null;
		String help = "This command sets additional instances for all or a subset of message flows for a specific Node.\n\nMandatory command options are:\ni=localhost p=port n=additionalInstance [s=server {[a=application] [m=messageflow] | |r=RestAPI]]\n\nWhere:\nlocalhost is the host name where the broker is running\nport is the port number for the listener on the queue manager for the broker\nadditionalInstances are the number of additional instances to set for each matching message flow\nserver is the server to update\napplication is the application name to update\nmessageflow is the message flow name to update\nrestAPI is the RestAPI name to update\nExample is : hursley.performance.tools.SetAdditionalInstances i=localhost p=2414 n=10\nExample is : hursley.performance.tools.SetAdditionalInstances i=localhost p=2414 n=10 s=MyServer\nExample is : hursley.performance.tools.SetAdditionalInstances i=localhost p=2414 n=10 s=MyServer m=MyMessageFlow\nExample is : hursley.performance.tools.SetAdditionalInstances i=localhost p=2414 n=10 s=MyServer a=MyApplication\nExample is : hursley.performance.tools.SetAdditionalInstances i=localhost p=2414 n=10 s=MyServer a=MyApplication m=MyMessageFlow\nExample is : hursley.performance.tools.SetAdditionalInstances i=localhost p=2414 n=10 s=MyServer a=MyRestAPI\n";
		BrokerProxy b = null;
		if (args == null || args.length == 0) {
			System.out.println("");
			System.out.println(help);
			throw new IllegalArgumentException("No parameters specified");
		}
		int parameterCount = 0;
		for (int i = 0; i < args.length; i++) {
			String temp = args[i];
			System.out.println(temp);
			String key = temp.substring(0, 2);
			String value = temp.substring(2);
			if (key.equals("i=")) {
				host = value;
				parameterCount++;
			} else if (key.equals("p=")) {
				port = Integer.parseInt(value);
				parameterCount++;
			} else if (key.equals("n=")) {
				numberOfAdditionalInstances = Integer.parseInt(value);
				parameterCount++;
			} else if (key.equals("s=")) {
				server = value;
				parameterCount++;
			} else if (key.equals("a=")) {
				application = value;
				parameterCount++;
			} else if (key.equals("m=")) {
				messageflow = value;
				parameterCount++;
			} else if (key.equals("r=")) {
				restapi = value;
				parameterCount++;
			} else {
				System.out.println("");
				System.out.println(help);
				throw new IllegalArgumentException("Invalid parameter : " + args[i]);
			}
		}
		if (parameterCount < 3 && parameterCount > 6) {
			System.out.println("");
			System.out.println(help);
			throw new IllegalArgumentException(
					"Invalid number of parameters - You supplied " + parameterCount + ", must be between 3 and 6");
		}
		System.out.println("Connecting to broker at localhost=" + host + ", port=" + port);
		try {
			IntegrationNodeConnectionParameters incp = new IntegrationNodeConnectionParameters(host, port);
			incp.setAdvancedConnectionParameters(10, 5000);
			b = BrokerProxy.getInstance((BrokerConnectionParameters) incp);
			b.setSynchronous(30000);
			if (b != null) {
				try {
					System.out.println("Connected");
					String brokerName = b.getName();
					System.out.println("IntegrationNode=" + brokerName);
					Enumeration<ExecutionGroupProxy> EGs = b.getExecutionGroups(null);
					if (EGs == null || !EGs.hasMoreElements()) {
						System.out.println("-No Servers found");
						return;
					}
					while (EGs.hasMoreElements()) {
						ExecutionGroupProxy EG = EGs.nextElement();
						if (server != null && !server.equals(EG.getName())) {
							System.out
									.println("-Skipping ServerName=" + EG.getName() + ", is running=" + EG.isRunning());
							continue;
						}
						System.out.println("-ServerName=" + EG.getName() + " , is running=" + EG.isRunning());
						Enumeration<MessageFlowProxy> MFs = EG.getMessageFlows(null);
						if (MFs == null || !MFs.hasMoreElements())
							System.out.println("--No MessageFlows found for this ExecutionGroup");
						Enumeration<ApplicationProxy> APPs = EG.getApplications(null);
						if (APPs == null || !APPs.hasMoreElements())
							System.out.println("--No Applications found for this ExecutionGroup");
						Enumeration<RestApiProxy> RestAPIs = null;
						RestAPIs = EG.getRestApis(null);
						if (RestAPIs == null || !RestAPIs.hasMoreElements())
							System.out.println("--No RestAPIs found for this ExecutionGroup");
						if ((APPs == null || !APPs.hasMoreElements()) && (MFs == null || !MFs.hasMoreElements())
								&& (RestAPIs == null || !RestAPIs.hasMoreElements())) {
							System.out.println("--No Applications, RestAPIs or MessageFlows for this ExecutionGroup");
							return;
						}
						while (MFs.hasMoreElements()) {
							MessageFlowProxy MF = MFs.nextElement();
							int ai = MF.getAdditionalInstances();
							if (messageflow != null && !messageflow.equals(MF.getName())) {
								System.out.println("--Skipping MessageFlowName=" + MF.getName()
										+ " , AdditionalInstances=" + ai + " , is running=" + EG.isRunning());
								continue;
							}
							System.out.println("--MessageFlowName=" + MF.getName() + " , AdditionalInstances=" + ai
									+ " , is running=" + EG.isRunning());
							messageFlowsNotChanged++;
						}
						while (APPs.hasMoreElements()) {
							ApplicationProxy APP = APPs.nextElement();
							Enumeration<MessageFlowProxy> AppMFs = APP.getMessageFlows(null);
							if (AppMFs == null || !AppMFs.hasMoreElements()) {
								System.out.println("--No MessageFlows found for this Application");
								return;
							}
							while (AppMFs.hasMoreElements()) {
								MessageFlowProxy AppMF = AppMFs.nextElement();
								int ai = AppMF.getAdditionalInstances();
								if (restapi != null || (application != null && (!application.equals(APP.getName())
										|| (messageflow != null && !messageflow.equals(AppMF.getName()))))) {
									System.out.println("--Skipping Application=" + APP.getName() + "--MessageFlowName="
											+ AppMF.getName() + " , AdditionalInstances=" + ai + " , is running="
											+ EG.isRunning());
									continue;
								}
								System.out.println(
										"--Application=" + APP.getName() + "--MessageFlowName=" + AppMF.getName()
												+ " , AdditionalInstances=" + ai + " , is running=" + EG.isRunning());
								messageFlowsNotChanged++;
							}
						}
						while (RestAPIs.hasMoreElements()) {
							ApplicationProxy RestAPI = (ApplicationProxy) RestAPIs.nextElement();
							Enumeration<MessageFlowProxy> RestMFs = RestAPI.getMessageFlows(null);
							if (RestMFs == null || !RestMFs.hasMoreElements()) {
								System.out.println("--No MessageFlows found for this RestAPI");
								return;
							}
							while (RestMFs.hasMoreElements()) {
								MessageFlowProxy RestMF = RestMFs.nextElement();
								int ai = RestMF.getAdditionalInstances();
								if (application != null || (restapi != null && !restapi.equals(RestAPI.getName()))) {
									System.out.println("--Skipping RestAPI=" + RestAPI.getName() + "--MessageFlowName="
											+ RestMF.getName() + " , AdditionalInstances=" + ai + " , is running="
											+ EG.isRunning());
									continue;
								}
								System.out.println(
										"--RestAPI=" + RestAPI.getName() + "--MessageFlowName=" + RestMF.getName()
												+ " , AdditionalInstances=" + ai + " , is running=" + EG.isRunning());
								messageFlowsNotChanged++;
							}
						}
					}
				} finally {
					b.disconnect();
				}
				System.out.println("");
				System.out.println("Flows changed    =" + messageFlowsChanges);
				System.out.println("Flows not changed=" + messageFlowsNotChanged);
				System.out.println("Completed");
			}
		} catch (ConfigManagerProxyRequestFailureException ex) {
			List<LogEntry> rm = ex.getResponseMessages();
			if (rm != null) {
				ListIterator<LogEntry> listIterator = rm.listIterator();
				if (listIterator != null)
					while (listIterator.hasNext()) {
						LogEntry le = listIterator.next();
						System.out.println(le);
					}
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}
