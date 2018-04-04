import shed.mbed.*;
import java.util.ArrayList;
import java.util.Random;
import java.awt.image.BufferedImage;
/**
 * Write a description of class Program here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Program
{

    private MBed mbed;
    
    public static LCD lcd;
    public static Potentiometer p1, p2;
    public static Accelerometer acc;
    public static Piezo piezo;
    
    public static ArrayList<boolean[][]> sketches;
    public static ArrayList<String> sketchNames;
    
    public static Pos brushSize;
    public static ScreenWriter screenWriter;
    
    public static int sleepTime;
    public static int pixelTime;
    
    private Sketch sketch;
    
    public static boolean goToMenu;
    
    // which screen is currently displayed
    // 0 menu, 1 gallery, 2 settings
    int screenIndex;
    // options for main menu
    int menuIndex;
    // opttions for gallery
    int galleryIndex;
    // options for settings
    int settingsIndex;
    
    // gallery specific fields
    int gallerySketchIndex;
    
    //settings specific fields
    int settingsBrushSizeIndex;
    boolean settingsSound;
    boolean settingsTop;
    //public static boolean sketchRun;
    
    boolean menuUpdateScreen;
    boolean galleryUpdateScreen;
    boolean settingsUpdateScreen;
    
    // due to timeouts
    public static boolean busyMBED;
    
    // buffer image for ui
    BufferedImage menuUI;
    BufferedImage galleryUI;
    BufferedImage settingsUI;
    /**
     * Constructor for objects of class Program
     */
    public Program()
    {
        mbed = MBedUtils.getMBed();
        
        lcd = mbed.getLCD();
        
        p1 = mbed.getPotentiometer1();
        p2 = mbed.getPotentiometer2();
        
        acc = mbed.getAccelerometerBoard();
        
        piezo = mbed.getPiezo();
        
        // default brush size
        brushSize = new Pos(1,1);
        
        screenWriter = new ScreenWriter();
        sketch = new Sketch();
        
        sketches = new ArrayList<>();
        sketchNames = new ArrayList<>();
        
        screenIndex = 0;
        menuIndex = 0;
        galleryIndex = 0;
        settingsIndex = 0;
        
        goToMenu = false;
        
        // whether to update that menu
        menuUpdateScreen = true;
        galleryUpdateScreen = false;
        settingsUpdateScreen = false;
        
        // where in the sketch array to display from
        gallerySketchIndex = 0;
        
        // 1.00, 1.25, 1.50, 1.75
        settingsBrushSizeIndex = 1;
        // on/off sound
        settingsSound = true;
        // editing sound or brushSize
        settingsTop = true;
        
        busyMBED = false;
        
        // sketchRun = false;
        
        // minimal sleep time
        sleepTime = 10;
        // min sleep for pixel
        pixelTime = 4;
    
        // image buffer of ui
        menuUI = screenWriter.imageToEtch("MENU.png");
        galleryUI = screenWriter.imageToEtch("GALLERY.png");
        settingsUI = screenWriter.imageToEtch("SETTINGS.png");
        
    }

    public void run() // remember to reset to menu after another function
    {
        Sleep(10);
        // draw main menu before loop
        screenWriter.writeToScreen(menuUI);
        
        // creates all the listeners
        ButtonListener left = (isPressed -> {
            if (isPressed && screenIndex == 0 && !busyMBED){
                // left button menu function
                System.out.println("- menu left");
                if (menuIndex > 0){
                    menuIndex--;
                    menuUpdateScreen = true;
                }
            } else if (isPressed && screenIndex == 1 && !busyMBED){
                // left button gallery function
                System.out.println("- gallery left");
                // do nothing
            } else if (isPressed && screenIndex == 2 && !busyMBED){
                // left button settings function
                System.out.println("- settings left");
                if (settingsTop){
                    if (settingsBrushSizeIndex > 1){
                        settingsBrushSizeIndex -= 1;
                        settingsUpdateScreen = true;
                    }
                } else {
                    if (!settingsSound){
                        settingsSound = true;
                        settingsUpdateScreen = true;
                    }
                }
            }
        });
        mbed.getJoystickLeft().addListener(left);
        
        ButtonListener right = (isPressed -> {
            if (isPressed && screenIndex == 0 && !busyMBED){
                // right button menu function
                System.out.println("- menu right");
                if (menuIndex < 2){
                menuIndex++;
                menuUpdateScreen = true;
            }
            } else if (isPressed && screenIndex == 1 && !busyMBED){
                // right button gallery function
                System.out.println("- gallery right");
                // do nothing
            } else if (isPressed && screenIndex == 2 && !busyMBED){
                // right button settings function
                System.out.println("- settings right");
                if (settingsTop){
                    if (settingsBrushSizeIndex < 4){
                        settingsBrushSizeIndex += 1;
                        settingsUpdateScreen = true;
                    }
                } else {
                    if (settingsSound){
                        settingsSound = false;
                        settingsUpdateScreen = true;
                    }
                }
            }
        });
        mbed.getJoystickRight().addListener(right);
        
        ButtonListener up = (isPressed -> {
            if (isPressed && screenIndex == 0 && !busyMBED){
                // up button menu function
                System.out.println("menu up");
                // do nothing
            } else if (isPressed && screenIndex == 1 && !busyMBED){
                // up button gallery function
                System.out.println("- gallery up");
                if (galleryIndex > 0){
                    // if at the top of the screen, 
                    // move through the sketches list
                    System.out.println("- scroll up");
                    galleryIndex--;
                } else if (galleryIndex == 0 && gallerySketchIndex > 0){
                    //(galleryIndex < 1 && gallerySketchIndex > 0 && sketchNames.size() >= 3)
                    // move selector up if there are any more sketches
                    System.out.println("- scroll up");
                    gallerySketchIndex--;
                }
                galleryUpdateScreen = true;
            } else if (isPressed && screenIndex == 2 && !busyMBED){
                // up button settings function
                System.out.println("- settings up");
                settingsTop = true;
                settingsIndex--;
                settingsUpdateScreen = true;
            }
        });
        mbed.getJoystickUp().addListener(up);
        
        ButtonListener down = (isPressed -> {
            if (isPressed && screenIndex == 0 && !busyMBED){
                // down button menu function
                System.out.println("- menu down");
                // do nothing
            } else if (isPressed && screenIndex == 1 && !busyMBED){
                // down button gallery function
                System.out.println("- gallery down");
                //(sketchNames.size() > 0 && galleryIndex <= sketches.size() && gallerySketchIndex < sketchNames.size())
                if(galleryIndex < 2 && (sketchNames.size()-1) > galleryIndex){
                    // move selector down if there are any more sketches
                    System.out.println("- move down");
                    galleryIndex++;
                    galleryUpdateScreen = true;
                    //(sketchNames.size() > 0 &&  gallerySketchIndex > 2 && gallerySketchIndex < sketchNames.size())
                } else if (galleryIndex >= 2 && (sketchNames.size()-1) > galleryIndex){
                    // if at the bottom of the screen, 
                    // move through the sketches list
                    System.out.println("- scroll down");
                    gallerySketchIndex++;
                    galleryUpdateScreen = true;
                }
            } else if (isPressed && screenIndex == 2 && !busyMBED){
                // down button settings function
                System.out.println("- settings down");
                settingsIndex++;
                settingsUpdateScreen = true;
                settingsTop = false;
            }
        });
        mbed.getJoystickDown().addListener(down);
        
        ButtonListener fire = (isPressed -> {
            if (isPressed && screenIndex == 0 && !busyMBED){
                // fire button menu function
                System.out.println("- menu fire");
                switch(menuIndex){
                    case 0 : // run sketch
                        System.out.println("run sketch");
                        // sketchRun = true;
                        sketch.run();
                        menuUpdateScreen = true;
                        menuIndex = 0;
                        screenIndex = 0;
                        System.out.println("back at main menu");
                        break;
                    case 1 : // run gallery
                        System.out.println("run gallery");
                        Sleep(sleepTime);
                        screenWriter.writeToScreen(galleryUI);
                        System.out.println("wrote ui to screen");
                        galleryUpdateScreen = true;
                        // changes listeners to run gallery code
                        screenIndex = 1;
                        break;
                    case 2 : // run settings
                        System.out.println("run settings");
                        Sleep(sleepTime);
                        screenWriter.writeToScreen(settingsUI);
                        System.out.println("wrote ui to screen");
                        settingsUpdateScreen = true;
                        // changes listeners to run settings code
                        screenIndex = 2;
                        break;
                }
            } else if (isPressed && screenIndex == 1 && sketches.size() > 0 && !busyMBED){
                // fire button gallery function
                System.out.println("- gallery fire");
                try{
                    screenWriter.load(sketches.get(galleryIndex));
                } catch(Exception e){
                    System.out.println("cannot find sketch");
                }
                // loads image to screen
            } else if (isPressed && screenIndex == 2 && !busyMBED){
                // fire button settings function
                System.out.println("- settings fire");
                // do nothing
            }
        });
        mbed.getJoystickFire().addListener(fire);
        
        //positions for options selection
        Pos[] menuOptions = new Pos[3];
        menuOptions[0] = new Pos(8,27);
        menuOptions[1] = new Pos(45,27);
        menuOptions[2] = new Pos(90,27);
        
        Pos[] galleryOptions = new Pos[3];
        galleryOptions[0] = new Pos(112,3);
        galleryOptions[1] = new Pos(112,13);
        galleryOptions[2] = new Pos(112,23);
        
        Pos[] settingsOptions = new Pos[2];
        settingsOptions[0] = new Pos(10,13);
        settingsOptions[1] = new Pos(10,26);
        
        Pos[] settingsSoundOption = new Pos[2];
        settingsSoundOption[0] = new Pos(80,18);
        settingsSoundOption[1] = new Pos(92,18);
        
        while (true) {
            Sleep(50);
            if (menuUpdateScreen && !busyMBED){
                System.out.println("menu update");
                Sleep(sleepTime);
                
                // clear previous menu selector
                lcd.drawLine(0,27,lcd.getWidth(),27,PixelColor.WHITE);
                Sleep(sleepTime);
                
                // draw menu selector
                lcd.drawLine(menuOptions[menuIndex].getX(), 
                             menuOptions[menuIndex].getY(),
                             menuOptions[menuIndex].getX() + 20, 
                             menuOptions[menuIndex].getY(), 
                             PixelColor.BLACK);
                Sleep(sleepTime);
                
                menuUpdateScreen = false;
                System.out.println("menu updated");
            } else if (galleryUpdateScreen && !busyMBED){
                System.out.println("gallery update");
                // prints atleast the amount of sketches made
                if (sketches.size() > 0){
                    Sleep(sleepTime);
                    lcd.print(0,1,sketchNames.get(gallerySketchIndex));
                    if (sketches.size() > 1){
                        lcd.print(0,11,sketchNames.get(gallerySketchIndex+1));
                        if (sketches.size() > 2){
                            lcd.print(0,21,sketchNames.get(gallerySketchIndex+2));
                        }
                    }
                }
                // clears selection
                Sleep(sleepTime);
                lcd.fillRectangle(112,3,4,5,PixelColor.WHITE);
                Sleep(sleepTime);
                lcd.fillRectangle(112,13,4,5,PixelColor.WHITE);
                Sleep(sleepTime);
                lcd.fillRectangle(112,23,4,5,PixelColor.WHITE);
                Sleep(sleepTime);
                // draws selected box
                lcd.fillRectangle(galleryOptions[galleryIndex].getX(),
                                  galleryOptions[galleryIndex].getY(),
                                  4,5,PixelColor.BLACK);
                galleryUpdateScreen = false;
                System.out.println("gallery updated");
            } else if (settingsUpdateScreen && !busyMBED){
                System.out.println("settings update");
                if (settingsTop){
                    // changes brush size
                    switch(settingsBrushSizeIndex){
                        case 1 : brushSize.setX(1);
                                 brushSize.setY(1);
                                 break;
                        case 2 : brushSize.setX(2);
                                 brushSize.setY(2);
                                 break;
                        case 3 : brushSize.setX(4);
                                 brushSize.setY(4);
                                 break;
                        case 4 : brushSize.setX(8);
                                 brushSize.setY(8);
                                 break;
                    }
                    // displays brush size
                    lcd.drawLine(63, 7, 121, 7, PixelColor.WHITE);
                    Sleep(sleepTime);
                    lcd.drawLine(63, 7, 65 + (settingsBrushSizeIndex * 14), 7, PixelColor.BLACK);
                    System.out.println("brush size updated");
                } else {
                    if (settingsSound){
                        lcd.fillRectangle(settingsSoundOption[0].getX(),
                                          settingsSoundOption[0].getY(),
                                          8, 6, PixelColor.BLACK);
                        Sleep(sleepTime);
                        lcd.fillRectangle(settingsSoundOption[1].getX(),
                                          settingsSoundOption[1].getY(),
                                          8, 6, PixelColor.WHITE);
                    } else {
                        lcd.fillRectangle(settingsSoundOption[0].getX(),
                                          settingsSoundOption[0].getY(),
                                          8, 6, PixelColor.WHITE);
                        Sleep(sleepTime);
                        lcd.fillRectangle(settingsSoundOption[1].getX(),
                                          settingsSoundOption[1].getY(),
                                          8, 6, PixelColor.BLACK);
                    }
                    System.out.println("sound updated");
                }
                settingsUpdateScreen = false;
            } else if ((!busyMBED && screenIndex != 0 && !shake()) || goToMenu){
                System.out.println("shake'd");
                menuUpdateScreen = true;
                goToMenu = false;
                // sketchRun = false;
                Sleep(sleepTime);
                screenWriter.writeToScreen(menuUI);
                menuIndex = 0;
                galleryIndex = 0;
                gallerySketchIndex = 0;
                settingsIndex = 0;
                screenIndex = 0;
                System.out.println("back at main menu");
                Sleep(sleepTime);
            }
            Sleep(sleepTime);
        }
    }
    
    public static boolean shake()
    {
        int screenX = brushSize.getX(); //screen size
        int screenY = brushSize.getY(); //screen size
        boolean screenLEDs[][] = new boolean[screenX][screenY];
        int randx, randy;
        
        int totalChange = screenX*screenY;
        
        Random randNum = new Random();
                    
        randx = randNum.nextInt() * screenX;
        randy = randNum.nextInt() * screenY;
        
        Sleep(sleepTime);
        if (!busyMBED){
            busyMBED = true;
            try {
                if (acc.getAcceleration().getMagnitude() > 2){ 
                    // erase false
                    Sleep(sleepTime);
                    lcd.clear();
                    Sleep(sleepTime);
                    System.out.println("ERASED");
                    return false;
                }
            } catch(Exception e){
                 System.out.println(e);
            }
            busyMBED = false;
        }
        return true;
    }
    
    public static void Sleep(int milli)
    {
        try{
            //System.out.println("sleep: " + milli);
            Thread.sleep(milli);
        } catch (InterruptedException e){
            System.out.println(e);
        }
    }
    
    public void finish()
    {
        mbed.close();
    }
}
