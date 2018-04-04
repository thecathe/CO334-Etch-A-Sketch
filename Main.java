
/**
 * Write a description of class Main here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Main
{
    
    private Main(){}   
    
    public static void main(String[] args)
    {
        
        Program prog = new Program();
        System.out.println("--------------------------------------");
        System.out.println("mbed run");
        prog.run();
        prog.finish();
        System.out.println("mbed Close");
        
        System.exit(0);
    }
}
