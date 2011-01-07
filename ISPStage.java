import javax.swing.JPanel;
import java.lang.Thread;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.*;

import js.math.LineSegment;


public class ISPStage extends JPanel implements Runnable
{
	private static int WIDTH;  //size of stage
	private static int HEIGHT;

	private Thread animator; //does the animation

	//volatile so that these variables don't get copied to local memory, so that the changes to them are immediately visible to all threads

	private volatile boolean running = false; //stops the animation
	private volatile boolean gameOver = false; //ends the game
	private volatile boolean isPaused;//pauses the game

	int period = 20; //20 milliseconds per animation cycle (50 fps)
	private static final int NO_DELAYS_PER_YIELD = 16;
	private static final int MAX_FRAME_SKIPS = 5; //the max # of frames skipped in the animation loop (updated but not rendered) before u stop trying to fix it (would cause flickers if too many frames were skipped)

	private static HashSet<ISPSprite> sprites = new HashSet<ISPSprite>();
	private static HashSet<ISPSprite> spritesToBeRemoved = new HashSet<ISPSprite>();
	private static boolean[][] grid;
	private static int gridSize = 16;

	private Color backcolor;

	//drawing variables
	private Graphics dbg;
	//probably a newer, better way to do this than the awt.Image class, but that'll be later
	private Image dbImage = null;

	//game variables


	/*int current = 0;
	JSShape shape1, shape2;
	char shape = (char)0;
	ArrayList<Point> pointsToBeAdded = new ArrayList<Point>();*/


	public ISPStage()
	{
		WIDTH = 640;
		HEIGHT = 480;
		backcolor = Color.WHITE;
		setBackground(backcolor);
		setPreferredSize( new Dimension(WIDTH,HEIGHT) );
		setFocusable(true);
		requestFocus();		//JPanel now recieves key events
		readyForKeyPresses();

		//set up grids
		grid = new boolean[(int)(Math.ceil(getHeight()/gridSize))][(int)(Math.ceil(getWidth()/gridSize))];

		//add game components

		//listen for mouse presses
		addMouseListener
		(
			new MouseAdapter()
			{
				public void mousePressed(MouseEvent e)
				{
					testPress(e.getX(),e.getY());
				}
			}
		);
	}

	public ISPStage(int w, int h)
	{
		this();
		WIDTH = w;
		HEIGHT = h;
	}

	public ISPStage(int w, int h, Color c)
	{
		this(w,h);
		backcolor = c;
		setBackground(backcolor);
	}

	/*
	 *public ISPStage(int w, int h, some sort of image i)
	 *{
	 	this(w,h);
	 	initialize the image background somehow
	  }
	 **/

	public void addNotify()
	//Wait for the JPanel to be added to the JFrame/JApplet before starting
	{
		super.addNotify();
		startGame();
	}

	private void startGame()
	//init + start Thread
	{
		if(animator == null || !running)
		{
			animator = new Thread(this);
			animator.start();
		}
	}


	public void run()
	//repeatedly update, render, sleep so cycle takes close to period ms
	//Overruns in updates/renders will cause the cycle to update but not render, in order to achieve as close to the requested UPS as possible
	{
		long beforeTime,afterTime,timeDiff,sleepTime;
		long overSleepTime = 0L;//handles sleep inaccuracies
		long excess = 0L;
		int noDelays = 0;

		beforeTime = System.currentTimeMillis();

		running = true;
		while (running)
		{
			gameUpdate(); //game state is updated
			gameRender(); //render to a buffer
			paintScreen();//draw buffer to screen

			afterTime = System.currentTimeMillis();
			timeDiff = afterTime - beforeTime;
			sleepTime = period - timeDiff - overSleepTime;

			if(sleepTime > 0) //some time left in this cycle
			{
				try
				{
					Thread.sleep(sleepTime);
				}
				catch(InterruptedException ex)
				{

				}
				//whether the sleep time was actually what u said it should be
				overSleepTime = System.currentTimeMillis() - afterTime - sleepTime;
			}
			else // sleepTime <=0 frame updating + rendering took longer than period ms
			{
				excess -= sleepTime; //stores the amount of time that the frame process went over
				overSleepTime = 0L;

				noDelays++;
				if(noDelays >= NO_DELAYS_PER_YIELD)
				{
					Thread.yield();  //give any other threads that were being held up by this thread's constant animation a chance to run
					noDelays = 0;
				}
			}


			beforeTime = System.currentTimeMillis();

			//if frame animation is taking too long, update the game but don't render it
			int skips = 0;
			while( 	(excess > period) //total time missed is at least one frame
					&&
					(skips < MAX_FRAME_SKIPS) ) //haven't skipped too many frames yet
			{
				excess -= period;
				gameUpdate(); //update state but don't render
				skips++;
			}
		}
		System.exit(0); //so enclosing JFrame/JApplet exits
	}

	private void gameUpdate()
	{
		if(!isPaused&&!gameOver)
		{
			if(sprites.size()>0)
			{
				for(ISPSprite spr: sprites)
				{
					spr.act();
					if(!(isInBounds(spr)))
					{
						spr.offStage(this);
					}
				}
				for(ISPSprite a: sprites)
				{
					if(spritesToBeRemoved.contains(a))
					{
						continue;
					}
					for(ISPSprite b: sprites)
					{
						if(spritesToBeRemoved.contains(b))
						{
							continue;
						}
						if(a!=b && a.intersects(b))
						{
							a.collision(b);
						}
					}
				}
				flushSprites();
			}

		}
	}

	//more methods
	private void gameRender()
	//draw the current frame to an image buffer
	{
		if(dbImage == null)
		//create the buffer
		{
			dbImage = createImage(WIDTH+10,HEIGHT+10);//for some reason it doesn't fill whole screen.....
			if(dbImage == null)
			//failed to create a buffer image
			{
				System.out.println("dbImage is null");
				return; //Mr. Elrod
			}
			else
			{
				dbg = dbImage.getGraphics();
			}
		}

		//clear the background
		dbg.setColor(backcolor);
		dbg.fillRect(0,0,WIDTH,HEIGHT);//for some reason it doesn't fill whole screen....

		//draw game elements
		//drawGrid((Graphics2D)dbg);
		dbg.setColor(Color.BLACK);
		if(sprites.size()>0)
		{
			for(ISPSprite spr: sprites)
			{
				spr.draw((Graphics2D)dbg);
			}
		}
		/*if(pointsToBeAdded != null)
		{
			ArrayList<Point> ptt = pointsToBeAdded;
			for(int i = 0; i<ptt.size();i++)
			{
				dbg.fillOval((int)(ptt.get(i).getX()-3),(int)(ptt.get(i).getY()-3),6,6);
			}
		}
		if(shape1!=null)
		{
			shape1.drawSelf((Graphics2D)dbg);
		}
		if(shape2!=null)
		{
				shape2.drawSelf((Graphics2D)dbg);
		}*/

		if(gameOver)
		{
			gameOverMessage(dbg);
		}
	}

	private void gameOverMessage(Graphics g)
	//center + draw the game over message
	{
		//code to calculate x and y
		//g.drawString(msg,x,y);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if(dbImage != null)
		{
			g.drawImage(dbImage,0,0,null);
		}
	}

	private void readyForKeyPresses()
	{
		addKeyListener
		(
			new KeyAdapter()
			{
				public void keyTyped(KeyEvent e)
				{
					int keyCode = e.getKeyCode();
					if	(
								(keyCode == KeyEvent.VK_ESCAPE) ||
								(keyCode == KeyEvent.VK_Q) 		||
								(keyCode == KeyEvent.VK_END) 	||
							( 	(keyCode == KeyEvent.VK_C) && e.isControlDown() )
						)
						{
							running = false;
						}

					/*if (keyCode == KeyEvent.VK_E)
					{
						staISPhape('e');
					}
					if (keyCode == KeyEvent.VK_P)
					{
						staISPhape('p');
					}
					if (keyCode == KeyEvent.VK_L)
					{
						staISPhape('l');
					}
					if (keyCode == KeyEvent.VK_I)
					{
						testShapes();
					}
					if (keyCode == KeyEvent.VK_F)
					{
						finishShape(shape);
					}
					if(keyCode == KeyEvent.VK_R)
					{
						System.out.println("Reset");
						shape1 = shape2 = null;
						current = 0;
						pointsToBeAdded = new ArrayList<Point>();
					}*/
				}
			}
		);
	}

	private void testPress(int x, int y)
	//see if (x,y) is important to the game
	{
		if(!gameOver)
		{
			if(!isPaused)
			{
				//do something
			}
			else
			{
				//do something with pause menu
			}
		}
	}

	private void paintScreen()
	//actively render the buffer image to the screen
	{
		Graphics g;
		try
		{
			g = this.getGraphics(); 	//get the JPanel's graphics context
			if( ( g != null ) && ( dbImage != null ) )
			{
				g.drawImage(dbImage,0,0,null);
			}
			Toolkit.getDefaultToolkit().sync();	//sync the display on some systems
			g.dispose();
		}
		catch(Exception e)
		{
			System.out.println("Graphics context error: "+e);
		}
	}

	//management methods

	public void pauseGame()//should this be synchronized?
	{
		isPaused = true;
	}

	//synchronized so that the animation thread doesn't miss the notification and remain suspended/running indefinately

	public synchronized void resumeGame()
	{
		isPaused = false;
		notify();
	}

	public synchronized void stopGame()
	{
		running = false;
		notify();
	}

	//ISP specific methods

	public void addSprite(ISPSprite spr)
	{
		sprites.add(spr);
		if(spr instanceof Obstacle)
		{
			//do this for all grids
			System.out.println("Recalculating grid");
			recalcGrid(spr);
		}
	}

	public void removeSprite(ISPSprite spr)
	{
		spritesToBeRemoved.add(spr);
	}
	
	public void flushSprite(ISPSprite spr)
	{
		sprites.remove(spr);
		if(spr instanceof Obstacle)
		{
			/*
			int startX = (spr.getX()/gridSize-spr.getSq());
			int startY = (spr.getY()/gridSize-spr.getSq());
			recalcGrid(startX,startY,spr.getSq()*2,spr.getSq()*2);
			*/
			recalcGrid(spr);
		}
	}

	public void clearSprites()
	{
		for(ISPSprite s: sprites)
		{
			spritesToBeRemoved.add(s);
		}
	}

	public void flushSprites()
	{
		for(ISPSprite sp: spritesToBeRemoved)
		{
			flushSprite(sp);
		}
		spritesToBeRemoved.clear();
	}

	//public void recalc grid(grid to recalc)
	//overload it with one that does only a specifiec section of the grid


	/*private void finishShape(char c)
	{
		if(current < 1)
		{
			System.out.println("You haven't started making shapes yet!");
			return;
		}
		if(current > 2)
		{
			System.out.println("Already have 2 shapes!");
			return;
		}
		if(c != 'e' && c != 'p' && c != 'l')
		{
			System.out.println("Cannot finish shape if no specified shape chosen.");
			return;
		}
		else if(c == 'e')
		{
			if(pointsToBeAdded.size() != 2)
			{
				System.out.println("Cannot finish ellipse, incorrect number of points.");
				return;
			}
			double rot = 0.0;//get them to enter rot
			double x = pointsToBeAdded.get(0).getX()+pointsToBeAdded.get(1).getX();
			x/=2;
			double y = pointsToBeAdded.get(0).getY()+pointsToBeAdded.get(1).getY();
			y/=2;
			double xr = Math.abs(pointsToBeAdded.get(0).getX()-x);
			double yr = Math.abs(pointsToBeAdded.get(0).getY()-y);
			if(current==1)
			{
				shape1 = new JSEllipse(x,y,xr,yr,rot);
			}
			else
			{
				shape2 = new JSEllipse(x,y,xr,yr,rot);
			}
			System.out.println("Finished Ellipse.");
		}
		else if(c== 'l')
		{
			if(current==1)
			{
				shape1 = new LineSegment(pointsToBeAdded.get(0),pointsToBeAdded.get(1));
			}
			else
			{
				shape2 = new LineSegment(pointsToBeAdded.get(0),pointsToBeAdded.get(1));
			}
			System.out.println("Finished Line Segment.");
		}
		else //(c == 'p')
		{
			if(pointsToBeAdded.size() <= 2)
			{
				System.out.println("Cannot finish polygon, not enough points.");
				return;
			}
			JSPolygon p = new JSPolygon(JSShape.getMedianPoint(pointsToBeAdded),0);
			for(Point pt: pointsToBeAdded)
			{
				p.add(pt);
			}
			if(current==1)
			{
				shape1 = p;
			}
			else
			{
				shape2 = p;
			}
			System.out.println("Finished Polygon.");
		}
		pointsToBeAdded = new ArrayList<Point>();
		shape = (char)0;
	}

	private void staISPhape(char c)
	{
		if(current>1)
		{
			System.out.println("No more shapes to initialize.");
		}
		if(shape != (char)(0) )
		{
			System.out.println("Shape already initialized!");
		}
		System.out.println("Starting shape: "+c);
		shape = c;
		current++;
	}

	private void testShapes()
	{
		if(shape1 != null && shape2 != null)
		{
			System.out.println("Testing intersection: "+shape1.intersection(shape2));
			shape1 = shape2 = null;
			current = 0;
		}
		else
		{
			System.out.println("Incorrect parameters to be able to test intersection, make sure both shapes are initialized.");
		}
	}*/

	private LinkedList<ISPSprite> getSpritesByDepth()
	{
		LinkedList<ISPSprite> result = new LinkedList<ISPSprite>();
		for(ISPSprite s: sprites)
		{
			if(result.size()==0)
			{
				result.add(s);
				continue;
			}
			int d = result.get(0).getDepth();
			int pos = 0;
			for(int i = 0; i<result.size();i++)
			{
				if(result.get(i).getDepth()>d)
				{
					d = result.get(i).getDepth();
					pos = i;
				}
			}
			result.add(pos,s);
		}
		return result;
	}

	/*public int getWidth()
	{
		return WIDTH+10;
	}
	public int getHeight()
	{
		return HEIGHT+10;
	}*/

	public int getWidth()
	{
		return WIDTH;
	}

	public int getHeight()
	{
		return HEIGHT;
	}

	public static boolean[][] getGrid()
	{
		return grid;
	}

	public static int getGridSize()
	{
		return gridSize;
	}

	private boolean isInBounds(ISPSprite s)
	{
		return (s.getX() >= 0 && s.getX()<=getWidth() && s.getY() >=0 && s.getY() <= getHeight() );
	}

	public static HashSet<ISPSprite> getSprites()
	{
		return sprites;
	}

	private void recalcGrid(ISPSprite spr)
	{
		if(spr == null)
		{
			throw new IllegalArgumentException("Can't recalc grid if Sprite is null");//probably change this...
		}

		//int dist = (int)(spr.getShape().maxDist());
		//int startX = (spr.getX()-dist)/gridSize;
		//int startY = (spr.getY()-dist)/gridSize;
		int dist = spr.getSq();
		int startX = (int)(spr.getX()/gridSize)-spr.getSq();
		int startY = (int)(spr.getY()/gridSize)-spr.getSq();
		if( grid==null || !( Pathfinder.isInGrid(new Point(startX,startY),grid) ) || !( Pathfinder.isInGrid(new Point(startX+(dist*2)/gridSize,startY+(dist*2)/gridSize),grid) ) )
		{
			//throw new IllegalArgumentException("recalcGrid()");
			System.out.println("Exception in recalcGrid(): grid == null: "+(grid==null)+", otherwise, bad point");
		}
		Point p = new Point();
		for(int r = startY;r<startY+dist*2;r++)
		{
			for(int c = startX;c<startX+dist*2;c++)
			{
				p.setLocation((int)(c),(int)(r));
				Rectangle2D rec = new Rectangle2D.Double(c*gridSize+2,r*gridSize+2,gridSize-4,gridSize-4);
				if(Pathfinder.isInGrid(p,grid)&&spr.getShape().intersectsRect(rec))
					grid[r][c] = true;
			}
		}
	}
}