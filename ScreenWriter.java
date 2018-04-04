import shed.mbed.*;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Color;
/**
 * Write a description of class ScreenWriter here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class ScreenWriter
{

    LCD lcd;
    
    BufferedImage img = null;
    
    /**
     * Constructor for objects of class ScreenWriter
     */
    public ScreenWriter()
    {
        lcd = Program.lcd;
    }
    
    public void writeToScreen(BufferedImage img)
    {
        Program.busyMBED = true;
        Program.Sleep(Program.sleepTime);
        lcd.clear();
        Program.Sleep(Program.sleepTime);
        if(img.getWidth() <= 128 && img.getHeight() <= 32){
            Color color = new Color(0xfff);
            for(int y = 0; y < img.getHeight(); y++){
                for(int x = 0; x < img.getWidth(); x++) {
                    // test defining color here
                    color = new Color(img.getRGB(x,y));
                    if(color.equals(Color.BLACK)){
                        // print pixel to screen
                        //System.out.println(x + "," + y);
                        lcd.setPixel(x,y,PixelColor.BLACK);
                        Program.Sleep(Program.pixelTime);
                    }
                }
            }
        }
        Program.busyMBED = false;
    }
    
    public void load(boolean[][] sketch)
    {
        Program.busyMBED = true;
        lcd.clear();
        int xr = Program.brushSize.getX();
        int yr = Program.brushSize.getY();
        System.out.println("load");
        Program.Sleep(10);
        for(int y = 0; y<32;y++){
            for(int x = 0; x < 128; x++){
                if(sketch[x][y]){
                    //lcd.setPixel(x,y,PixelColor.BLACK); // debugging to see where it saves
                    lcd.fillRectangle(x, y, xr, yr, PixelColor.BLACK);
                    Program.Sleep(Program.pixelTime);
                }
            }
        }
        Program.busyMBED = false;
    }
    
    public BufferedImage imageToEtch(String path)
    {
        BufferedImage img = null;
        try {
            img = ImageIO.read(getClass().getResource("/resources/" + path));
            return img;
        } catch(IOException e) {
            System.out.println(e);
        }
        return null;
    }
}
