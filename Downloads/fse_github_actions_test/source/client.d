import std.socket;
import std.stdio;
import std.conv;
import std.concurrency;
import std.parallelism;
import packet : Packet;
import SDLApp : SDLApp;
import surface : Surface;

/**
*	Client class represents a client-side network communication program that establishes a connection 
*	with a server at a specified IP and port and can be used to send and 
*	receive data between the client and server.
*	The class Client has three fields: IP, port, and socket. 
*	IP and port represent the IP address and port number of the server that the client is connecting to, 
*	while socket represents the client's socket object used for communication.
*	The this method is a constructor method that takes in IP and port as arguments and initializes the IP and port fields. 
*	The constructor also creates a socket object of type Socket with the given IP and port using the AddressFamily.
*	INET and SocketType.STREAM options. The connect() method is then called on the socket object to 
*	establish a connection with the server at the specified IP and port. If the connection is successful, 
*	the method prints "Connected" to the console.
*/

class Client {
	string IP;
	int port;
    Socket socket;
	

this(string IP, int port) {
		this.IP = IP;
		this.port = port;

		writeln("Starting client...attempt to create socket");
		this.socket = new Socket(AddressFamily.INET, SocketType.STREAM);
        
        ushort cP = cast(ushort) port;
		// Connec to the same socket of Server
		socket.connect(new InternetAddress(IP, cP));
		writeln("******Connected");
	}

	~this() {
		writeln("******client closed");
		socket.close();
	}

	void transmitPacketsToServer(Packet data) {
		socket.send(data.GetPacketAsBytes());
	}

	void end() {
		socket.close();
	}
}