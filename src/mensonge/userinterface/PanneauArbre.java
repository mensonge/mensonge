package mensonge.userinterface;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.sql.ResultSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import mensonge.core.BaseDeDonnees.BaseDeDonnees;
import mensonge.core.BaseDeDonnees.DBException;


public class PanneauArbre extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int TYPE_TRIE_CATEGORIE = 1;
	private static final int TYPE_TRIE_SUJET = 2;

	private BaseDeDonnees bdd = null;

	private JButton playEcouteArbre = new JButton("play");
	private JButton stopEcouteArbre = new JButton("Stop");
	private JButton pauseEcouteArbre = new JButton("Pause");
	private JSlider slideAvance;
	private JSlider slideSon;

	private JPanel panelLecteur;

	private double volume = 0.5;
	private Sound lecteurSonArbre;

	private PanneauInformationFeuille infoArbre = new PanneauInformationFeuille();
	private DefaultMutableTreeNode racine;
	private JTree arbre;
	private JScrollPane scrollPane;

	private JPopupMenu menuClicDroit = new JPopupMenu();// sers au clic droit

	private int typeTrie = PanneauArbre.TYPE_TRIE_SUJET;

	public PanneauArbre(BaseDeDonnees bdd)
	{
		this.bdd = bdd;

		Toolkit tk;
		Image i;
		URL url = this.getClass().getResource("/images/Lecture.png");
		if (url != null)
		{
			tk = this.getToolkit();
			i = tk.getImage(url);
			playEcouteArbre = new JButton(new ImageIcon(i));
		}
		url = this.getClass().getResource("/images/Pause.png");
		if (url != null)
		{
			tk = this.getToolkit();
			i = tk.getImage(url);
			pauseEcouteArbre = new JButton(new ImageIcon(i));
		}
		url = this.getClass().getResource("/images/CloseTab.png");
		if (url != null)
		{
			tk = this.getToolkit();
			i = tk.getImage(url);
			stopEcouteArbre = new JButton(new ImageIcon(i));
		}

		this.racine = new DefaultMutableTreeNode("Sujet");
		this.remplirArbreEnregistrementSujet();
		this.arbre = new JTree(racine);
		this.arbre.addMouseListener(new ClicDroit());

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
					panelLecteur.setVisible(true);
				}
				else
				{
					infoArbre.setListeInfo(null);
					infoArbre.repaint();// on le repaint
					panelLecteur.setVisible(false);
				}
			}
		});

		this.scrollPane = new JScrollPane(arbre);
		this.scrollPane.setPreferredSize(new Dimension(270, 300));
		this.scrollPane.setAutoscrolls(true);

		this.infoArbre.setPreferredSize(new Dimension(270, 100));

		JPanel panelArbre = new JPanel(new GridLayout(0, 1));
		this.panelLecteur = new JPanel(new GridBagLayout());
		this.panelLecteur.setVisible(false);
		panelArbre.add(this.scrollPane);
		panelArbre.add(this.infoArbre);

		// panelEnregistrements.add(panelArbre, BorderLayout.CENTER);

		/*
		 * Creation du mini lecteur audio
		 */
		this.slideAvance = new JSlider();
		this.slideAvance.setValue(0);
		this.slideSon = new JSlider();
		this.slideSon.setMinimum(0);
		this.slideSon.setMaximum(100);
		this.slideSon.setValue((int) (volume * 100));
		this.slideSon.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent event)
			{
				volume = ((double) slideSon.getValue() / 100);
				if (lecteurSonArbre != null)
				{
					lecteurSonArbre.setVolume(volume);
				}
			}
		});
		// Ajout de bouton pour le lecteur
		this.playEcouteArbre.addMouseListener(new PlayEcouteArbre());
		this.stopEcouteArbre.addMouseListener(new StopEcouteArbre());
		this.pauseEcouteArbre.addMouseListener(new PauseEcouteArbre());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		this.panelLecteur.add(slideSon, c);
		c.gridx = 1;
		c.gridy = 0;
		this.panelLecteur.add(new JLabel("Volume"), c);
		c.gridx = 0;
		c.gridy = 1;
		this.panelLecteur.add(slideAvance, c);
		c.gridx = 1;
		c.gridy = 1;
		this.panelLecteur.add(new JLabel("Curseur"), c);
		c.gridx = 0;
		c.gridy = 2;
		this.panelLecteur.add(playEcouteArbre, c);
		c.gridx = 1;
		c.gridy = 2;
		this.panelLecteur.add(pauseEcouteArbre, c);
		c.gridx = 2;
		c.gridy = 2;
		this.panelLecteur.add(stopEcouteArbre, c);
		panelArbre.add(this.panelLecteur);
		this.add(panelArbre);
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
		this.arbre.updateUI();

	}

	public void remplirArbreEnregistrementCategorie()
	{
		ResultSet rs_cat = null, rs_enr = null;
		try
		{
			rs_cat = this.bdd.getListeCategorie();
			while (rs_cat.next())
			{
				Branche node = new Branche(rs_cat.getString("nomcat"));
				rs_enr = this.bdd.getListeEnregistrementCategorie(rs_cat.getInt("idcat"));
				while (rs_enr.next())
				{
					Feuille f = new Feuille(rs_enr.getInt("id"), rs_enr.getString("nom"), rs_enr.getInt("duree"),
							rs_enr.getInt("taille"), rs_enr.getString("nomCat"), rs_enr.getString("nomsuj"));
					node.add(f);
				}
				rs_enr.close();
				this.racine.add(node);

			}
			rs_cat.close();
			this.racine.setUserObject("Categorie");
		}
		catch (Exception e)
		{
			GraphicalUserInterface.popupErreur("Erreur lors du chargement des enregistrement.", "Erreur");
		}
	}

	public void remplirArbreEnregistrementSujet()
	{
		ResultSet rs_cat = null, rs_enr = null;
		try
		{
			rs_cat = this.bdd.getListeSujet();
			while (rs_cat.next())
			{
				Branche node = new Branche(rs_cat.getString("nomsuj"));
				rs_enr = this.bdd.getListeEnregistrementSujet(rs_cat.getInt("idsuj"));
				while (rs_enr.next())
				{
					Feuille f = new Feuille(rs_enr.getInt("id"), rs_enr.getString("nom"), rs_enr.getInt("duree"),
							rs_enr.getInt("taille"), rs_enr.getString("nomCat"), rs_enr.getString("nomsuj"));
					node.add(f);
				}
				rs_enr.close();
				this.racine.add(node);

			}
			rs_cat.close();
			this.racine.setUserObject("Sujet");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			GraphicalUserInterface.popupErreur("Erreur lors du chargement des enregistrement.", "Erreur");
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
			for (int i = 0; i < paths.length; i++)
			{
				if (!(paths[i].getLastPathComponent() instanceof Branche))
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
			for (int i = 0; i < paths.length; i++)
			{
				if (!(paths[i].getLastPathComponent() instanceof Feuille))
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

	class ClicDroit implements MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent arg0)
		{
		}

		@Override
		public void mouseEntered(MouseEvent arg0)
		{
		}

		@Override
		public void mouseExited(MouseEvent arg0)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)
			{
				if (arbre.getSelectionCount() <= 1)
				{
					arbre.setSelectionPath(arbre.getPathForLocation(e.getX(), e.getY()));
				}
				menuClicDroit.setEnabled(false);
				menuClicDroit.setVisible(false);
				menuClicDroit = new JPopupMenu();
				JMenuItem exporter = new JMenuItem("Exporter");
				JMenuItem renommer = new JMenuItem("Renommer");

				JMenuItem ecouter = new JMenuItem("Ecouter");
				JMenuItem modifierCategorie = new JMenuItem("Changer categorie");
				JMenuItem supprimer = new JMenuItem("Supprimer les enregistrements");
				JMenuItem ajouterCategorie = new JMenuItem("Ajouter Categorie");
				JMenuItem supprimerCategorie = new JMenuItem("Supprimer Categorie");
				JMenuItem ajouterSujet = new JMenuItem("Ajouter Sujet");
				JMenuItem supprimerSujet = new JMenuItem("Supprimer Sujet");
				JMenuItem modifierSujet = new JMenuItem("Changer Sujet");
				JMenuItem changerTri = new JMenuItem();
				JMenuItem renomerCategorie = new JMenuItem("Renommer categorie");
				JMenuItem renomerSujet = new JMenuItem("Renommer Sujet");

				exporter.addMouseListener(new ExporterEnregistrementClicDroit());
				renommer.addMouseListener(new RenommerEnregistrementClicDroit());
				ecouter.addMouseListener(new PlayEcouteArbre());
				ajouterCategorie.addMouseListener(new AjouterCategorieEnregistrementClicDroit(menuClicDroit, bdd));
				modifierCategorie.addMouseListener(new ModifierCategorieEnregistrementClicDroit());
				supprimer.addMouseListener(new SupprimerEnregistrementClicDroit());
				supprimerCategorie.addMouseListener(new SupprimerCategorieEnregistrementClicDroit());
				ajouterSujet.addMouseListener(new AjouterSujetClicDroit(menuClicDroit, bdd));
				supprimerSujet.addMouseListener(new SupprimerSujetClicDroit());
				modifierSujet.addMouseListener(new ModifierSujetEnregistrementClicDroit());
				changerTri.addMouseListener(new ModifierTri());
				renomerCategorie.addMouseListener(new RenommerCategorieClicDroit());
				renomerSujet.addMouseListener(new RenommerSujetClicDroit());

				if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
				{
					changerTri.setText("Grouper par categories");
				}
				else if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
				{
					changerTri.setText("Grouper par sujets");
				}

				if (arbre.getSelectionCount() == 0)
				{
					if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
					{
						menuClicDroit.add(ajouterSujet);
					}
					else if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
					{
						menuClicDroit.add(ajouterCategorie);
					}

				}
				if (arbre.getSelectionCount() >= 1 && onlySelectFeuille())
				{
					menuClicDroit.add(supprimer);
					if (typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
					{
						menuClicDroit.add(modifierCategorie);
					}
					if (typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
					{
						menuClicDroit.add(modifierSujet);
					}

				}
				if (arbre.getSelectionCount() == 1 && onlySelectFeuille())
				{
					menuClicDroit.add(renommer);
					menuClicDroit.add(ecouter);
				}

				if (arbre.getSelectionCount() >= 1 && onlySelectBranche()
						&& typeTrie == PanneauArbre.TYPE_TRIE_CATEGORIE)
				{
					menuClicDroit.add(renomerCategorie);
					menuClicDroit.add(supprimerCategorie);
				}
				if (arbre.getSelectionCount() >= 1 && onlySelectBranche() && typeTrie == PanneauArbre.TYPE_TRIE_SUJET)
				{
					menuClicDroit.add(renomerSujet);
					menuClicDroit.add(supprimerSujet);
				}

				menuClicDroit.add(changerTri);

				menuClicDroit.setEnabled(true);
				menuClicDroit.setVisible(true);

				menuClicDroit.show(arbre, e.getX(), e.getY());
			}
			else
			{
				menuClicDroit.setEnabled(false);
				menuClicDroit.setVisible(false);
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0)
		{
		}

	}

	class SupprimerEnregistrementClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
			int option = JOptionPane.showConfirmDialog(null,
					"Voulez-vous supprimer les enregistrements ?\n(Notez que les categories seront concervées)",
					"Suppression", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (option == JOptionPane.OK_OPTION)
			{
				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
				{
					if (arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
					{
						bdd.supprimerEnregistrement(((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent())
								.getId());
					}
				}
			}
			updateArbre();
		}

	}

	class ExporterEnregistrementClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
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

	class RenommerEnregistrementClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
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
			updateArbre();
		}
	}

	class RenommerCategorieClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
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
			updateArbre();
		}
	}

	class RenommerSujetClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
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
			updateArbre();
		}
	}

	class AjouterCategorieEnregistrementClicDroit implements MouseListener
	{
		private JPopupMenu menuClicDroit;
		private BaseDeDonnees bdd;

		public AjouterCategorieEnregistrementClicDroit(JPopupMenu menuClicDroit, BaseDeDonnees bdd)
		{
			this.bdd = bdd;
			this.menuClicDroit = menuClicDroit;
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (menuClicDroit != null)
			{
				menuClicDroit.setEnabled(false);
				menuClicDroit.setVisible(false);
			}

			String nom = JOptionPane.showInputDialog(null, "Entrez le nom de la nouvelle catégorie", "Renommer",
					JOptionPane.QUESTION_MESSAGE);
			if (nom != null && !nom.equals(""))
			{
				try
				{
					bdd.ajouterCategorie(nom);
				}
				catch (DBException e1)
				{
					GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
				}
			}
			updateArbre();
		}
	}

	class ModifierCategorieEnregistrementClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
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
				updateArbre();
			}
		}
	}

	class SupprimerCategorieEnregistrementClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
			int option = JOptionPane.showConfirmDialog(null, "Voulez-vous supprimer les categories ?\n", "Suppression",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (option == JOptionPane.OK_OPTION)
			{
				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
				{
					try
					{
						if (!(arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille))
						{
							ResultSet rs = bdd.getListeEnregistrementCategorie(bdd.getCategorie(arbre
									.getSelectionPaths()[i].getLastPathComponent().toString()));
							if (rs.next())
							{
								GraphicalUserInterface.popupErreur(
										"Une categorie peut être supprimée quand elle n'a plus d'enregistrements.",
										"Erreur");
							}
							else
							{
								bdd.supprimerCategorie(bdd.getCategorie(arbre.getSelectionPaths()[i]
										.getLastPathComponent().toString()));
							}
							rs.close();
						}
					}
					catch (Exception e1)
					{
						GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
					}
				}
			}
			updateArbre();
		}
	}

	class AjouterSujetClicDroit implements MouseListener
	{
		private JPopupMenu menuClicDroit;
		private BaseDeDonnees bdd;

		public AjouterSujetClicDroit(JPopupMenu menuClicDroit, BaseDeDonnees bdd)
		{
			this.bdd = bdd;
			this.menuClicDroit = menuClicDroit;
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (menuClicDroit != null)
			{
				menuClicDroit.setEnabled(false);
				menuClicDroit.setVisible(false);
			}
			String option = JOptionPane.showInputDialog("Nouveau sujet");
			if (option != "" && option != null)
			{
				try
				{
					this.bdd.ajouterSujet(option);
				}
				catch (Exception e1)
				{
					GraphicalUserInterface.popupErreur(
							"Erreur lors de l'ajout du sujet " + option + " " + e1.getMessage(), "Erreur");
				}
			}
			updateArbre();
		}
	}

	class SupprimerSujetClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
			int option = JOptionPane.showConfirmDialog(null, "Voulez-vous supprimer les sujets ?\n", "Suppression",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (option == JOptionPane.OK_OPTION)
			{
				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
				{
					try
					{
						if (!(arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille))
						{
							ResultSet rs = bdd.getListeEnregistrementSujet(bdd.getSujet(arbre.getSelectionPaths()[i]
									.getLastPathComponent().toString()));
							if (rs.next())
							{
								GraphicalUserInterface.popupErreur(
										"Un sujet peut être supprimée quand il n'a plus d'enregistrements.", "Erreur");
							}
							else
							{
								bdd.supprimerSujet(bdd.getSujet(arbre.getSelectionPaths()[i].getLastPathComponent()
										.toString()));
							}
							rs.close();
						}
					}
					catch (Exception e1)
					{
						GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
					}
				}
			}
			updateArbre();
		}
	}

	class ModifierSujetEnregistrementClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
			DialogueNouveauSujet pop = new DialogueNouveauSujet(null, null, true, bdd);
			String nom = ((String) pop.activer()[0]);
			if (!nom.equals("Ne rien changer"))
			{
				for (int i = 0; i < arbre.getSelectionPaths().length; i++)
				{
					if (arbre.getSelectionPaths()[i].getLastPathComponent() instanceof Feuille)
					{
						try
						{
							bdd.modifierEnregistrementSujet(
									((Feuille) arbre.getSelectionPaths()[i].getLastPathComponent()).getId(), nom);
						}
						catch (DBException e1)
						{
							GraphicalUserInterface.popupErreur(e1.getMessage(), "Erreur");
						}
					}
				}
				updateArbre();
			}
		}
	}

	class ModifierTri implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			menuClicDroit.setEnabled(false);
			menuClicDroit.setVisible(false);
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

	class HighlightClicDroit implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			menuClicDroit.setBackground(new Color(238, 238, 238));
			Component a = menuClicDroit.getComponentAt(e.getX(), e.getY());
			if (a instanceof JMenuItem)
			{
				a.setBackground(Color.CYAN);
			}
			System.out.println(a.getClass());
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			Component a = menuClicDroit.getComponentAt(e.getX(), e.getY());
			a.setBackground(new Color(238, 238, 238));
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
		}
	}

	class PlayEcouteArbre extends MouseAdapter
	{
		private int id_lu;

		@Override
		public void mouseReleased(MouseEvent event)
		{
			// Créer le fichier
			File f = new File("tmp.wav");
			if (menuClicDroit != null)
			{
				menuClicDroit.setEnabled(false);
				menuClicDroit.setVisible(false);
			}
			try
			{
				if (lecteurSonArbre != null && lecteurSonArbre.isPause() == true
						&& ((Feuille) arbre.getLastSelectedPathComponent()).getId() == id_lu)
				{
					lecteurSonArbre.setPause(false);
				}
				else
				// if(! (lecteurSonArbre.isPause() == true && ((Feuille) arbre.getLastSelectedPathComponent()).getId()
				// == id_lu))
				{
					if (lecteurSonArbre != null)
					{
						lecteurSonArbre.stop();
					}
					if (f.exists())
					{
						f.delete();
					}
					if (!f.createNewFile())
					{
						throw new Exception("Impossible de créer le fichier temporaire.");
					}
					byte[] contenu = bdd.recupererEnregistrement(((Feuille) arbre.getLastSelectedPathComponent())
							.getId());
					GraphicalUserInterface.ecrireFichier(contenu, f);
					lecteurSonArbre = new Sound(f, slideAvance, volume);
					id_lu = ((Feuille) arbre.getLastSelectedPathComponent()).getId();
					lecteurSonArbre.play();
				}
			}
			catch (Exception e)
			{
				GraphicalUserInterface.popupErreur("Erreur lors du lancement de l'écoute: " + e.getMessage(), "Erreur");
				return;
			}

		}
	}

	class StopEcouteArbre extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent event)
		{
			// Créer le fichier
			File f = new File("tmp.wav");
			if (menuClicDroit != null)
			{
				menuClicDroit.setEnabled(false);
				menuClicDroit.setVisible(false);
			}
			try
			{
				if (lecteurSonArbre != null)
				{
					lecteurSonArbre.stop();
				}
				if (f.exists())
				{
					f.delete();
				}
			}
			catch (Exception e)
			{
				GraphicalUserInterface.popupErreur("Erreur lors du lancement de l'écoute: " + e.getMessage(), "Erreur");
				return;
			}

		}
	}

	class PauseEcouteArbre extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent event)
		{
			// DialogueOperationLongue d = new DialogueOperationLongue(null, "toot", true);
			// d.exporterBase();
			if (menuClicDroit != null)
			{
				menuClicDroit.setEnabled(false);
				menuClicDroit.setVisible(false);
			}

			try
			{
				if (lecteurSonArbre != null)
				{
					lecteurSonArbre.pause();
				}
			}
			catch (Exception e)
			{
				GraphicalUserInterface.popupErreur("Erreur lors du lancement de l'écoute: " + e.getMessage(), "Erreur");
				return;
			}

		}
	}

}
