
import java.io.IOException;
import java.util.HashMap;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.ota.OTACommandServer;



/**
 * A class to communicate with Sun SPOT devices in range and receive their accelerometer values
 * via the basestation
 * @author David Dixon
 *
 */
public class SunSpotManager implements Runnable{

	/** Port the basestation and Sun SPOT device communicate via  */
	private static final int HOST_PORT = 67;
	/** Connection between between basestation and Sun SPOT  */
	private RadiogramConnection rgConnection;
	/** Data packet received */
	private Datagram datagram;
	/** Stores the addresses(MAC) of nearby devices */
	private HashMap<String, Integer> addressValues = new HashMap<String, Integer>();
	/** Changed to true first time run() is called to prevent multiple threads */
	private boolean isListening = false;


	/**
	 * Constructor, sets the serial port used and sets up neccessary communication values
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public SunSpotManager() throws IOException, IllegalArgumentException{
		System.setProperty("SERIAL_PORT", "COM5");
		OTACommandServer.start("FYP");
		rgConnection = (RadiogramConnection)Connector.open("radiogram://:" + HOST_PORT);
		datagram = rgConnection.newDatagram(rgConnection.getMaximumLength());
	}

	/* 
	 * Loops continuously retrieving accelerometer values from nearby devices
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		isListening = true;
		while(true){
			try{
				rgConnection.receive(datagram);
				String address = datagram.getAddress();
				int valueX = datagram.readInt();
				addressValues.put(address, valueX);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}



	/**
	 * @return all address values of Sun SPOTs in range
	 */
	public HashMap<String, Integer> getAddresses(){
		return addressValues;
	}

	/**
	 * @return if a listening is thread is already running
	 */
	public boolean isRunning(){
		return isListening;
	}



}
