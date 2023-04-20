import std.stdio;
import bindbc.sdl;
import loader = bindbc.loader.sharedlib;
import surface : Surface;
import SDLApp : SDLApp;
import packet : Packet;
import client : Client;
import server : Server;
import core.thread;
import std.socket;
import std.format;

// TC:1
@("unittest for selecting red color.")
unittest
{
    auto app = new SDLApp();
    
    SDL_Color[] colors = [
            SDL_Color(255, 0, 0, 255), // Red
            SDL_Color(0, 255, 0, 255), // Green
            SDL_Color(0, 0, 255, 255), // Blue
            SDL_Color(255, 255, 0, 255) // Yellow
    ];
    int x = 40;
    int y = 35;
    int colorIndex = x / (60 + 10);
    assert(app.getcolorRGB(colorIndex).r==255);
    assert(app.getcolorRGB(colorIndex).g==0);
    assert(app.getcolorRGB(colorIndex).b==0);
}

// TC:2
@("unittest for selecting green color.")
unittest
{
    auto app = new SDLApp();
    
    SDL_Color[] colors = [
            SDL_Color(255, 0, 0, 255), // Red
            SDL_Color(0, 255, 0, 255), // Green
            SDL_Color(0, 0, 255, 255), // Blue
            SDL_Color(255, 255, 0, 255) // Yellow
    ];
    int x = 100;
    int y = 35;
    int colorIndex = x / (60 + 10);
    assert(app.getcolorRGB(colorIndex).r==0);
    assert(app.getcolorRGB(colorIndex).g==255);
    assert(app.getcolorRGB(colorIndex).b==0);
}

// TC:3
@("unittest for selecting blue color.")
unittest
{
    auto app = new SDLApp();
    
    SDL_Color[] colors = [
            SDL_Color(255, 0, 0, 255), // Red
            SDL_Color(0, 255, 0, 255), // Green
            SDL_Color(0, 0, 255, 255), // Blue
            SDL_Color(255, 255, 0, 255) // Yellow
    ];
    int x = 160;
    int y = 35;
    int colorIndex = x / (60 + 10);
    assert(app.getcolorRGB(colorIndex).r==0);
    assert(app.getcolorRGB(colorIndex).g==0);
    assert(app.getcolorRGB(colorIndex).b==255);
}

// TC:4
@("unittest for selecting yellow color.")
unittest
{
    auto app = new SDLApp();
    
    SDL_Color[] colors = [
            SDL_Color(255, 0, 0, 255), // Red
            SDL_Color(0, 255, 0, 255), // Green
            SDL_Color(0, 0, 255, 255), // Blue
            SDL_Color(255, 255, 0, 255) // Yellow
    ];
    int x = 220;
    int y = 35;
    int colorIndex = x / (60 + 10);
    assert(app.getcolorRGB(colorIndex).r==255);
    assert(app.getcolorRGB(colorIndex).g==255);
    assert(app.getcolorRGB(colorIndex).b==0);
}

// TC:5 ..
@("unittest for testing brush size selection functionality.")
unittest
{
    Surface surface = new Surface(1);
    // Test small brush size
    assert(surface.getBrushSize(4) == 's');

    // Test medium brush size
    assert(surface.getBrushSize(8) == 'm');

    // Test large brush size
    assert(surface.getBrushSize(12) == 'l');
}

// TC:6
@("unittest for selecting small brush size.")
unittest
{
    Surface surface = new Surface(1);
    int x = 40;
    int y = 95;
    int smallBrush = surface.getBrushSizeFromPanel(x);
    assert(surface.getBrushSize(smallBrush) == 's');
}

// TC:7
@("unittest for selecting medium brush size.")
unittest
{
    Surface surface = new Surface(1);
    int x = 100;
    int y = 95;
    int mediumBrush = surface.getBrushSizeFromPanel(x);
    assert(surface.getBrushSize(mediumBrush) == 'm');
}

// TC:8
@("unittest for selecting large brush size.")
unittest
{
    Surface surface = new Surface(1);
    int x = 160;
    int y = 95;
    int largeBrush = surface.getBrushSizeFromPanel(x);
    assert(surface.getBrushSize(largeBrush) == 'l');
}

// TC:9
@("unittest for selecting eraser.")
unittest
{
    Surface surface = new Surface(1);
    auto app = new SDLApp();
    int x = 220;
    int y = 95;
    int eraser = surface.getBrushSizeFromPanel(x); 
    
    assert(app.getEraserMode(eraser) == true);
}

//// TC:10
@("Network testing")
unittest {
    Packet packet;
    packet.x = 10;
    packet.y = 0;
    packet.b = 0;
    packet.g = 0;
    packet.r = 0;
    packet.type[0] = 0;
    packet.msg = "network_testing\0";
    packet.size = 0;
    auto test_server = new Server("127.0.0.1", 8080);
    auto test_client_thread = new Thread({ 
        Thread.sleep(1.seconds);
        auto test_client = new Client("127.0.0.1", 8080);
        test_client.transmitPacketsToServer(packet);
        test_client.end();
    });
    test_client_thread.start();
    test_server.start();
    test_server.end();
    test_client_thread.join();
    string received = test_server.getMostRecentMsgFromPacket();
    writeln("Received:", received);
    assert(received[0] == 'n');
}