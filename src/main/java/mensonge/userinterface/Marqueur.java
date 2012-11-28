package mensonge.userinterface;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class Marqueur extends JComponent
{
	int x;
	
	private static final long serialVersionUID = 1L;

	public Marqueur(int x)
	{
		this.x=x;
		this.setSize(5, 35);
	}
	
	public void paintComponent(Graphics g)
	{
		//paintDashedBorder(g);
		g.setColor(Color.RED);
		g.fillRect(x, 0, this.getWidth(), this.getHeight());
	}

	


}
