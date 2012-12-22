package mensonge.core.BaseDeDonnees;

import static org.junit.Assert.assertEquals;
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

public class TestBaseDeDonneesControlleur
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

	private static BaseDeDonneesControlleur db = null;

	@BeforeClass
	public static void init() throws Exception
	{
		File fichier = new File("LieLabTest.db");
		fichier.createNewFile();
		db = new BaseDeDonneesControlleur("LieLabTest.db");
		
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

	@Test(expected=DBException.class)
	public void testAjoutCategorieErreur() throws DBException, SQLException
	{
		db.ajouterCategorie(null);
	}
	@Test(expected=DBException.class)
	public void testAjoutCategorieExistant() throws DBException, SQLException
	{
		db.ajouterCategorie("Poney");
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
	
	@Test(expected=DBException.class)
	public void testModifierCategorieErreur() throws DBException, SQLException
	{
		db.modifierCategorie(1, null);
	}
	
	@Test(expected=DBException.class)
	public void testModifierCategorieExistant() throws DBException, SQLException
	{
		db.modifierCategorie(1, "Poney");
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
	
	@Test(expected=DBException.class)
	public void testSupprimerCategorieErreur() throws DBException, SQLException
	{
		db.supprimerCategorie(25);
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

	@Test(expected=DBException.class)
	public void testAjoutSujetErreur() throws DBException, SQLException
	{
		db.ajouterSujet(null);
	}
	@Test(expected=DBException.class)
	public void testAjoutSujetExistant() throws DBException, SQLException
	{
		db.ajouterSujet("Gwen");
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
	
	@Test(expected=DBException.class)
	public void testModifierSujetErreur() throws DBException, SQLException
	{
		db.modifierSujet(1, null);
	}
	
	@Test(expected=DBException.class)
	public void testModifierSujetExistant() throws DBException, SQLException
	{
		db.modifierSujet(1, "Gwen");
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

	@Test(expected=DBException.class)
	public void testSupprimerSujetErreur() throws DBException, SQLException
	{
		db.supprimerSujet(25);
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

	@Test (expected=DBException.class)
	public void testAjoutHorsCatEnregistrement() throws DBException
	{
		db.ajouterEnregistrement("Exception", 24, 25, "abcdefg".getBytes(), 1);
	}

	@Test (expected=DBException.class)
	public void testAjoutHorsSujEnregistrement() throws DBException
	{
		db.ajouterEnregistrement("Exception", 24, 1, "abcdefg".getBytes(), 33);
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

	@Test(expected=DBException.class)
	public void testAjoutErreurDuree() throws DBException, SQLException
	{		
		db.ajouterEnregistrement("Exception", -1, 1, "toto".getBytes(), 1);
	}
	
	@Test(expected=DBException.class)
	public void testAjoutErreurNom() throws DBException, SQLException
	{		
		db.ajouterEnregistrement(null, 1, 1, "toto".getBytes(), 1);
	}
	
	@Test(expected=DBException.class)
	public void testAjoutErreurContenu() throws DBException, SQLException
	{		
		db.ajouterEnregistrement("Exception", 1, 1, null, 1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurContenu() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Exception", 1, null, 1, 1);
	}
	@Test(expected=DBException.class)
	public void testModificationErreurNom1() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, null, 1, "toto".getBytes(), 1, 1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurNom2() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, null, 1, 1, 1, 1);
	}
	
	
	@Test(expected=DBException.class)
	public void testModificationErreurNom3() throws DBException, SQLException
	{		
		db.modifierEnregistrementNom(1, null);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurNomExistant1() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Gracia", 1, "toto".getBytes(), 1, 1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurNomExistant2() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Gracia", 1, 1, 1, 1);
	}
	
	
	@Test(expected=DBException.class)
	public void testModificationErreurNomExistant3() throws DBException, SQLException
	{		
		db.modifierEnregistrementNom(1, "Gracia");
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurTaille1() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Exception", 1, -1, 1, 1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurTaille2() throws DBException, SQLException
	{		
		db.modifierEnregistrementTaille(1, -1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurDuree1() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Exception", -1, 1, 1, 1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurDuree2() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Exception", -1, "toto".getBytes() , 1, 1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurDuree3() throws DBException, SQLException
	{		
		db.modifierEnregistrementDuree(1, -1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurCategorie1() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Exception", 1, "toto".getBytes() , 25, 1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurCategorie2() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Exception", 1, 1 , 25, 1);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurCategorie3() throws DBException, SQLException
	{		
		db.modifierEnregistrementCategorie(1, 25);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurCategorie4() throws DBException, SQLException
	{		
		db.modifierEnregistrementCategorie(1, "CatInexistante");
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurSujet1() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Exception", 1, "toto".getBytes() , 1, 25);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurSujet2() throws DBException, SQLException
	{		
		db.modifierEnregistrement(1, "Exception", 1, 1 , 1, 25);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurSujet3() throws DBException, SQLException
	{		
		db.modifierEnregistrementSujet(1, 25);
	}
	
	@Test(expected=DBException.class)
	public void testModificationErreurSujet4() throws DBException, SQLException
	{		
		db.modifierEnregistrementSujet(1, "SujInexistante");
	}
	
	@Test 
	public void testGetNomEnregistrement() throws DBException
	{
		String nom = db.getNomEnregistrement(1);
		assertEquals(nom, "Esperan");
	}
	
	@Test (expected=DBException.class)
	public void testGetNomEnregistrementErreur() throws DBException
	{
		db.getNomEnregistrement(55);
	}
	
	@Test
	public void testGetNombreEnregistrement() throws DBException
	{
		int nb = db.getNombreEnregistrement();
		assertEquals(nb, 3);
	}
	
	@Test
	public void testSuprimmerEnregistrement() throws DBException, DBException
	{
		db.supprimerEnregistrement(1);
		List<LigneEnregistrement> liste = db.getListeEnregistrement();
		for (LigneEnregistrement ligne : liste)
		{
			assertTrue(ligne.getId() != 1);

		}
		assertTrue(liste.size() == 2);
	}

	@Test(expected=DBException.class)
	public void testSuprimmerEnregistrementErreur() throws DBException, DBException
	{
		db.supprimerEnregistrement(77);
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

	@Test (expected=DBException.class)
	public void testRecupererEnregistrementInexistant() throws DBException, SQLException
	{
		db.recupererEnregistrement(25);
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
	public void testListeEnregistrementCategorie() throws DBException
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
	public void testListeEnregistrementSujet() throws DBException
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
		byte[] contenu_base = readFile("LieLabTest.db");
		assertEquals(contenu_fichier.length, contenu_base.length);
		String sortie = sha1(contenu_fichier), entree = sha1(contenu_base);
		assertEquals(sortie, entree);
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
		
		db.importer("LieLabTest2.db");
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
		base = new File("LieLabTest2.db");
		base.delete();
		
	}
}
