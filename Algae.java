
import java.awt.*;
import java.util.HashSet;

import js.math.*;

public class Algae extends ISPSprite {

	private Color myColor;
	private int radius, maxRadius;
	private int lifespan, age;
	private ISPStage myStage;
	
	public Algae() {
		myColor = Color.GREEN;
		lifespan = 100+(int)(Math.random()*50);
		age = 0;
		radius = 5;
		maxRadius = 10;
		setX(Math.random()*ISPStage.WIDTH);
		setY(Math.random()*ISPStage.HEIGHT);
	}
	
	public Algae(Algae d, Algae m) {
		setX((d.getdX()+m.getdX())/2.0);
		setY((d.getdY()+m.getdY())/2.0);
		myColor = new Color((int)(d.getColor().getRed()+d.getColor().getRed())/2,(int)(d.getColor().getGreen()+d.getColor().getGreen())/2,(int)(d.getColor().getBlue()+d.getColor().getBlue())/2);
		if (Math.random() < 0.08) {
			int dr = 5*(int)(Math.random()*2-1);
			int dg = 5*(int)(Math.random()*2-1);
			int db = 5*(int)(Math.random()*2-1);
			int r = myColor.getRed();
			int g = myColor.getGreen();
			int b = myColor.getBlue();
			r+=dr;
			g+=dg;
			b+=db;
			if(r<0)
				r+=256;
			r = r%256;
			if(g<0)
				g+=256;
			g = g%256;
			if(b<0)
				b+=256;
			b = b%256;
			myColor = new Color(r,g,b);
		}
	}
	
	public void setStage(ISPStage s) {
		myStage = s;
	}
	
	public void act() {
		int x = getX();
		int y = getY();
		if (age>lifespan) {
			remove();
		}
		x += Math.random()*2;
		y += Math.random()*2;
		HashSet<ISPSprite> sprs = ISPStage.getSprites();
		for (ISPSprite spr: sprs) {
			if (spr instanceof Algae && this.intersects(spr)) {
				myStage.addSprite(new Algae(this, (Algae)spr));
			}
		}
		age += 1;
		
		
		setX(x);
		setY(y);
	}
	
	public void remove() {
		myStage.removeSprite(this);
	}
	
	public JSShape getShape()
	{
		return new JSEllipse(getdX(),getdY(),radius*1.0,radius*1.0,1.0);
	}
	
	public void draw(Graphics2D g)
	{
		g.setColor(myColor);
		g.fill(this.getShape().toAwt());
	}
	
	public Color getColor() {
		return myColor;
	}
	
}
