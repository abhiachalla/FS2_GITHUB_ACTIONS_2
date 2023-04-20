import core.stdc.string;

/**
*	The code defines a C-like struct called Packet which contains various fields such as x, y, r, g, b, msg, type, and size. 
*	The x, y, r, g, and b fields are integers and msg is an array of characters with a length of 64. 
*	type is also an array of characters, but with a length of 1. Finally, size is an integer that represents the size of the packet.
*	The struct also defines a method called GetPacketAsBytes(), which returns a character array that represents the Packet struct in bytes. 
*	The method creates a character array called payload with a length of Packet.sizeof, which is the total size of the Packet struct in bytes. 
*	The method then uses the memmove function from the string library to copy the values of each field in the Packet struct into the payload array. 
*	The memmove function is used to ensure that the values are correctly aligned in memory.
*	The method then returns the payload array, which can be used to transmit the Packet struct over a network or store it in a file.
*/

struct Packet {

	int x;
	int y;
	ubyte r;
	ubyte g;
	ubyte b;
	char[64] msg; 
	char[1] type;
	int size;

	char[Packet.sizeof] GetPacketAsBytes() {
		char[Packet.sizeof] payload;
		/// Populate the payload array with bits
		memmove(&payload, &x, x.sizeof);
		memmove(&payload[4], &y, y.sizeof);
		memmove(&payload[8], &r, r.sizeof);
		memmove(&payload[9], &g, g.sizeof);
		memmove(&payload[10], &b, b.sizeof);
		memmove(&payload[11], &type, type.sizeof);
		memmove(&payload[12], &size, size.sizeof);
		memmove(&payload[16], &msg, msg.sizeof);
		return payload;
	}

}
