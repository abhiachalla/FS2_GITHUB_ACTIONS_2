/**
*   Class Surface creates an SDL surface and draws a brush toolbox and color palette on it. 
*   The Surface class has a constructor that initializes an SDL surface and draws the brush toolbox and color palette 
*   on it. The class has a destructor that frees the SDL surface.
*   The Surface class has several member variables that define the size and appearance of the brush toolbox and 
*   color palette, including canvas_w, canvas_h, padding, brushToolbox_w, brushToolbox_h, and brushSizes.
*/

import std.conv;
import std.stdio;
import std.string;
import std.algorithm;
import bindbc.sdl;
import loader = bindbc.loader.sharedlib;

class Surface {
    SDL_Surface* imgSurface;
    int canvas_w = 100;
    int canvas_h = 100;
    int padding = 10;
    int brushToolbox_w = 60;
    int brushToolbox_h;
    int[] brushSizes = [5, 10, 15];

    this(...) {
        imgSurface = SDL_CreateRGBSurface(0, 1280, 960, 32, 0, 0, 0, 0);
        
        drawBrushToolbox();
        drawColorPalette();
    }

    ~this() {
        SDL_FreeSurface(imgSurface);
    }

/**
*   getBrushSize() funciton returns the brush size as a character ('s' for small, 'm' for medium, or 'l' for large), 
*/
    char getBrushSize(int brushSize) {
        char tempSize = 's';

        if (brushSize <= 5) {
            tempSize = 's';
        } else if (brushSize <= 10) {
            tempSize = 'm';
        } else {
            tempSize = 'l';
        }

        return tempSize;
    }

/**
*   drawColorPalette() function draws a color palette on the SDL surface
*/
     void drawColorPalette() {
        SDL_Color[] colors = [
            SDL_Color(255, 0, 0, 255), // R
            SDL_Color(0, 255, 0, 255), // G
            SDL_Color(0, 0, 255, 255), // B
            SDL_Color(255, 255, 0, 255) // Yellow
        ];
        int padding = 10;
        int borderWidth = 2;
        int canvas_w = 60;
        int canvas_h = imgSurface.h;
        foreach (i; 0 .. colors.length)
        {
            SDL_Rect outrect = SDL_Rect(cast(int)(padding + i * (canvas_w + padding)), padding,
                                        canvas_w, canvas_w);
            SDL_FillRect(imgSurface, &outrect, SDL_MapRGBA(imgSurface.format, 128, 128, 128, 255));

            SDL_Rect innerRect = SDL_Rect(cast(int)(padding + borderWidth +
                                        i * (canvas_w + padding)), padding + borderWidth, canvas_w - 2 * borderWidth, canvas_w - 2 * borderWidth);
            SDL_FillRect(imgSurface, &innerRect, SDL_MapRGBA(imgSurface.format, colors[i].r,
                                        colors[i].g, colors[i].b, colors[i].a));
        }
    }

/**
*   drawBrushToolbox() function draws the brush toolbox on the SDL surface.
*/
    void drawBrushToolbox() {
        string mediaPath = "images/";
        brushToolbox_h = (brushToolbox_w + padding) * cast(int)(brushSizes.length + 1); 
        int borderWidth = 2;
        foreach (i; 0 .. brushSizes.length + 1) {
            SDL_Rect outrect = SDL_Rect(cast(int)(padding + i *
                (brushToolbox_w + padding)), brushToolbox_w + padding, brushToolbox_w, brushToolbox_w);
            SDL_FillRect(imgSurface, &outrect, SDL_MapRGBA(imgSurface.format, 128, 128, 128, 255));
            SDL_Rect innerRect = SDL_Rect(cast(int)(padding + borderWidth + i * (brushToolbox_w + padding)), brushToolbox_w + padding + borderWidth,
            brushToolbox_w - 2 * borderWidth,  brushToolbox_w - 2 * borderWidth);
            if (i < brushSizes.length) { 
                SDL_FillRect(imgSurface, &innerRect, SDL_MapRGBA(imgSurface.format, 0, 0, 0, 150));
            } else { 
                SDL_FillRect(imgSurface, &innerRect, SDL_MapRGBA(imgSurface.format, 255, 255, 255, 150));
            }
            string imageName;
            
            if (i <= brushSizes.length) {
                
                switch (i) {
                    case 0: imageName = "smallBrush.bmp"; break;
                    case 1: imageName = "mediumBrush.bmp"; break;
                    case 2: imageName = "largeBrush.bmp"; break;
                    case 3: imageName = "eraser.bmp"; break;
                    default: break;
                }

            } else {
                imageName = "eraser.bmp";
            }

            SDL_Surface* bmpFile = SDL_LoadBMP((mediaPath ~ imageName).toStringz);

            if (bmpFile is null) {
                writeln("1st occ");
                writeln("SDL Error:", SDL_GetError());
            }

            if (bmpFile) {
                int x = brushToolbox_w + padding + borderWidth;
                int y = cast(int)(padding + i * (brushToolbox_w + padding)) + borderWidth +
                (brushToolbox_w - bmpFile.h) / 2; 

                SDL_Rect imageRect = SDL_Rect(y, x, bmpFile.w, bmpFile.h);

                SDL_BlitSurface(bmpFile, null, imgSurface, &imageRect);

                SDL_FreeSurface(bmpFile);


            }
        }
    }

/**
*   isClickInsideBrushPanel() function takes two integer arguments, x and y, and returns a boolean value indicating 
*   whether the specified coordinates are within the bounds of the brush toolbox.
*/
    bool isClickInsideBrushPanel(int x, int y) {
        int panelY = padding;

        return (x >= brushToolbox_w + padding) && (x <= (brushToolbox_w * 2 + padding)) &&
            (y >= panelY) && (y <= (panelY + brushToolbox_h));
    }

/**
*   The getBrushSizeFromPanel function takes a Y-coordinate value and calculates the brush size based on the 
*   position of the cursor within a brush toolbox panel. The function returns the brush size as an integer value or -1 
*   if the cursor is outside the panel.
*/
    int getBrushSizeFromPanel(int y) {
        int panelY = padding;
        y -= panelY;

        int index = y / (brushToolbox_w + padding);
        if (index >= 0 && index < brushSizes.length) {
            return brushSizes[index];
        } else if (index == brushSizes.length) {
            return -1; 
        }

        return 0; 
    }

/**
*   The getImageSurface function returns a pointer to an SDL_Surface object, which represents an image surface.
*/
     SDL_Surface* getImageSurface() {
        return imgSurface;
    }

/**
*   The changePixel function is used to change the color of a pixel at a given coordinate (x, y) on the image surface. 
*The function takes the RGB values of the new color as well as the brush type and size. 
*   Depending on the brush type, the function modifies several pixels around the target pixel to create the effect 
*   of a brush stroke. The function uses SDL_LockSurface and SDL_UnlockSurface functions to access the pixel 
*   data of the image surface safely.
*/
    void changePixel(int x, int y, ubyte b, ubyte g, ubyte r, char brushType, int brushSize) {
        SDL_LockSurface(imgSurface);
        scope (exit)
            SDL_UnlockSurface(imgSurface);

        ubyte* pixelArray = cast(ubyte*) imgSurface.pixels;

        int halfBrushSize = brushSize / 2;

        int currentX = x;
        int currentY = y;

        int offsetB = (currentY) * imgSurface.pitch + (currentX) * imgSurface.format.BytesPerPixel + 0;
        int offsetG = (currentY) * imgSurface.pitch + (currentX) * imgSurface.format.BytesPerPixel + 1;
        int offsetR = (currentY) * imgSurface.pitch + (currentX) * imgSurface.format.BytesPerPixel + 2;

        switch(brushType) {
            case 'l':
                pixelArray[offsetB] = b;
                pixelArray[offsetG] = g;
                pixelArray[offsetR] = r;
                pixelArray[(currentY + 15) * imgSurface.pitch + (currentX + 15) * imgSurface.format.BytesPerPixel + 0] = b;
                pixelArray[(currentY + 27) * imgSurface.pitch + (currentX + 27) * imgSurface.format.BytesPerPixel + 1] = g;
                pixelArray[(currentY + 43) * imgSurface.pitch + (currentX + 43) * imgSurface.format.BytesPerPixel + 2] = r;
                break;
            case 'm':
                pixelArray[offsetB] = b;
                pixelArray[offsetG] = g;
                pixelArray[offsetR] = r;
                pixelArray[(currentY + 5) * imgSurface.pitch + (currentX + 3) * imgSurface.format.BytesPerPixel + 0] = b;
                pixelArray[(currentY + 5) * imgSurface.pitch + (currentX + 8) * imgSurface.format.BytesPerPixel + 1] = g;
                pixelArray[(currentY + 5) * imgSurface.pitch + (currentX + 11) * imgSurface.format.BytesPerPixel + 2] = r;
                break;
            case 's':
                pixelArray[offsetB] = b;
                pixelArray[offsetG] = g;
                pixelArray[offsetR] = r;
                break;
            default:
                break;
        }
    }

/**
*   The PixelAt function returns the color of the pixel at the given coordinate (x, y) on the image surface. 
*   The function uses SDL_LockSurface and SDL_UnlockSurface functions to access the pixel data of the image 
*   surface safely. It returns an SDL_Color object containing the RGB and alpha values of the pixel.
*/
    SDL_Color PixelAt(int x, int y) {
        SDL_LockSurface(imgSurface);
        scope(exit) SDL_UnlockSurface(imgSurface);
        
        ubyte* pixelArray = cast(ubyte*) imgSurface.pixels;
        int index = y * imgSurface.pitch + x * imgSurface.format.BytesPerPixel;
        int r = pixelArray[index + imgSurface.format.Rshift / 8];
        int g = pixelArray[index + imgSurface.format.Gshift / 8];
        int b = pixelArray[index + imgSurface.format.Bshift / 8];
        int a = pixelArray[index + imgSurface.format.Ashift / 8];
        return SDL_Color(r.to!ubyte, g.to!ubyte, b.to!ubyte, a.to!ubyte);
    }

/**
*   The loadTexture function loads an image file from the given mediaPath and creates an SDL_Texture object 
*   from the loaded surface using the given renderer. It returns the created texture object or null if there was an error
*    loading or creating the texture.
*/
    SDL_Texture* loadTexture(string mediaPath, SDL_Renderer* renderer) {
        auto surface = SDL_LoadBMP(mediaPath.toStringz);
        if (!surface) {
            writeln("Failed to load the image ", mediaPath);
            writeln("SDL Error ", SDL_GetError());
            return null;
        }
        auto texture = SDL_CreateTextureFromSurface(renderer, surface);
        SDL_FreeSurface(surface);
        if (!texture) {
            writeln("Failed to create texture from image ", mediaPath);
            writeln("SDL Error ", SDL_GetError());
            return null;
        }
        return texture;
    }
}