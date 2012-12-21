package mensonge.userinterface.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Classe representant alternativement les sujets ou les categorie dans l'arbre
 * @author Azazel
 *
 */
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
	 * 
	 * @param nom
	 *            nom de l'objet
	 */
	public Branche(String nom)
	{
		super(nom);
	}
}
