import std.socket;
import std.stdio;
import std.conv;
import std.concurrency;
import std.parallelism;
import packet : Packet;
import SDLApp : SDLApp;
import surface : Surface;

/**
*	fetchDataFromServer that takes two shared variables, a Socket and a Surface. 
*	The purpose of this function is to fetch data from the server using the Socket and update the Surface accordingly.
*	The function uses a while loop to keep fetching data from the server as long as the connection is active. 
*	Inside the loop, it first creates a buffer of bytes with a size equal to the Packet.
*	sizeof constant (presumably the size of the packet being sent/received). 
*	It then receives data from the server using the socket.receive method and stores it in the buffer. 
*	The received data is then parsed to extract various fields such as the x and y coordinates, the RGB values, 
*	the packet type, and the message.
*	Next, the parsed values are used to create a Packet object, which is a custom-defined struct with 
*	fields corresponding to the extracted fields. The function then checks the message type and, if it is 'u', 
*	it iterates through a nested loop to update the pixels on the Surface with the provided RGB values, brush size, 
*	and type. If the message type is 'q', it sets the isConnectionActive flag to false to exit the loop and 
*	terminate the connection.
*	Overall, this function fetches packets of data from a server, parses and extracts the relevant fields from the packet, 
*	and updates the Surface accordingly.
*/

void fetchDataFromServer(shared Socket sharedSocket, shared Surface sharedSurface) {
	Socket socket = cast(Socket) sharedSocket;
	Surface surface = cast(Surface) sharedSurface;
	bool isConnectionActive = true;

	while (isConnectionActive) {
		byte[Packet.sizeof] buffer;
		auto fromServer = buffer[0 .. socket.receive(buffer)];
		Packet formattedPacket;
		byte[4] fieldX = fromServer[0 .. 4].dup;
		byte[4] fieldY = fromServer[4 .. 8].dup;
		byte[1] fieldR = fromServer[8 .. 9].dup;
		byte[1] fieldG = fromServer[9 .. 10].dup;
		byte[1] fieldB = fromServer[10 .. 11].dup;
		byte[64] fieldMsg;
		byte[1] packetType = fromServer[11 .. 12].dup;
		byte[4] fieldSize = fromServer[12 .. 16].dup;
		if (fromServer.length == Packet.sizeof) {
			fieldMsg = fromServer[16 .. 80].dup;
		}
		int fx = *cast(int*)&fieldX;
		int fy = *cast(int*)&fieldY;
		int fsize = *cast(int*)&fieldSize;
		ubyte fb = *cast(ubyte*)&fieldB;
		ubyte fg = *cast(ubyte*)&fieldG;
		ubyte fr = *cast(ubyte*)&fieldR;
		char ft = *cast(char*)&packetType;

        formattedPacket.x = fx;
        formattedPacket.y = fy;
        formattedPacket.b = fb;
        formattedPacket.g = fg;
        formattedPacket.r = fr;
        formattedPacket.type = ft;
        formattedPacket.size = fsize;
        if (fromServer.length == Packet.sizeof) {
				formattedPacket.msg = cast(string) fieldMsg;
			} else {
				formattedPacket.msg = "None\0";
			}
        int size_of_brush = formattedPacket.size;
		
        string castedMsg = cast(string) formattedPacket.msg;

		if (formattedPacket.msg[0] == 'u') {
			for (int brushWidth = 1 - size_of_brush; brushWidth <= size_of_brush; brushWidth++) {
				for (int brushHeight = 1 - size_of_brush; brushHeight <= size_of_brush; brushHeight++) {
					
                    auto finalX = formattedPacket.x + brushWidth;
                    auto finalY = formattedPacket.y + brushHeight;

                    surface.changePixel(finalX, finalY, formattedPacket.r,
						formattedPacket.g, formattedPacket.b, formattedPacket.type[0], formattedPacket
							.size);
				}
			}
		}
		if (formattedPacket.msg[0] == 'q') {
			isConnectionActive = false;
		}

	}
}