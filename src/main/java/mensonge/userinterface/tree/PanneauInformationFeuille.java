package mensonge.userinterface.tree;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

public class PanneauInformationFeuille extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, String> listeInfo = null;

	@Override
	public void paint(Graphics g)
	{
		int i = 0;
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(
		        RenderingHints.KEY_ANTIALIASING, 
		        RenderingHints.VALUE_ANTIALIAS_ON);
		Font font = new Font("Courier", Font.BOLD, 15);
		g2.setFont(font);
		g2.setColor(this.getBackground());
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		g2.setColor(Color.BLACK);
		if (listeInfo != null)
		{
			Set<String> listeKey = listeInfo.keySet();
			Iterator<String> iterator = listeKey.iterator();
			while (iterator.hasNext())
			{
				int y = 20 + 20 * i;
				int x = 10;
				String current = iterator.next();
				g2.drawString(current + " : " + listeInfo.get(current), x, y);
				i++;
			}
		}
	}

	public Map<String, String> getListeInfo()
	{
		return listeInfo;
	}

	public void setListeInfo(Map<String, String> listeInfo)
	{
		this.listeInfo = listeInfo;
	}

}
