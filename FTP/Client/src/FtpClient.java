import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class FtpClient {
	private int portNumber = 2020;
	private String serverIP = "127.0.0.1";
	private int status = 0;
	private String statusPhrase = "";

	public FtpClient() {

	}

	public FtpClient(int portNumber) {
		this.portNumber = portNumber;
	}

	public void client() throws IOException {
		String request, response;
	
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		Socket clientSocket = new Socket(serverIP, portNumber);

	//	DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		while (((request = inFromUser.readLine()) != null)) {
		response = processRequest(request, inFromServer, clientSocket);
		if (response.equals("Done")) break;
		System.out.println(response);
		}

		clientSocket.close();
		return;
	}

	private String processRequest(String request, BufferedReader inFromServer,
			Socket clientSocket) throws IOException {
		StringTokenizer st = new StringTokenizer(request);
		String command = st.nextToken();
		String contents = null;
		if (st.hasMoreTokens()) {
			contents = st.nextToken();
		}
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.writeBytes(request + System.getProperty("line.separator"));
		String response = null;
		switch (command) {
		case "LIST":
			response = listFile(inFromServer);
			break;
		case "GET":
			response = getFile(inFromServer, clientSocket, contents);
			break;
		case "PUT":
			response = putFile(clientSocket, inFromServer, contents);
			break;
		case "CD":
			response = changeDirect(inFromServer);
			break;
		default:
			response = inFromServer.readLine();
			break;
		}

		return response;
	}

	private String listFile(BufferedReader inFromServer) throws IOException {
		String response = "";
		status = Integer.parseInt(inFromServer.readLine());
		if (status > 0) {
			inFromServer.readLine();
			response = tokenizeList(inFromServer.readLine());
		} else {
			response = inFromServer.readLine();
		}
		return response;
	}

	private String getFile(BufferedReader inFromServer, Socket clientSocket, String contents) throws IOException {
		String response = "";
		status = Integer.parseInt(inFromServer.readLine());
		if (status > 0) {
			int fileSize = Integer.parseInt(inFromServer.readLine());
			DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
			FileOutputStream fos = new FileOutputStream(contents);
			
			byte[] buffer = new byte[1024 * 8];

			int totalLength = 0;
			int length;

			while ((length = dis.read(buffer)) != -1) {
				totalLength += length;
				fos.write(buffer, 0, length);
				if (totalLength >= fileSize)
					break;
			}
			if (fileSize == totalLength) {
				response = "Received " + contents + String.valueOf(" " + fileSize) + " bytes";
			} else {
				response = "Download Failed";
			}
			
			fos.close();
		} 
		else {
			response = inFromServer.readLine();
		}

		return response;
	}

	private String putFile(Socket clientSocket, BufferedReader inFromServer, String contents)
			throws IOException {
		String response = "";
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		File file = new File(contents);
		if (!file.exists()) {
			out.println("not exists");
			status = Integer.parseInt(inFromServer.readLine());
			response = inFromServer.readLine();
			return response;
		}
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		out.println(String.valueOf(file.length()));
	///	outToServer.writeBytes(String.valueOf(file.length()) + System.getProperty("line.separator"));
		FileInputStream fis = new FileInputStream(file);

		byte[] buffer = new byte[1024 * 8];
		int length = 0;
		
		while ((length = fis.read(buffer)) > 0) {
			outToServer.write(buffer, 0, length);
		}
		
		outToServer.flush();
		fis.close();
		status = Integer.parseInt(inFromServer.readLine());
		response = inFromServer.readLine();
		return response;
	}

	private String changeDirect(BufferedReader inFromServer) throws IOException {
		String response;
		status = Integer.parseInt(inFromServer.readLine());
		if (status > 0) {
			inFromServer.readLine();
		}
		response = inFromServer.readLine();
		return response;
	}

	public String tokenizeList(String line) {
		String result = "";
		StringTokenizer st = new StringTokenizer(line, ",");
		while (st.hasMoreTokens()) {
			result += st.nextToken() + ",";
			result += st.nextToken();
			if (st.hasMoreTokens())
				result += System.getProperty("line.separator");
		}
		return result;
	}

}
