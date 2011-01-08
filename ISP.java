/**
 * @(#)ISP.java
 *
 *
 * @author
 * @version 1.00 2010/3/14
 */

 import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class ISP extends JFrame implements WindowListener
{

    /**
     * Creates a new instance of <code>ISP</code>.
     */

    private ISPStage myStage;
    public ISP()
    {
    	super("ISP");
    	myStage = new ISPStage();
    	this.getContentPane().add(myStage,"Center");
        addWindowListener(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);//
        pack();//switching these two caused unwanted size problems.....
    	setVisible(true);
    	myStage.addSprite(new Algae());
    	myStage.addSprite(new Algae());
    	System.out.println("panel dimensions: "+myStage.getSize());
    	System.out.println("frame dimensions: "+this.getSize());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
    	new ISP();
    }

    public void windowActivated(WindowEvent e)
  	{ myStage.resumeGame();  }

  	public void windowDeactivated(WindowEvent e)
  	{  myStage.pauseGame();  }


  	public void windowDeiconified(WindowEvent e)
  	{  myStage.resumeGame();  }

  	public void windowIconified(WindowEvent e)
  	{  myStage.pauseGame(); }

  	public void windowClosing(WindowEvent e)
  	{  myStage.stopGame();  }


  	public void windowClosed(WindowEvent e) { System.exit(0); }
  	public void windowOpened(WindowEvent e) {}
}
