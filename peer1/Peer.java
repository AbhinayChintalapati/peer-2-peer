import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.ThreadLocalRandom;

public class Peer implements Runnable{
    // ServerSocket pSocket;
    Socket requestSocket;           //socket connect to the server
    Socket peerSocket;
    int fileownerPort;
    static int pPort = 8002;
    static int dPort = 8003;
    // int dPort;
    // static int pPort;
	ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;
    ObjectOutputStream peerout;         //stream write to the socket
    ObjectInputStream peerin;
    Socket connection = null;
    String message;                //message send to the server
    String MESSAGE;                //capitalized message read from the server
    String response;
    int numparts;
    List <Integer> chunkllist = new ArrayList<Integer>();

    static HashMap<Integer, File> map1 = new HashMap<>();

	public static void main(String args[])
	{
        Peer peer = new Peer();

		peer.run();
	}

//     public Handler(Socket connection, int no) {
//         this.connection = connection;
//     this.no = no;
// }
private static class PeerHandler extends Thread{

    Socket connection = null;
    HashMap<Integer, File> map2 = new HashMap<>();
    ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;

    public void set(Socket connection, HashMap<Integer, File> chunks) throws IOException{
        this.connection = connection;
        this.map2 = chunks;
        out = new ObjectOutputStream(connection.getOutputStream());
        out.flush();
        in = new ObjectInputStream(connection.getInputStream());
        System.out.println("connect good0");
        List<Integer> chunklist = new ArrayList<Integer>(chunks.keySet());
        // sendMessage(chunklist);


    }



        void sendMessage(List<Integer> chunklist) {
            try{
                //stream write the message
                out.writeObject(chunklist);
                out.flush();
                // System.out.println("Send message: " + msg);
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }

        public void sendfile(int chunknum) throws IOException {
		File chunkfile = map1.get(chunknum);
		// File tmpDir1 = new File("./"+"12.pdf");
		DataOutputStream doutp = new DataOutputStream(connection.getOutputStream());
		FileInputStream finp = new FileInputStream(chunkfile);
		byte[] bits = new byte[8192];
		while (finp.read(bits) > 0) {
			doutp.write(bits);
			System.out.println("sending chunk");
		}
		finp.close();
		doutp.close();
		System.out.println("chunk sent");
    }



    void sendMessage(String msg)
    {
        try{

            out.writeObject(msg);
            out.flush();

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

}



    public void run()
    {
        try{
            requestSocket = new Socket("localhost", 8000);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());

            numparts = (Integer)in.readObject();
            while(map1.size()<numparts){
            int randomNum = ThreadLocalRandom.current().nextInt(1, numparts + 1);
            if(map1.containsKey(randomNum)){
                continue;
            }
            System.out.println(map1.size());
            System.out.println(numparts);

            sendMessage(randomNum);
            receive(randomNum);

            downpeerconnect();
            connectpeer();
            requestSocket = new Socket("localhost", 8000);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
            // connectpeer();
            downpeerconnect();
            // out1 = new ObjectOutputStream(connection.getOutputStream());
            // out1.flush();
            // in1 = new ObjectInputStream(connection.getInputStream());
            }
            sortbykey(map1);
            List<File> Filelist = new ArrayList<File>(map1.values());
            // File[] Files = Arrays.
            File mergedfile = new File("newfile.pdf");
            mergeFiles(Filelist, mergedfile);
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
    }
        catch ( ClassNotFoundException e ) {
                System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost){
        System.err.println("You are trying to connect to an unknown host!");
    }
        catch(IOException ioException){
        ioException.printStackTrace();
    }
        finally{
        //Close connections
        try{
            in.close();

            out.close();
            requestSocket.close();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    }

    void receive(int chunknum) throws IOException{
        try{

            byte[] bits = new byte[1];
            // File tmpDir = new File("./"+"12.pdf");
            String filePartName = String.format("\\PARTFILES\\%d-%s", chunknum, "12.pdf");
            // File tmpfile = new File(filePartName);
            File tmpDir = new File("./"+filePartName);
            FileOutputStream foutp = new FileOutputStream(tmpDir);
            DataInputStream dinp = new DataInputStream(requestSocket.getInputStream());

            int fsize = 100000000; // Send file size in separate msg
            int r = 0;
            // int tr = 0;
            int tr = 0;
            int rem = fsize;
            while((r = dinp.read(bits, 0, Math.min(bits.length, rem))) > 0) {
                // tr += r;
                tr+=r;
                rem -= r;
                // System.out.println("read " + tr + " bytes.");
                foutp.write(bits, 0, r);
                // count++;
            }

            foutp.close();
            dinp.close();
            map1.put(chunknum,tmpDir);
            chunkllist.add(chunknum);
            // if(count == 0){
            //     System.out.println("File not found!");
            // }

        // }
    }
        catch (Exception ex){
            //
        }
    }

public static void mergeFiles(List<File> files, File mergedFile) {


    FileOutputStream fos;
    FileInputStream fis;
    byte[] fileBytes;
    int bytesRead = 0;
    try{
        fos = new FileOutputStream(mergedFile,true);
        for(File file : files){
            fis = new FileInputStream(file);
            fileBytes = new byte[(int) file.length()];
            bytesRead = fis.read(fileBytes,0,(int) file.length());
            assert (bytesRead == fileBytes.length);
            assert (bytesRead == (int) file.length());
            fos.write(fileBytes);
            fos.flush();
            fileBytes = null;
            fis.close();
            fis = null;
        }
        fos.close();
        fis = null;

    }
    catch(Exception exception){
        exception.printStackTrace();
    }
}


    public void sortbykey(HashMap map1)
    {
        // TreeMap to store values of HashMap
        TreeMap<Integer, File> sorted = new TreeMap<>();

        // Copy all data from hashMap into TreeMap
        sorted.putAll(map1);

        // Display the TreeMap which is naturally sorted
        for (Map.Entry<Integer, File> entry : sorted.entrySet())
            System.out.println("Key = " + entry.getKey() +
                         ", Value = " + entry.getValue());
    }

    void downpeerconnect(){
        (new Thread(){
            public void run(){
                try{

                    peerSocket = new Socket("localhost", dPort);
                    peerout = new ObjectOutputStream(peerSocket.getOutputStream());
                    peerout.flush();
                    peerin = new ObjectInputStream(peerSocket.getInputStream());
                    List<Integer> chunklist;
                    // chunklist = (List<Integer>)in.readObject();

                    while(map1.size()<numparts){
                    int randomNum = ThreadLocalRandom.current().nextInt(1, numparts + 1);
                    if(map1.containsKey(randomNum)){
                        continue;
                    }
                    System.out.println(map1.size());
                    System.out.println(numparts);

                    sendMessage(randomNum);
                    receive(randomNum);
                    peerSocket = new Socket("localhost", dPort);
                    peerout = new ObjectOutputStream(peerSocket.getOutputStream());
                    peerout.flush();
                    peerin = new ObjectInputStream(peerSocket.getInputStream());
                    }


                }
                catch (ConnectException e) {
                    System.err.println("Connection refused. You need to initiate a server first.");
            }
                // catch ( ClassNotFoundException e ) {
                //         System.err.println("Class not found");
                // }
                catch(UnknownHostException unknownHost){
                System.err.println("You are trying to connect to an unknown host!");
            }
                catch(IOException ioException){
                ioException.printStackTrace();
            }
                finally{
                //Close connections
                try{
                    in.close();

                    out.close();
                    requestSocket.close();
                }
                catch(IOException ioException){
                    ioException.printStackTrace();
                }
            }
            };
    }).start();
    }
    void connectpeer(){
        try{
        ServerSocket peerlistener = new ServerSocket(pPort);
        while(true){
            PeerHandler ph = new PeerHandler();
            Socket s = peerlistener.accept();
            ph.set(s, map1);
        }}
        catch(IOException ex){
            //
        }
    }


void sendMessage(String msg)
{
    try{
        //stream write the message
        out.writeObject(msg);
        out.flush();

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

}
