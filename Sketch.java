import shed.mbed.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Write a description of class EtchASketch here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Sketch
{
    private Potentiometer xPot, yPot;
    private LCD lcd;
    private Button exitButton;
    private Accelerometer acc;
    
    private int xMax, yMax;
    private int lastX, lastY, newX, newY;
    private int xr, yr;
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    //Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    //private boolean stop;
    
    public Sketch()
    {        
        xPot = Program.p1;
        yPot = Program.p2;
        
        lcd = Program.lcd;
        
        xMax = lcd.getWidth();
        yMax = lcd.getHeight();
        
        xr = Program.brushSize.getX();
        yr = Program.brushSize.getY();
        
        acc = Program.acc;
        
        lastX = 0;
        lastY = 0;
        newX = 0;
        newY = 0;
    }
    
    public void run()
    {
        Program.busyMBED = true;
        lcd.clear();
        Program.Sleep(Program.sleepTime);
        
        boolean[][] sketch = new boolean[128][32];
        
        xr = Program.brushSize.getX();
        yr = Program.brushSize.getY();
        
        boolean loop = true;
        
        while(loop){
            Program.Sleep(Program.sleepTime); 
            newX = getCoordinate(xPot.getValue(),xMax);
            newY = getCoordinate((1 - yPot.getValue()),yMax);
            
            if(!(lastX == newX) || !(lastY == newY)){
                lcd.fillRectangle(newX, newY, xr, yr, PixelColor.BLACK);
                lastX = newX;
                lastY = newY;
                sketch[newX][newY] = true;
            }
            Program.Sleep(Program.sleepTime);
            
            if(acc.getAcceleration().getMagnitude() > 2){
                loop = false;
            }
        }
        
        Program.sketches.add(sketch);
        Program.sketchNames.add(sdf.format(new Timestamp(System.currentTimeMillis())));
        Program.Sleep(100);
        lcd.clear();
        Program.busyMBED = false;
        Program.goToMenu = true;
        System.out.println("left sketch");
    }
    
    /**
     * Using both potentiometer values
     * decides the XY position on the screen
     */
    private int getCoordinate(double pot, int range)
    {
        int x = (int) Math.round(pot * range);
        
        if(x < range){
            return x;
        }else{
            System.out.println("coordfailed: " + x);
            return 0;
        }
    }
    
}
