package mensonge.core.BaseDeDonnees;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestBaseDeDonneesModele
{

	/**
	 * Fonction permettant de creer une base de donnees pour les test de checkRunning. Elle dispose de 2 categorie
	 * Licorne et Dieux avec un enregistrement dans chacune.
	 * 
	 * @return true si la base s'est bien creer.
	 * @see checkRunning
	 */
	public static boolean createFictiveDataBase()
	{
		/*
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
		*/
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

	private static BaseDeDonneesModele db = null;

	@BeforeClass
	public static void init() throws Exception
	{
		File fichier = new File("LieLabTest.db");
		fichier.createNewFile();
		db = new BaseDeDonneesModele("LieLabTest.db");
		db.connexion();
	}

	@Before
	public void beforeTest() throws SQLException, DBException
	{
		db.createDatabase();
		db.ajouterCategorie("Poney");
		db.ajouterCategorie("Flamment");
		db.ajouterCategorie("Pegase");
		
		db.ajouterSujet("Artemis");
		db.ajouterSujet("Ronald");
		db.ajouterSujet("Gwen");
		
		db.ajouterEnregistrement("Esperan", 21, 1, "love".getBytes(), 1);
		db.ajouterEnregistrement("Gracia", 21, 1, "mort".getBytes(), 1);
		db.ajouterEnregistrement("Chuck", 21, 1, "naissance".getBytes(), 1);
	}
	@Test
	public void testAjoutCategorie() throws DBException, SQLException
	{
		int i = 0;
		db.ajouterCategorie("Licorne");

		List<LigneEnregistrement> liste = db.getListeCategorie();
		i = liste.size();
		assertEquals(4, i);
	}

	@Test
	public void testRenommerCategorie() throws DBException, SQLException
	{
		int i = 0;
		String nom = null;
		db.modifierCategorie(2, "Licorne");

		List<LigneEnregistrement> liste = db.getListeCategorie();
		for (LigneEnregistrement ligne : liste)
		{
			i++;
			if (ligne.getIdCat() == 2)
			{
				nom = ligne.getNomCat();
			}
		}
		assertTrue(i == 3 && nom.equals("Licorne"));
	}

	@Test
	public void testAfficherCategorie() throws DBException, SQLException, NoSuchAlgorithmException
	{
		String nom = "";
		List<LigneEnregistrement> liste = db.getListeCategorie();
		for (LigneEnregistrement ligne : liste)
		{
			nom += ligne.getNomCat();
		}
		assertEquals("PoneyFlammentPegase", nom);
	}

	@Test
	public void testConvertionCategorie() throws DBException, SQLException, NoSuchAlgorithmException
	{
		assertTrue(db.getCategorie(2).equals("Flamment") && db.getCategorie("Pegase") == 3);
	}

	@Test
	public void testSupprimerCategorie() throws DBException, SQLException
	{
		int i = 0;
		db.supprimerCategorie(1);

		List<LigneEnregistrement> liste = db.getListeCategorie();
		for(LigneEnregistrement ligne : liste)
		{
			assertTrue(ligne.getIdCat() != 1);
		}
		i = liste.size();
		assertEquals(2, i);
	}

	@Test
	public void testAjoutSujet() throws DBException, SQLException
	{
		int i = 0;
		
		db.ajouterSujet("Toshiro");

		List<LigneEnregistrement> liste = db.getListeSujet();
		i = liste.size();
		assertEquals(4, i);
	}

	@Test
	public void testRenommerSujet() throws DBException, SQLException
	{
		int i = 0;
		String nom = null;
		db.modifierSujet(2, "Toshiro");

		List<LigneEnregistrement> liste = db.getListeSujet();
		for (LigneEnregistrement ligne : liste)
		{
			if (ligne.getIdSuj() == 2)
			{
				nom = ligne.getNomSuj();
			}
		}
		i = liste.size();
		assertTrue(i == 3 && nom.equals("Toshiro"));
	}

	@Test
	public void testAfficherSujet() throws DBException, SQLException, NoSuchAlgorithmException
	{
		String nom = new String();
		List<LigneEnregistrement> liste = db.getListeSujet();
		for (LigneEnregistrement ligne : liste)
		{
			nom += ligne.getNomSuj();
		}
		assertEquals("ArtemisRonaldGwen", nom);
	}

	@Test
	public void testConvertionSUjet() throws DBException, SQLException, NoSuchAlgorithmException
	{
		assertTrue(db.getSujet(2).equals("Ronald") && db.getSujet("Gwen") == 3);
	}

	@Test
	public void testSupprimerSujet() throws DBException, SQLException
	{
		int i = 0;
		db.supprimerSujet(1);

		List<LigneEnregistrement> liste = db.getListeSujet();
		for(LigneEnregistrement ligne : liste)
		{
			assertTrue(ligne.getIdSuj() != 1);
		}
		i = liste.size();
		assertEquals(2, i);
	}

	@Test
	public void testAjoutEnregistrement() throws DBException, SQLException
	{
		db.ajouterEnregistrement("Ermes", 18, 3, "Olympe".getBytes(), 3);
		int i = 0;
		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		i = liste.size();
		assertEquals(4, i);
	}

	@Test
	public void testAjoutHorsCatEnregistrement() throws SQLException
	{
		db.ajouterEnregistrement("Exception", 24, 25, "abcdefg".getBytes(), 1);
		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		assertEquals(3, liste.size());
	}

	@Test
	public void testAjoutHorsSujEnregistrement() throws SQLException
	{
		db.ajouterEnregistrement("Exception", 24, 1, "abcdefg".getBytes(), 33);
		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		assertEquals(3, liste.size());
	}

	@Test
	public void testModifEnregistrement() throws DBException, SQLException
	{		
		
		
		db.modifierEnregistrement(1, "Zeus", 15, 55, 3, 2);
		db.modifierEnregistrementCategorie(2, "Poney");
		db.modifierEnregistrementSujet(2, "Gwen");
		db.modifierEnregistrementTaille(2, 250);
		db.modifierEnregistrementDuree(2, 77);
		db.modifierEnregistrementNom(2, "Chuck1");
		
		String nom = null;
		LigneEnregistrement tmp = null;
		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		for (LigneEnregistrement ligne : liste)
		{
			switch (ligne.getId())
			{
				case 1:
					nom = ligne.getNom();
					break;
				case 2:
					tmp = ligne;
					break;
			}
		}
		
		assertEquals(3, liste.size());
		assertEquals(250, tmp.getTaille());
		assertEquals(77, tmp.getDuree());
		assertEquals("Zeus", nom);
		assertEquals("Poney", tmp.getNomCat());
		assertEquals("Gwen", tmp.getNomSuj());
		assertEquals("Chuck1", tmp.getNom());
	}

	@Test
	public void testGetNomEnregistrement() throws SQLException
	{
		String nom = db.getNomEnregistrement(1);
		assertEquals(nom, "Esperan");
		nom = db.getNomEnregistrement(55);
		assertTrue(nom == null);
	}
	
	@Test
	public void testGetNombreEnregistrement() throws SQLException
	{
		int nb = db.getNombreEnregistrement();
		assertEquals(nb, 3);
	}
	
	@Test
	public void testSuprimmerEnregistrement() throws DBException, SQLException
	{
		db.supprimerEnregistrement(1);
		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		for (LigneEnregistrement ligne : liste)
		{
			assertTrue(ligne.getId() != 1);

		}
		assertTrue(liste.size() == 2);
	}

	@Test
	public void testRecupererEnregistrement() throws DBException, SQLException
	{
		String enr1, enr2;
		byte[] tab = null;
		tab = db.recupererEnregistrement(1);
		enr1 = new String(tab);
		tab = db.recupererEnregistrement(2);
		enr2 = new String(tab);
		assertEquals("love", enr1);
		assertEquals("mort", enr2);
	}

	@Test
	public void testRecupererEnregistrementInexistant() throws DBException, SQLException
	{
		assertTrue(db.recupererEnregistrement(25) == null);
	}

	@Test
	public void testNombreEnregistrement() throws DBException, SQLException
	{
		int nb = db.getNombreEnregistrement();
		int i = 0;

		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		i = liste.size();
		assertTrue(i == 3 && nb == 3);
	}

	@Test
	public void testExporterEnregistrement() throws DBException, SQLException
	{
		db.exporterEnregistrement("TestExport1", 1);
		byte[] contenu_fichier = readFile("TestExport1");
		byte[] contenu_enregistrement = db.recupererEnregistrement(1);
		for(int i = 0; i < contenu_enregistrement.length; i++)
		{
			assertEquals(contenu_enregistrement[i], contenu_fichier[i]);
		}
		assertEquals(2, contenu_fichier.length - contenu_enregistrement.length);
	}
	
	@Test
	public void testExporterBase() throws DBException, NoSuchAlgorithmException
	{
		db.exporterBase("TestExport2");
		byte[] contenu_fichier = readFile("TestExport2");
		byte[] contenu_base = readFile(db.getFileName());
		assertEquals(contenu_fichier.length, contenu_base.length);
		String sortie = sha1(contenu_fichier), entree = sha1(contenu_base);
		assertEquals(sortie, entree);
	}
	
	@AfterClass
	public static void fin() throws SQLException
	{
		db.deconnexion();
		db = null;
	}
}
