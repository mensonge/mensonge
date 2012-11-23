package mensonge.userinterface;

import javax.swing.tree.DefaultMutableTreeNode;

public class Branche extends DefaultMutableTreeNode
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructeur par défaut
	 */
	public Branche()
	{
		super();
	}
	
	/**
	 * Constructeur avec nom
	 * @param nom nom de l'objet
	 */
	public Branche(String nom)
	{
		super(nom);
	}
}
