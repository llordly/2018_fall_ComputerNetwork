import java.io.IOException;

public class Client {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		FtpClient ftpClient;
		if (args.length == 1) {
			ftpClient = new FtpClient(Integer.parseInt(args[0]));
		}
		else if (args.length == 0) {
			ftpClient = new FtpClient();
		}
		else
			return;
		ftpClient.client();
	}

}
