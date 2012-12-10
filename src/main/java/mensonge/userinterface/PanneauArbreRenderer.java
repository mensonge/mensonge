package mensonge.userinterface;

import java.awt.Component;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * Classe affichant des image dans l'arbre
 * 
 * @author Azazel
 * 
 */
public class PanneauArbreRenderer extends DefaultTreeCellRenderer
{

	/**
	 * Le serail id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Image associée à la racine
	 */
	private ImageIcon openRacine = null;

	/**
	 * Image associée aux caseArbre
	 */
	private ImageIcon openBranche1 = null;
	
	/**
	 * Image associée aux caseArbre
	 */
	private ImageIcon openBranche2 = null;

	/**
	 * Image associée au feuille contenant des murs
	 */
	private ImageIcon openFeuille1 = null;

	/**
	 * Image associée au feuille contenant des murs
	 */
	private ImageIcon openFeuille2 = null;

	/**
	 * Image associée au feuille contenant des murs
	 */
	private ImageIcon openFeuille3 = null;

	/**
	 * Image associée au feuille contenant des murs
	 */
	private ImageIcon openFeuille4 = null;
	

	/**
	 * Constructeur par défaut faisant des verifications pour pallier à l'absence des fichiers
	 */
	public PanneauArbreRenderer()
	{
		
			this.openRacine = new ImageIcon("images/tree.png");
		
			this.openBranche1 = new ImageIcon("images/star_pink.png");
			
			this.openBranche2 = new ImageIcon("images/star_blue.png");
		
			this.openFeuille2 = new ImageIcon("images/dec1.png");
		
			this.openFeuille1 = new ImageIcon("images/dec2.png");
			
			this.openFeuille3 = new ImageIcon("images/dec3.png");
			
			this.openFeuille4 = new ImageIcon("images/dec4.png");
		
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		int nb = (int)Math.floor(Math.random()*100);
		if(value instanceof Branche)
		{
			if(nb %2 == 0)
			{
				this.setIcon(this.openBranche1);
			}
			else
			{
				this.setIcon(this.openBranche2);
			}
		}
		else if(value instanceof Feuille)
		{
			if(nb %4 == 0)
			{
				this.setIcon(this.openFeuille1);
			}
			else if(nb%4 == 1)
			{
				this.setIcon(this.openFeuille2);
			}
			else if(nb%4 == 2)
			{
				this.setIcon(this.openFeuille3);
			}
			else if(nb%4 == 3)
			{
				this.setIcon(this.openFeuille4);
			}
		}
		else
		{
			this.setIcon(this.openRacine);
		}
		return this;
	}

}
