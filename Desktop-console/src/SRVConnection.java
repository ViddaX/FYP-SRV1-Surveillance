import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A class to handle the connection and sending/receiving of data between
 * the server and SRV-1 robot.
 * @author David Dixon
 */
public class SRVConnection {

	/** Connection between server and SRV-1 robot */
	private Socket socket;
	/** Output stream to send data */
	private DataOutputStream output;
	/** Input stream to receive data */
	private DataInputStream input;
	/** Used to store received data, needs to be high for large images */
	private byte[] buf = new byte[145000];
	/** Count of images received, set to 1 as the default image is counted */
	private int imageCount = 1;
	/** Location of saved received images */
	private String[] receivedImages = new String[1000];
	/** The location of the last image received, default image is used */
	private String lastImageLocation= "Y:\\My Documents\\eclipse\\FYP\\images\\default.jpeg";
	/** True when SRV-1 needs a new service */
	private boolean serviceRequested = false;
	
	/**
	 * Constructor to create connection and set up data streams
	 * @param ip SRV-1 address
	 * @param port SRV-1 port used
	 */
	public SRVConnection(String ip, String port){
		InetAddress addr = null;
		int portA = Integer.parseInt(port);
		try {
			addr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		try {
			socket = new Socket(addr, portA);
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
		}
		receivedImages[0] = "Y:\\My Documents\\eclipse\\FYP\\survey\\default.jpeg";
	}


	/**
	 * Sends the patrol code to the SRV-1
	 * @param patrol The selected patrol code to use
	 */
	public void deployPatrol(String patrol){
		File file = new File("Y:\\My Documents\\eclipse\\FYP\\patrol\\"+patrol);
		//ASCII value representing escape character
		String escStr = "1B";
		//Value to clear the flash buffer
		String clearBuf = "zc";
		//Byte version of escape character
		byte esc = (byte)Integer.parseInt(escStr, 16);	

		//Attempt to send the patrol
		try {
			//clear the flash buffer
			output.writeBytes(clearBuf);
			//handle the response
			handleReceive();
			//Enter line editor mode, allowing the C program to be wrote
			output.writeByte('E');
			handleReceive();
			//Get the C file
			try {
				Thread.sleep(100);
				BufferedReader reader=null;
				try {
					reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				String line;
				line = reader.readLine();
				while ( line != null){
					//Send value to 'Input' 
					output.writeByte('I');
					handleReceive();
					byte[] lineData = line.getBytes();
					//send the line of code and a newline character
					output.write(lineData);
					output.writeByte('\n');
					//escape to save line and return to editor menu
					output.writeByte(esc);
					handleReceive();
					line = reader.readLine();
				}
				//Exit the editor menu
				output.writeByte('X');
				handleReceive();
				//Execute the program stored in the flash buffer
				output.writeByte('Q');
				//Handle "printf ("##req.." and "exit(1)"
				handleReceive();
				handleReceive();
				Thread.sleep(100);
				reader.close();
			} catch (InterruptedException e3) {
				e3.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * Deploy the requested service to the SRV-1 robot
	 * @param spotAddress
	 */
	public void deployService(String spotAddress){
		File file = new File("Y:\\My Documents\\eclipse\\FYP\\spots\\"+spotAddress+".c");

		//ASCII value representing escape character
		String escStr = "1B";
		//Value to clear the flash buffer
		String clearBuf = "zc";
		//Byte version of escape character
		byte esc = (byte)Integer.parseInt(escStr, 16);			

		try {
			//clear the flash buffer
			output.writeBytes(clearBuf);
			//handle the response
			handleReceive();
			//Enter line editor mode, allowing the C program to be wrote
			output.writeByte('E');
			handleReceive();
			//Get the C file
			try {
				Thread.sleep(100);
				BufferedReader reader=null;
				try {
					reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				String line;
				line = reader.readLine();
				while ( line != null){
					//Send value to 'Input' 
					output.writeByte('I');
					handleReceive();
					byte[] lineData = line.getBytes();
					//send the line of code and a newline character
					output.write(lineData);
					output.writeByte('\n');
					//escape to save line and return to editor menu
					output.writeByte(esc);
					handleReceive();
					line = reader.readLine();
				}
				//Exit the editor menu
				output.writeByte('X');
				handleReceive();
				//Execute the program stored in the flash buffer
				output.writeByte('Q');
				//Handle "exit(1)" or image received, which will handle future images or exit(1)
				handleReceive();
				Thread.sleep(100);
				reader.close();
			} catch (InterruptedException e3) {
				e3.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Handle received data from SRV-1
	 */
	public void handleReceive(){
		try {
			//Get the first two chars and check what they are to decide action
			input.readFully(buf, 0, 2);	
			if ((char)buf[0] == '#'){
				if((char)buf[1]=='#'){
					//Get the next 3 chars
					input.readFully(buf,2,3);
					//clear the flash buffer
					if(((char)buf[2]=='z') && ((char)buf[3]=='c')){	
						handleZC();
					}
					//image received
					else if(((char)buf[2]=='I') &&((char)buf[3]=='M')){	
						handleImg();
					}
					//"##request service" received
					else if((char)buf[2]=='r' && (char)buf[3]=='e'){
						handleServiceRequest();
					}else{
						//should never happen but attempt to handle
						handleMisc();
					}
				}
			}
			//return from 'E' command
			else if(((char)buf[0]== '(') && ((char)buf[1]== 'T')) { 
				handleLineEditor();
			}
			//return at exit()
			else if ((char)buf[0]=='e' && ((char)buf[1]== 'x')){	
				handleExit();
			}
			//Image received without header, shouldn't happen but handle if erroroneously done
			else if((buf[0]==-1) && buf[1]==(byte)216){
				handleImg();
			}
			//Handle misc things such as "*"
			else{
				handleMisc();
			}
		} catch (EOFException e){
			e.printStackTrace();
			return;	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handle SRV-1 wanting a new service
	 * @throws IOException
	 */
	private void handleServiceRequest() throws IOException{
		while(true){
			//read until \n char to clear input stream
			buf[4] = input.readByte();	
			if ((char)buf[4]=='\n'){
				serviceRequested = true;
				return;
			}
		}
	}
	
	/**
	 * Handles non specific received data that can just be cleared and ignored
	 * @throws IOException
	 */
	private void handleMisc() throws IOException{
		//Should only be feedback such as "leaving editor" or 
		//"starting picooc v2.1" or "insert" or "*"
		while(true){
			buf[4] = input.readByte();	
			if ((char)buf[4]=='\n'){		
				return;
			}
		}		
	}

	/**
	 * Handle the response when exit(1) is called
	 * @throws IOException
	 */
	private void handleExit() throws IOException{
		while(true){
			buf[5] = input.readByte();	//read until \n char to clear input stream
			if ((char)buf[5]=='\n'){ 
				return;
			}
		}
	}


	/**
	 * Handle the response when line editor mode is entered via 'E'
	 * @throws IOException
	 */
	private void handleLineEditor() throws IOException{
		//returns 4 lines so need to eat 4 '\n'
		int y = 0;
		while(true){
			buf[4] = input.readByte();	
			if ((char)buf[4]=='\n'){
				y++;
			}
			if (y==3)return;
		}	
	}

	/**
	 * Handle an image received and save it to file
	 * @throws IOException
	 */
	private void handleImg() throws IOException{
		//Get current time to identify images
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("mm-HH-dd-MM-yy");
		cal.getTime();
		
		//Get the header data
		buf[4] = input.readByte();
		buf[5] = input.readByte();
		buf[6] = input.readByte();
		buf[7] = input.readByte();
		buf[8] = input.readByte();

		//Calculate the size of image, can be used to check if any data got lost
		int sizeOfImg = convertImageSize(buf[5],buf[6],buf[7],buf[8]);

		//Receive the image by getting all data until the end bytes 0xFF and 0xD9 are reached
		//indicating the end of data
		boolean receivingImage = true;
		int y = 0;
		while (receivingImage){
			buf[y] = input.readByte();
			if (buf[y]==(byte)0xFF){
				y++;
				buf[y] = input.readByte();
				if (buf[y]==(byte)0xD9){
					receivingImage = false;
				}
			}
			y++;
		}
				
		//save the image
		String imgDate = sdf.format(cal.getTime());
		OutputStream ops = null;
		try{
			ops = new BufferedOutputStream(new FileOutputStream("Y:\\My Documents\\eclipse\\FYP\\images\\image"+ imgDate + ".jpeg"));
			ops.write(buf);
		}finally{
			if (ops!=null)ops.close();
		}
		
		lastImageLocation = "Y:\\My Documents\\eclipse\\FYP\\images\\image"+ imgDate + ".jpeg";		
		receivedImages[imageCount] = lastImageLocation;
		imageCount++;
		
		//call handleReceive again as we know at least exit(1) will be called again
		handleReceive();
	}


	/**
	 * Handle response from 'zc', clear buffer
	 * @throws IOException
	 */
	private void handleZC() throws IOException{
		while(true){
			//read until \n char to clear input stream
			buf[5] = input.readByte();	
			if ((char)buf[5]=='\n'){ 
				return;
			}
		}
	}

	/**
	 * Calculates the image size from the header values
	 * @param b1 First byte
	 * @param b2 Second byte
	 * @param b3 Third byte
	 * @param b4 Fourth byte
	 * @return An int containing the size of the image(the amount of bytes to receive)
	 */
	private int convertImageSize(byte b1, byte b2, byte b3, byte b4){
		//multiply by 0xFF to ensure value is positive
		int first = (int)(b1&0xFF);
		int second = (int)(b2&0xFF)*256;
		int third = (int)(b3&0xFF)*256*256;
		int fourth = (int)(b4&0xFF)*256*256*256;

		int result = first + second + third + fourth;
		return result;
	}



	/**
	 * Checks if connection exists
	 * @return
	 */
	public boolean connected(){
		return (socket != null);
	}

	/**
	 * Close the connection between host and SRV-1
	 */
	public void disconnect(){	
		try {
			socket.shutdownInput();
			socket.shutdownOutput();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Writes a byte to stream, used for testing purposes and to switch on/off laser
	 */
	public void writeByte(byte sendByte){
		try {
			output.writeByte(sendByte);
			output.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read a byte from stream, used for testing purposes and laser response
	 */
	public void readByte(){
		byte[] buf = new byte[4];
		try {
			input.readFully(buf, 0, 1);
		} catch (EOFException e){
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	//	System.out.println("Next byte: "+ (char)buf[0]);

	}

	/**
	 * @return If the SRV-1 wants a new service yet
	 */
	public boolean getServiceRequested(){
		return serviceRequested;
	}
	
	
	/**
	 * @return The location of the last image received
	 */
	public String getLastImage(){
		return lastImageLocation;
	}

	/**
	 * @return All images received since the start of the application
	 */
	public String[] getImages(){
		return receivedImages;
	}
	
	/**
	 * Used to set the service requested back to false
	 */
	public void setServiceRequested(boolean bool){
		serviceRequested = bool;
	}
}
