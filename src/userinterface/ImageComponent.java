package userinterface;
import javax.swing.*;
import java.awt.*;
import java.io.*;
public class ImageComponent extends JPanel implements Serializable
{
	private Image mImage=null;

	ImageComponent(Image image)
	{
		this.mImage=image;
	}

	ImageComponent()
	{

	}

	public Image getImage()
	{
		return this.mImage;
	}
	public void setImage(Image image)
	{
		mImage=image;
		this.paintComponent(getGraphics());
	}

	public void update() {
		paint(this.getGraphics());
	}


	@Override
	public void paintComponent(Graphics g) {
	//	super.paintComponent(g); //paint background
		if (mImage != null) { //there is a picture: draw it
			int height = this.getSize().height;
			int width = this.getSize().width;
			//g.drawImage(image, 0, 0, this); //use image size
			g.drawImage(mImage,0,0, width, height, this);
		}
	}
}

