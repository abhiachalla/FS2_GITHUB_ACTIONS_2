import std.socket;
import std.stdio;
import std.container;
import std.algorithm;
import std.array;
import core.thread;
import packet : Packet;

/**
*	Class Server is the implementation of a server for a multi-client drawing application using sockets
*/
class Server {

	bool serverIsRunning;
	string IP;
	int serverPort;
	private Socket listener;
	Socket[] connectedClientsList;
	DList!Packet* packetsSoFar;

	this(string address, int port) {
		IP = address;
		serverPort = port;
		packetsSoFar = new DList!Packet();
	}

	private void closeServer() {
		serverIsRunning = false;
		writeln("Closing server...");
		listener.close();
	}

	~this() {
		closeServer();
	}
/**
*	The createPacket() method is used to create a Packet object from the raw byte data received from the client. 
*	The packet class seems to be a custom class for the drawing packets, but its implementation is not shown in the code 
*	snippet.
*/
	private Packet createPacket(byte[] buffer) {

			Packet p;
			byte[4] field1 = buffer[0 .. 4].dup;
			byte[4] field2 = buffer[4 .. 8].dup;
			byte[1] field3 = buffer[8 .. 9].dup;
			byte[1] field4 = buffer[9 .. 10].dup;
			byte[1] field5 = buffer[10 .. 11].dup;
			byte[1] field7 = buffer[11 .. 12].dup;
			byte[64] field6 = buffer[16 .. 80].dup;
			byte[4] field8 = buffer[12 .. 16].dup;


			p.x = *cast(int*)&field1;
			p.y = *cast(int*)&field2;
			p.size = *cast(int*)&field8;
			p.b = *cast(ubyte*)&field3;
			p.g = *cast(ubyte*)&field4;
			p.r = *cast(ubyte*)&field5;
			p.msg = cast(char[])(field6);
			p.type = cast(char[])(field7);
			return p;
	}

/**
*	The start() method is the main application loop for the server. It creates a listener socket, 
*	binds it to the specified IP and port, and starts listening for client connections. 
*	It also creates a readSet that allows for multiplexing of sockets to enable multiple clients to connect to the server. 
*	The server waits for clients to connect and adds them to the connectedClientsList. 
*	Once a client connects, the server sends them a welcome message, and the client can start sending drawing packets.
*	When the server receives a drawing packet from a client, it checks if the message is a quit message or not. 
*	If it is a quit message, it removes the client from the connectedClientsList and sends the quit packet to the client. 
*	If it is not a quit message, it adds the packet to the packetsSoFar list and broadcasts the packet to all connected 
*	clients except the sender.
*/
	void start() {
		writeln("Server is starting");
		writeln("Server should be started first before clients");
		listener = new Socket(AddressFamily.INET, SocketType.STREAM);
        ushort sP = cast(ushort) serverPort;
		listener.bind(new InternetAddress(IP, sP));
		listener.listen(10);
		auto readSet = new SocketSet();

		byte[Packet.sizeof] buffer;
		serverIsRunning = true;
		writeln("Waiting for client connections");
		while (serverIsRunning) {
			readSet.reset();
			readSet.add(listener);
			foreach (client; connectedClientsList) {
				readSet.add(client);
			}
			if (Socket.select(readSet, null, null)) {
				foreach (client; connectedClientsList) {
					if (readSet.isSet(client)) {
						auto got = client.receive(buffer);
						Packet p = createPacket(buffer);
						if (p.msg[0] == 'q') {
							client.send(p.GetPacketAsBytes());
							connectedClientsList = connectedClientsList.filter!(x => x != client)
								.array;
							writeln("Client removed from connectedClientsList");
						} else if (p.msg[0] == 'u') {
							packetsSoFar.insertBack(p);

							packetsSoFar.insertBack(p);

							foreach (otherClient; connectedClientsList) {
								if (otherClient != client) {
									otherClient.send(p.GetPacketAsBytes());
								}
							}
						}
						else if (p.msg[0] == 'n') {
							writeln("Received packet");
							packetsSoFar.insertBack(p);
							serverIsRunning = false;
						}

					}
				}
				if (readSet.isSet(listener)) {
					auto newSocket = listener.accept();
					newSocket.send("Connection active");
					connectedClientsList ~= newSocket;
					writeln("The Client", connectedClientsList.length, " added to connectedClientsList");
					Thread.sleep(250.msecs); 
					foreach (packet; *packetsSoFar) {
						newSocket.send(packet.GetPacketAsBytes());
					}
				}
			}
		}
	}

	public void end() {
		writeln("Server is ending");
		serverIsRunning = false;
		listener.close();
	}

	public string getMostRecentMsgFromPacket() {
		if (packetsSoFar.empty) {
			writeln("packet is null");
			return "null"; // Return an empty string if historyPackets is empty
		}

		return packetsSoFar.back.msg; // Return the last message in historyPackets
	}
}