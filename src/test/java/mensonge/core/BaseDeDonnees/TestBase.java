package mensonge.core.BaseDeDonnees;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestBase
{

	/**
	 * Fonction permettant de verifier le bon fonctionnement de l'objet BaseDeDonnees en fournissant un echantillons de
	 * test.
	 */
	public static void checkRunning()
	{
		// BaseDeDonnees db;

		/*
		 * Creer l'objet avec le nom On tente une connexion S'il y a une erreur, on lance la creation de la base
		 * 
		 * ****Manipulation des categorie*****Ajouter 3 categoriemodifier la seconde categorieLister les categories
		 * supprimer la premiereLister les categorie
		 * 
		 * ***Manipulation des enregistrements******Ajouter 7 enregistrements (meme fichier mais nom different (+simple)
		 * recuperer la liste de tous les enregistrementsmodifier l'enregistrement 3 et 7afficher les enregistrements la
		 * categorie 1 et 2supprimer l'enregistrement 5afficher le nombre d'enregistrementrecuperer l'enregistrement 2
		 * recuperer l'enregistrement 4
		 * 
		 * 
		 * ***Manipuler import/export****exporter la base (facultatif dans l'immediat)importer un fichier dbAfficher la
		 * liste des categories et des enregistrements
		 * 
		 * Deconnexion
		 */

		BaseDeDonnees db = null;
		try
		{
			db = new BaseDeDonnees("LieLabTest.db");
			db.connexion();// connexion
			db.createDatabase();
		}
		catch (DBException e)
		{
			int a = e.getCode();
			if (a == 2)
			{
				System.out.println("[i]Base en cour de creation ...");
				try
				{
					db.createDatabase();
				}
				catch (DBException e1)
				{
					System.out.println("[-] Erreur lors de la creation: " + e1.getMessage());
				}
				// creation de la base
				System.out.println("[i]Base cree.");
			}
			else
			{
				System.out.println("[-]Erreur lors de la connexion. " + e.getMessage());
				return;
			}
		}

		ResultSet rs;

		// ****IMPORTE****

		if (TestBase.createFictiveDataBase())// On creer une seconde base de donnees fictive, si on reussi a la creer,
												// on l'importe
		{
			// Importation
			try
			{
				System.out.println("\n[i] Importation");
				db.importer("LieLabTest2.db");
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			// AFFICHAGE ENREGISTREMENT
			rs = null;
			
			System.out.println("[i] Affichage.");
			try
			{
				System.out.println("NOM\t\tNOM CAT\t\tDUREE\t\tTAILLE");
				while (rs != null && rs.next())
				{
					System.out.println(rs.getString(3) + "\t\t" + rs.getString(4) + "\t\t" + rs.getString(1) + "\t\t"
							+ rs.getString(2));
				}
			}
			catch (Exception e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			// AFFICHAGE CATEGORIE
			ResultSet l = null;
			try
			{
				//l = db.getListeCategorie();
				System.out.println("[i] Affichage categorie.");
				while (l.next())
				{
					System.out.println(l.getString(1));
				}
			}
			catch (SQLException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
		}
		// ****Exportation****

		try
		{
			System.out.println("[i] Exportation Base vers export.db");
			db.exporter("export.db", 0, 1);

			if (!sha1(readFile("export.db")).equals(sha1(readFile(db.getFileName()))))
			{
				System.out.println("[-] Les deux fichiers exportés sont differents");
			}
		}
		catch (DBException e)
		{
			System.out.println("[-] " + e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println("[-] " + e.getMessage());
		}

		try
		{
			System.out.println("[i] Exportation de l'enregistrement 1");
			if (!sha1(db.recupererEnregistrement(1)).equals(sha1(readFile("export.txt"))))
			{
				System.out.println("[-] L'exportation de l'enregistrement à échoué.");
			}
		}
		catch (DBException e)
		{
			System.out.println("[-] " + e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println("[-] " + e.getMessage());
		}

		File f = new File("test.wav");
		if (f.exists())
		{
			try
			{
				FileInputStream sourceFile = new java.io.FileInputStream(f);
				byte[] contenu = new byte[(int) f.length()];
				sourceFile.read(contenu);
				sourceFile.close();
				System.out.println((int) f.length());
				System.out.println(contenu.length);
				// db.ajouterEnregistrement("Fichierwave", 5, 2, contenu);
				System.out.println("[+] Ajout du fichier test.wav");
			}
			catch (Exception e)
			{
				System.out.println("[-] TEST.WAV (pas important): " + e.getMessage());
			}
		}
		// ****Deconnexion****
		try
		{
			System.out.println("[i] Deconnexion.");
			db.deconnexion();
		}
		catch (DBException e)
		{
			System.out.println("[-] " + e.getMessage());
		}
	}

	/**
	 * Fonction permettant de creer une base de donnees pour les test de checkRunning. Elle dispose de 2 categorie
	 * Licorne et Dieux avec un enregistrement dans chacune.
	 * 
	 * @return true si la base s'est bien creer.
	 * @see checkRunning
	 */
	public static boolean createFictiveDataBase()
	{
		BaseDeDonnees db = null;
		try
		{
			db = new BaseDeDonnees("LieLabTest2.db");
			db.connexion();// connexion
			db.createDatabase();
		}
		catch (DBException e)
		{
			int a = e.getCode();
			if (a == 2)
			{
				System.out.println("[i]Base en cour de creation ...");
				try
				{
					db.createDatabase();
				}
				catch (DBException e1)
				{
					System.out.println("[-] Erreur lors de la creation: " + e1.getMessage());
				}
				// creation de la base
				System.out.println("[i]Base cree.");
			}
			else
			{
				System.out.println("[-]Erreur lors de la connexion. " + e.getMessage());
				return false;
			}
		}
		try
		{
			db.ajouterCategorie("Dieux*");
			db.ajouterCategorie("Licorne");

			// db.ajouterEnregistrement("Hades* ", 7, 1, "azerty".getBytes());
			// db.ajouterEnregistrement("Bella*", 7, 2, "qsdfgh".getBytes());

			db.deconnexion();
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	public static byte[] readFile(String nom)
	{
		File fichier = new File(nom);
		byte[] contenu = null;
		try
		{
			contenu = new byte[(int) fichier.length()];
			FileInputStream sourceFile = new FileInputStream(fichier);
			sourceFile.read(contenu);
			sourceFile.close();
		}
		catch (Exception e)
		{
			return null;
		}
		return contenu;
	}

	public static String sha1(byte[] convertme) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		return byteArray2Hex(md.digest(convertme));
	}

	public static String byteArray2Hex(byte[] hash)
	{
		Formatter formatter = new Formatter();
		for (byte b : hash)
		{
			formatter.format("%02x", b);
		}
		String ret = formatter.toString();
		formatter.close();
		return ret;
	}

	private static BaseDeDonnees db = null;

	@BeforeClass
	public static void init() throws Exception
	{
		try
		{
			db = new BaseDeDonnees("LieLabTest.db");// on creer l'objet BaseDeDonnee sur un fichier special
			db.connexion();// connexion
			db.createDatabase();// creation de la base et effacement d'evantuel table existante
		}
		catch (DBException e)
		{
			int a = e.getCode();// on recupere le code de l'exception
			if (a == 2)// Si c'est une erreur de structure de base on creer la base
			{
				try
				{
					db.createDatabase();
				}
				catch (DBException e1)
				{
					throw new Exception("Erreur de creation");
				}
			}
			else
			// Sinon on affiche l'erreur et on arrete
			{
				throw new Exception("Erreur connexion");
			}
		}
	}

	@Test
	public void testAjoutCategorie() throws DBException, SQLException
	{
		int i = 0;
		db.ajouterCategorie("Poney");
		db.ajouterCategorie("Flamment");
		db.ajouterCategorie("Pegase");

		List<LigneEnregistrement> liste = db.getListeCategorie();
		i = liste.size();
		assertTrue(i == 3);
	}

	@Test
	public void testRenommerCategorie() throws DBException, SQLException
	{
		int i = 0;
		String nom = null;
		db.modifierCategorie(2, "Licorne");

		List<LigneEnregistrement> liste = db.getListeCategorie();
		for(LigneEnregistrement ligne : liste)
		{
			i++;
			if (i == 2)
			{
				nom = ligne.getNomCat();
			}
		}
		assertTrue(i == 3 && nom.equals("Licorne"));
	}

	@Test
	public void testAfficherCategorie() throws DBException, SQLException, NoSuchAlgorithmException
	{
		String nom = new String();
		List<LigneEnregistrement> liste = db.getListeCategorie();
		for(LigneEnregistrement ligne : liste)
		{
			nom += ligne.getNomCat();
		}
		assertTrue(sha1("PoneyLicornePegase".getBytes()).equals(sha1(nom.getBytes())));
	}

	@Test
	public void testConvertionCategorie() throws DBException, SQLException, NoSuchAlgorithmException
	{
		assertTrue(db.getCategorie(2).equals("Licorne") && db.getCategorie("Pegase") == 3);
	}

	@Test
	public void testSupprimerCategorie() throws DBException, SQLException
	{
		int i = 0;
		db.supprimerCategorie(1);

		List<LigneEnregistrement> liste = db.getListeCategorie();
		i = liste.size();
		assertTrue(i == 2);
	}

	@Test
	public void testAjoutSujet() throws DBException, SQLException
	{
		int i = 0;
		db.ajouterSujet("Artemis");
		db.ajouterSujet("Ronald");
		db.ajouterSujet("Gwen");

		List<LigneEnregistrement> liste = db.getListeSujet();
		i = liste.size();
		assertTrue(i == 3);
	}

	@Test
	public void testRenommerSujet() throws DBException, SQLException
	{
		int i = 0;
		String nom = null;
		db.modifierSujet(2, "Toshiro");

		List<LigneEnregistrement> liste = db.getListeSujet();
		for(LigneEnregistrement ligne : liste)
		{
			i++;
			if (i == 2)
			{
				nom = ligne.getNomSuj();
			}
		}
		assertTrue(i == 3 && nom.equals("Toshiro"));
	}

	@Test
	public void testAfficherSujet() throws DBException, SQLException, NoSuchAlgorithmException
	{
		String nom = new String();
		List<LigneEnregistrement> liste = db.getListeSujet();
		for(LigneEnregistrement ligne : liste)
		{
			nom += ligne.getNom();
		}
		assertTrue(sha1("ArtemisToshiroGwen".getBytes()).equals(sha1(nom.getBytes())));
	}

	@Test
	public void testConvertionSUjet() throws DBException, SQLException, NoSuchAlgorithmException
	{
		assertTrue(db.getSujet(2).equals("Toshiro") && db.getSujet("Gwen") == 3);
	}

	@Test
	public void testSupprimerSujet() throws DBException, SQLException
	{
		int i = 0;
		db.supprimerSujet(1);

		List<LigneEnregistrement> liste = db.getListeSujet();
		i = liste.size();
		assertTrue(i == 2);
	}

	@Test
	public void testAjoutEnregistrement() throws DBException, SQLException
	{
		db.ajouterCategorie("Poney");

		db.ajouterEnregistrement("Esperan", 23, db.getCategorie("Poney"), "love".getBytes(), 2);
		db.ajouterEnregistrement("Gracia", 22, 3, "mort".getBytes(), 3);
		db.ajouterEnregistrement("Chuck", 21, 3, "naissance".getBytes(), 2);
		db.ajouterEnregistrement("Jilano", 18, 3, "erreur".getBytes(), 3);
		db.ajouterEnregistrement("Ellana", 18, 3, "anti".getBytes(), 3);
		db.ajouterEnregistrement("Knight", 18, 3, "IUT".getBytes(), 3);
		db.ajouterEnregistrement("Ermes", 18, 3, "Olympe".getBytes(), 3);
		int i = 0;
		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		i = liste.size();
		assertTrue(i == 7);
	}

	@Test(expected = DBException.class)
	public void testAjoutHorsCatEnregistrement() throws DBException, SQLException
	{
		db.ajouterEnregistrement("Exception", 24, 5, "abcdefg".getBytes(), 3);
		assertTrue(false);
	}

	@Test(expected = DBException.class)
	public void testAjoutHorsSujEnregistrement() throws DBException, SQLException
	{
		db.ajouterEnregistrement("Exception", 24, 2, "abcdefg".getBytes(), 1);
		assertTrue(false);
	}

	@Test
	public void testModifEnregistrement() throws DBException, SQLException
	{
		db.modifierEnregistrement(1, "Zeus", 15, 55, 3, 2);
		db.modifierEnregistrement(2, "Taylor", 17, 77, 2, 3);
		db.modifierEnregistrementCategorie(3, "Licorne");
		db.modifierEnregistrementNom(4, "Norris");
		db.modifierEnregistrementTaille(5, 250);
		int i = 0;
		int taille = 0;
		String nom1 = null, nom2 = null, categorie3 = null, nom4 = null;
		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		i = liste.size();
		for(LigneEnregistrement ligne : liste)
		{
			switch (ligne.getId())
			{
				case 1:
					nom1 = ligne.getNom();
					break;
				case 2:
					nom2 = ligne.getNom();
					break;
				case 3:
					categorie3 = ligne.getNomCat();
					break;
				case 4:
					nom4 = ligne.getNom();
					break;
				case 5:
					taille = ligne.getTaille();
					break;
			}
		}
		
		assertTrue(i == 7);
		assertTrue(taille == 250);
		assertTrue(nom1.equals("Zeus"));
		assertTrue(nom2.equals("Taylor"));
		assertTrue(categorie3.equals("Licorne")); 
		assertTrue(nom4.equals("Norris"));
	}

	@Test
	public void testSuprimmerEnregistrement() throws DBException, SQLException
	{
		db.supprimerEnregistrement(5);
		int i = 0;
		boolean tmp = true;
		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		for(LigneEnregistrement ligne : liste)
		{
			i++;
			if (ligne.getId() == 5)
			{
				tmp = false;
			}

		}
		assertTrue(i == 6 && tmp == true);
	}

	@Test
	public void testRecupererEnregistrement() throws DBException, SQLException
	{
		String enr1, enr2;
		byte[] tab = null;
		tab = db.recupererEnregistrement(4);
		enr1 = new String(tab);
		tab = db.recupererEnregistrement(6);
		enr2 = new String(tab);
		assertTrue(enr1.equals("erreur"));
		assertTrue(enr2.equals("IUT"));
	}

	@Test(expected = DBException.class)
	public void testRecupererEnregistrementInexistant() throws DBException, SQLException
	{
		db.recupererEnregistrement(25);
		assertTrue(false);
	}

	@Test
	public void testNombreEnregistrement() throws DBException, SQLException
	{
		int nb = db.getNombreEnregistrement();
		int i = 0;

		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		i = liste.size();
		assertTrue(i == 6 && nb == 6);
	}

	@AfterClass
	public static void fin() throws DBException
	{
		db.deconnexion();
		db = null;
	}
}
