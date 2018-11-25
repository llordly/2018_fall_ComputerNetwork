import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.StringTokenizer;

public class FtpServer {
	private int portNumber = 2020;
	private String serverIP = "127.0.0.1";
	private int status = 0;
	private String statusPhrase = "";
	private String currentPath = System.getProperty("user.dir");

	public FtpServer() {

	}

	public FtpServer(int portNumber) {
		this.portNumber = portNumber;
	}

	public void server() throws IOException {
		final ServerSocket serverSocket = new ServerSocket(portNumber);
		System.out.println("server is opened");

		BufferedReader in = null;
		PrintWriter out = null;
		StringTokenizer st = null;

		while (true) {
			final Socket clientSocket = serverSocket.accept();
			System.out.println("connected..");

			new Thread(new Runnable() {
				public void run() {
					try {
						serve(in, out, clientSocket, st);
						System.out.println("connect closed.");
						in.close();
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
	
	private void serve(BufferedReader in, PrintWriter out, Socket clientSocket, StringTokenizer st) throws IOException {
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		
		String request, response;

		while ((request = in.readLine()) != null) {
			if ("Done".equals(request)) {
				out.println("Done");
				break;
			}

			String command;
			String contents = null;
			st = new StringTokenizer(request);
			command = st.nextToken();
			
			//if contents is null, request is "CD"
			if (st.hasMoreTokens()) {
				contents = st.nextToken();
			}
			response = processRequest(command, contents, clientSocket, in, out);
			if ("wrong".equals(response)) continue;
			
			// case of not GET
			if (!command.equals("GET")) {
				out.println(String.valueOf(status));
				if (status > 0) {
					// CD or LIST
					if (!command.equals("PUT")) {
						out.println(String.valueOf(response).length());
					}
					out.println(response);
				} else {
					out.println(statusPhrase);
				}
			}
			
		}
	}

	private String processRequest(String command, String contents, Socket clientSocket, BufferedReader in,
			PrintWriter out) throws IOException {
		String response = null;
		switch (command) {
		case "LIST":
			response = listFile(contents);
			break;
		case "GET":
			pushFile(contents, clientSocket, out);
			break;
		case "PUT":
			response = getFile(contents, clientSocket, in);
			break;
		case "CD":
			response = changeDirect(contents);
			break;
		default:
			out.println("Command is wrong");
			response = "wrong";
			break;
		}

		return response;
	}

	private String listFile(String contents) throws IOException {
		String response = "";
		
		if (contents == null) {
			status = -6;
			statusPhrase = "There are not enough commands.";
			return response;
		}
		
		File directory = new File(contents);
		if (!directory.isAbsolute()) {
//			directory = new File(System.getProperty("user.dir") + File.separator + contents);
			directory = new File(currentPath + File.separator + contents);
		}
		
		if (!directory.isDirectory()) {
			status = -5;
			statusPhrase = "Failed - directory name is invalid";
			return response;
		} else {
			status = 1;// status code
		}
		File[] fileList = directory.listFiles();
		for (int i = 0; i < fileList.length; ++i) {
			if (fileList[i].isDirectory()) {
				response += fileList[i].getName() + ",-";
			} else {
				response += fileList[i].getName() + "," + String.valueOf(fileList[i].length());
			}
			if (i != fileList.length - 1) {
				response += ",";
			}
		}
		
		return response;
	}

	private void pushFile(String contents, Socket clientSocket, PrintWriter out) throws IOException {
		String response = "";
		
		File file = new File(contents);
		if (!file.isAbsolute()) {
//			file = new File(System.getProperty("user.dir") + File.separator + contents);
			file = new File(currentPath + File.separator + contents);
		}
		/*
		 * if File is not in this server or Path is not accurate, first line is status
		 * code and second line is status phrase
		 */
		if (!file.exists()) {
			status = -1;
			statusPhrase = "Failed - Such file does not exist";
			out.println(status);
			out.println(statusPhrase);
			return;
		}
		else if (file.isDirectory()) {
			status = -2;
			statusPhrase = "Failed - directory can not be downloaded";
			out.println(status);
			out.println(statusPhrase);
			return;
		}
		else {
			FileInputStream fis = new FileInputStream(file);
			DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
			status = 1;// status code - OK
			response = String.valueOf(file.length()); // file length must be pushed
			out.println(String.valueOf(status));
			out.println(response);

			// push file object stream
			byte[] buffer = new byte[1024 * 8];

			int length;
			while ((length = fis.read(buffer)) > 0) {
				dos.write(buffer, 0, length);
			}
			
			dos.flush();
			fis.close();
		}
		return;
	}

	private synchronized String getFile(String contents, Socket clientSocket, BufferedReader in) throws IOException {
		String response = "";
		int fileSize;
		try {
			fileSize = Integer.parseInt(in.readLine());
		}
		catch (NumberFormatException e) {
			status = -8;
			statusPhrase = "Failed for wrong file name";
			return response;
		}
		
		File file = new File(currentPath + File.separator + contents);
		
		DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
		FileOutputStream fos = new FileOutputStream(file);

		byte[] buffer = new byte[1024 * 8];

		int totalLength = 0;
		int length = 0;
		
		while ((length = dis.read(buffer)) != -1) {
			totalLength += length;
			fos.write(buffer, 0, length);
			if (totalLength >= fileSize)
				break;
		}

		if (fileSize == totalLength) {
			status = 1; // status code - OK
			response = contents + " transferred/ " + String.valueOf(fileSize) + " bytes";
		} else {
			status = -10; // status code - failed
			statusPhrase = "Failed for unknown reason";
			if (totalLength > fileSize) {
				status = -7; // status code - failed
				statusPhrase = "Failed for File corruption. Please put again";
			}
			fos.close();
			File existFile = new File(contents);
			if (!existFile.isAbsolute()) {
				existFile = new File(currentPath + File.separator + contents);
			}
			if (existFile.exists())
				existFile.delete();
			return response;
		}
		fos.close();

		return response;
	}

	private String changeDirect(String contents) throws IOException {
		String response = "";
		if (contents == null) {
			status = 1;
			//return response = System.getProperty("user.dir");
			return response = currentPath;
		}
		File directory = new File(contents);
		if (!directory.isAbsolute()) {
//			directory = new File(System.getProperty("user.dir") + File.separator + contents);
			directory = new File(currentPath + File.separator + contents);
		}
		if (!directory.exists()) {
			status = -4;
			statusPhrase = "Failed - directory is not exists";
			return null;
		}
		if (!directory.isDirectory()) {
			status = -3;// status code
			statusPhrase = "Failed - directory name is invalid";
			return null;
		}
		// change current directory
		String path = directory.getCanonicalPath();
//		Properties prop = System.getProperties();
//		prop.setProperty("user.dir", path);
		currentPath = path;
		status = 1; // status code - OK
		// return changed path's canonical path
		return path;
	}
}