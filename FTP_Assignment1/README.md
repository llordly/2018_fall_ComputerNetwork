# Programming Assignment – FTP Client and Server Program

 

**컴퓨터소프트웨어학부 2017029589 류지범**

 

## #0 Compile and Run

- 압축폴더의 Client.jar와 Server.jar의 압출을 풀면 Client의 경우 .classpath .project Client.java FtpClient.java가 생기고 Server의 경우 .classpath .project Server.java FtpServer.java가 생긴다. 
- Client 폴더에서 `javac Client.java FtpClient.java` 명령어를 입력하고, Server 폴더에서 `javac Server.java FtpServer.java`를 입력하면 컴파일이 된다.
-  Server 폴더에서 `java Server`를 입력하면 서버가 실행되고, Client 폴더에서 `java Client`를 입력하면 클라이언트가 실행된다.
- 모든 명령은 Window cmd에서 실행한 결과이다.





## #1 FTP

- File Transfer Protocol의 약자로 TCP/IP 프로토콜을 가지고 서버와 클라이언트 사이의 파일 전송을 하기 위한 프로토콜이다.
- 이번 과제에서 FTP Server와 Client를 만들기 위해 소켓을 통해 TCP 연결을 설정하고 클라이언트와 서버의 지속된 통신을 하였다.
- FTP 프로토콜 Server, Client를 Java blocking IO를 사용하여 구현하는 것이 assignment1의 목적이므로 기본적으로 서버는 하나의 클라이언트와 통신하며, 동시에 통신하지 않는다.

 

 

## #2 Code

- 큰 틀에서 Server와 Client로 나누어 소스코드를 작성했다. 아래 두 코드는 각각 Server와 Client의 main문이 포함된 class이다. 두 main 모두 FtpServer와 FtpClient라는 Class를 사용하여 통신을 시작한다. 파일 실행 시 인자값으로 portNumber를 넘겨줄 경우 사용자 설정 포트 기준으로 통신이 시작되며, 인자값을 넣지 않을 경우에는 default로 2020으로 통신을 시작한다. 
-  Server가 실행 되면, portNumber에 해당하는 welcomingSocket을 만들고 클라이언트가 연결을 시도할 때까지 기다린다.
-  클라이언트와의 통신이 끝나면 다른 클라이언트와 통신을 이어서 해야하므로 while(true)로 무한루프를 돌렸고, `serverSocket.accept()`로 client의 연결을 수락하여 TCP연결을 설정한다. 그 후 client와의 통신을 위한 inputstream과 outputstream을 wrapping한다. client로부터 오는 채널 전송 데이터를 line단위로 받아서 명령을 수행한다. 첫 줄은 항상 명령어이므로 space기준으로 data를 parsing하고 processRequest로 명령어를 넘겨준다. processRequest를 통해 모든 명령과 채널 데이터를 처리한다.
- processRequest는 명령어를 case로 나누어 처리하며 잘못된 명령이 입력됐을 경우 **“Command is wrong”**을 리턴한다.
- listFile은 contents에 해당하는 폴더에 있는 파일의 목록을 출력하는 함수이다. Status code에 대한 내용들은 맨 아래에 따로 기술하도록 하겠다. 경로가 없을 경우 오류코드와 메시지를 리턴하며, 디렉토리가 상대경로일 경우 절대경로로 바꾸어서 file클래스를 생성한다. 디렉토리가 아닐 경우도 마찬가지로 오류코드와 메시지를 리턴한다. 정상적인 경우에서 `listFiles()` 메소드를 이용하여 과제 명시서에 기록된 내용처럼 `“subdir1,-,subdir2,-,subdir3,-,test.txt,13,test.jpg,71514”`꼴로 데이터를 전송하기 위한 string을 리턴한다.
- `pushFile() `메소드는 서버기준에서 client에서 요청한 파일을 전송해주는 메소드이며 파일이 상대경로일 경우 절대경로로 바꾸어서 file클래스를 만들어주고 파일이 존재하지 않거나 파일이 디렉토리일 경우 오류코드와 메시지를 클라이언트로 보내주고 종료한다. 그 외의 경우 서버에 있는 파일을 fileinputstream을 통해 읽어오고, dataoutputstream을 통해 파일의 내용을 byte stream형태로 클라이언트로 보내주고 종료한다.
- `getFile()` 
  - 클라이언트가 put한 파일을 서버에 저장하는 메소드이다.
  - Client로부터 file의 size를 전달받고, datainputstream을 통해 file을 byte stream으로 읽는다. 읽은 file은 서버에 원래 받았던 filename으로 저장한다.
- `changeDirect()` 
  - 현재 디렉토리의 위치를 바꾸는 메소드
  - `System.getProperty(“user.dir”)`를 통해 현재 디렉토리의 위치를 얻어낸다.
  - CD만 들어왔을 경우에는 현재 디렉토리 위치를 리턴한다.
  - Path가 들어왔을 경우에는 상대경로일 경우 절대경로로 바꾸어서 현재 디렉토리의 위치를 path로 설정해준다.
  - 디렉토리가 존재하지 않거나 path가 디렉토리가 아닐 경우에는 오류코드와 메시지를 리턴한다.

- 클라이언트의 경우 서버의 ip와 portnumber로 서버와의 TCP 연결을 설정하고, 소켓을 통해 서버로부터 data를 받아오고 보내는 활동을 한다. 사용자로부터 명령어를 받고 processRequest를 통해 명령어를 실행한다. 사용자로부터 **“Done”**이라는 명령어가 들어 올 경우에는 클라이언트와 서버의 연결을 끊고 종료한다.

- Client의 processRequest도 server의 경우와 별반 다르지 않으며 사용자로부터의 명령어를 parsing하여 case별로 명령을 수행한다. 사용자의 명령은 서버로 라인단위로 전송된다.

- `listFile()`

  - 서버로부터 채널 전송 데이터를 받아서 콘솔으로 출력하기 위해 리턴한다. 이 때 파일을 줄 단위로 출력하기 위해 “,”기준으로 parsing한다. 이 때 tokenizeList라는 메소드를 이용한다.

- `TokenizeList()`

  - “,”단위로 list를 parsing하며 디렉토리의 경우 “filename,-“ 파일의 경우 “filename,filesize”를 줄단위로 구분하며 리턴한다.

- `getFile()`

  - 서버로부터 file을 받아오는 메소드이며 오류코드를 받았을 경우에는 오류 메시지를 리턴하고, 정상적일 경우에는 filename에 해당하는 file을 datainputstream으로부터 받아오고, “Received filename filesize bytes”를 리턴해준다. 다운로드에 실패했을 경우 “Download Failed”를 리턴한다

- `putFile()`

  - 클라이언트의 파일을 서버로 전송하는 메소드이며, dataoutputstream을 통해 서버로 파일을 버퍼단위로 끊어서 전송한다. File이 존재하지 않을 경우에는 오류코드를 서버로부터 받고 오류 메시지도 받아서 리턴한다. 정상적일 경우에는 서버로부터의 정상 응답메세지를 받아서 리턴할 수 있도록 한다. 그 형태는 “a.txt transferred/ 13 bytes”와 같다.

- `changeDirect()`

  - 서버로부터 status코드를 받고 오류코드가 아닐 경우 응답길이를 받고, 응답에 대한 메시지(서버가 이동한 directory path)를 받고 리턴한다. 오류코드일 경우에는 오류메시지만 리턴받을 수 있도록 한다.

  

## #3 Result and Analysis

- 서버를 실행하면 “server is opened”라는 메시지가 콘솔창에 뜬다. 
- Client가 실행되어 서버에 연결되면 “connected”라는 메시지라 서버에 뜬다.
- “CD” 만 입력했을 경우 서버의 현재 디렉토리를 리턴한다.
- “LIST .”을 입력하면 서버의 현재 디렉토리의 파일의 내용을 출력해준다.
- 위는 상위 디렉토리로 이동한 후 디렉토리의 list를 출력한 결과이다.
- “GET filename”을 하면 서버로부터 파일을 내려받고 “Received filename filesize bytes”라는 메시지를 클라이언트에 띄워준다.
- “PUT filename”을 입력할 경우에는 정상 전송이 완료되었을 경우에는 “filename transferred/ filesize bytes”가 cilent에 보여진다. 실제로 전송되었는지 확인하기 위해 “LIST .”을 해보면 전송이 실제로 이루어졌음을 확인할 수 있다.
- client로부터 “Done”이라는 명령어가 들어왔을 경우에는 통신이 종료되며 서버에는 “connect closed”라는 메시지가 뜨며 Client는 프로그램이 종료된다.



## #4 Status Code & Status Phrase

- -1 : such file does not exist (GET)
- -2 : Failed - directory can not be downloaded (GET)
- -3 : directory name is invalid (CD)
- -4 : failed - directory is not exists (CD)
- -5 : directory name is invalid (LIST)
- -6 : There are not enough commands (LIST)
- -7 : Failed for File corruption. Please put again (PUT)
- -10 : failed for unknown reason (PUT)