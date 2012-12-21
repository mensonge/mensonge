package mensonge.userinterface.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import mensonge.core.BaseDeDonnees.BaseDeDonneesControlleur;
import mensonge.core.BaseDeDonnees.BaseDeDonneesModele;
import mensonge.core.BaseDeDonnees.DBException;
import mensonge.userinterface.Feuille;

/**
 * Classe permettant d'effectuer le drag and drop sur l'arbre
 * 
 * @author Azazel
 * 
 */
public class HandlerDragArbre extends TransferHandler
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger("drag Arbre");
	private BaseDeDonneesControlleur bdd;
	private PanneauArbre panneauArbre;

	/**
	 * Constructeur par defaut
	 * 
	 * @param panneauArbre
	 *            pour connaitre le tri actuel
	 * @param bdd
	 *            la base pour mettre à jour
	 */
	public HandlerDragArbre(PanneauArbre panneauArbre, BaseDeDonneesControlleur bdd)
	{
		this.bdd = bdd;
		this.panneauArbre = panneauArbre;
	}

	/**
	 * Méthode permettant à l'objet de savoir si les données reçues via un drop sont autorisées à être importées
	 * 
	 * @param info
	 * @return boolean
	 */
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		if (!info.isDataFlavorSupported(DataFlavor.stringFlavor))
		{
			return false;
		}
		return true;
	}

	/**
	 * C'est ici que l'insertion des données dans notre composant est réalisée
	 * 
	 * @param support
	 * @return boolean
	 */
	public boolean importData(TransferHandler.TransferSupport support)
	{
		// On récupère l'endroit du drop via un objet approprié
		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
		// Les informations afin de pouvoir créer un nouvel élément
		TreePath path = dl.getPath();
		// On verifie que le chemin du drop porte bien sur une branche
		if (!(path.getLastPathComponent() instanceof Branche))
		{
			return false;
		}
		Branche cat = (Branche) path.getLastPathComponent();
		// On verifie bien que l'userObject est une chaine utilisable
		if (!(cat.getUserObject() instanceof String))
		{
			return false;
		}
		Transferable data = support.getTransferable();// On recupere le transferable
		String str = "", nom = (String) cat.getUserObject();// On recupere le nom de la nouvelle categorie en fonction
															// du chemin
		try
		{
			str = (String) data.getTransferData(DataFlavor.stringFlavor);// On extrait la chaine du transferable
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, e.getLocalizedMessage());
		}
		// Si la chaine est vide ou correspondant a -1 (une erreur de selection) on stop
		if (str.equals("") || str.equals("-1"))
		{
			return false;
		}
		int[] listeId = HandlerDragArbre.convertirListeId(str.split(";"));

		for (int id : listeId)
		{
			// On modifie les categories ou le sujet
			if (id != -1)
			{
				effectuerMaj(id, nom);
			}
		}
		return true;
	}

	/**
	 * Convertie un tableau de string contenant des nombres en tableau de in
	 * 
	 * @param tab
	 * @return
	 */
	private static int[] convertirListeId(String[] tab)
	{
		int[] retour = new int[tab.length];
		int i = 0;
		for (String nombre : tab)
		{
			try
			// S'il y a un erreur de conversion, on met -1
			{
				retour[i] = Integer.parseInt(nombre);
			}
			catch (NumberFormatException e)
			{
				logger.log(Level.WARNING, e.getLocalizedMessage());
				retour[i] = -1;
			}
			i++;
		}
		return retour;
	}

	/**
	 * Effectue la mise a jour en fonction du trii pour un id vers son nouveau groupe (categorie ou sujet)
	 * 
	 * @param id
	 * @param nom
	 */
	private void effectuerMaj(int id, String nom)
	{
		try
		{
			if (this.panneauArbre.getTypeTrie() == PanneauArbre.TYPE_TRIE_CATEGORIE)
			{
				this.bdd.modifierEnregistrementCategorie(id, this.bdd.getCategorie(nom));
			}
			else
			{
				this.bdd.modifierEnregistrementSujet(id, this.bdd.getSujet(nom));
			}
		}
		catch (DBException e)
		{
			logger.log(Level.WARNING, e.getLocalizedMessage());
		}
	}

	/**
	 * Dans cette méthode, nous allons créer l'objet utilisé par le système de drag'n drop afin de faire circuler les
	 * données entre les composants Vous pouvez voir qu'il s'agit d'un objet de type Transferable
	 * 
	 * @param c
	 * @return
	 */
	protected Transferable createTransferable(JComponent c)
	{
		// On retourne un nouvel objet implémentant l'interface Transferable
		// StringSelection implémente cette interface, nous l'utilisons donc
		JTree arbre = (JTree) c;// on convertie le compansant en JTree
		Feuille feuille;
		StringBuffer buffer = new StringBuffer();

		for (TreePath path : arbre.getSelectionPaths())
		{
			if (path.getLastPathComponent() instanceof Feuille)// On recupére l'id de toutes les feuilles
			{
				feuille = (Feuille) path.getLastPathComponent();
				buffer.append(feuille.getId());
				buffer.append(";");
			}
		}
		return new StringSelection(buffer.toString());
	}

	/**
	 * Cette méthode est utilisée afin de déterminer le comportement du composant vis-à-vis du drag'n drop : nous
	 * retrouverons nos variables statiques COPY, MOVE, COPY_OR_MOVE, LINK ou NONE
	 * 
	 * @param c
	 * @return int
	 */
	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}
}
