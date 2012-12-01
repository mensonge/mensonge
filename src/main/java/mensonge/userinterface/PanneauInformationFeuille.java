package mensonge.userinterface;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
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
	Map<String, String> listeInfo = null;

	@Override
	public void paint(Graphics g)
	{
		int i = 0;
		Font font = new Font("Courier", Font.BOLD, 15);
		g.setFont(font);
		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(Color.BLACK);
		if (listeInfo != null)
		{
			Set<String> listeKey = listeInfo.keySet();
			Iterator<String> iterator = listeKey.iterator();
			while (iterator.hasNext())
			{
				int y = 20 + 20 * i;
				int x = 10;
				String current = iterator.next();
				g.drawString(current + " : " + listeInfo.get(current), x, y);
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
