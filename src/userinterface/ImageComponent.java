package userinterface;
import javax.swing.*;
import java.awt.*;
public class ImageComponent extends JComponent
{
	private Image mImage;

	public void setImage(Image image)
	{
		mImage=image;
		repaint();
	}

	@Override
    public synchronized void paint(Graphics g)
	{
		if(mImage!= null)
		{
			g.drawImage(mImage,0,0,this);
		}
	}
}

