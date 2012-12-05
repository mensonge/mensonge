package mensonge.userinterface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;

import mensonge.userinterface.PanneauArbre.AjouterCategorieEnregistrementClicDroit;
import mensonge.userinterface.PanneauArbre.AjouterSujetClicDroit;
import mensonge.userinterface.PanneauArbre.CollapseClicDroit;
import mensonge.userinterface.PanneauArbre.ExpandClicDroit;
import mensonge.userinterface.PanneauArbre.ExporterEnregistrementClicDroit;
import mensonge.userinterface.PanneauArbre.ModifierCategorieEnregistrementClicDroit;
import mensonge.userinterface.PanneauArbre.ModifierSujetEnregistrementClicDroit;
import mensonge.userinterface.PanneauArbre.ModifierTri;
import mensonge.userinterface.PanneauArbre.PlayEcouteArbre;
import mensonge.userinterface.PanneauArbre.RenommerCategorieClicDroit;
import mensonge.userinterface.PanneauArbre.RenommerEnregistrementClicDroit;
import mensonge.userinterface.PanneauArbre.RenommerSujetClicDroit;
import mensonge.userinterface.PanneauArbre.SupprimerCategorieEnregistrementClicDroit;
import mensonge.userinterface.PanneauArbre.SupprimerEnregistrementClicDroit;
import mensonge.userinterface.PanneauArbre.SupprimerSujetClicDroit;

public class SliderWithMarkers extends JSlider
{

	float position1=0.0f;
	float position2=0.0f;
	boolean pos1=false;
	boolean pos2=false;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	SliderWithMarkers(int orientation)
	{
		super(orientation);
		this.setOpaque(false);
		this.setPaintTicks(false);
		this.setPaintLabels(false);
		this.setMinimum(0);
		this.setValue(0);
		this.setMaximum((int) SliderPositionEventListener.SLIDER_POSITION_MAX);
		this.addMouseListener(new ClicDroit());
	}

	public void setMarkerOneAt(float position)
	{
		position1= position;
		pos1=true;
		this.repaint();

	}

	public void setMarkerTwoAt(float position)
	{
		position2= position;
		pos2=true;
		this.repaint();
	}

	protected void paintComponent(Graphics g) {
		   int h = getHeight();
		   int w = getWidth();

		   g.setColor(Color.RED);
		   if(pos1)
		   {
			   g.fillRect(Math.round(position1*w), 0, 1, h);
		   }
		   g.setColor(Color.BLUE);
		   if(pos2)
		   {
			   g.fillRect(Math.round(position2*w), 0, 1, h);
		   }
		   super.paintComponent(g);
	}

	private class ClicDroit extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			int w  = getWidth();
			if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)
			{
				if(e.getX()>Math.round(position1*w))
				{
					setMarkerOneAt((float)e.getX()/(float)w);
				}
				else
				{
					setMarkerTwoAt((float)e.getX()/(float)w);
				}
			}
		}
	}
}



