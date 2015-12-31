import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class PiClient implements Runnable{

	Socket connection;
	ObjectOutputStream output;
	ObjectInputStream input;
	DataInputStream dinput;
	Scanner scan = new Scanner(System.in);
	String message,ip;
	boolean connection_success = false;
	
	public PiClient(){
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println(">Enter server ip");
		ip = scan.nextLine();
		while(true){
			System.out.println(">Attempting connection");
			connectToServer(120);
			try {
				setupStreams();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(">Error while setting up streams");
			}
			connected();
		}
	}
	
	private void connectToServer(int attempts){
		// TODO Auto-generated method stub
		if(attempts>0){
			try{
				connection = new Socket(InetAddress.getByName(ip),6789); //manifestation of the connection; attempts to connect to given ip with given port
				System.out.println(">Connected to server, please wait");
			}catch(Exception e){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				connectToServer(attempts-1);
			}
		}
		else{
			System.out.println(">Connection timeout");
			System.exit(0);
		}
	}
	
	private void setupStreams() throws IOException {
		// TODO Auto-generated method stub
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		dinput = new DataInputStream(connection.getInputStream());	
		System.out.println(">Streams established, now ready for commands"); //sets up streams
	}
	
	private void connected(){

		
		while(true){ //output thread
			String us = scan.nextLine();
			if(us.equals("give")){
				giveProtocal();
			}
			else if(us.equals("get")){ //gets file from server
				getProtocal();
			}
			else if(us.equals("list")){
				listProtocal();
			}
			else if(us.length()!=0&&!us.equals(null)){
				sendMessage(us);
			}
		}
		
	}
	
	private void listProtocal() {
		// TODO Auto-generated method stub
		sendMessage("list");
	}

	private void getProtocal() {
		// TODO Auto-generated method stub
		sendMessage("get"); //sends get request
		System.out.println("Enter file name and extension");
		String p = scan.nextLine();
		sendMessage(p); //gets & sends file name
		int length = 0;
		try {
			length = Integer.parseInt(((String)input.readObject())); //reads file length
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Receiving file with length of " + length);
		if(length>0){
			byte[] data = new byte[length];
			try {
				dinput.readFully(data,0,length);
				createFile(p,data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("File received!");
	}

	private void giveProtocal() {
		// TODO Auto-generated method stub
		String filename;
		sendMessage("give"); //sends give request or notification if you want to call it that
		System.out.println("Enter file name & extension: ");
		filename = scan.nextLine();
		sendMessage(filename); //sends file name to server
		File file = new File(filename);
		byte[] filedata = new byte[(int) file.length()]; //byte array of file data
		FileInputStream fs;
		
		try {
			fs = new FileInputStream(file);
			fs.read(filedata); //attempts to read file data and store in byte array
			fs.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendMessage(filedata); //sends file data to server
	}

	protected void createFile(String name, byte[] data) throws IOException {
		// TODO Auto-generated method stub
		FileOutputStream fos = new FileOutputStream(name);
		fos.write(data);
		fos.close();
	}

	@SuppressWarnings("unused")
	private void closeAll() {
		// TODO Auto-generated method stub
		System.out.println("CLOSING...");
		try{
			output.close();
			input.close();
			connection.close(); //closes streams and actual connection
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void sendMessage(String m){
		try {
			output.writeObject(m);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(">Error sending data");
		}
	}
	
	private void sendMessage(byte[] b){
		try {
			output.writeObject(b);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(">Error sending data");
		}
	}
	
}
