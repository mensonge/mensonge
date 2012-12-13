package mensonge.userinterface.tree;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import mensonge.userinterface.Feuille;

/**
 * Classe affichant des image dans l'arbre
 * 
 * @author Azazel
 * 
 */
public class PanneauArbreRenderDefault extends DefaultTreeCellRenderer
{

	/**
	 * Le serail id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Image associée à la racine
	 */
	private ImageIcon dossier = null;

	/**
	 * Image associée aux caseArbre
	 */
	private ImageIcon enregistrement = null;

	
	/**
	 * Constructeur par défaut faisant des verifications pour pallier à l'absence des fichiers
	 */
	public PanneauArbreRenderDefault()
	{

		this.dossier = new ImageIcon("images/directory_default.png");

		this.enregistrement = new ImageIcon("images/file_default.png");

		
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if(value instanceof Feuille)
		{
			this.setIcon(this.enregistrement);
		}
		else
		{
			this.setIcon(this.dossier);
		}
		return this;
	}

}
