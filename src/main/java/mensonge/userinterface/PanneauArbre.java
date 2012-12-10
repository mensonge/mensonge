package mensonge.userinterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import javax.swing.JTree;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import mensonge.core.Cache;
import mensonge.core.DataBaseObserver;
import mensonge.core.Utils;
import mensonge.core.BaseDeDonnees.BaseDeDonnees;
import mensonge.core.BaseDeDonnees.DBException;
import mensonge.core.BaseDeDonnees.LigneEnregistrement;

public final class PanneauArbre extends JPanel implements DataBaseObserver
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int TYPE_TRIE_CATEGORIE = 1;
	private static final int TYPE_TRIE_SUJET = 2;

	private BaseDeDonnees bdd = null;
	private int idLu = -1;

	private LecteurAudio lecteurAudio;

	private PanneauInformationFeuille infoArbre = new PanneauInformationFeuille();
	private DefaultMutableTreeNode racine;
	private JTree arbre;

	private JPopupMenu menuClicDroit = new JPopupMenu();// sers au clic droit

	private int typeTrie = PanneauArbre.TYPE_TRIE_SUJET;
	private JLabel labelCacheSize;
	private JLabel labelDBSize;

	public PanneauArbre(BaseDeDonnees bdd)
	{
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
		this.bdd = bdd;

		this.racine = new DefaultMutableTreeNode("Sujet");
		this.remplirArbreEnregistrementSujet();
		this.arbre = new JTree(racine);
		this.arbre.addMouseListener(new ClicDroit());
		this.arbre.addMouseListener(new ClicGauche());
		this.arbre.addKeyListener(new KeyListenerTree());
		this.arbre.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent event)
			{

				if (arbre.getLastSelectedPathComponent() != null
						&& arbre.getLastSelectedPathComponent() instanceof Feuille)
				{
					infoArbre.setListeInfo(((Feuille) arbre.getLastSelectedPathComponent()).getInfo());// On informe le
																										// panneau
																										// d'information
					infoArbre.repaint();// on le repaint
					lecteurAudio.setVisible(true);
				}
				else
				{
					infoArbre.setListeInfo(null);
					infoArbre.repaint();// on le repaint
					lecteurAudio.setVisible(false);
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(arbre);
		scrollPane.setPreferredSize(new Dimension(332, 450));
		scrollPane.setAutoscrolls(true);

		this.infoArbre.setPreferredSize(new Dimension(332, 100));

		this.labelCacheSize = new JLabel("Taille du cache : " + Utils.humanReadableByteCount(Cache.getSize(), false));
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

		this.lecteurAudio = new LecteurAudio();
		this.lecteurAudio.setVisible(false);

		this.add(panelConteneur, BorderLayout.NORTH);
		this.add(lecteurAudio, BorderLayout.SOUTH);
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
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
							if (liste.size() != 0)
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
							if (liste.size() != 0)
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

	private class KeyListenerTree implements KeyListener
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

		@Override
		public void keyReleased(KeyEvent arg0)
		{
		}

		@Override
		public void keyTyped(KeyEvent arg0)
		{
		}
	}

	class ClicDroit extends MouseAdapter
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
						if (idLu != ((Feuille) arbre.getLastSelectedPathComponent()).getId())
						{
							idLu = ((Feuille) arbre.getLastSelectedPathComponent()).getId();
							loadAudioFile(idLu);
						}
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

	class SupprimerEnregistrementClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			removeSelectedRecords();
		}
	}

	class ExporterEnregistrementClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
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
				catch (Exception e1)
				{
					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
					return;
				}
			}
		}
	}

	class RenommerEnregistrementClicDroit extends MouseAdapter
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

	class RenommerCategorieClicDroit extends MouseAdapter
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

	class RenommerSujetClicDroit extends MouseAdapter
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

	class ModifierCategorieEnregistrementClicDroit extends MouseAdapter
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

	class SupprimerCategorieEnregistrementClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			removeSelectedCategories();
		}
	}

	class SupprimerSujetClicDroit extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e)
		{
			removeSelectedSubjects();
		}
	}

	class ModifierSujetEnregistrementClicDroit extends MouseAdapter
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

	class ModifierTri extends MouseAdapter
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

	class PlayEcouteArbre extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent event)
		{
			lecteurAudio.play();
		}
	}

	/**
	 * Charge un fichier enregistrement.
	 * 
	 */
	private class ClicGauche extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
			{
				if (arbre.getSelectionCount() == 1 && onlySelectFeuille())
				{
					if (idLu != ((Feuille) arbre.getLastSelectedPathComponent()).getId())
					{
						idLu = ((Feuille) arbre.getLastSelectedPathComponent()).getId();
						loadAudioFile(idLu);
					}
					if (e.getClickCount() > 1)
					{
						lecteurAudio.play();
					}
				}
			}
		}
	}

	/**
	 * Permet replier l'arbre
	 * 
	 * @author Azazel
	 * 
	 */
	class CollapseClicDroit extends MouseAdapter
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
	class ExpandClicDroit extends MouseAdapter
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

	private void loadAudioFile(final int id)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
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
		});
	}

	public File getRecordCacheFile(int id) throws IOException, DBException
	{
		final String fileName = id + ".wav";
		if (!Cache.exists(fileName))
		{
			byte[] contenu = bdd.recupererEnregistrement(id);
			Cache.createFile(fileName, contenu);
			updateCacheSizeInfo();
		}
		return Cache.get(fileName);
	}

	public void updateCacheSizeInfo()
	{
		labelCacheSize.setText("Taille du cache : " + Utils.humanReadableByteCount(Cache.getSize(), false));
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
}
