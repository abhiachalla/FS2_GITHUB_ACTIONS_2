import std.socket;
import std.stdio;
import std.string;
import std.container;
import std.concurrency;
import std.conv;

import bindbc.sdl;
import loader = bindbc.loader.sharedlib;

import surface : Surface;
import client : Client;
import packet : Packet;

/**
*   Class SDLApp creates a window and handles user events for mouse and keyboard inputs. 
*   It also includes undo and redo functionality.
*/
class SDLApp {

    private Surface surface;
    
    Socket socket;

    this() {
        surface = new Surface(1);
    }

    Surface getSurface() {
        return surface;
    }

    /**
    *   The colorPalette is an array of SDL_Color that contains the default color options for drawing. 
    *   The defaultBrushSize, defaultRed, defaultGreen, and defaultBlue variables store the default brush size and color options.
    */
    SDL_Color[] colorPalette = [ SDL_Color(255, 0, 0, 255), // R 
        SDL_Color(0, 255, 0, 255), // G
        SDL_Color(0, 0, 255, 255), // B
        SDL_Color(255, 255, 0, 0) // Yellow
        ];

    /**
    *   The MainApplicationLoop function is the main graphics loop that runs until a quit event occurs. 
    *   Inside the loop, it checks for events using SDL_PollEvent function and handles each event accordingly.
    */
    void MainApplicationLoop() {

        /**
        *   The DrawingInfo struct contains the information required to draw a line or dot on the surface. 
        *   The undoStack and redoStack are arrays of DrawingInfo that are used to store the drawing actions and allow undo and redo functionality.
        */
        struct DrawingInfo {
            int x, y;

            ubyte b, g, r;

            char brushType;

            int brushSize;
        }

        Array!DrawingInfo undoStack;
        Array!DrawingInfo redoStack;

        
        
        bool runApplication = true;
        bool isClientDrawing = false;

        SDL_Window* window = SDL_CreateWindow("D SDL Painting",
            SDL_WINDOWPOS_UNDEFINED,
            SDL_WINDOWPOS_UNDEFINED,
            1000,
            1000,
            SDL_WINDOW_SHOWN);

        SDL_Surface* imgSurface = surface.getImageSurface();
    
        bool isEraserClicked = false;

        int defaultBrushSize = 5;
        
        ubyte defaultRed = 255;
        ubyte defaultGreen = 0;
        ubyte defaultBlue = 0;

        while (runApplication) {
            SDL_Event e;
            while (SDL_PollEvent(&e) != 0) {
                if (e.type == SDL_QUIT) {
                    runApplication = false;
                    Packet data;
                    data.x = 0;
                    data.y = 0;

                    data.b = 0;
                    data.g = 0;
                    data.r = 0;

                    data.type[0] = 0;
                    data.msg = "quit\0";

                    data.size = 0;
                    socket.send(data.GetPacketAsBytes());
                } else if (e.type == SDL_MOUSEBUTTONDOWN) {
                    
                    isClientDrawing = true;

                    if (e.button.y - 10 < 60) {
                        
                        int colorIndex = e.button.x / (60 + 10);

                        if (colorIndex < colorPalette.length && colorIndex >= 0) {
                            
                            SDL_Color colorPaletteObject = getcolorRGB(colorIndex);
                            defaultRed = colorPaletteObject.r;
                            defaultGreen = colorPaletteObject.g;
                            defaultBlue = colorPaletteObject.b;                            
                            isClientDrawing = false;
                        }

                    } else if (surface.isClickInsideBrushPanel(e.button.y, e.button.x)) {

                        int brushSize = surface.getBrushSizeFromPanel(e.button.x);

                        isEraserClicked = getEraserMode(brushSize);

                        if(!isEraserClicked){
                            defaultBrushSize = brushSize;
                        }
                        isClientDrawing = false;
                    }

                } else if (e.type == SDL_MOUSEBUTTONUP) {
                    isClientDrawing = false;
                } else if (e.type == SDL_KEYDOWN) {
                    /// click the 'u' on keyboard to undo
                    if (e.key.keysym.sym == SDLK_u) {
                        if (!undoStack.empty) {
                            DrawingInfo p = undoStack.back;

                            int xPos = p.x;
                            int yPos = p.y;

                            ubyte B = p.b;
                            ubyte G = p.g;
                            ubyte R = p.r;
                            
                            
                            ubyte drawB = 0; ubyte drawR = 0; ubyte drawG = 0;
                            
                            int brushSize = p.brushSize;
                            char brushType = p.brushType;

                            getpacket(xPos, yPos, drawB, drawG, drawR, brushType, brushSize);

                            for (int tempWidth = 1 - brushSize; tempWidth <= brushSize; tempWidth++) {
                                for (int tempHeight = 1 - brushSize; tempHeight <= brushSize; tempHeight++) {
                                    undoStack.removeBack();
                                    redoStack.insertBack(DrawingInfo(xPos, yPos, B, G, R, brushType, brushSize));
                                    surface.changePixel(xPos + tempWidth, yPos + tempHeight, drawB, drawG, drawR, brushType, brushSize);
                                    
                                }
                            }
                            writeln("Add the current event to the undo stack" ~ undoStack.length.to!string);
                        } else {
                            writeln("No event has occured yet to undo!");
                        }
                    } else if (e.key.keysym.sym == SDLK_r) {
                        /// click 'r' on the keyboard to redo
                        if (!redoStack.empty) {

                            DrawingInfo q = redoStack.back;

                            int xPos = q.x;
                            int yPos = q.y;

                            ubyte drawR = q.r;
                            ubyte drawG = q.g;
                            ubyte drawB = q.b;

                            char brushType = q.brushType;
                            int brushSize = q.brushSize;
                            
                            getpacket(xPos, yPos, drawB, drawG, drawR, brushType, brushSize);

                            for (int tempWidth = 1 - brushSize; tempWidth <= brushSize; tempWidth++) {
                                for (int tempHeight = 1 - brushSize; tempHeight <= brushSize; tempHeight++) {
                                    undoStack.insertBack(DrawingInfo(xPos, yPos, drawB, drawG, drawR, brushType, brushSize));
                                    redoStack.removeBack();
                                    surface.changePixel(xPos + tempWidth, yPos + tempHeight, drawB, drawG, drawR, brushType, brushSize);
                                    
                                }
                            }
                        } else {
                            writeln("No event is undoed ro redo!");
                        }
                    }
                } else if (e.type == SDL_MOUSEMOTION && isClientDrawing) {
                    
                    int xPos = e.button.x;
                    int yPos = e.button.y;

                    ubyte drawR = isEraserClicked ? 0 : defaultRed;
                    ubyte drawG = isEraserClicked ? 0 : defaultGreen;
                    ubyte drawB = isEraserClicked ? 0 : defaultBlue;

                    char brushType = surface.getBrushSize(defaultBrushSize);
                    int brushSize = defaultBrushSize;

                    if (brushSize == 0) {
                        brushSize = 5;
                    }
                    
                    getpacket(xPos, yPos, drawB, drawG, drawR, brushType, brushSize);

                    for (int tempWidth = 1 - brushSize; tempWidth <= brushSize; tempWidth++) {
                        for (int tempHeight = 1 - brushSize; tempHeight <= brushSize; tempHeight++) {
                            surface.changePixel(xPos + tempWidth, yPos + tempHeight, drawB, drawG, drawR, brushType, brushSize);

                            undoStack.insertBack(DrawingInfo(xPos, yPos, drawB, drawG, drawR, brushType, brushSize));
                        }
                    }
                    writeln("Latest Undo: " ~ undoStack.length.to!string);
                }
            }

            SDL_BlitSurface(imgSurface, null, SDL_GetWindowSurface(window), null);
            SDL_UpdateWindowSurface(window);
            SDL_Delay(16);
        }

        SDL_DestroyWindow(window);
    }

    /**
    *   This function takes an integer brushSize as input and returns a boolean value. 
    *   If the brushSize is -1, it returns true, indicating that the eraser mode is active. 
    *   Otherwise, it returns false.
    */
    bool getEraserMode(int brushSize) {
        if (brushSize == -1) {
            return true;
        } 
        else {
            return false;
        }   
    }
    
    /**
    *   This function takes various inputs related to drawing, such as the x and y position of the brush, 
    *   the color of the brush (specified as separate red, green, and blue values), the type of brush 
    *   (specified as a single character), and the size of the brush (specified as an integer). 
    *   It then creates a Packet object and sends it over a socket.
    */
    void getpacket(int xPos, int yPos, ubyte drawB, ubyte drawG, ubyte drawR, char brushType, int brushSize) {
        Packet data;

        data.x = xPos;
        data.y = yPos;
        data.b = drawB;
        data.g = drawG;
        data.r = drawR;
        data.type[0] = brushType;
        data.msg = "update\0";
        data.size = brushSize;

        socket.send(data.GetPacketAsBytes());
    }

    /**
    *   This function takes a Socket object as input and sets the class variable 'socket' equal to it.
    */
    void addNewClient(Socket socket) {
        this.socket = socket;
    }

    /**
    *   This function takes an integer colorIndex as input and returns an SDL_Color object from a colorPalette 
    *   array at the given index.
    */
    SDL_Color getcolorRGB(int colorIndex){
        return colorPalette[colorIndex];
    }
}



const SDLSupport ret;

/**
*   This is the constructor for the class. It first loads the SDL library using different commands depending on the 
*   operating system. It then initializes SDL and sets the class variable 'ret' to the result of loading SDL. 
*   Finally, it prints out some diagnostic messages to the console.
*/
shared static this() {
    version (Windows) {
        writeln("Searching for SDL on Windows");
        ret = loadSDL("SDL2.dll");
    }
    version (OSX) {
        writeln("Searching for SDL on Mac");
        ret = loadSDL();
    }
    version (linux) {
        writeln("Searching for SDL on Linux");
        ret = loadSDL();
    }
    if (ret != sdlSupport) {
        writeln("error loading SDL library");

        foreach (info; loader.errors) {
            writeln(info.error, ':', info.message);
        }
    }
    if (ret == SDLSupport.noLibrary) {
        writeln("error no library found");
    }
    if (ret == SDLSupport.badLibrary) {
        writeln(
            "Eror badLibrary, missing symbols, perhaps an older or very new version of
             SDL is causing the problem?");
    }
    if (SDL_Init(SDL_INIT_EVERYTHING) != 0) {
        writeln("SDL_Init: ", fromStringz(SDL_GetError()));
    }
    writeln("Exit class constructor");

}

/**
*   This is the destructor for the class. It simply calls SDL_Quit() to clean up SDL resources and prints a 
*   message to the console indicating that the application is ending.
*/
shared static ~this() {
    SDL_Quit();
    writeln("Ending application--good bye!");
}
