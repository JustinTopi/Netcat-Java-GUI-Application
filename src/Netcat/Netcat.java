package Netcat;
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

class NetcatGUI extends JFrame {
	public JButton sendButton;
	public JTextArea rxArea, txArea;
	public JScrollPane pane;
	public Container container;

 /* This constructor adds to the content pane
  * all the components of the UI */
	public NetcatGUI(String title) {
		super(title); // invoke the JFrame constructor
		
		container = getContentPane();
		container.setLayout(new FlowLayout());
		txArea = new JTextArea(6, 40);
		rxArea = new JTextArea(6, 40);
		rxArea.setEditable(false);
		pane = new JScrollPane(rxArea);
		sendButton = new JButton("Send");
		container.add(pane);
		container.add(txArea);
		container.add(sendButton);
	}
}

public class Netcat extends NetcatGUI {
	
	// VARIABLE DECLARATION
	String inputLine, outputLine;
	String role;
	static String remoteAddr;
	static String localPort, remotePort;
	
	class NetcatStartupGUI extends JFrame {
		
		//Variable Declaration
		public JButton startButton;
		JComboBox netcatRole;
		public JTextField remotePortTextField, remoteIPTextField, localPortTextField;
		public Container container;

		public NetcatStartupGUI(String title) {

			super(title);

			String[] netcatRoleString = {
					"                                              Netcat Role:                                         ",
					"TCP Server", "TCP Client", "UDP Server", "UDP Client" };

			container = getContentPane();

			container.setLayout(new FlowLayout());

			netcatRole = new JComboBox(netcatRoleString);

			remoteIPTextField = new JTextField(40);
			remotePortTextField = new JTextField(40);
			localPortTextField = new JTextField(40);

			// Adding all new JComponents to the container
			startButton = new JButton("Start");
			container.add(new JLabel("                                                       "));
			container.add(netcatRole);
			container.add(new JLabel("Remote IP:"));
			container.add(remoteIPTextField);
			container.add(new JLabel("Remote Port:"));
			container.add(remotePortTextField);
			container.add(new JLabel("Local Port:"));
			container.add(localPortTextField);
			container.add(startButton);

			updateTextFieldStates();

			// Adding a listener to hear new netcatRole updates
			netcatRole.addActionListener(e -> updateTextFieldStates());
		}

		void updateTextFieldStates() {
			remoteIPTextField.setEnabled(false);
			remotePortTextField.setEnabled(false);
			localPortTextField.setEnabled(false);

			/* If you get selected case 1 or 3, then the local port gets enabled
			otherwise, both remote ip and port get enabled to connect as a client */
			switch (netcatRole.getSelectedIndex()) {
				case 1:
					// TCP Server
				case 3:
					// UDP Server
					localPortTextField.setEnabled(true);
					break;
				case 2:
					// TCP Client
				case 4:
					// UDP Client
					remoteIPTextField.setEnabled(true);
					remotePortTextField.setEnabled(true);
					break;
			}
		}

	}

	class NetcatStartup extends NetcatStartupGUI {

		public ButtonHandler bHandler;
		volatile boolean finished = false;
		
		/* Setups button handler when you press start */
		public NetcatStartup(String title) {

			super(title);

			bHandler = new ButtonHandler();
			startButton.addActionListener(bHandler);

		}

		private class ButtonHandler implements ActionListener {
			// When you set valid fields, then we update the
			// Netcat object properties with data found below:
			public void actionPerformed(ActionEvent event) {
				if (!isValidSelection()) return;

				role = (String) netcatRole.getSelectedItem();
				remoteAddr = remoteIPTextField.getText();
				remotePort = remotePortTextField.getText();
				localPort = localPortTextField.getText();
				finished = true;
			}

			/* This is simply validations depending on which case was selected */
			private boolean isValidSelection() {
				switch (netcatRole.getSelectedIndex()) {
					case 0:
						alert("Please select a Netcat role!");
						return false;

					case 1:
					case 3:
						if (localPortTextField.getText().isEmpty()) {
							alert("Please enter local port!");
							return false;
						}
						break;

					case 2:
					case 4:
						if (remoteIPTextField.getText().isEmpty()) {
							alert("Please enter remote IP!");
							return false;
						}
						if (remotePortTextField.getText().isEmpty()) {
							alert("Please enter remote port!");
							return false;
						}
						break;
				}

				return true;
			}

			private void alert(String message) {
				JOptionPane.showMessageDialog(null, message);
			}
		}

		public boolean run() {
			while (!finished)
				;

			return true;
		}
	}

	public Netcat(String title) throws IOException {

		super(title);
	}

	class TcpServer {
		// VARIABLE DECLARATION
		ServerSocket serverSocket;
		Socket socket;
		PrintWriter out;
		// necessary buffer define
		BufferedReader in;
		ButtonHandler txButtonHandler;

		TcpServer(String port) throws IOException {		
			//Assign Port number
			serverSocket = new ServerSocket(Integer.parseInt(port));
			socket = serverSocket.accept();//connected		
			//Open an input stream and output stream to the socket.
			//then Read and write to the stream through rx method below
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// Assign a method when the button is pressed
			txButtonHandler = new ButtonHandler();
			sendButton.addActionListener(txButtonHandler);
			// Starts the server as itself
			rx();
			// Finishing up stuff..
			socket.close();
			serverSocket.close();
			System.exit(1);
		}
		// Adds the text of the textArea to the out printWriter that eventually goes to the socket
		void tx() throws IOException {
			out.println(txArea.getText());
		}
		private class ButtonHandler implements ActionListener {

			public void actionPerformed(ActionEvent event) // throws IOException
				// This is handled when the Send button is pressed
				// and then calls tx which ends up sending data to the socket
			{
				try {
					tx();
				} catch (IOException e) {
					System.out.println("Exception...");
				}
			}
		}

		void rx() throws IOException {
			// All this code ends up reading from the in stream, and
			// beware that readLine is blocking, so it will wait listening
			String fromServer;

			do
			{
				fromServer = in.readLine();

				if (fromServer != null)
					rxArea.setText(fromServer);
			}

			while (fromServer != null);
		}
		
	}//TcpServer ENDS HERE
	
	class TcpClient {
		// VARIABLE DECLARATION
		Socket clientSocket;
	    private PrintWriter out;
		private BufferedReader in;
		ButtonHandler sendButtonHandler;

		TcpClient(String remoteAddr, String remotePort) throws IOException {    	
	        super();
	        // TCP Connection getting established 
	        clientSocket = new Socket(remoteAddr, Integer.parseInt(remotePort));
	        // send the message to the server
	        out = new PrintWriter(clientSocket.getOutputStream(), true);
	     	// receive the message which the server sends back
	        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			sendButtonHandler = new ButtonHandler();
			sendButton.addActionListener(sendButtonHandler);
			rx();
			// Cleanup...
			clientSocket.close();
	        System.exit(1);
	    }

		private class ButtonHandler implements ActionListener {

			public void actionPerformed(ActionEvent event) // throws IOException
			// This again seems to be putting to the output stream what is inside the textArea.
			{
				out.println(txArea.getText());
			}
		}

		private void rx() throws IOException {
			// This does also read from the buffered reader that reads 
			// the input stream of the socket connection
			// And then sets the content to the textArea rxArea
			String fromServer;

			do
			{
				fromServer = in.readLine();

				if (fromServer != null)
					rxArea.setText(fromServer);
			}
			
			while (fromServer != null);	
		}
	}//TcpClient ENDS HERE
	
	class UdpServer {
		
		// VARIABLE DECLARATION
		DatagramSocket socket;
		ButtonHandler txButtonHandler;
		InetAddress remoteIpAddress = null;	
	    int remotePortNumber;
	    
	    UdpServer(String localPort) throws IOException {
	        super();
	        // UDP connection getting Established.
	        socket = new DatagramSocket(Integer.parseInt(localPort));	
			// Defining the button handler again
	        txButtonHandler = new ButtonHandler();
            sendButton.addActionListener(txButtonHandler);	
			// Executing all the networking logic here
	        rx();
	        System.exit (1);
	    }
	    
	    //transmit method
	    void tx () throws IOException
        {           
	    	// prepare transmit
            byte[] buf = new byte[256];
            
                
            //txMessage is the users input
            String txMessage = txArea.getText();
            //rxArea.setText(rxArea.getText() + System.lineSeparator());
            buf = txMessage.getBytes();
            
            //Making Connection and sending through the socket.
            DatagramPacket packet = new DatagramPacket (buf, buf.length, remoteIpAddress, remotePortNumber);
            socket.send(packet);
            txArea.setText("");
        }
       
        private class ButtonHandler implements ActionListener {
        	
            public void actionPerformed (ActionEvent event) //throws IOException 
            
            {
				// This does exactly the same as previous cases, where you send
				// a packet to destination.
            	try{
                	tx();
                } 
                catch (IOException e){
                	System.out.println("Exception...");
                }
            }
        }
	    
        //Receive data
		private void rx() throws IOException {
			// TODO Auto-generated method stub
			DatagramPacket packet;
			// prepare 
        	byte[] receivePacket = new byte[256];
        	do
            {      
				for (int i = 0; i < 256; i ++) receivePacket [i] = 0;
				// We build the packet we receive
                packet = new DatagramPacket(receivePacket, receivePacket.length);
				socket.receive (packet);
				// We fetch the ip and port which was sent from
            	remoteIpAddress = packet.getAddress();
				remotePortNumber = packet.getPort();
				// And get the content to the textarea.
            	String rxMessage = new String(packet.getData());
            	rxArea.setText (rxArea.getText() + "" + rxMessage + System.lineSeparator());
            	
            	
            
            }
        	while (true);
		}
	}//UDP Server ENDS HERE
	
	
	class UdpClient {
		
		// Variable Declaration
		DatagramSocket clientSocket;
        ButtonHandler txButtonHandler;
        InetAddress remoteIpAddress = null;// InetAddress class Internet protocol convert by java
        int remotePortNumber;
	    
	    UdpClient(String remoteAddr, String remotePort) throws IOException {
			this.remoteIpAddress = InetAddress.getByName(remoteAddr);
			this.remotePortNumber = Integer.parseInt(remotePort);
	    	// UDP connection getting Established.
			clientSocket = new DatagramSocket();
	        send("");			
			// Defining events
	        txButtonHandler = new ButtonHandler();
            sendButton.addActionListener(txButtonHandler);			
			// Connection logic here
	        rx();
	        System.exit (1);
	    }

	    private void send(String message) throws IOException {
				byte[] buf = message.getBytes();
				// We build a datagram and send it to the remote ip address
				DatagramPacket packet = new DatagramPacket(buf, buf.length, remoteIpAddress, remotePortNumber);
				clientSocket.send(packet);
			}
	        
	    private class ButtonHandler implements ActionListener {
       	
            public void actionPerformed (ActionEvent event) //throws IOException 
            // Same as usual, but we send the content of the text area to the remote connection on the socket
            {
            	try{
            			send(txArea.getText() + "\n");
                }
                catch (IOException e){
                	System.out.println("Exception...");
                }
            }
        }
	    	    
	   	// receive UDP data
	    private void rx() throws IOException {
	    	//transmit method
			DatagramPacket packet;
			// prepare 
        	byte[] receivePacket = new byte[256];
        	do
            {      
				for (int i = 0; i < 256; i ++) receivePacket [i] = 0;
				// We build and receive the packet
                packet = new DatagramPacket(receivePacket, receivePacket.length);
				clientSocket.receive (packet);
				// And then get the data and display in the textarea
            	String rxMessage = new String(packet.getData(), 0, packet.getLength());
            	rxArea.setText (rxArea.getText() + "" + rxMessage + System.lineSeparator());
            }
        	while (true);
		}
	}//UDP Client Ends here

	public void run() throws IOException {

		if (role.equals("TCP Server")) {
			System.out.println("nc -l " + localPort);
			new TcpServer(localPort);
		}

		 if (role.equals ("TCP Client")) {
			 System.out.println ("nc " + remoteAddr + " " + remotePort); 
			 new TcpClient (remoteAddr, remotePort);
		}

		if (role.equals ("UDP Server")) {
			System.out.println ("nc -u -l " + localPort); 
			new UdpServer (localPort);
		}
		
		
		 if (role.equals ("UDP Client")) {
			 System.out.println ("nc -u " + remoteAddr + " " + remotePort); 
			 new UdpClient (remoteAddr, remotePort);
		}
		
		 
	}

	public void g() {

		NetcatStartup p = new NetcatStartup("Netcat Connection");

		p.setSize(new Dimension(550, 280));
		p.setVisible(true);
		p.run();
		p.dispose();
	}

	public static void main(String[] args) throws IOException {

		Netcat f = new Netcat("Netcat");
		
		f.g();
		f.setSize(new Dimension(550, 280));
		f.setVisible(true);
		f.run();
	}
}//netcat class ends here
