package userinterface;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class PaneauDialogueOperation extends JPanel
{
	private Image img;
	public PaneauDialogueOperation(Image img)
	{
		this.img = img;
	}
	public void paint(Graphics g)
	{
		if(img != null) // Si l'image existe, ...
		{
			g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
		}
	}
}
