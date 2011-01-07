import javax.swing.JPanel;
import java.lang.Thread;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import java.awt.geom.*;

import js.math.*;
import js.physics.*;

public abstract class ISPSprite
{
	private double x,y;
	private int depth;
	private int gridSize, numSq, pathPointer;
	private JSShape myShape;
	private BufferedImage myImage;
	private js.physics.Vector myVector;
	private LinkedList<Point> path;
	
	public ISPSprite()
	{
		x = 0;
		y = 0;
		myVector = new js.physics.Vector();
		depth = 0;
		gridSize = 0;
	}

	public ISPSprite(int xx, int yy)
	{
		this((double)xx,(double)yy);
	}

	public ISPSprite(double xx, double yy)
	{
		this();
		x = xx;
		y = yy;
	}

	public ISPSprite(int xx, int yy, int d, int sq)
	{
		this((double)xx,(double)yy,d,sq);
	}

	public ISPSprite(double xx, double yy, int d, int sq)
	{
		this(xx,yy);
		depth = d;
		numSq = sq;
	}

	public ISPSprite(double xx, double yy, js.physics.Vector v, int d, int sq)
	{
		this(xx,yy,d,sq);
		myVector = v;
	}

	public ISPSprite(int xx, int yy, js.physics.Vector v, int d, int sq)
	{
		this((double)xx,(double)yy,v,d,sq);
	}

	public void loadImage(String s)
	{
		//use ImageLoader to get the image from the file
		//myImage = JSImageLoader.loadImage()?
	}

	//physics
	public void path(Point destination)
	{
		if(gridSize==0)
		{
			gridSize = ISPStage.getGridSize();
		}
		Point a = new Point((int)(x/gridSize),(int)(y/gridSize));
		Point b = new Point((int)(destination.getX()/gridSize),(int)(destination.getY()/gridSize));
		int xDif = (int)(x)%gridSize;
		int yDif = (int)(y)%gridSize;
		if(numSq>1)
		{
			if(numSq%2==0)//even
			{
				//snap to closest point
				if(xDif>7)//snap up
				{
					a.translate(1,0);
					b.translate(1,0);
					xDif-=16;
				}
				if(yDif>7)//snap up
				{
					a.translate(0,1);
					b.translate(0,1);
					yDif-=16;
				}
			}
			else//odd
			{
				//already snapped

			}
			a.translate(-1*(int)(numSq/2),-1*(int)(numSq/2));
			b.translate(-1*(int)(numSq/2),-1*(int)(numSq/2));
			xDif += gridSize*(int)(numSq/2);
			yDif += gridSize*(int)(numSq/2);
		}
		//LinkedList<Point> temp = Pathfinder.findPath(a,b,ISPStage.getGrid());
		//path = new LinkedList<Point>();
		path = Pathfinder.findPath(a,b,ISPStage.getGrid(),gridSize,numSq,xDif,yDif);
		if(path/*temp*/ == null || path/*temp*/.size() == 0)
		{
			System.out.println("Path failzors");
			return;
		}
		/*for(Point p: temp)
		{
			path.add( new Point((int)(p.getX()*gridSize+xDif),(int)(p.getY()*gridSize+yDif)) );
		}*/
		pathPointer = 0;
	}


	//physics
	//all accessor and modifier methods necessary
	public void setDepth(int d) { depth = d; }
	public void setX(int xx) { x=xx; }
	public void setY(int yy) { y=yy; }
	public void setX(double xx) { x=xx; }
	public void setY(double yy) { y=yy; }
	public void setVector(js.physics.Vector v) { myVector = v; }
	public int getDepth() { return depth; }
	public int getX() { return (int)(x); }
	public int getY() { return (int)(y); }
	public double getdX() { return x; }
	public double getdY() { return y; }
	public js.physics.Vector getVector() { return myVector; }
	public int getGridSize() { return gridSize; }
	public LinkedList<Point> getPath() { return path; }
	public int getSq() { return numSq; }

	protected void setShape(JSShape sh) { myShape = sh; }

	//methods that subclasses must implement

	//physics
	public void act()
	{
		
		//all below this replaced
		if(path!=null)
		{
			if(path.size()==0)
			{
				path = null;
				move();
				return;
			}
			if(Calc.pointDistance(new Point((int)x,(int)y),path.get(pathPointer))<myVector.getMag())
			{
				pathPointer++;
				if(pathPointer>=path.size())
				{
					path = null;
					pathPointer = -1;
					myVector.setMag(0);
					return;
				}
				myVector.setDir(Calc.pointDirection(new Point((int)x,(int)y),path.get(pathPointer)));
			}
		}
		move();
		//subclasses should call this and then implement their own functionality
	}

	public void move()//don't need
	{
		double xChange = Math.cos(myVector.getDir()*(Math.PI/180))*myVector.getMag();
		double yChange = Math.sin(myVector.getDir()*(Math.PI/180))*myVector.getMag();
		x+=xChange;
		y-=yChange;
	}

	public void draw(Graphics2D g)
	{
		//g.fillOval((int)(x-8),(int)(y-8),16,16);
		//subclasses should overload this and then implement their own functionality
	}

	public String toString()
	{
		return "ISPSprite";
		//subclasses should override this with their proper functionality
	}

	public abstract JSShape getShape();
	//subclasses must implement this, returning their default shape rotated over direction


	public boolean intersects(ISPSprite other)
	{
		return this.getShape().intersection(other.getShape());
	}

	//called if 2 sprites intersect one another
	public void collision(ISPSprite other)
	{

	}

	public void offStage(ISPStage yeah)
	{
		yeah.removeSprite(this);
	}
	//subclasses should override this if they don't want to die when they go off the stage
}