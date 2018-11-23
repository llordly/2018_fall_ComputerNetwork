import java.io.IOException;

public class Server {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		FtpServer ftpServer;
		if (args.length == 1) {
			ftpServer = new FtpServer(Integer.parseInt(args[0]));
		}
		else if (args.length == 0) {
			ftpServer = new FtpServer();
		}
		else
			return;
		ftpServer.server();
	}

}