
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;




/**
 * A class to create a GUI for the Service-oriented architecture used to communicate
 * with an SRV-1 robot and Sun SPOT devices.
 * @author David Dixon
 */
public class  Console extends JFrame implements ActionListener, Runnable {

	/** Tabbed panel of GUI */
	private JTabbedPane tabbedPane = new JTabbedPane();
	/** Main tab used for connection to robot, starting of system and displaying images */
	private JPanel mainTab = new JPanel();
	/** Tab used to configure initial patrol */
	private JPanel patrolTab = new JPanel();
	/** Tab used to configure routes */
	private JPanel routeTab = new JPanel();
	/** Tab used to configure services */
	private JPanel serviceTab = new JPanel();
	/** Tab used to configure Sun SPOT's and pair them to a route and service */
	private JPanel spotTab = new JPanel();
	/** Tab containing useful info */
	private JPanel faqTab = new JPanel();

	/** Text field to input SRV-1 robot IP address */
	private JTextField ipBox = new JTextField("169.254.0.10");
	/** Text field to input SRV-1 robot port */
	private JTextField portBox = new JTextField("10001");
	/** Connect to SRV-1 robot using ipBox and portBox values */
	private JButton connect = new JButton("Connect");
	/** Images received are displayed here */
	private JPanel imageStream = new JPanel(new BorderLayout());
	/** Default image used before any are received */
	private ImageIcon image = new ImageIcon("Y:\\My Documents\\eclipse\\FYP\\images\\default.jpeg");
	/** Label containing image */
	private JLabel imageLabel = new JLabel("", image, JLabel.CENTER);
	/** Default value for availablePatrolMain */
	private String[] patrolListMain = {"Select a patrol route"};
	/** Used to select initial survey program to use */
	private JComboBox<String> availablePatrolMain = new JComboBox<String>(patrolListMain);
	/** Default value for availableSpots  */
	private String[] availSpotsList = {"Select configured Sun SPOT to add"};
	/** Contains all configured spots that are matched with a route and service) */
	private JComboBox<String> availableSpots = new JComboBox<String>(availSpotsList);
	/** Add a configured Sun SPOT to the system */
	private JButton addSPOT = new JButton("Add Selected Sun SPOT");
	/** Start the service-oriented system */
	private JButton start = new JButton("Start System!");
	/** The amount of configured Sun SPOT's used in the system */
	private JLabel activeSpots = new JLabel("Active Sun SPOTs: 0");
	/** ScrollPane used to the prevent the status updates going off screen */
	private JScrollPane statusScroll = null;
	/** Provides updates on what the application is doing and useful feedback  */
	private JTextArea status = new JTextArea("Status: Waiting for connection\n",4,1);

	/** Text area to modify and display patrol code */
	private JTextArea patrolCode = new JTextArea("Enter patrol code");
	/** Clears the text area */
	private JButton newPatrol = new JButton("Clear Text");
	/** Loads patrol code from file */
	private JButton loadPatrol = new JButton("Load from file");
	/** Save patrol code to file */
	private JButton savePatrol = new JButton("Save Patrol");
	/** Default value for availablePatrol */
	private String[] patrolList = {"Select a patrol"};
	/** Displays all saved patrols and can load them by selecting */
	private JComboBox<String> availablePatrol = new JComboBox<String>(patrolList);


	/** Text area to modify and display route code */
	private JTextArea routeCode = new JTextArea("Enter route code");
	/** Clears the text area */
	private JButton newRoute = new JButton("Clear Text");
	/** Loads route code from file */
	private JButton loadRoute = new JButton("Load from file");
	/** Save route code to file */
	private JButton saveRoute = new JButton("Save Route");
	/** Default value for availableRoute */
	private String[] routesList = {"Select a route"};
	/** Displays all saved routes and can load them by selecting */
	private JComboBox<String> availableRoutes = new JComboBox<String>(routesList);


	/** Text are to modify and display service code */
	private JTextArea serviceCode = new JTextArea("Enter service code");
	/** Clears the text area */
	private JButton newService = new JButton("Clear Text");
	/** Loads service from file */
	private JButton loadService = new JButton("Load from file");
	/** Save service to file */
	private JButton saveService = new JButton("Save Service");
	/** Default value for availableServices */
	private String[] serviceList = {"Select a service"};
	/** Displays all saved services and can load them by selecting */
	private JComboBox<String> availableServices = new JComboBox<String>(serviceList);

	/** Find all nearby Sun SPOT devices */
	private JButton findSpots = new JButton("Click to search for nearby Sun SPOTs...");
	/** Display the MAC addresses of found Sun SPOT devices */
	private JTextArea spotsField = new JTextArea("Detected sun SPOTs");
	/** Default value for detectedSpots */
	private String[] spotList = {"Select a sun SPOT"};
	/** Select a Sun SPOT to be configured */
	private JComboBox<String> detectedSpots = new JComboBox<String>(spotList);
	/** Default value for availableRoutesSpotTab */
	private String[] routesListSpotTab = {"Select a route"};
	/** Select a route to be matched to Sun SPOT */
	private JComboBox<String> availableRoutesSpotTab = new JComboBox<String>(routesListSpotTab);
	/** Default value for availableServicesSpotTab */
	private String[] serviceListSpotTab = {"Select a service"};
	/** Select a service to be matched to Sun SPOT */
	private JComboBox<String> availableServicesSpotTab = new JComboBox<String>(serviceListSpotTab);
	/** Save the C code from route, service and create reverse route from both. Matched to selected
	 *  Sun SPOT device */
	private JButton saveSpot = new JButton("Save Configured SPOT");

	/** Text area displaying useful info about the application */
	private JTextArea faqText = new JTextArea();
	/** Scroll to prevent text going out of bounds */
	private JScrollPane faqScroll = null;

	/** Connection to the SRV-1 robot */
	private SRVConnection connection = null;
	/** Manages communication with Sun SPOT devices via basestation */
	private SunSpotManager sm = null;
	/** Default location of file browser */
	private JFileChooser chooser= new JFileChooser("Y:\\My Documents\\eclipse\\FYP");
	/** Sets the default program type to C files */
	private FileNameExtensionFilter filter = new FileNameExtensionFilter("c files", "c");
	/** Reader for reading files */
	private BufferedReader reader;
	/** Contains the addresses of all Sun SPOT's in range */
	private HashMap<String, Integer> foundSpots = new HashMap<String, Integer>();
	/** List of configured Sun SPOT's used in the system */
	private ArrayList<String> configSpotsUsed = new ArrayList<String>();
	/** Count of configured Sun SPOT's used in the system */
	private int spotCount = 0;
	/** True for system currently running */
	private boolean systemRunning = false;




	/**
	 * GUI constructor
	 */
	public Console(){
		this.setTitle("SRV-1 and Sun SPOT Console");
		this.setSize(450,600);
		this.setContentPane(tabbedPane);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createRibbon();

		spotsField.setEditable(false);
		status.setEditable(false);
		faqText.setEditable(false);
		start.setEnabled(false);
		chooser.setFileFilter(filter);

		connect.addActionListener(this);
		addSPOT.addActionListener(this);
		start.addActionListener(this);
		newPatrol.addActionListener(this);
		savePatrol.addActionListener(this);
		loadPatrol.addActionListener(this);
		availablePatrol.addActionListener(this);
		newRoute.addActionListener(this);
		saveRoute.addActionListener(this);
		loadRoute.addActionListener(this);
		availableRoutes.addActionListener(this);
		newService.addActionListener(this);
		loadService.addActionListener(this);
		saveService.addActionListener(this);
		availableServices.addActionListener(this);
		findSpots.addActionListener(this);
		saveSpot.addActionListener(this);

		try {
			sm = new SunSpotManager();
		} catch (IOException e) {
			status.append("Status: Sun SPOT IO error\n");
		} catch (IllegalArgumentException e){
			status.append("Status: Sun SPOT error, COM port unavailable\n");
		}
	}



	/* 
	 * Used when system is running to decide the programs to be executed on the SRV-1
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		HashMap<String, Integer> newValue;

		while (systemRunning){
			image = new ImageIcon(connection.getLastImage());
			imageLabel.setIcon(image);
			
			//Deploy the patrol program which is ran until a Sun SPOT requests a service
			//via the accelerometer values
			connection.deployPatrol((String)availablePatrolMain.getSelectedItem());
			
			
			//Wait until robot enters checkpoint/start position and wants to know
			//if a Sun SPOT requires a service
			while (!connection.getServiceRequested()){
				
			}
			
			//Acknowledge service request
			connection.setServiceRequested(false);
			
			//Test if accelerometer values have changed
			newValue = new HashMap<String, Integer>(sm.getAddresses());
			for (Map.Entry<String, Integer> entry: foundSpots.entrySet()){ 
				if (configSpotsUsed.contains(entry.getKey())){
					int diff = newValue.get(entry.getKey()) - entry.getValue();
					if (diff >=2 || diff <= -2){
						foundSpots.putAll(newValue);
						//Change has been detected so deploy route and service to Sun SPOT
						goToSpot(entry.getKey());
					}
				}	
			}
			//wait at start position for 10 seconds
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				status.append("Status: Thread interrupted, stopping system");
				stopSystem();
			}
		}
	}


	/**
	 * Deploy code to execute service at Sun SPOT location then return to start position
	 * @param sunSpotAddr
	 */
	public void goToSpot(String sunSpotAddr){
		status.append("Status: Deploying service at "+sunSpotAddr+"\n");
		connection.writeByte((byte)'#');	//turn on laaer
		connection.readByte();
		connection.readByte();
		connection.writeByte((byte)'l');
		connection.readByte();
		connection.readByte();
		connection.deployService(sunSpotAddr);
		connection.writeByte((byte)'#');	//turn off laser
		connection.readByte();
		connection.readByte();
		connection.writeByte((byte)'L');
		connection.readByte();
		connection.readByte();
		status.append("Status: SRV-1 returned to checkpoint\n");
	}



	/* 
	 * Detect events in the GUI
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == connect){
			connect();
		}
		else if (e.getSource() == addSPOT){
			addSPOT();						
		}
		else if ((e.getSource() == start) && (!systemRunning)){
			startSystem();
		}
		else if ((e.getSource() == start) && (systemRunning)){
			stopSystem();
		}
		else if (e.getSource() == newPatrol){
			patrolCode.setText("");
		}
		else if (e.getSource() == savePatrol){	
			savePatrol();
		}
		else if (e.getSource() == loadPatrol){			
			loadPatrol();
		}
		else if (e.getSource() == availablePatrol){
			availablePatrol();
		}
		else if (e.getSource() == newRoute){
			routeCode.setText("");
		}
		else if (e.getSource() == saveRoute){	
			saveRoute();
		}
		else if (e.getSource() == loadRoute){			
			loadRoute();
		}
		else if (e.getSource() == availableRoutes){
			availableRoutes();
		}
		else if (e.getSource() == newService){
			serviceCode.setText("");
		}
		else if (e.getSource() == loadService){
			loadService();
		}
		else if (e.getSource() == saveService){	
			saveService();
		}
		else if (e.getSource() == availableServices){
			availableServices();
		}
		else if (e.getSource() == findSpots) {
			findSpots();
		}
		else if (e.getSource() == saveSpot){
			saveSpot();					
		}
	}

	/**
	 * Connect to the SRV-1 robot
	 */
	private void connect(){
		if (connect.getText().equals("Connect")){
			connection = new SRVConnection(ipBox.getText(), portBox.getText());
			if (connection.connected()){
				connect.setText("Disconnect");
				start.setEnabled(true);
				status.append("Status: Connected\n");
			}else{
				status.append("Status: Error connecting\n");
			}
		}
		//Disconnect
		else{
			connection.disconnect();
			connect.setText("Connect");
			start.setEnabled(false);
			status.append("Status: Disconnected\n");
		}
	}


	/**
	 * Add a configured Sun SPOT to be used when system is active
	 */
	private void addSPOT(){
		if (!availableSpots.getSelectedItem().toString().equals("Select configured Sun SPOT to add")){
			configSpotsUsed.add("" +availableSpots.getSelectedItem());
			spotCount++;
			activeSpots.setText("Active Sun SPOTs: "+spotCount);
			status.append("Status: Added spot device\n");	
		}else{
			status.append("Status: Incorrect Sun SPOT selection\n");
		}
	}

	/**
	 * Start the service-oriented based system
	 */
	private void startSystem(){	
		if (!availablePatrolMain.getSelectedItem().toString().equals("Select a patrol")){
			systemRunning = true;
			start.setText("Stop System and reset spots used!");
			
			//thread used to handle communcation with robot, allows GUI buttons to still
			//be used. See run()
			Thread t = new Thread(this);
			t.start();
			status.append("Status: System started\n");
		}else{
			status.append("Status: select a patrol and Sun SPOT devices to start\n");
		}
	}


	/**
	 * Stop the system from running, it will finish executing the current service
	 * before returning to base
	 */
	private void stopSystem(){
		systemRunning = false;
		configSpotsUsed.clear();
		spotCount = 0;
		start.setText("Start System!");
		status.append("Status: System stopped\n");
	}


	/**
	 * Save the patrol code to file
	 */
	private void savePatrol(){
		String patrol = patrolCode.getText();
		int returnVal = chooser.showOpenDialog(Console.this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e1) {
					status.append("Status: IO Error creating patrol\n");
				}
			}
			try {
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(patrol);
				bw.close();
			} catch (IOException e1) {
				status.append("Status: IO error writing patrol\n");
			}
			availablePatrol.addItem(file.getName());
			availablePatrolMain.addItem(file.getName());				
		}
	}


	/**
	 * Load a C program from file containing patrol code
	 */
	private void loadPatrol(){
		int returnVal = chooser.showOpenDialog(Console.this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
				patrolCode.setText("");
				String line = reader.readLine();
				while ( line != null){
					patrolCode.append(line);
					patrolCode.append("\n");
					line = reader.readLine();
				}
				reader.close();
			} catch ( IOException e1) {
				status.append("Status: IO error loading patrol\n");
			}
		}
	}

	/**
	 * Load selected file to text box from dropdown menu
	 */
	private void availablePatrol(){
		if (!(availablePatrol.getSelectedItem() == patrolList[0])){
			File file = new File("Y:\\My Documents\\eclipse\\FYP\\patrol\\" + availablePatrol.getSelectedItem());
			try {
				reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
				patrolCode.setText("");
				String line = reader.readLine();
				while ( line != null){
					patrolCode.append(line);
					patrolCode.append("\n");
					line = reader.readLine();
				}
				reader.close();
			} catch ( IOException e1) {
				status.append("Status: IO error loading patrol\n");
			}
		}
	}



	/**
	 * Save route code in textbox to C file
	 */
	private void saveRoute(){
		String route = routeCode.getText();
		int returnVal = chooser.showOpenDialog(Console.this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();

			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e1) {
					status.append("Status: IO error creating route\n");
				}
			}
			try {
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(route);
				bw.close();
			} catch (IOException e1) {
				status.append("Status: IO error writing route\n");
			}
			availableRoutes.addItem(file.getName());
			availableRoutesSpotTab.addItem(file.getName());				
		}
	}


	/**
	 * Load a C file containing a route
	 */
	private void loadRoute(){
		int returnVal = chooser.showOpenDialog(Console.this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
				routeCode.setText("");
				String line = reader.readLine();
				while ( line != null){
					routeCode.append(line);
					routeCode.append("\n");
					line = reader.readLine();
				}
				reader.close();
			} catch ( IOException e1) {
				status.append("Status: IO error loading route\n");
			}
		}
	}

	/**
	 * Select a saved route from drop down menu and load it
	 */
	private void availableRoutes(){
		if (!(availableRoutes.getSelectedItem() == routesList[0])){
			File file = new File("Y:\\My Documents\\eclipse\\FYP\\routes\\" + availableRoutes.getSelectedItem());
			try {
				reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
				routeCode.setText("");
				String line = reader.readLine();
				while ( line != null){
					routeCode.append(line);
					routeCode.append("\n");
					line = reader.readLine();
				}
				reader.close();
			} catch ( IOException e1) {
				status.append("Status: IO error loading route\n");
			}
		}
	}


	/**
	 * Load a C program containing a service from file
	 */
	private void loadService(){
		int returnVal = chooser.showOpenDialog(Console.this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
				serviceCode.setText("");
				String line = reader.readLine();
				while ( line != null){
					serviceCode.append(line);
					serviceCode.append("\n");
					line = reader.readLine();
				}
				reader.close();
			} catch ( IOException e1) {
				status.append("Status: IO error loading service\n");
			}
		}	
	}


	/**
	 * Save the C code in the service text box to a file
	 */
	private void saveService(){
		String service = serviceCode.getText();
		int returnVal = chooser.showOpenDialog(Console.this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e1) {
					status.append("Status: IO error saving service\n");
				}
			}
			try {
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(service);
				bw.close();
			} catch (IOException e1) {
				status.append("IO error writing service\n");
			}
			availableServices.addItem(file.getName());
			availableServicesSpotTab.addItem(file.getName());
		}
	}

	/**
	 * Select a already saved service and load it
	 */
	private void availableServices(){
		if (!(availableServices.getSelectedItem() == serviceList[0])){
			File file = new File("Y:\\My Documents\\eclipse\\FYP\\services\\" + availableServices.getSelectedItem());
			try {
				reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
				serviceCode.setText("");
				String line = reader.readLine();
				while ( line != null){
					serviceCode.append(line);
					serviceCode.append("\n");
					line = reader.readLine();
				}
				reader.close();
			} catch ( IOException e1) {
				status.append("Status: IO error loading service\n");
			}
		}
	}

	/**
	 * Search for nearby Sun SPOT's and display their MAC address as well as adding
	 * them to the drop down box
	 */
	private void findSpots(){
		//Start the Sun SPOT manager if it isn't already, thread continues to receive
		//accelerometer values from nearby Sun SPOTs
		if (!sm.isRunning()){
			Thread t = new Thread(sm);
			t.start();
		}		
		foundSpots = new HashMap<String, Integer>(sm.getAddresses());
		spotsField.setText("The following sun SPOT devices were found:\n");

		//Add the MAC addresses to text field
		for (String key:foundSpots.keySet()){
			spotsField.append(key);
			spotsField.append("\n");
			detectedSpots.addItem(key);
		}
	}


	/**
	 * Save a C file containing the route, service, and reverse route to a file based on the
	 * drop down menus. This file is then linked to the selected Sun SPOT device also selected
	 * with the drop down menu.
	 */
	private void saveSpot(){
		//Check valid selections
		if (!(detectedSpots.getSelectedItem()==spotList[0]) 
				&& !(availableRoutesSpotTab.getSelectedItem()==routesListSpotTab[0]) 
				&& !(availableServicesSpotTab.getSelectedItem()==serviceListSpotTab[0])){
			
			File tempRoute = new File("Y:\\My Documents\\eclipse\\FYP\\routes\\" + availableRoutesSpotTab.getSelectedItem());
			File tempService = new File("Y:\\My Documents\\eclipse\\FYP\\services\\" + availableServicesSpotTab.getSelectedItem());
			File savedCode = new File("Y:\\My Documents\\eclipse\\FYP\\spots\\" + detectedSpots.getSelectedItem()+".c");
			//Array to contain route created to return to start position
			String[] reverseRoute = new String[1000];
			//Remembers position in reverseRoute
			int reverseRoutePos = 0;
			//"exit(1)" has to be reposition after the return route instead of service
			String exitStr = null;
			
			if (!savedCode.exists()) {
				try {
					savedCode.createNewFile();
				} catch (IOException e1) {
					status.append("Status: IO error saving spot config\n");
				}
			}
			try {
				//write route to file
				FileWriter fw = new FileWriter(savedCode.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				reader = new BufferedReader(new FileReader(tempRoute.getAbsolutePath()));				
				String line = reader.readLine();
				
				while ( line != null){
					//check if motor() or delay() command to add to reverse route
					if (line.contains("motors")){
						reverseRoute[reverseRoutePos] = line.replaceAll("\\d+", "-$0");
						reverseRoutePos++;
					}
					if (line.contains("delay")){
						reverseRoute[reverseRoutePos] = line;
						reverseRoutePos++;
					}
					bw.write(line);
					bw.write("\n");
					line = reader.readLine();
				}
				reader.close();
				bw.close();

				//write service to file
				fw = new FileWriter(savedCode.getAbsoluteFile(),true);
				bw = new BufferedWriter(fw);
				reader = new BufferedReader(new FileReader(tempService.getAbsolutePath()));
				line = reader.readLine();
				while ( line != null){
					//check if motor() or delay() command to add to reverse route
					if (line.contains("motors")){
						reverseRoute[reverseRoutePos] = line.replaceAll("\\d+", "-$0");
						reverseRoutePos++;
					}
					if (line.contains("delay")){
						reverseRoute[reverseRoutePos] = line;
						reverseRoutePos++;
					}
					//grab exit() line for later
					if (line.contains("exit")){
						exitStr = line;
					}else{
						bw.write(line);
						bw.write("\n");	
					}
					line = reader.readLine();
				}
				reader.close();

				//Test if last line was motors(0,0) or if a delay followed it
				reverseRoutePos--;
				if (reverseRoute[reverseRoutePos].contains("motors")){
					reverseRoutePos--;
					reverseRoutePos--;
				}else{
					reverseRoutePos--;
				}

				//write reverse route to file
				while (reverseRoutePos >=0){
					bw.write(reverseRoute[reverseRoutePos]);
					bw.write("\n");
					reverseRoutePos++;
					bw.write(reverseRoute[reverseRoutePos]);
					bw.write("\n");
					reverseRoutePos--;
					reverseRoutePos--;
					reverseRoutePos--;	
				}
				//ensure SRV-1 stops and program exits
				bw.write("motors(0,0);");
				bw.write("\n");
				bw.write(exitStr);
				bw.write("\n");

				bw.close();
				availableSpots.addItem(detectedSpots.getSelectedItem().toString());

			} catch ( IOException e1) {
				status.append("Status: IO error writing spot config\n");
			}	
		}
	}


	/**
	 * Create the tabbed GUI
	 * @return tabbedPane containing the GUI elements
	 */
	private JTabbedPane createRibbon(){
		createMainPanel();
		createPatrolPanel();
		createRoutePanel();
		createServicePanel();
		createSpotPanel();
		createFaqPanel();
		tabbedPane.addTab("Main", mainTab);
		tabbedPane.addTab("Patrol", patrolTab);
		tabbedPane.addTab("Routes", routeTab);
		tabbedPane.addTab("Services", serviceTab);
		tabbedPane.addTab("Sun SPOTs",spotTab);
		tabbedPane.addTab("FAQ", faqTab);
		return tabbedPane;
	}

	/**
	 * Modify the main panel
	 */
	private void createMainPanel(){
		mainTab.setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		JPanel middlePanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JPanel splitPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.PAGE_AXIS));
		statusScroll = new JScrollPane(status);
		mainTab.add(topPanel, BorderLayout.NORTH);
		mainTab.add(middlePanel, BorderLayout.CENTER);
		mainTab.add(bottomPanel, BorderLayout.SOUTH);
		topPanel.add(ipBox);
		topPanel.add(portBox);
		topPanel.add(connect);
		imageStream.add( imageLabel, BorderLayout.CENTER );
		middlePanel.add(imageStream);
		middlePanel.setBorder(BorderFactory.createLineBorder(Color.black));
		bottomPanel.add(availablePatrolMain);
		bottomPanel.add(splitPanel);
		splitPanel.add(availableSpots);
		splitPanel.add(addSPOT);
		bottomPanel.add(start);
		bottomPanel.add(activeSpots);
		bottomPanel.add(statusScroll);
	}


	/**
	 * Modify the patrol panel
	 */
	private void createPatrolPanel(){
		patrolTab.setLayout(new BorderLayout());
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(4,1));
		patrolTab.add(patrolCode, BorderLayout.CENTER);
		patrolTab.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.add(newPatrol);
		bottomPanel.add(loadPatrol);
		bottomPanel.add(savePatrol);
		bottomPanel.add(availablePatrol);
	}

	/**
	 * Modify the route panel
	 */
	private void createRoutePanel(){
		routeTab.setLayout(new BorderLayout());
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(4,1));
		routeTab.add(routeCode, BorderLayout.CENTER);
		routeTab.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.add(newRoute);
		bottomPanel.add(loadRoute);
		bottomPanel.add(saveRoute);
		bottomPanel.add(availableRoutes);

	}

	/**
	 * Modify the service panel
	 */
	private void createServicePanel(){
		serviceTab.setLayout(new BorderLayout());
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(4,1));
		serviceTab.add(serviceCode, BorderLayout.CENTER);
		serviceTab.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.add(newService);
		bottomPanel.add(loadService);
		bottomPanel.add(saveService);
		bottomPanel.add(availableServices);
	}

	/**
	 * Modify the Sun SPOT panel
	 */
	private void createSpotPanel(){
		spotTab.setLayout(new BorderLayout());
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(4,1));
		spotTab.add(findSpots, BorderLayout.NORTH);
		spotTab.add(spotsField, BorderLayout.CENTER);
		spotTab.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.add(detectedSpots);
		bottomPanel.add(availableRoutesSpotTab);
		bottomPanel.add(availableServicesSpotTab);
		bottomPanel.add(saveSpot);
	}


	/**
	 * Modify the FAQ panel
	 */
	private void createFaqPanel(){
		faqText.setText("What does this application do?\n\n"
				+ "This application works with the SRV-1 robot and Sun SPOT devices to create"
				+ "\n a service oriented architecture. This works by creating a route to a Sun SPOT"
				+ "\n device and matching this to a service and the SPOT device. When changes\n in the"
				+ " accelerometer are detected the SRV-1 will then go to the Sun SPOT, "
				+ "\n and execute the service then return route for the Sun SPOT."
				+ "\n\nWhat is a patrol?\n\n"
				+ "A patrol is the initial route the SRV-1 is patrolling before returning\n"
				+ " to the start point and checking if a new service is required.\n\n"
				+ "What is a route?\n\n"
				+ "A route is the path the SRV-1 follows to get to a Sun SPOT\n\n"
				+ "What is a service?\n\n"
				+ "A service is a program executed at the Sun SPOT's location, such as\n"
				+ " taking a screenshot.\n\n"
				+ "TIPS:\n -Follow all motor() commands by a delay() command\n "
				+ " -Use\n     \" printf(\"##IMJ5%c%c%c%c\", size & 0x000000FF, (size & 0x0000FF00)\n "
						+ "   / 0x100, (size & 0x00FF0000) / 0x10000, 0);\" \nbefore vsend() command."
				+ "\n -Avoid loops containing motors() and delay() commands\n"
				+ "-Ensure exit(1) is placed at the end of the service\n"
				+ "-Use \"printf(\"##requestService\\n\");\" at the end of patrol before\n"
				+ "calling exit(1);");
		faqTab.setLayout(new GridLayout(1,1));
		faqScroll = new JScrollPane(faqText);
		faqTab.add(faqScroll);

	}

}
