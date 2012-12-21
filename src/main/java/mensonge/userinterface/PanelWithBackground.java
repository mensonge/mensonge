package mensonge.userinterface;

import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Permet de créer un panel possédant une image en arrière plan
 * 
 */
public class PanelWithBackground extends JPanel
{
	private static final long serialVersionUID = 4969722390664163943L;
	private BufferedImage image;

	/**
	 * Panel avec une image en arrière plan
	 * 
	 * @param layout
	 *            Layout du panel
	 * @param imgPath
	 *            Chemin d'accès de l'image
	 */
	public PanelWithBackground(LayoutManager layout, String imgPath)
	{
		super(layout);
		try
		{
			image = ImageIO.read(new File(imgPath));
		}
		catch (IOException e)
		{
			GraphicalUserInterface.popupErreur(e.getMessage());
		}
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(image, 0, this.getHeight() - image.getHeight(), this);
	}
}
