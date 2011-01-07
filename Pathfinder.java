
 import java.awt.*;
 import java.awt.geom.*;
 import java.util.*;
 import java.lang.*;
 //test
 import java.io.*;

 import js.math.*;

public class Pathfinder
{
	private static long startTime;
	public Pathfinder()
	{

	}


	public static LinkedList<Point> findPath(Point start, Point finish, boolean[][] grid, int size, int sqW, int dx, int dy)
    {
    	System.out.println(start.toString()+finish.toString()+sqW);
    	startTime = System.currentTimeMillis();
    	LinkedList<Point> visited = new LinkedList<Point>();
    	LinkedList<Point> prevs = new LinkedList<Point>();
    	LinkedList<Integer> weights = new LinkedList<Integer>();
    	int pointer = 0;
    	if(!( isInGrid(start,grid) ) || !(isInGrid(finish,grid)) )
    	{
    		throw new IllegalArgumentException();
    	}

		if(grid[(int)(start.getY())][(int)(start.getX())])
		{
			System.out.println("Starting from unstartable place....");
			//throw new IllegalArgumentException("unstartable starting point");
		}

		if(grid[(int)(finish.getY())][(int)(finish.getX())])
		{
			System.out.println("BadFinish");
			LinkedList<Point> find = getNeighbors(finish,grid,sqW);
			sortToClosest/*Distance*/(find,start);
			if(find.size()!=0)
			{
				finish = find.get(0);
				System.out.println("new finish: "+finish.toString());
			}
			else
			{
				find = getConditionlessNeighbors(finish,grid);
				sortToClosest/*Distance*/(find, start);
				int ptr = 0;
				while(ptr<find.size())
				{
					LinkedList<Point> temp = getNeighbors(find.get(ptr),grid,sqW);
					sortToClosest(temp, start);
					if(temp.size()!=0)
					{
						finish = find.get(0);
						System.out.println("new finish: "+finish.toString());
						break;
					}
					temp = getConditionlessNeighbors(find.get(ptr),grid);
					sortToClosest(temp, start);
					for(Point p: temp)
					{
						if( !(find.contains(p)) )
							find.addLast(p);
					}
					ptr++;
				}
				if(grid[(int)(finish.getY())][(int)(finish.getX())])//still fail
				{
					System.out.println("Grid is f-ed up");
					return null;
				}
			}

		}

    	visited.add(start);
    	prevs.add(null);
    	if(start.equals(finish)) { return visited; }
    	weights.add(0);

    	boolean found = false;
    	int smallestWeight = -1;
    	int smallestPos = -1;
		while( pointer<visited.size() )
		{
			Point p = visited.get(pointer);
			if(p.equals(finish))
			{
				if(smallestWeight<0||(smallestWeight>0&&weights.get(pointer)<smallestWeight))
				{
					System.out.print("new smallest weight found: ");
					smallestWeight = weights.get(pointer);
					smallestPos = pointer;
					System.out.println(smallestWeight);
				}
				//return simplifyPath(parsePath(visited,prevs,pointer),grid);
			}
			LinkedList<Point> neighbors = getNeighbors(p,grid,sqW);
			sortToClosest(neighbors, finish/*, start*/);

			int w = 0;
			for(Point n: neighbors)
			{
				w = weights.get(pointer);
				if(LineSegment.rightLine(p,n))
					w += 10;
				else
					w += 14;
				if( /*!(parsePath(visited,prevs,pointer).contains(n))*/!(visited.contains(n)) && (smallestWeight<0 || (smallestWeight>0&&w<smallestWeight) ) )
				{
					visited.add(n);
					prevs.add(p);
					weights.add(w);
				}
			}
			pointer++;
		}
		if(visited.size()==0)
			return null;

		/*PrintWriter out = null;
		try
		{
			out = new PrintWriter(new BufferedWriter(new FileWriter("log.txt")));
		}
		catch(Exception e)
		{
			System.out.println("not allowed to log :(");
		}


		out.println("Start: ("+(int)(start.getX())+","+(int)(start.getY())+"), Finish: ("+(int)(finish.getX())+","+(int)(finish.getY())+")");
		for(int i = 0; i<visited.size();i++)
		{
			if(visited.get(i)==null)
				out.print(" null  -");
			else
				out.printf("(%02d,%02d)-",(int)(visited.get(i).getX()),(int)(visited.get(i).getY()));
		}
		out.println();
		for(int i = 0; i<prevs.size();i++)
		{
			if(prevs.get(i)==null)
				out.print(" null  -");
			else
				out.printf("(%02d,%02d)-",(int)(prevs.get(i).getX()),(int)(prevs.get(i).getY()));
		}
		out.println();
		for(int i = 0; i<weights.size();i++)
		{
			out.printf("%07d-",weights.get(i));
		}
		out.println();
		out.close();*/
		//return simplifyPath(parsePath(visited,prevs,visited.size()-1),grid);
		return editPath(simplifyPath(parsePath(visited,prevs,smallestPos),grid),grid,size,dx,dy);
    }

    public static boolean isInGrid(Point p, boolean[][] g)
    {
    	if( (int)(p.getX()) <0 || (int)(p.getX()) >= g[0].length || (int)(p.getY()) <0 || (int)(p.getY()) >= g.length )
    	{
    		return false;
    	}
    	return true;
    }

    private static LinkedList<Point> getNeighbors(Point p, boolean[][] g, int num)
    {
    	LinkedList<Point> list = new LinkedList<Point>();
    	Point r = new Point( (int)(p.getX())+1, (int)(p.getY()) );
		Point l = new Point( (int)(p.getX())-1, (int)(p.getY()) );
		Point u = new Point( (int)(p.getX()), (int)(p.getY())-1 );
		Point d = new Point( (int)(p.getX()), (int)(p.getY())+1 );
		Point ru = new Point( (int)(p.getX())+1, (int)(p.getY())-1 );
		Point lu = new Point( (int)(p.getX())-1, (int)(p.getY())-1 );
		Point rd = new Point( (int)(p.getX())+1, (int)(p.getY())+1 );
		Point ld = new Point( (int)(p.getX())-1, (int)(p.getY())+1 );
		if( goodPoint(g,r,num))
			list.add(r);
		if( goodPoint(g,l,num))
			list.add(l);
		if( goodPoint(g,u,num))
			list.add(u);
		if( goodPoint(g,d,num))
			list.add(d);
		if( goodPoint(g,ru,num))
			list.add(ru);
		if( goodPoint(g,lu,num))
			list.add(lu);
		if( goodPoint(g,rd,num))
			list.add(rd);
		if( goodPoint(g,ld,num))
			list.add(ld);
		/*if(list.size()==0)
			return null;*/
		return list;
    }

    private static boolean goodPoint(boolean[][] g, Point p, int num)
    {
    	Point temp = new Point();
    	for(int r = 0; r<num;r++)
    	{
    		for(int c = 0; c<num;c++)
    		{
    			temp.setLocation((int)(p.getX()+c),(int)(p.getY()+r));
    			if(!isInGrid(temp,g)||g[(int)(temp.getY())][(int)(temp.getX())])
    				return false;
    		}
    	}
    	return true;
    }

    private static LinkedList<Point> getConditionlessNeighbors(Point p, boolean[][] g)
    {
    	LinkedList<Point> list = new LinkedList<Point>();
    	list.add( new Point( (int)(p.getX())+1, (int)(p.getY()) ) );
		list.add( new Point( (int)(p.getX())-1, (int)(p.getY()) ) );
		list.add( new Point( (int)(p.getX()), (int)(p.getY())-1 ) );
		list.add( new Point( (int)(p.getX()), (int)(p.getY())+1 ) );
		list.add( new Point( (int)(p.getX())+1, (int)(p.getY())-1 ) );
		list.add( new Point( (int)(p.getX())-1, (int)(p.getY())-1 ) );
		list.add( new Point( (int)(p.getX())+1, (int)(p.getY())+1 ) );
		list.add( new Point( (int)(p.getX())-1, (int)(p.getY())+1 ) );
		return list;
    }

    private static LinkedList<Point> parsePath(LinkedList<Point> pts, LinkedList<Point> pre, int pointer)
    {

		LinkedList<Point> toReturn = new LinkedList<Point>();
		if(pointer<0||pointer>=pre.size())
			return toReturn;
		Point pp = pre.get(pointer);
		if(pp!=null)
		{
			toReturn.add(pts.get(pointer));
		}
		while(pp!=null)
		{
			toReturn.addFirst(pp);
			pp = pre.get( pts.indexOf(pp) );
		}
		/*if(toReturn.size()==0)
			return null;*/
		return toReturn;
    }

    private static LinkedList<Point> simplifyPath(LinkedList<Point> path, boolean[][] g)
    {
    	if(path.size()>2)
    	{
    		int i = 0;
    		Point p;
    		LineSegment a = new LineSegment(null,path.get(0)), b = new LineSegment(path.get(0),path.get(1));
    		do
    		{
    			p = a.getA();
    			a.setA(a.getB());
    			a.setB(b.getB());
    			b.setA(b.getB());
    			b.setB(path.get(i+2));
    			if(LineSegment.sameSlope(a,b))
    			{
    				b.setA(a.getA());
    				a.setB(a.getA());
    				a.setA(p);
    				path.remove(i+1);
    			}
    			else
    				i++;
    		}
    		while(i<path.size()-2);
    		//other part
    	}
    	return path;
    }

    private static LinkedList<Point> editPath(LinkedList<Point> path, boolean[][] grid, int size, int dx, int dy)
    {
    	for(Point p: path)
		{
			p.setLocation((int)(p.getX()*size+dx),(int)(p.getY()*size+dy));
		}
    	if(path.size()>2)//could possibly rewrite changing longest possible stretch and going down from there? (expensiveeeeeee)
    	{
    		LineSegment ls = new LineSegment(new Point(), new Point());
    		Rectangle temp = new Rectangle();
    		int minX, minY, maxX, maxY;
    		boolean free;
    		for(int i = 0;i<path.size()-2;i++)
    		{
    			free = true;
    			ls.setA(path.get(i));
    			ls.setB(path.get(i+2));
    			minX = (int)(Math.min(ls.getA().getX()/size,ls.getB().getX()/size)); minY = (int)(Math.min(ls.getA().getY()/size,ls.getB().getY()/size));
    			maxX = (int)(Math.max(ls.getA().getX()/size,ls.getB().getX()/size)); maxY = (int)(Math.max(ls.getA().getY()/size,ls.getB().getY()/size));
    			for(int r = minY;free&&r<=maxY;r++)//no point in checking if already not gonna work
    			{
    				for(int c = minX;free&&c<=maxX;c++)
    				{
    					if(grid[r][c])
    					{
    						temp.setFrame(c*size,r*size,size,size);
    						free = !(ls.intersectsRect(temp));
    					}
    				}
    			}
    			if(free)//clear path
    			{
    				path.remove(i+1);
    				i--;
    			}
    		}

    	}
    	System.out.println("milliseconds taken by pathfinding algorithm: "+(System.currentTimeMillis()-startTime));
    	return path;
    }


	private static void sortToClosestAngle(LinkedList<Point> n, Point end, Point beg)
	{
		if(n==null || n.size()==0)
		{
			System.out.println("sorting neighbors: bad input");
			return;
		}
		for(int i = 1; i<n.size();i++)
		{
			int j = i;
			//while(j>0 && Calc.pointDistance(n.get(j),p) < Calc.pointDistance(n.get(j-1),p))
			while(Calc.angleDifference(Calc.pointDirection(beg,n.get(j)),Calc.pointDirection(n.get(j),end))<Calc.angleDifference(Calc.pointDirection(beg,n.get(j-1)),Calc.pointDirection(n.get(j-1),end)))
			{
				n.set(j, n.set(j-1, n.get(j)));
				j--;
			}
		}
	}

	private static void sortToClosest/*Distance*/(LinkedList<Point> n, Point p)
	{
		if(n==null || n.size()==0)
		{
			System.out.println("sorting neighbors: bad input");
			return;
		}
		for(int i = 1; i<n.size();i++)
		{
			int j = i;
			while(j>0 && Calc.pointDistance(n.get(j),p) < Calc.pointDistance(n.get(j-1),p))
			{
				n.set(j, n.set(j-1, n.get(j)));
				j--;
			}
		}
	}

}