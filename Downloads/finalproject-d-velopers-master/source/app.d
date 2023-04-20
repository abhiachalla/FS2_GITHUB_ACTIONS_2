import std.stdio;
import std.string;
import std.conv;
import std.concurrency;
import std.socket;

import SDLApp : SDLApp;
import server : Server;
import client : Client;
import serverData: fetchDataFromServer;

/**
*	The problem statement of the project is to create a painting application that enables 
*	at least three users to connect to a server application and collaborate on a single painting in real-time. 
*	The server will broadcast changes to each user as they paint, ensuring that all users are working on the 
*	same version of the painting. 
*	This project will address the need for a collaborative painting application that allows users to work together 
*	on a single canvas in real-time, enhancing creativity and collaboration among users.
*/

void main()
{
    writeln("Enter the port number:");
    auto portString = readln();
	portString = portString.strip();
	int port = to!int(portString);

	writeln("Enter the IP address:");
    string IP = readln();
	IP = IP.strip();

	writeln("Press 1 to start client and 2 to start server:");

	auto ch = readln().strip();
	int choice = to!int(ch);

    /// For creating Client connection

	if(choice == 1) {
		auto client = new Client(IP, port);
		auto socket =  client.socket;     
		auto app = new SDLApp();
        app.addNewClient(socket);
		auto sharedSocket = cast(shared) socket;
		auto surface = cast(shared) app.getSurface();
        spawn(&fetchDataFromServer, sharedSocket, surface);
        app.MainApplicationLoop();
	} else {
		/// For creating Server connection
		Server server = new Server(IP, port);
        server.start();
	}
}