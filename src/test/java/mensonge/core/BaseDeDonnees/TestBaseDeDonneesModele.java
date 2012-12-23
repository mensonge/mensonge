package mensonge.core.BaseDeDonnees;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestBaseDeDonneesModele
{
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
	}

	@Before
	public void beforeTest() throws SQLException, DBException, IOException, ClassNotFoundException
	{
		db.connexion();
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
	
	@After
	public void afterTest() throws SQLException, DBException, IOException, ClassNotFoundException
	{
		db.deconnexion();
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
		assertTrue(i == 3);
		assertTrue(nom.equals("Licorne"));
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
		assertTrue(i == 3);
		assertTrue(nom.equals("Toshiro"));
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
		db.modifierEnregistrementSujet(3, 3);
		db.modifierEnregistrementCategorie(3, 2);
		
		String nom = null;
		LigneEnregistrement tmp = null, tmp2 = null;
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
				case 3:
					tmp2 = ligne;
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
		assertEquals(2, tmp2.getIdCat());
		assertEquals(3, tmp2.getIdSuj());
	}

	@Test
	public void testGetNomEnregistrement() throws SQLException
	{
		String nom = db.getNomEnregistrement(1);
		assertEquals(nom, "Esperan");
		nom = db.getNomEnregistrement(55);
		assertNull(nom);
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
		assertNull(db.recupererEnregistrement(25));
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
	public void testListeEnregistrementCategorie() throws SQLException
	{
		List<LigneEnregistrement> listeCat1 = db.getListeEnregistrementCategorie(1);
		List<LigneEnregistrement> listeCat2 = db.getListeEnregistrementCategorie(2);
		assertEquals(listeCat1.size(), 3);
		assertEquals(listeCat2.size(), 0);
		db.modifierEnregistrementCategorie(1, 2);
		listeCat1 = db.getListeEnregistrementCategorie(1);
		listeCat2 = db.getListeEnregistrementCategorie(2);
		assertEquals(listeCat1.size(), 2);
		assertEquals(listeCat2.size(), 1);
	}
	
	@Test
	public void testListeEnregistrementSujet() throws SQLException
	{
		List<LigneEnregistrement> listeSuj1 = db.getListeEnregistrementSujet(1);
		List<LigneEnregistrement> listeSuj2 = db.getListeEnregistrementSujet(2);
		assertEquals(listeSuj1.size(), 3);
		assertEquals(listeSuj2.size(), 0);
		db.modifierEnregistrementSujet(1, 2);
		listeSuj1 = db.getListeEnregistrementSujet(1);
		listeSuj2 = db.getListeEnregistrementSujet(2);
		assertEquals(listeSuj1.size(), 2);
		assertEquals(listeSuj2.size(), 1);
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
	
	@Ignore
	@Test
	public void testCompacter() throws SQLException
	{
		int tailleAvant = -1, tailleApres = -1;
		byte[] newContenu = new byte[10000000];
		File base = new File("LieLabTest.db");
		db.ajouterEnregistrement("Gros", 1, 1, newContenu, 1);
		tailleAvant = (int) base.length();
		db.compacter();
		tailleApres = (int) base.length();
		System.out.println(tailleAvant + " " + tailleApres + " "+ db.getNomEnregistrement(4));
		assertTrue(tailleAvant < tailleApres);
		
	}
	
	@Test
	public void testImporter() throws ClassNotFoundException, SQLException, DBException
	{
		BaseDeDonneesModele bdd = new BaseDeDonneesModele("LieLabTest2.db");
		bdd.connexion();
		bdd.createDatabase();
		bdd.ajouterCategorie("Licorne");
		bdd.ajouterSujet("Jurah");
		db.ajouterEnregistrement("import", 1, 1, "toto".getBytes(), 1);
		
		db.importer(bdd.getListeCategorie(), bdd.getListeSujet(), bdd.getListeEnregistrement(), bdd);
		bdd.deconnexion();
		List<LigneEnregistrement> listeCat = db.getListeCategorie();
		List<LigneEnregistrement> listeSuj = db.getListeSujet();
		List<LigneEnregistrement> listeEnr = db.getListeEnregistrement();
		assertEquals(listeCat.size(), 4);
		assertEquals(listeSuj.size(), 4);
		assertEquals(listeEnr.size(), 4);
		assertEquals(db.getSujet("Jurah"), 4);
		assertEquals(db.getCategorie("Licorne"), 4);
	}
	
	@Test
	public void testCategorieExiste() throws SQLException
	{
		assertTrue(db.categorieExiste(1));
		assertFalse(db.categorieExiste(77));
		assertTrue(db.categorieExiste("Pegase"));
		assertFalse(db.categorieExiste("Inexiste"));
	}
	
	@Test
	public void testSujetExiste() throws SQLException
	{
		assertTrue(db.sujetExiste(1));
		assertFalse(db.sujetExiste(77));
		assertTrue(db.sujetExiste("Gwen"));
		assertFalse(db.sujetExiste("Inexiste"));
	}
	
	@Test
	public void testEnregistrementExiste() throws SQLException
	{
		assertTrue(db.enregistrementExist(1));
		assertFalse(db.enregistrementExist(77));
		assertTrue(db.enregistrementExist("Gracia"));
		assertFalse(db.enregistrementExist("Inexiste"));
	}
	
	@Test
	public void testGetFileName()
	{
		assertEquals(db.getFileName(), "LieLabTest.db");
	}
	
	@AfterClass
	public static void fin() throws SQLException
	{
		db = null;
		File base = new File("LieLabTest.db");
		base.delete();
		base = new File("TestExport1");
		base.delete();
		base = new File("TestExport2");
		base.delete();
		
	}
}
