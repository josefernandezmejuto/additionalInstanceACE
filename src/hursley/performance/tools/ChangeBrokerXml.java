package hursley.performance.tools;

import java.io.BufferedInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class ChangeBrokerXml {

	public static void main(String[] args) {
		String pathBaseFiles = "/tmp/";
		// String filePathBroker = "";
		String integrationNode = "";
		String integrationServer = "";
		String applicationName = "";
		String flowName = "";
		String fileName = "";
		// String filePathBrokerDfmxml = "";
		// String fileContent = "";
		int additionalInstance = 0;
		/*
		 * int startAdditionalInstances = 0; int inicioPosicionAdditionalInstances = 0;
		 * int finPosicionAdditionalInstances = 0; String[] arrayFileContents; int
		 * posicionCompiledMessageFlow = 0; int posicionConfigurableProperty = 0;
		 */

		if (args[0].equalsIgnoreCase("--help")) {
			System.out.println("Debe ingresar los siguiente parametros");
			System.out.println("--integrationNode");
			System.out.println("--integrationServer");
			System.out.println("--applicationName");
			System.out.println("--flowName");
			System.out.println("--additionalInstance");
			System.out.println("--fileName");
			return;
		} else if (args[0].equalsIgnoreCase("--fileName")) {

			if (args[0].equalsIgnoreCase("--fileName") && args[1] != null) {
				fileName = args[1];
				readCsvFileForFlow(fileName);
			} else {
				System.out.println("1.- Debe ingredar los valores de entrada...  fileName --help");
				return;
			}

		} else if (args.length < 8 || args.length > 8) {

			if (args[0].equalsIgnoreCase("--integrationNode") && args[1] != null) {
				integrationNode = args[1];
			} else {
				System.out.println("1.- Debe ingredar los valores de entrada...  ChangeBrokerXml --help");
				return;
			}
			if (args[2].equalsIgnoreCase("--integrationServer") && args[3] != null) {
				integrationServer = args[3];
			} else {
				System.out.println("2.- Debe ingredar los valores de entrada...  ChangeBrokerXml --help");
				return;
			}
			if (args[4].equalsIgnoreCase("--applicationName") && args[5] != null) {
				applicationName = args[5];
			} else {
				System.out.println("3.- Debe ingredar los valores de entrada...  ChangeBrokerXml --help");
				return;
			}
			if (args[6].equalsIgnoreCase("--flowName") && args[7] != null) {
				flowName = args[7];
			} else {
				System.out.println("4.- Debe ingredar los valores de entrada...  ChangeBrokerXml --help");
				return;
			}
			if (args[8].equalsIgnoreCase("--additionalInstance") && args[9] != null) {
				additionalInstance = Integer.valueOf(args[9]);
			} else {
				System.out.println("5.- Debe ingredar los valores de entrada...  AdditionalInstance --help");
				return;
			}

		} else {
			System.out.println("6.- Debe ingresar los valores de entrada... Para obtener ayuda  --help");
			return;
		}
		updateAdditionalInstance(pathBaseFiles, integrationNode, integrationServer, applicationName, flowName,
				additionalInstance);
	}

	public static void updateAdditionalInstance(String pathBaseFiles, String integrationNode, String integrationServer,
			String applicationName, String flowName, int additionalInstance) {
		String filePathBroker = "";
		// String filePathBrokerDfmxml = "";
		String fileContent = "";
		int startAdditionalInstances = 0;
		int inicioPosicionAdditionalInstances = 0;
		int finPosicionAdditionalInstances = 0;
		String[] arrayFileContents;
		int posicionCompiledMessageFlow = 0;
		int posicionConfigurableProperty = 0;

		filePathBroker = getFilePathBrokerXmlSameFlowName(pathBaseFiles, integrationNode, integrationServer,
				applicationName, flowName);
		if (!filePathBroker.contentEquals("#")) {
			writeLogsFile("INFO: File PATH: " + filePathBroker);
		}
		if (!getFilePathBrokerXmlSameFlowName(pathBaseFiles, integrationNode, integrationServer, applicationName,
				flowName).equalsIgnoreCase("#")) {
			writeLogsFile("INFO: Primera evaluacion: " + "OK");
			fileContent = getFileContent(getFilePathBrokerXmlSameFlowName(pathBaseFiles, integrationNode,
					integrationServer, applicationName, flowName));
			if (fileContent.contains("CompiledMessageFlow name=\"" + flowName + "\"")) {
				if (fileContent.contains("uri=\"" + flowName + "#additionalInstances\"")) {
					arrayFileContents = getFileContentOnArray(getFilePathBrokerXmlSameFlowName(pathBaseFiles,
							integrationNode, integrationServer, applicationName, flowName));
					for (int i = 0; i < arrayFileContents.length; i++) {
						if (arrayFileContents[i].contains("<CompiledMessageFlow name=\"" + flowName + "\"")) {
							posicionCompiledMessageFlow = i;
							writeLogsFile("INFO: Tag XML actual : " + "<CompiledMessageFlow name=\"" + flowName + "\""
									+ " en la posicion file: " + String.valueOf(posicionCompiledMessageFlow));
						} else if (arrayFileContents[i].contains("uri=\"" + flowName + "#additionalInstances\"")) {
							posicionConfigurableProperty = i;
							writeLogsFile("INFO: Tag XML actual : " + "uri=" + flowName + "#additionalInstances"
									+ " en la posicion file: " + String.valueOf(posicionConfigurableProperty));
						}
					}
					for (int i = 0; i < arrayFileContents.length; i++) {
						arrayFileContents[i] = arrayFileContents[i] + ">";
					}
					if (posicionConfigurableProperty > 0) {
						arrayFileContents[posicionConfigurableProperty] = "<ConfigurableProperty override=\"{"
								+ "BechPolicyProjectForWLM" + "}:BechPolicyWLM"+additionalInstance+"\" uri=\"" + flowName + "#wlmPolicy\"/>";
						writeLogsFile("INFO: Tag actualizado : " + "<ConfigurableProperty override=\"{" + "BechPolicyProjectForWLM"
								+ "}:BechPolicyWLM"+additionalInstance+"\" uri=\"" + flowName + "#wlmPolicy\"/>");
						writeLogsFile("INFO: Additional Instance Actualizadas: " + "OK");
						try {
							copyFileIntegrationNode(filePathBroker, filePathBroker + ".original");
							clearBrokerXmlFile(filePathBroker);
							for (int i = 0; i < arrayFileContents.length; i++) {
								createBrokerXmlFile(filePathBroker, arrayFileContents[i]);
							}
							writeLogsFile("INFO: Archivo actualizado : OK");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				} else {
					writeLogsFile("ERROR: Tag XML no presente : "
							+ "uri=\"singleSelectExchangeCurrencyCommission#additionalInstances\"");
				}
			} else if (fileContent.contains("CompiledMessageFlow name=\"" + applicationName + "\"")) {
				if (fileContent.contains("uri=\"" + applicationName + "#additionalInstances\"")) {
					arrayFileContents = getFileContentOnArray(getFilePathBrokerXmlSameFlowName(pathBaseFiles,
							integrationNode, integrationServer, applicationName, flowName));
					for (int i = 0; i < arrayFileContents.length; i++) {
						if (arrayFileContents[i].contains("<CompiledMessageFlow name=\"" + applicationName + "\"")) {
							posicionCompiledMessageFlow = i;
							writeLogsFile("INFO: Tag XML actual : " + "<CompiledMessageFlow name=\"" + applicationName
									+ "\"" + " en la posicion file: " + String.valueOf(posicionCompiledMessageFlow));
						} else if (arrayFileContents[i]
								.contains("uri=\"" + applicationName + "#additionalInstances\"")) {
							posicionConfigurableProperty = i;
							writeLogsFile("INFO: Tag XML actual : " + "uri=" + applicationName + "#additionalInstances"
									+ " en la posicion file: " + String.valueOf(posicionConfigurableProperty));
						}
					}
					for (int i = 0; i < arrayFileContents.length; i++) {
						arrayFileContents[i] = arrayFileContents[i] + ">";
					}
					if (posicionConfigurableProperty > 0) {
						arrayFileContents[posicionConfigurableProperty] = "<ConfigurableProperty override=\"{"
								+ "BechPolicyProjectForWLM" + "}:BechPolicyWLM"+additionalInstance+"\" uri=\"" + applicationName + "#wlmPolicy\"/>";
						writeLogsFile("INFO: Tag actualizado : " + "<ConfigurableProperty override=\"{" + "BechPolicyProjectForWLM"
								+ "}:BechPolicyWLM"+additionalInstance+"\" uri=\"" + applicationName + "#wlmPolicy\"/>");
						writeLogsFile("INFO: Additional Instance Actualizadas: " + "OK");
						try {
							copyFileIntegrationNode(filePathBroker, filePathBroker + ".original");
							clearBrokerXmlFile(filePathBroker);
							for (int i = 0; i < arrayFileContents.length; i++) {
								createBrokerXmlFile(filePathBroker, arrayFileContents[i]);
							}
							writeLogsFile("INFO: Archivo actualizado : OK");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					writeLogsFile(
							"ERROR: Tag XML no presente : " + "uri=\"" + applicationName + "#additionalInstances\"");
				}
			} else {
				writeLogsFile("ERROR: Tag XML no presente : " + "CompiledMessageFlow name=\"" + flowName + "\"");
			}

		} else if (!getFilePathBrokerXmlDifFlowName(pathBaseFiles, integrationNode, integrationServer, applicationName,
				flowName).equalsIgnoreCase("#")) {
			writeLogsFile("INFO: Segunda evaluacion: " + "OK");
			getFileContent(getFilePathBrokerXmlDifFlowName(pathBaseFiles, integrationNode, integrationServer,
					applicationName, flowName));
		} else if (!getFilePathDfmxml(pathBaseFiles, integrationNode, integrationServer, applicationName, flowName)
				.equalsIgnoreCase("#")) {
			writeLogsFile("INFO: Tercera evaluacion: " + "OK");
			writeLogsFile("INFO: File PATH: "
					+ getFilePathDfmxml(pathBaseFiles, integrationNode, integrationServer, applicationName, flowName));
			filePathBroker = getFilePathDfmxml(pathBaseFiles, integrationNode, integrationServer, applicationName,
					flowName);
			// Entro por esta opcion cuando debo manejar un archivo dfmxml
			fileContent = getFileContent(
					getFilePathDfmxml(pathBaseFiles, integrationNode, integrationServer, applicationName, flowName));

			if (fileContent.contains("label=\"" + flowName + "\"")) {
				writeLogsFile("INFO: Tag XML actual : " + "label=\"" + flowName + "\"");
				// Entra aqui buscando la cadena de caracter label.....
				startAdditionalInstances = fileContent.indexOf("label=\"" + flowName + "\"") + "label=".length()
						+ flowName.length() + 3;
				if (fileContent
						.substring(startAdditionalInstances, startAdditionalInstances + "additionalInstances=".length())
						.equalsIgnoreCase("additionalInstances=")) {
					// Entra aqui buscando si existe la cadena de additionalInstances
					if (fileContent
							.substring(startAdditionalInstances + "additionalInstances=".length(),
									3 + startAdditionalInstances + "additionalInstances=".length())
							.contentEquals("\"0\"")) {
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances valor = 0");
						// Entra aqui si la cantidad de Instancias adicionales es = 0
						inicioPosicionAdditionalInstances = (startAdditionalInstances + "additionalInstances=".length()
								+ 1);
						finPosicionAdditionalInstances = (fileContent.indexOf("\" commitCount"));
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances en la posicion inicial: "
								+ String.valueOf(inicioPosicionAdditionalInstances));
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances en la posicion final: "
								+ String.valueOf(finPosicionAdditionalInstances));
						// setAdditionalInstances((fileContent.substring(0,
						// inicioPosicionAdditionalInstances)),
						// (fileContent.substring((finPosicionAdditionalInstances),
						// fileContent.length())), "10");
						writeLogsFile("INFO: Additional Instance Actualizadas: " + "OK");
						try {
							copyFileIntegrationNode(filePathBroker, filePathBroker + ".original");
							clearBrokerDfmxmlFile(filePathBroker);
							createBrokerDfmxmlFile(filePathBroker,
									setAdditionalInstances(
											(fileContent.substring(0, inicioPosicionAdditionalInstances)), (fileContent
													.substring((finPosicionAdditionalInstances), fileContent.length())),
											String.valueOf(additionalInstance)));
							writeLogsFile("INFO: Archivo actualizado : OK");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances valor != 0");
						// Entra aqui si la cantidad de Instancias adicionales es diferente de 0
						inicioPosicionAdditionalInstances = (startAdditionalInstances + "additionalInstances=".length()
								+ 1);
						finPosicionAdditionalInstances = (fileContent.indexOf("\" commitCount"));

						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances en la posicion inicial: "
								+ String.valueOf(inicioPosicionAdditionalInstances));
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances en la posicion final: "
								+ String.valueOf(finPosicionAdditionalInstances));

						// setAdditionalInstances((fileContent.substring(0,
						// inicioPosicionAdditionalInstances)),
						// (fileContent.substring((finPosicionAdditionalInstances),
						// fileContent.length())), "10");
						writeLogsFile("INFO: Additional Instance Actualizadas: " + "OK");
						try {
							copyFileIntegrationNode(filePathBroker, filePathBroker + ".original");
							clearBrokerDfmxmlFile(filePathBroker);
							createBrokerDfmxmlFile(filePathBroker,
									setAdditionalInstances(
											(fileContent.substring(0, inicioPosicionAdditionalInstances)), (fileContent
													.substring((finPosicionAdditionalInstances), fileContent.length())),
											String.valueOf(additionalInstance)));
							writeLogsFile("INFO: Archivo actualizado : OK");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else if (fileContent.contains("label=\"" + applicationName + "." + flowName + "\"")) {
				// Entra aqui buscando la cadena de caracter label.....
				startAdditionalInstances = fileContent.indexOf("label=\"" + applicationName + "." + flowName + "\"")
						+ ("label=\"" + applicationName + "." + flowName + "\"").length() + 1;
				if (fileContent
						.substring(startAdditionalInstances, startAdditionalInstances + "additionalInstances=".length())
						.equalsIgnoreCase("additionalInstances=")) {
					// Entra aqui buscando si existe la cadena de additionalInstances
					if (fileContent
							.substring(startAdditionalInstances + "additionalInstances=".length(),
									3 + startAdditionalInstances + "additionalInstances=".length())
							.contentEquals("\"0\"")) {
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances valor = 0");

						// Entra aqui si la cantidad de Instancias adicionales es = 0
						inicioPosicionAdditionalInstances = (startAdditionalInstances + "additionalInstances=".length()
								+ 1);
						finPosicionAdditionalInstances = (fileContent.indexOf("\" commitCount"));
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances en la posicion inicial: "
								+ String.valueOf(inicioPosicionAdditionalInstances));
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances en la posicion final: "
								+ String.valueOf(finPosicionAdditionalInstances));

						// setAdditionalInstances((fileContent.substring(0,
						// inicioPosicionAdditionalInstances)),
						// (fileContent.substring((finPosicionAdditionalInstances),
						// fileContent.length())), "10");

						writeLogsFile("INFO: Additional Instance Actualizadas: " + "OK");
						try {
							copyFileIntegrationNode(filePathBroker, filePathBroker + ".original");
							clearBrokerDfmxmlFile(filePathBroker);
							createBrokerDfmxmlFile(filePathBroker,
									setAdditionalInstances(
											(fileContent.substring(0, inicioPosicionAdditionalInstances)), (fileContent
													.substring((finPosicionAdditionalInstances), fileContent.length())),
											String.valueOf(additionalInstance)));
							writeLogsFile("INFO: Archivo actualizado : OK");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						// Entra aqui si la cantidad de Instancias adicionales es diferente de 0
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances valor != 0");
						inicioPosicionAdditionalInstances = (startAdditionalInstances + "additionalInstances=".length()
								+ 1);
						finPosicionAdditionalInstances = (fileContent.indexOf("\" commitCount"));

						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances en la posicion inicial: "
								+ String.valueOf(inicioPosicionAdditionalInstances));
						writeLogsFile("INFO: Tag XML actual : " + "additionalInstances en la posicion final: "
								+ String.valueOf(finPosicionAdditionalInstances));

						// setAdditionalInstances((fileContent.substring(0,
						// inicioPosicionAdditionalInstances)),
						// (fileContent.substring((finPosicionAdditionalInstances),
						// fileContent.length())), "10");
						writeLogsFile("INFO: Additional Instance Actualizadas: " + "OK");
						try {
							copyFileIntegrationNode(filePathBroker, filePathBroker + ".original");
							clearBrokerDfmxmlFile(filePathBroker);
							createBrokerDfmxmlFile(filePathBroker,
									setAdditionalInstances(
											(fileContent.substring(0, inicioPosicionAdditionalInstances)), (fileContent
													.substring((finPosicionAdditionalInstances), fileContent.length())),
											String.valueOf(additionalInstance)));
							writeLogsFile("INFO: Archivo actualizado : OK");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else {
				// System.out.println("NO EXISTE");
			}
		} else if (!getFilePathDfmxml(pathBaseFiles, integrationNode, integrationServer, applicationName, flowName)
				.equalsIgnoreCase("#")) {
			writeLogsFile("INFO: Cuarta evaluacion: " + "OK");
			// Entro por esta opcion cuando debo manejar un archivo dfmxml
			fileContent = getFileContent(
					getFilePathDfmxml(pathBaseFiles, integrationNode, integrationServer, applicationName, flowName));
			if (fileContent.contains("label=\"" + flowName + "\"")) {
				// Entra aqui buscando la cadena de caracter label.....
				startAdditionalInstances = fileContent.indexOf("label=\"" + flowName + "\"") + "label=".length()
						+ flowName.length() + 3;
				if (fileContent
						.substring(startAdditionalInstances, startAdditionalInstances + "additionalInstances=".length())
						.equalsIgnoreCase("additionalInstances=")) {
					// Entra aqui buscando si existe la cadena de additionalInstances
					if (fileContent
							.substring(startAdditionalInstances + "additionalInstances=".length(),
									3 + startAdditionalInstances + "additionalInstances=".length())
							.contentEquals("\"0\"")) {

						// Entra aqui si la cantidad de Instancias adicionales es = 0
						inicioPosicionAdditionalInstances = (startAdditionalInstances + "additionalInstances=".length()
								+ 1);
						finPosicionAdditionalInstances = (fileContent.indexOf("\" commitCount"));
						setAdditionalInstances((fileContent.substring(0, inicioPosicionAdditionalInstances)),
								(fileContent.substring((finPosicionAdditionalInstances), fileContent.length())),
								String.valueOf(additionalInstance));
					} else {
						// Entra aqui si la cantidad de Instancias adicionales es diferente de 0
						inicioPosicionAdditionalInstances = (startAdditionalInstances + "additionalInstances=".length()
								+ 1);
						finPosicionAdditionalInstances = (fileContent.indexOf("\" commitCount"));
						setAdditionalInstances((fileContent.substring(0, inicioPosicionAdditionalInstances)),
								(fileContent.substring((finPosicionAdditionalInstances), fileContent.length())),
								String.valueOf(additionalInstance));
					}
				}

			} else {
				writeLogsFile("ERROR: Cuarta evaluacion: " + "ERROR!!");
			}
		}

	}

	public static String[] getFileContentOnArray(String pathBaseFiles) {
		String lienaOfFile = "";
		Scanner localFileScanner;
		try {
			localFileScanner = new Scanner(new File(pathBaseFiles)).useDelimiter(">");
			List<String> listLineFile = new ArrayList<String>();
			while (localFileScanner.hasNext()) {
				lienaOfFile = localFileScanner.next();
				listLineFile.add(lienaOfFile.trim());
			}
			localFileScanner.close();
			String[] tempsArray = listLineFile.toArray(new String[0]);
			return tempsArray;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String setAdditionalInstances(String cadenaInicial, String cadenaFinal,
			String valorAdditionalInstances) {
		return cadenaInicial + valorAdditionalInstances + cadenaFinal;
	}

	public static String getFilePathDfmxml(String pathBaseFiles, String integrationNode, String integrationServer,
			String applicationName, String flowName) {
		File directoryPath = new File(
				pathBaseFiles + integrationNode + "/servers/" + integrationServer + "/run/" + applicationName);
		String[] listFiles = directoryPath.list();
		String fileNameList = "";
		if (listFiles == null) {
			return "#";
		} else if (listFiles.length > 0) {
			for (int i = 0; i < listFiles.length; i++) {
				fileNameList = listFiles[i];
				if (fileNameList.equalsIgnoreCase(flowName + ".dfmxml")) {
					return pathBaseFiles + integrationNode + "/servers/" + integrationServer + "/run/" + applicationName
							+ "/" + flowName + ".dfmxml";
				} else if (fileNameList.equalsIgnoreCase(flowName + "." + flowName + ".dfmxml")) {
					return pathBaseFiles + integrationNode + "/servers/" + integrationServer + "/run/" + applicationName
							+ "/" + flowName + "." + flowName + ".dfmxml";
				}
			}
		}
		return "#";
	}

	public static String getFilePathBrokerXmlSameFlowName(String pathBaseFiles, String integrationNode,
			String integrationServer, String applicationName, String flowName) {
		File directoryPath = new File(pathBaseFiles + integrationNode + "/servers/" + integrationServer + "/run/"
				+ applicationName + "/META-INF/");
		String[] listFiles = directoryPath.list();
		String fileNameList = "";
		if (listFiles == null) {
			return "#";
		} else if (listFiles.length > 0) {
			for (int i = 0; i < listFiles.length; i++) {
				fileNameList = listFiles[i];
				if (fileNameList.equalsIgnoreCase("broker.xml")) {
					return pathBaseFiles + integrationNode + "/servers/" + integrationServer + "/run/" + applicationName
							+ "/META-INF/broker.xml";
				}
			}
		}
		return "#";
	}

	public static String getFilePathBrokerXmlDifFlowName(String pathBaseFiles, String integrationNode,
			String integrationServer, String applicationName, String flowName) {
		File directoryPath = new File(pathBaseFiles + integrationNode + "/servers/" + integrationServer + "/run/"
				+ applicationName + "/" + flowName + "/META-INF/");
		String[] listFiles = directoryPath.list();
		String fileNameList = "";
		if (listFiles == null) {
			return "#";
		} else if (listFiles.length > 0) {
			for (int i = 0; i < listFiles.length; i++) {
				fileNameList = listFiles[i];
				if (fileNameList.equalsIgnoreCase("broker.xml")) {
					return pathBaseFiles + integrationNode + "/servers/" + integrationServer + "/run/" + applicationName
							+ "/" + flowName + "/META-INF/broker.xml";
				}
			}
		}
		return "#";
	}

	public static String getFileContent(String filePathBroker) {
		File fileNameIObject = new File(filePathBroker);
		FileReader fileNameIObjectRead;
		try {
			fileNameIObjectRead = new FileReader(fileNameIObject);
			BufferedReader localBufferedReader = new BufferedReader(fileNameIObjectRead);
			StringBuffer localStringBuffer = new StringBuffer();
			String lineFile;
			while ((lineFile = localBufferedReader.readLine()) != null) {
				localStringBuffer.append(lineFile);
				localStringBuffer.append("\n");
			}
			fileNameIObjectRead.close();
			return localStringBuffer.toString();
		} catch (FileNotFoundException e) {
			return "##";
		} catch (IOException e) {
			return "##";
		}
	}

	public static void writeLogsFile(String messageLog) {
		String localFileName = "/tmp/SystemOut.log";
		File tempFile = new File(localFileName);
		try {
			boolean existsFile = tempFile.exists();
			if (!existsFile) {
				createLogsFile("/tmp/SystemOut.log");
			}
			FileOutputStream outputStream;

			outputStream = new FileOutputStream(localFileName, true);
			byte[] strToBytes = ("\r\n" + LocalDate.now() + ":" + LocalTime.now() + " - " + messageLog).getBytes();
			outputStream.write(strToBytes);
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearBrokerDfmxmlFile(String fileName) {
		PrintWriter localWriterClear = null;
		try {
			writeLogsFile("INFO: Limpiando contenido archivo: " + fileName);
			File fileBrokerXmlFile = new File(fileName);
			localWriterClear = new PrintWriter(fileBrokerXmlFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			localWriterClear.print("");
			localWriterClear.close();
			writeLogsFile("INFO: Limpiando contenido archivo: OK");
		}

	}

	public static void createBrokerDfmxmlFile(String fileName, String lineContent) {
		try {
			FileOutputStream outputStream;
			outputStream = new FileOutputStream(fileName, true);
			byte[] strToBytes = (lineContent + "\r\n").getBytes();
			outputStream.write(strToBytes);
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearBrokerXmlFile(String fileName) {
		PrintWriter localWriterClear = null;
		try {
			writeLogsFile("INFO: Limpiando contenido archivo: " + fileName);
			File fileBrokerXmlFile = new File(fileName);
			localWriterClear = new PrintWriter(fileBrokerXmlFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			localWriterClear.print("");
			localWriterClear.close();
			writeLogsFile("INFO: Limpiando contenido archivo: OK");
		}

	}

	public static void createBrokerXmlFile(String fileName, String lineContent) {
		try {
			FileOutputStream outputStream;
			outputStream = new FileOutputStream(fileName, true);
			byte[] strToBytes = (lineContent + "\r\n").getBytes();
			outputStream.write(strToBytes);
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createLogsFile(String pathAndFileName) {
		File fileNewLogs = new File(pathAndFileName);
		try {
			fileNewLogs.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void copyFileIntegrationNode(String inputSource, String imputSource) throws IOException {

		File originalSource = new File(inputSource);
		File copySource = new File(imputSource);

		InputStream localInputStream = null;
		OutputStream localOutputStream = null;
		try {
			localInputStream = new FileInputStream(originalSource);
			localOutputStream = new FileOutputStream(copySource);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = localInputStream.read(buffer)) > 0) {
				localOutputStream.write(buffer, 0, length);
			}
		} finally {
			writeLogsFile("INFO: Archivo original: " + inputSource);
			writeLogsFile("INFO: Archivo copiado: " + imputSource);
			writeLogsFile("INFO: Archivo copiado: " + "OK");
			localOutputStream.close();
			localInputStream.close();
		}
	}

	private static void readCsvFileForFlow(String filePathName) {
		// Estructura = String pathBaseFiles, String integrationNode, String
		// integrationServer, String applicationName, String flowName, int
		// additionalInstance
		File originalSource = new File(filePathName);
		FileReader originalSourceFileReader;
		int numeroDeFilaDelArchivoCsv = 0;

		try {

			originalSourceFileReader = new FileReader(originalSource);
			BufferedReader originalBufferFileReader = new BufferedReader(originalSourceFileReader);

			String lineOfFileCsv = "";
			String[] tempArrOfLineFileCsv;
			String[] tempArrOfAllLineFileCsv;

			try {
				while ((lineOfFileCsv = originalBufferFileReader.readLine()) != null) {
					numeroDeFilaDelArchivoCsv = numeroDeFilaDelArchivoCsv + 1;
					writeLogsFile("INFO: Archivo CSV Fila: " + String.valueOf(numeroDeFilaDelArchivoCsv));
					tempArrOfLineFileCsv = lineOfFileCsv.split(",");
					// for (int i = 0; i < tempArrOfLineFileCsv.length; i++) {
					// System.out.println(tempArrOfLineFileCsv[i]);
					// }
					// updateAdditionalInstance(String pathBaseFiles, String integrationNode, String
					// integrationServer, String applicationName, String flowName, int
					// additionalInstance)
					try {
					updateAdditionalInstance(tempArrOfLineFileCsv[0], tempArrOfLineFileCsv[1], tempArrOfLineFileCsv[3],
							tempArrOfLineFileCsv[4], tempArrOfLineFileCsv[2], Integer.valueOf(tempArrOfLineFileCsv[5]));
					} catch (Exception e) {
						writeLogsFile("ERROR: Archivo CSV Fila: " + String.valueOf(numeroDeFilaDelArchivoCsv));
					}
				}

				originalSourceFileReader.close();
			} catch (NumberFormatException | IOException e) {
				writeLogsFile("ERROR: Archivo CSV Fila: " + String.valueOf(numeroDeFilaDelArchivoCsv));
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			writeLogsFile("ERROR: Archivo CSV Fila: " + String.valueOf(numeroDeFilaDelArchivoCsv));
			e.printStackTrace();
		} finally {

		}

	}

}
