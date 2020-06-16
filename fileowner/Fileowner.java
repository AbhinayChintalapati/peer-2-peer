import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

class Map{
	static HashMap<Integer, File> map = new HashMap<>(); 
}

public class Fileowner{
    	// private static final int sPort = 8000;   //The server will be listening on this port number
	static int sPort = 8000;    //The server will be listening on this port number
	ServerSocket sSocket;   //serversocket used to listen on port number 8000
	Socket requeSocket;
	Socket connection = null; //socket for the connection with the client
	String message;    //message received from the client
    String MESSAGE;    //uppercase message send to the client
    
    ObjectOutputStream out;  //stream write to the socket
	ObjectInputStream in;    //stream read from the socket
	

    
    public static void main(String[] args) throws Exception {
		System.out.println("The server is running.");
        	ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
        	try {
            		while(true) {
                		new Handler(listener.accept(),clientNum).start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
            			}
        	} finally {
            		listener.close();
        	}

        }

        private static class Handler extends Thread {
        	private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
        	private ObjectInputStream in;	//stream read from the socket
        	private ObjectOutputStream out;    //stream write to the socket
		private int no;
		private String usernamedot;
		private String passworddot;	//The index number of the client
		private String servercommand;
		private String [] namearr;
	    private String arrayString;
		private String namefile;
		private String namefile1;
		int numparts;
		int chunknum; 
		

        	public Handler(Socket connection, int no) {
            		this.connection = connection;
	    		this.no = no;
        	}

        public void run() {
 		try{
			// requestSocket = new Socket("localhost", 8001);
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();		
			in = new ObjectInputStream(connection.getInputStream());
			// System.out.println("coming");
			try{
				// while(true){
					// System.out.println("coming");
					// namefile = (String)in.readObject();
					// System.out.println("coming");
                    File tmpDir = new File("./"+"12.pdf");
                    // if(tmpDir.exists()){
						// System.out.println("file found");
						// sendMessage("found");	
                    // }
                    // else{
					// 	System.out.println("file not found");
                    //     sendMessage("not found");
					// }
					numparts = SplitFile.splitFile(tmpDir);
					System.out.println(Map.map.get(1).getName()+"\t"+Map.map.get(1).length());
					System.out.println(Map.map.get(2).getName()+"\t"+Map.map.get(2).length());
					sendMessage(numparts);
					chunknum = (Integer)in.readObject();
					System.out.println("chunknumber is "+chunknum);
					sendfile(chunknum);
					// getfile(namefile);
			// }
			}catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
		}
	}

	public void	sendfile(int chunknum) throws IOException{
		File chunkfile = Map.map.get(chunknum);
		// File tmpDir1 = new File("./"+"12.pdf");
		DataOutputStream doutp = new DataOutputStream(connection.getOutputStream());
		FileInputStream finp = new FileInputStream(chunkfile);
		byte[] bits = new byte[1];
		while (finp.read(bits) > 0) {
			doutp.write(bits);
			// System.out.println("sending chunk");
		}
		finp.close();
		doutp.close();
		System.out.println("chunk sent");
	} 
	

        
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
			// System.out.println("Send message: " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	void sendMessage(int num)
	{
		try{
			//stream write the message
			out.writeObject(num);
			out.flush();
			// System.out.println("Send message: " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
}}

// import java.io.*;
// import java.text.*;
// import java.util.*;
// import java.net.*;

class SplitFile {
//   Map map = new HashMap();
    public static int splitFile(File f) throws IOException {
        int partCounter = 1;//I like to name parts from 001, 002, 003, ...
                            //you can change it to 0 if you want 000, 001, ...

        int sizeOfFiles = 100 * 1024;// 1MB
        byte[] buffer = new byte[sizeOfFiles];

        String fileName = f.getName();

        double fileSize;
        int filesArraySize;

        fileSize = f.length()/1024;
        if(fileSize%100 == 0)
        {
          filesArraySize = (int)(fileSize/100);
        }
        else
        {
          filesArraySize = (int)(fileSize/100) + 1;
        }


        //int NumFilesPeer = filesArraySize/5;
        //File[][] filesArray = new File[5][NumFilesPeer + filesArraySize%NumFilesPeer];
        //try-with-resources to ensure closing stream
        try (FileInputStream fis = new FileInputStream(f);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            System.out.println("The file size is : "+fileSize+" KB");
            System.out.println("Number of part files will be :"+filesArraySize);
            // int lastIteration = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {

                //write each chunk of data into separate file with different number in name
                String filePartName = String.format("%d-%s", partCounter++, fileName);
                System.out.println("partCounter ="+partCounter);
                File newFile = new File(f.getParent(), filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                }

              /*  if(((partCounter-2)/(filesArraySize/5)) >= 4)
                {
                  filesArray[4][lastIteration] = newFile;
                  lastIteration++;
                  System.out.println("done"+lastIteration);
                }
                else
                {
                  filesArray[(int)((partCounter-2)/NumFilesPeer)][(partCounter-2)%(NumFilesPeer)] = newFile;
                }*/
                Map.map.put(partCounter-1,newFile);
            }
            System.out.println("Number of parts = "+(partCounter-1));
            System.out.println("Number of parts assigned to each of peers 1-4 = "+(partCounter-1)/5);
            System.out.println("Number of parts for peer 5 = "+((partCounter-1)/5 + (partCounter-1)%5));
            //System.out.println("The files created are : ");
          //  System.out.println(filesArray[4][10].getName());
            /*for(int i=0;i<5;i++)
            {
              for(int j =0; j < (NumFilesPeer + filesArraySize%NumFilesPeer);j++)
              {
                if((j >= filesArraySize/5)&&(i != 4))
                {
                  continue;
                }
                else
                {
                  System.out.println(filesArray[i][j].getName()+"\n"+"\ti="+i+"\tj="+j);
                }
              }
            }*/
		}
		return partCounter-1;
    }

    // public static void main(String[] args) throws IOException {
    //     splitFile(new File("C:\\Users\\sony\\Desktop\\CNproject2\\test shit\\testfile.pdf"));
    // }
}