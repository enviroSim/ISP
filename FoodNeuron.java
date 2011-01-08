//This class was made specifically to decide what to eat. It is currently not expandable to other problems.
public class FoodNeuron {
	public double[][] w;
	public double[][] t;
	
	public FoodNeuron(double[][] wa, double[][] ta) {
		w = wa;
		t = ta;
		if (t.length != w.length) {
			System.out.println("We have a probrem.");
			
		}
		for (int i = 0; i < w.length; i++)
			for (int j = 0; j < w[i].length; j++)
					w[i][j] = Math.random();
		for (int i = 0; i < t.length; i++)
			for (int j = 0; j < t[i].length; j++)
			t[i][j] = Math.random();
	}
	
	public FoodNeuron() {
		double[][] a = {{0.0,0.0,0.0,0.0},{0.0,0.0}}; 
		double[][] b = {{0.0,0.0},{0.0}}; 
		w = a;
		t = b;
		if (t.length != w.length) {
			System.out.println("We have a probrem.");
			
		}
		for (int i = 0; i < w.length; i++)
			for (int j = 0; j < w[i].length; j++)
					w[i][j] = Math.random();
		for (int i = 0; i < t.length; i++)
			for (int j = 0; j < t[i].length; j++)
			t[i][j] = Math.random();
	}
	
	public boolean update(double[] inputs) {
		boolean decide = false;
		if (w[0].length != inputs.length) {
			System.out.println("We have a probrem.");
			return decide;
		}
		if ((double)w[0].length/(double)t[0].length%1 != 0) {
			System.out.println("We have a probrem.");
			return decide;
		}
		int tot = 0;
		for (int j = 0; j < t[0].length; j++) {
			double net = 0;
			int we = 0;
			for (int k = 0; k < (double)w[0].length/(double)t[0].length; k++) {
				net += inputs[k]*w[0][we++];
			}
			if (net>t[0][j]) {
				tot += w[1][j];
			} else {
				tot += w[1][j];
			}
		}
		if (tot > t[1][0]) {
			decide = true;
		}
		return decide;
	}
	
	public boolean breed(FoodNeuron a, FoodNeuron b) {
		for (int i = 0; i < w.length; i++) {
			if (w.length != a.w.length || w.length != b.w.length) return false;
			for (int j = 0; j < w[i].length; j++) {
				if (w[i].length != a.w[i].length || w[i].length != b.w[i].length) return false;
				w[i][j] = (Math.random()>0.5)?a.w[i][j]:b.w[i][j];
			}
		}
		for (int i = 0; i < t.length; i++) {
			if (t.length != a.t.length || t.length != b.t.length) return false;
			for (int j = 0; j < t[i].length; j++) {
				if (t[i].length != a.t[i].length || t[i].length != b.t[i].length) return false;
				t[i][j] = (Math.random()>0.5)?a.t[i][j]:b.t[i][j];
			}
		}
		 return true;
	}
	
}
