package mensonge.userinterface.tree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import javax.swing.JTree;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import mensonge.core.BaseDeDonnees.BaseDeDonnees;
import mensonge.core.BaseDeDonnees.DBException;
import mensonge.core.BaseDeDonnees.LigneEnregistrement;
import mensonge.core.tools.ActionMessageObserver;
import mensonge.core.tools.Cache;
import mensonge.core.tools.DataBaseObserver;
import mensonge.core.tools.Lockable;
import mensonge.core.tools.Utils;
import mensonge.userinterface.AjouterCategorieListener;
import mensonge.userinterface.AjouterSujetListener;
import mensonge.userinterface.Feuille;
import mensonge.userinterface.GraphicalUserInterface;
import mensonge.userinterface.LecteurAudio;

public final class PanneauArbre extends JPanel implements DataBaseObserver, Lockable, ActionMessageObserver
{
	/**
	 *
	 */
	public static final int TYPE_TRIE_CATEGORIE = 1;
	public static final int TYPE_TRIE_SUJET = 2;
	
	private static final long serialVersionUID = 1L;
	

	private BaseDeDonnees bdd = null;

	private LecteurAudio lecteurAudio;

	private PanneauInformationFeuille infoArbre = new PanneauInformationFeuille();
	private DefaultMutableTreeNode racine;
	private JTree arbre;

	private JPopupMenu menuClicDroit = new JPopupMenu();// sers au clic droit

	private int typeTrie = PanneauArbre.TYPE_TRIE_SUJET;
	private JLabel labelCacheSize;
	private JLabel labelDBSize;

	private boolean lock = false;

	private boolean event = false;
	private Cache cache;

	public PanneauArbre(BaseDeDonnees bdd, Cache cache)
	{
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
		this.bdd = bdd;
		this.cache = cache;
		
		this.racine = new DefaultMutableTreeNode("Sujet");
		this.remplirArbreEnregistrementSujet();
		this.arbre = new JTree(racine);
		ClicGauche clicGauche = new ClicGauche();
		this.arbre.addMouseListener(new ClicDroit());
		this.arbre.addMouseListener(clicGauche);
		this.arbre.addKeyListener(new KeyListenerTree());
		this.arbre.addTreeSelectionListener(clicGauche);
		
		JScrollPane scrollPane = new JScrollPane(arbre);
		scrollPane.setPreferredSize(new Dimension(332, 450));
		scrollPane.setAutoscrolls(true);

		this.infoArbre.setPreferredSize(new Dimension(336, 100));

		this.labelCacheSize = new JLabel("Taille du cache : " + Utils.humanReadableByteCount(cache.getSize(), false));
		this.labelDBSize = new JLabel("Taille de la base de données : "
				+ Utils.humanReadableByteCount(Utils.getDBSize(), false));

		JPanel panelInfo = new JPanel(new GridLayout(0, 1));
		panelInfo.add(labelCacheSize);
		panelInfo.add(labelDBSize);

		JPanel panelArbreInfo = new JPanel(new BorderLayout());
		panelArbreInfo.add(scrollPane, BorderLayout.CENTER);
		panelArbreInfo.add(panelInfo, BorderLayout.SOUTH);

		JPanel panelConteneur = new JPanel(new BorderLayout());
		panelConteneur.add(panelArbreInfo, BorderLayout.NORTH);
		panelConteneur.add(this.infoArbre, BorderLayout.SOUTH);

		this.setEvent(true);
		this.lecteurAudio = new LecteurAudio();
		this.lecteurAudio.setVisible(false);

		this.add(panelConteneur, BorderLayout.NORTH);
		this.add(lecteurAudio, BorderLayout.SOUTH);
		this.arbre.setDragEnabled(true);
		this.arbre.setTransferHandler(new HandlerDragArbre(this, bdd));
		
		this.arbre.addMouseListener(new MouseAdapter(){
	         
		      public void mousePressed(MouseEvent e)
		      {
		        JComponent lab = (JComponent)e.getSource();
		        TransferHandler handle = lab.getTransferHandler();
		        handle.exportAsDrag(lab, e, TransferHandler.COPY);
		      }
		    });
	}

	public int getTypeTrie()
	{
		return typeTrie;
	}

	public void setTypeTrie(int typeTrie)
	{
		this.typeTrie = typeTrie;
	}

	public boolean isEvent()
	{
		return event;
	}

	public void setEvent(boolean event)
	{
		this.event = event;
		if (this.event)
		{
			// on crée l'objet en passant en paramétre une chaîne representant le format
			SimpleDateFormat formatter = new SimpleDateFormat("MM");
			// récupération de la date courante
			Date currentTime = new Date();
			// on crée la chaîne à partir de la date
			String dateStringMois = formatter.format(currentTime);
			if (dateStringMois.equals("12"))
			{
				this.arbre.setCellRenderer(new PanneauArbreRendererNoel());
				this.event = true;
			}
			else
			{
				this.event = false;
				this.arbre.setCellRenderer(new PanneauArbreRenderDefault());

			}
		}
		else
		{
			this.arbre.setCellRenderer(new PanneauArbreRenderDefault());
		}
	}

	/**
	 * Permet de fermer proprement ce qu'il a ouvert
	 */
	public void close()
	{
		this.lecteurAudio.close();
	}

	public List<File> getListSelectedRecords()
	{
		List<File> recordsList = new LinkedList<File>();
		TreePath[] paths = arbre.getSelectionPaths();
		if (paths != null)
		{
			for (TreePath path : paths)
			{
				if (path.getLastPathComponent() instanceof Feuille)
				{
					Feuille record = (Feuille) path.getLastPathComponent();
					try
					{
						recordsList.add(getRecordCacheFile(record.getId()));
					}
					catch (IOException e)
					{
						GraphicalUserInterface.popupErreur(e.getLocalizedMessage());
					}
					catch (DBException e)
					{
						GraphicalUserInterface.popupErreur(e.getLocalizedMessage());
					}
				}
			}
		}
		return recordsList;
	}

	public void updateArbre()
	{
		if (!lock)
		{
			Enumeration<TreePath> pathExpand = this.arbre.getExpandedDescendants(new TreePath(racine));
			viderNoeud(this.racine);
			if (this.typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
			{
				remplirArbreEnregistrementCategorie();
			}
			else if (this.typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
			{
				remplirArbreEnregistrementSujet();
			}
			this.arbre.setExpandsSelectedPaths(true);
			// this.redeployerArbre(pathExpand);
			this.arbre.expandPath(new TreePath(this.racine));
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					arbre.updateUI();
				}
			});
		}
	}

	public void redeployerArbre(Enumeration<TreePath> pathExpand)
	{
		String userObject;
		DefaultMutableTreeNode node;
		TreePath element;
		Object path[] = new Object[2];
		path[0] = racine;
		while (pathExpand.hasMoreElements())
		{
			element = pathExpand.nextElement();
			userObject = (String) ((DefaultMutableTreeNode) element.getPath()[1]).getUserObject();
			node = trouverNoeud(racine, userObject);
			if (node != null)
			{
				path[1] = node;
				this.arbre.expandPath(new TreePath(path));
			}
		}
	}

	public static DefaultMutableTreeNode trouverNoeud(DefaultMutableTreeNode noeud, String userObject)
	{
		Enumeration children = noeud.children();
		DefaultMutableTreeNode tmp;
		while (children.hasMoreElements())
		{
			tmp = (DefaultMutableTreeNode) children.nextElement();
			if (tmp.getUserObject().equals(userObject))
			{
				return tmp;
			}
		}
		return null;
	}

	public void remplirArbreEnregistrementCategorie()
	{
		List<LigneEnregistrement> rsCat = null;
		List<LigneEnregistrement> rsEnr = null;

		try
		{
			rsCat = this.bdd.getListeCategorie();
			for (LigneEnregistrement ligneCat : rsCat)
			{
				Branche node = new Branche(ligneCat.getNomCat());
				rsEnr = this.bdd.getListeEnregistrementCategorie(ligneCat.getIdCat());
				for (LigneEnregistrement ligne : rsEnr)
				{
					Feuille f = new Feuille(ligne);
					node.add(f);
				}
				this.racine.add(node);

			}
			this.racine.setUserObject("Catégorie");
		}
		catch (DBException e)
		{
			GraphicalUserInterface.popupErreur(
					"Erreur lors du chargement des enregistrements : " + e.getLocalizedMessage(), "Erreur");
		}
	}

	public void remplirArbreEnregistrementSujet()
	{
		List<LigneEnregistrement> rsSuj = null;
		List<LigneEnregistrement> rsEnr = null;

		try
		{
			rsSuj = this.bdd.getListeSujet();
			for (LigneEnregistrement ligneSuj : rsSuj)
			{
				Branche node = new Branche(ligneSuj.getNomSuj());
				rsEnr = this.bdd.getListeEnregistrementSujet(ligneSuj.getIdSuj());
				for (LigneEnregistrement ligne : rsEnr)
				{
					Feuille f = new Feuille(ligne);
					node.add(f);
				}
				this.racine.add(node);
			}
			this.racine.setUserObject("Sujet");
		}
		catch (DBException e)
		{
			GraphicalUserInterface.popupErreur(
					"Erreur lors du chargement des enregistrements : " + e.getLocalizedMessage(), "Erreur");
		}
	}

	public void viderNoeud(DefaultMutableTreeNode selectednode)
	{
		int nbchildren = selectednode.getChildCount();

		for (int i = 0; i < nbchildren; i++)
		{
			if (selectednode.getChildAt(0).isLeaf())
			{
				((DefaultMutableTreeNode) selectednode.getChildAt(0)).removeFromParent();
			}
			else
			{
				viderNoeud((DefaultMutableTreeNode) selectednode.getChildAt(0));
			}
		}
		if (selectednode.isRoot() == false)
		{
			selectednode.removeFromParent();
		}
	}

	public boolean onlySelectBranche()
	{
		TreePath[] paths = arbre.getSelectionPaths();
		if (paths != null)
		{
			for (TreePath path : paths)
			{
				if (!(path.getLastPathComponent() instanceof Branche))
				{
					return false;
				}
			}
		}
		else
		{
			return false;
		}
		return true;
	}

	public boolean onlySelectFeuille()
	{
		TreePath[] paths = arbre.getSelectionPaths();
		if (paths != null)
		{
			for (TreePath path : paths)
			{
				if (!(path.getLastPathComponent() instanceof Feuille))
				{
					return false;
				}
			}
		}
		else
		{
			return false;
		}
		return true;
	}

	public JPopupMenu getMenuClicDroit()
	{
		return this.menuClicDroit;
	}

	public void setMenuClicDroit(JPopupMenu menuClicDroit)
	{
		this.menuClicDroit = menuClicDroit;
	}

	private void removeSelectedRecords()
	{
		if (arbre.getSelectionCount() >= 1 && onlySelectFeuille())
		{
			int option = -1;
			if (arbre.getSelectionCount() == 1)
			{
				option = JOptionPane.showConfirmDialog(null,
						"Voulez-vous supprimer cet enregistrement ?\n(Notez que la catégorie sera conservée)",
						"Suppression", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			}
			else
			{
				option = JOptionPane.showConfirmDialog(null,
						"Êtes-vous sûr de vouloir ces enregistrements ?\n(Notez que les catégories seront conservées)",
						"Suppression", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			}
			if (option == JOptionPane.OK_OPTION)
			{
				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
				{
					if (arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
					{
						try
						{
							bdd.supprimerEnregistrement(((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent())
									.getId());
						}
						catch (DBException exception)
						{
							GraphicalUserInterface.popupErreur("Impossible de supprimer l'enregistrement : "
									+ exception.getMessage());
						}
					}
				}
			}
		}
	}

	private void removeSelectedCategories()
	{
		if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE && arbre.getSelectionCount() >= 1 && onlySelectBranche())
		{
			int option = -1;

			if (arbre.getSelectionCount() == 1)
			{
				option = JOptionPane.showConfirmDialog(null, "Êtes-vous sûr de vouloir supprimer cette catégorie ?\n",
						"Suppression", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			}
			else
			{
				option = JOptionPane.showConfirmDialog(null, "Êtes-vous sûr de vouloir supprimer ces catégories ?\n",
						"Suppression", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			}
			if (option == JOptionPane.OK_OPTION)
			{
				for (TreePath treePath : arbre.getSelectionPaths())
				{
					try
					{
						if (!(treePath.getLastPathComponent() instanceof Feuille))
						{
							String nomCategorie = treePath.getLastPathComponent().toString();
							List<LigneEnregistrement> liste = bdd.getListeEnregistrementCategorie(bdd
									.getCategorie(nomCategorie));
							if (!liste.isEmpty())
							{
								GraphicalUserInterface
										.popupErreur(
												"Une catégorie ne peut être supprimée que quand elle n'a plus d'enregistrements.",
												"Erreur");
							}
							else
							{
								bdd.supprimerCategorie(bdd.getCategorie(nomCategorie));
							}
						}
					}
					catch (Exception e1)
					{
						GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
					}
				}
			}
		}
	}

	private void removeSelectedSubjects()
	{
		if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET && arbre.getSelectionCount() >= 1 && onlySelectBranche())
		{
			int option = -1;

			if (arbre.getSelectionCount() == 1)
			{
				option = JOptionPane.showConfirmDialog(null, "Êtes-vous sûr de vouloir supprimer ce sujet ?\n",
						"Suppression", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			}
			else
			{
				option = JOptionPane.showConfirmDialog(null, "Êtes-vous sûr de vouloir supprimer ces sujets ?\n",
						"Suppression", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			}

			if (option == JOptionPane.OK_OPTION)
			{
				for (TreePath treePath : arbre.getSelectionPaths())
				{
					try
					{
						if (!(treePath.getLastPathComponent() instanceof Feuille))
						{
							String nomSujet = treePath.getLastPathComponent().toString();
							List<LigneEnregistrement> liste = bdd.getListeEnregistrementSujet(bdd.getSujet(nomSujet));
							if (!liste.isEmpty())
							{
								GraphicalUserInterface.popupErreur(
										"Un sujet ne peut être supprimé que quand il n'a plus d'enregistrements.",
										"Erreur");
							}
							else
							{
								bdd.supprimerSujet(bdd.getSujet(nomSujet));
							}
						}
					}
					catch (DBException e1)
					{
						GraphicalUserInterface.popupErreur(e1.getMessage());
					}
				}
			}
		}
	}

	private class KeyListenerTree extends KeyAdapter
	{

		@Override
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_DELETE)
			{
				removeSelectedCategories();
				removeSelectedRecords();
				removeSelectedSubjects();
			}
		}
	}

	private class ClicDroit extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)
			{
				if (arbre.getSelectionCount() <= 1)
				{
					arbre.setSelectionPath(arbre.getPathForLocation(e.getX(), e.getY()));
				}
				menuClicDroit = new JPopupMenu();

				JMenuItem changerTri = new JMenuItem();
				changerTri.addMouseListener(new ModifierTri());

				if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
				{
					changerTri.setText("Grouper par catégorie");
				}
				else if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
				{
					changerTri.setText("Grouper par sujet");
				}

				if (onlySelectFeuille())
				{

					if (arbre.getSelectionCount() >= 1)
					{
						JMenuItem dupliquer = new JMenuItem("Dupliquer les enregistrements");
						if (arbre.getSelectionCount() == 1)
						{
							dupliquer.setText("Dupliquer l'enregistrement");
						}
						dupliquer.addMouseListener(new DupliquerClicDroit());
						menuClicDroit.add(dupliquer);

						JMenuItem supprimer = new JMenuItem("Supprimer les enregistrements");
						if (arbre.getSelectionCount() == 1)
						{
							supprimer.setText("Supprimer l'enregistrement");
						}
						supprimer.addMouseListener(new SupprimerEnregistrementClicDroit());
						menuClicDroit.add(supprimer);

						if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
						{
							JMenuItem modifierCategorie = new JMenuItem("Changer catégorie");
							modifierCategorie.addMouseListener(new ModifierCategorieEnregistrementClicDroit());
							menuClicDroit.add(modifierCategorie);
						}
						else if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
						{
							JMenuItem modifierSujet = new JMenuItem("Changer sujet");
							modifierSujet.addMouseListener(new ModifierSujetEnregistrementClicDroit());
							menuClicDroit.add(modifierSujet);
						}
					}
					if (arbre.getSelectionCount() == 1)
					{
						JMenuItem exporter = new JMenuItem("Exporter");
						JMenuItem renommer = new JMenuItem("Renommer");
						JMenuItem ecouter = new JMenuItem("Écouter");
						exporter.addMouseListener(new ExporterEnregistrementClicDroit());
						ecouter.addMouseListener(new PlayEcouteArbre());
						renommer.addMouseListener(new RenommerEnregistrementClicDroit());
						menuClicDroit.add(renommer);
						menuClicDroit.add(ecouter);
						menuClicDroit.add(exporter);
					}
				}
				else if (arbre.getSelectionCount() >= 1 && onlySelectBranche())
				{
					if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
					{
						JMenuItem supprimerCategorie = new JMenuItem("Supprimer ces catégories");
						if (arbre.getSelectionCount() == 1)
						{
							supprimerCategorie.setText("Supprimer cette catégorie");
							JMenuItem renomerCategorie = new JMenuItem("Renommer cette catégorie");
							renomerCategorie.addMouseListener(new RenommerCategorieClicDroit());
							menuClicDroit.add(renomerCategorie);
						}
						supprimerCategorie.addMouseListener(new SupprimerCategorieEnregistrementClicDroit());
						menuClicDroit.add(supprimerCategorie);
					}
					else if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
					{
						JMenuItem supprimerSujet = new JMenuItem("Supprimer ces sujets");
						if (arbre.getSelectionCount() == 1)
						{
							supprimerSujet.setText("Supprimer ce sujet");
							JMenuItem renomerSujet = new JMenuItem("Renommer ce sujet");
							renomerSujet.addMouseListener(new RenommerSujetClicDroit());
							menuClicDroit.add(renomerSujet);
						}
						supprimerSujet.addMouseListener(new SupprimerSujetClicDroit());
						menuClicDroit.add(supprimerSujet);
					}
				}
				else if (arbre.getSelectionCount() == 0 || arbre.getSelectionCount() == 1)
				{
					JMenuItem collapseAll = new JMenuItem("Replier tout");
					JMenuItem expandAll = new JMenuItem("Développer tout");
					collapseAll.addMouseListener(new CollapseClicDroit());
					expandAll.addMouseListener(new ExpandClicDroit());
					menuClicDroit.add(expandAll);
					menuClicDroit.add(collapseAll);
					if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
					{
						JMenuItem ajouterSujet = new JMenuItem("Ajouter sujet");
						ajouterSujet.addMouseListener(new AjouterSujetListener(menuClicDroit, bdd));
						menuClicDroit.add(ajouterSujet);
					}
					else if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
					{
						JMenuItem ajouterCategorie = new JMenuItem("Ajouter catégorie");
						ajouterCategorie.addMouseListener(new AjouterCategorieListener(menuClicDroit, bdd));
						menuClicDroit.add(ajouterCategorie);
					}
				}

				menuClicDroit.add(changerTri);

				menuClicDroit.setEnabled(true);
				menuClicDroit.setVisible(true);

				menuClicDroit.show(arbre, e.getX(), e.getY());
			}
		}
	}

	private class SupprimerEnregistrementClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			removeSelectedRecords();
		}
	}

	private class ExporterEnregistrementClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent event)
		{
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.showOpenDialog(null);
			String fichier;
			if (fileChooser.getSelectedFile() != null)
			{
				try
				{
					fichier = fileChooser.getSelectedFile().getCanonicalPath();
					int id = ((Feuille) arbre.getLastSelectedPathComponent()).getId();
					// afficher gif
					bdd.exporter(fichier, id, 2);
				}
				catch (IOException e1)
				{
					GraphicalUserInterface.popupErreur(e1.getMessage());
				}
				catch (DBException e2)
				{
					GraphicalUserInterface.popupErreur(e2.getMessage());
				}
			}
		}
	}

	private class RenommerEnregistrementClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			String nom = JOptionPane.showInputDialog(null, "Entrez le nouveau nom", "Renommer",
					JOptionPane.QUESTION_MESSAGE);
			if (nom != null && !nom.equals(""))
			{
				try
				{
					if (arbre.getLastSelectedPathComponent() instanceof Feuille)// renommer enregistrement
					{
						bdd.modifierEnregistrementNom(((Feuille) arbre.getLastSelectedPathComponent()).getId(), nom);
					}
					else if (arbre.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode
							&& typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)// renommer une categorie
					{
						bdd.modifierCategorie(
								bdd.getCategorie(arbre.getSelectionPaths()[0].getLastPathComponent().toString()), nom);
					}
					else if (arbre.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode
							&& typeTrie == PanneauArbre.TYPE_TRIE_SUJET)// renommer une categorie
					{
						bdd.modifierSujet(bdd.getSujet(arbre.getSelectionPaths()[0].getLastPathComponent().toString()),
								nom);
					}
				}
				catch (DBException e1)
				{
					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
				}
			}
		}
	}

	private class RenommerCategorieClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			String nom = JOptionPane.showInputDialog(null, "Entrez le nouveau nom", "Renommer",
					JOptionPane.QUESTION_MESSAGE);
			if (nom != null && !nom.equals(""))
			{
				try
				{
					if (arbre.getLastSelectedPathComponent() instanceof Branche
							&& typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)// renommer une categorie
					{
						bdd.modifierCategorie(
								bdd.getCategorie(arbre.getSelectionPaths()[0].getLastPathComponent().toString()), nom);
					}
				}
				catch (DBException e1)
				{
					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
				}
			}
		}
	}

	private class RenommerSujetClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			String nom = JOptionPane.showInputDialog(null, "Entrez le nouveau nom", "Renommer",
					JOptionPane.QUESTION_MESSAGE);
			if (nom != null && !nom.equals(""))
			{
				try
				{
					if (arbre.getLastSelectedPathComponent() instanceof Branche
							&& typeTrie == PanneauArbre.TYPE_TRIE_SUJET)// renommer une categorie
					{
						bdd.modifierSujet(bdd.getSujet(arbre.getSelectionPaths()[0].getLastPathComponent().toString()),
								nom);
					}
				}
				catch (DBException e1)
				{
					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
				}
			}
		}
	}

	private class ModifierCategorieEnregistrementClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			DialogueNouvelleCategorie pop = new DialogueNouvelleCategorie(null, null, true, bdd);
			String nom = ((String) pop.activer()[0]);
			if (!nom.equals("Ne rien changer"))
			{
				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
				{
					if (arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
					{
						try
						{
							bdd.modifierEnregistrementCategorie(
									((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent()).getId(), nom);
						}
						catch (DBException e1)
						{
							GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
						}
					}
				}
			}
		}
	}

	private class SupprimerCategorieEnregistrementClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			removeSelectedCategories();
		}
	}

	private class SupprimerSujetClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			removeSelectedSubjects();
		}
	}

	private class ModifierSujetEnregistrementClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			DialogueNouveauSujet pop = new DialogueNouveauSujet(null, null, true, bdd);
			String nom = ((String) pop.activer()[0]);
			if (!nom.equals("Ne rien changer"))
			{
				for (TreePath treePath : arbre.getSelectionPaths())
				{
					if (treePath.getLastPathComponent() instanceof Feuille)
					{
						try
						{
							bdd.modifierEnregistrementSujet(((Feuille) treePath.getLastPathComponent()).getId(), nom);
						}
						catch (DBException e1)
						{
							GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
						}
					}
				}
			}
		}
	}

	private class ModifierTri extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
			{
				typeTrie = PanneauArbre.TYPE_TRIE_SUJET;
			}
			else
			{
				typeTrie = PanneauArbre.TYPE_TRIE_CATEGORIE;
			}
			updateArbre();
		}
	}

	private class PlayEcouteArbre extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent event)
		{
			lecteurAudio.play();
		}
	}

	/**
	 * Quand on change l'élement selectionné on change le panneau info et s'il y en a que un on charge le fichier audio
	 * 
	 */
	private class ClicGauche extends MouseAdapter implements TreeSelectionListener
	{
		@Override
		public void mouseClicked(final MouseEvent event)
		{
			if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 && arbre.getSelectionCount() == 1
					&& onlySelectFeuille() && event.getClickCount() > 1)
			{
				lecteurAudio.play();
			}
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (arbre.getLastSelectedPathComponent() != null
							&& arbre.getLastSelectedPathComponent() instanceof Feuille)
					{
						loadAudioFile(((Feuille) arbre.getLastSelectedPathComponent()).getId());
						infoArbre.setListeInfo(((Feuille) arbre.getLastSelectedPathComponent()).getInfo());
						lecteurAudio.setVisible(true);
					}
					else
					{
						lecteurAudio.setVisible(false);
						infoArbre.setListeInfo(null);
					}
					infoArbre.repaint();
				}
			}).run();
		}
	}

	/**
	 * Permet replier l'arbre
	 * 
	 * @author Azazel
	 * 
	 */
	private class CollapseClicDroit extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			Enumeration children = racine.children();
			Object tmp, tab[] = new Object[2];
			tab[0] = racine;
			while (children.hasMoreElements())
			{
				tmp = children.nextElement();
				tab[1] = tmp;
				arbre.collapsePath(new TreePath(tab));
			}
		}
	}

	/**
	 * Permet deplier l'arbre
	 * 
	 * @author Azazel
	 * 
	 */
	private class ExpandClicDroit extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			Enumeration children = racine.children();
			Object tmp, tab[] = new Object[2];
			tab[0] = racine;
			while (children.hasMoreElements())
			{
				tmp = children.nextElement();
				tab[1] = tmp;
				arbre.expandPath(new TreePath(tab));
			}
		}
	}

	/**
	 * Permet dupliquer des enregistrements
	 * 
	 * @author Azazel
	 * 
	 */
	private class DupliquerClicDroit extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			TreePath[] liste = arbre.getSelectionPaths();
			String nom;
			for (TreePath path : liste)
			{
				if (path.getLastPathComponent() instanceof Feuille)
				{
					Feuille feuille = (Feuille) path.getLastPathComponent();
					nom = feuille.getNom() + ".copie";
					try
					{
						while (bdd.enregistrementExist(nom))
						{
							nom += ".copie";
						}
						bdd.ajouterEnregistrement(nom, feuille.getDuree(), feuille.getIdCategorie(),
								bdd.recupererEnregistrement(feuille.getId()), feuille.getIdSujet());
					}
					catch (DBException e1)
					{
						GraphicalUserInterface.popupErreur(e1.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Charge un fichier audio
	 * 
	 * @param id
	 */
	private void loadAudioFile(final int id)
	{
		try
		{
			lecteurAudio.stop();
			File idAudioFile = getRecordCacheFile(id);
			if (idAudioFile != null)
			{
				lecteurAudio.load(idAudioFile.getCanonicalPath());
			}
		}
		catch (IOException e)
		{
			GraphicalUserInterface.popupErreur("Création du fichier audio temporaire : " + e.getMessage());
		}
		catch (DBException e)
		{
			GraphicalUserInterface.popupErreur("Création du fichier audio temporaire : " + e.getMessage());
		}

	}

	public File getRecordCacheFile(int id) throws IOException, DBException
	{
		final String fileName = id + ".wav";
		if (!cache.fileExists(fileName))
		{
			byte[] contenu = bdd.recupererEnregistrement(id);
			cache.createFile(fileName, contenu);
			updateCacheSizeInfo();
		}
		return cache.getFile(fileName);
	}

	public void updateCacheSizeInfo()
	{
		labelCacheSize.setText("Taille du cache : " + Utils.humanReadableByteCount(cache.getSize(), false));
	}

	@Override
	public void onUpdateDataBase()
	{

		if (this.labelCacheSize != null)
		{
			this.labelDBSize.setText("Taille de la base de données : "
					+ Utils.humanReadableByteCount(Utils.getDBSize(), false));
		}
		this.updateArbre();
	}

	@Override
	public void lockUpdate()
	{
		this.lock = true;
	}

	@Override
	public void unlockUpdate()
	{
		this.lock = false;
		this.updateArbre();
	}

	@Override
	public void onInProgressAction(String message)
	{
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	@Override
	public void onCompletedAction(String message)
	{
		this.setCursor(Cursor.getDefaultCursor());
	}
}
