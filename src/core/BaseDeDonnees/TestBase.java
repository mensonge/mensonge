package core.BaseDeDonnees;

import core.BaseDeDonnees.DBException;
import core.BaseDeDonnees.BaseDeDonnees;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Formatter;

import org.junit.*;
import static org.junit.Assert.*;


public class TestBase
{

	public static void main(String[] args)
	{
		TestBase.checkRunning();
	}

	/**
	 * Fonction testant le temps d'execution en ecriture passant par l'objet BaseDeDonnees de facon à pouvoir optimiser.
	 */
	public static void checkOptiWrite()//780Mo en 105 seconde sur mon PC
	{
		long max = 100; //le nombre d'operation a repeter
		long InitTime = System.currentTimeMillis(), endTime;//on declare les variable du temps et on initialise le depart
		BaseDeDonnees db = null;
		try
		{
			db = new BaseDeDonnees("LieLabTest.db");//on creer l'objet BaseDeDonnee sur un fichier special
			db.connexion();//connexion
			db.createDatabase();//creation de la base et effacement d'evantuel table existante
			db.ajouterCategorie("Poney des bois.");//ajout de categorie
		}
		catch(DBException e)
		{
			int a = e.getCode();//on recupere le code de l'exception
			if(a == 2)//Si c'est une erreur de structure de base on creer la base
			{
				System.out.println("[i]Base en cour de creation ...");
				try
				{
					db.createDatabase();
					db.ajouterCategorie("Poney des bois.");//ajout de categorie
				} 
				catch (DBException e1)
				{
					System.out.println("[-] Erreur lors de la creation: " + e1.getMessage());
				}
				//creation de la base
				System.out.println("[i]Base cree.");
			}
			else//Sinon on affiche l'erreur et on arrete
			{
				System.out.println("[-]Erreur lors de la connexion. " + e.getMessage());
				return;
			}
		}
		
		for(long i = 0; i < max; i++)//boucle sur l'ajout d'un enregistrement
		{
			try
			{
				db.ajouterEnregistrement("Statl3r est un demi-elf nain quadri classe", 77, 1, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz".getBytes());
			}
			catch (DBException e)
			{
				System.out.println("[-] Erreur lors de l'ajout " + i + ": " + e.getMessage() );
			}
		}
		
		try
		{
			db.deconnexion();//deconnexion a la base
		}
		catch (DBException e)
		{
			System.out.println("[-] Erreur lors de la deconnexion: " + e.getMessage() );
		}
		endTime = System.currentTimeMillis();//recuperation du temps puis affichage d'information
		System.out.println("[Opti Write] Le temps ecoule depuis le debut de la fonction est de " + (endTime - InitTime) + " ms.");
		System.out.println("[Opti Write] Ajout de " + max + " enregistrement de chacun 78 bytes soit un total de " + 78*max + " bytes ajoute.");
		System.out.println("[Opti Write] Temps/byte: " + (endTime - InitTime)/(78*max) + " ms\tTemps/enregistrement: " + (endTime - InitTime)/max + " ms");
	}
	/**
	 * Fonction testant le temps d'execution en lecture passant par l'objet BaseDeDonnees de facons a pouvoir optimiser.
	 */
	public static void checkOptiRead()//780Mo en 24 seconde sur mon PC
	{
		long max = 100;//le nombre d'operation a repeter
		long InitTime = System.currentTimeMillis(), endTime;//on declare les variable du temps et on initialise le depart
		BaseDeDonnees db = null;
		try
		{
			db = new BaseDeDonnees("LieLabTest.db");//on creer l'objet BaseDeDonnee sur un fichier special
			db.connexion();//connexion
			db.createDatabase();//creation de la base et effacement d'evantuel table existante
			db.ajouterCategorie("Poney des bois.");//ajout de categorie
		}
		catch(DBException e)
		{
			int a = e.getCode();//on recupere le code de l'exception
			if(a == 2)//Si c'est une erreur de structure de base on creer la base
			{
				System.out.println("[i]Base en cour de creation ...");
				try
				{
					db.createDatabase();
					db.ajouterCategorie("Poney des bois.");//ajout de categorie
				} 
				catch (DBException e1)
				{
					System.out.println("[-] Erreur lors de la creation: " + e1.getMessage());
				}
				//creation de la base
				System.out.println("[i]Base cree.");
			}
			else//Sinon on affiche l'erreur et on arrete
			{
				System.out.println("[-]Erreur lors de la connexion. " + e.getMessage());
				return;
			}
		}
		try
		{
			//On ajoute un enregistrement
			db.ajouterEnregistrement("Statl3r est un demi-elf nain quadri classe", 77, 1, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz".getBytes());
		}
		catch (DBException e)
		{
			System.out.println("[-] Erreur lors de l'ajout : " + e.getMessage() );
			return;
		}
		
		for(long i = 0; i < max; i++)//boucle en lecture
		{
			try
			{
				ResultSet rs = db.getListeEnregistrement();
				byte[] tab = db.recupererEnregistrement(rs.getInt(5));
				if(tab != null)
				{
					
				}
			}
			catch (DBException e)
			{
				System.out.println("[-] Erreur lors de la lecture numero " + i + ": " + e.getMessage() );
			}
			catch(Exception e)
			{
				System.out.println("[-] Erreur lors de la lecture numero " + i + ": " + e.getMessage() );
			}
		}
		
		try
		{
			db.deconnexion();//deconexion
		}
		catch (DBException e)
		{
			System.out.println("[-] Erreur lors de la deconnexion: " + e.getMessage() );
		}
		endTime = System.currentTimeMillis();//recuperation du temps puis affichage des information
		System.out.println("[Opti Read]La fonction a inseree un enregistrement de " + 78 + " bytes et l'a lu " + max + " fois.");
		System.out.println("[Opti Read]Le temps ecoule depuis le debut de la fonction est de " + (endTime - InitTime) + " ms.");
		System.out.println("[Opti Read]Le temps par enregistrement :" + (endTime - InitTime)/max + " ms.");
	}
	/**
	 * Fonction permettant de verifier le bon fonctionnement de l'objet BaseDeDonnees en fournissant un echantillons de test.
	 */
	public static void checkRunning()
	{
		//BaseDeDonnees db;
		
				/*
			Creer l'objet avec le nom
			On tente une connexion
			S'il y a une erreur, on lance la creation de la base
			
			*****Manipulation des categorie*****
			*Ajouter 3 categorie
			*modifier la seconde categorie
			*Lister les categories
			*supprimer la premiere
			*Lister les categorie
				
			****Manipulation des enregistrements******
			*Ajouter 7 enregistrements (meme fichier mais nom different (+simple)
			*recuperer la liste de tous les enregistrements
			*modifier l'enregistrement 3 et 7
			*afficher les enregistrements la categorie 1 et 2
			*supprimer l'enregistrement 5
			*afficher le nombre d'enregistrement
			*recuperer l'enregistrement 2
			*recuperer l'enregistrement 4
			
			
			****Manipuler import/export****
			*exporter la base (facultatif dans l'immediat)
			*importer un fichier db 
			*Afficher la liste des categories et des enregistrements
			
			*Deconnexion
			*/
			
			BaseDeDonnees db = null;
			try
			{
				db = new BaseDeDonnees("LieLabTest.db");
				db.connexion();//connexion
				db.createDatabase();
			}
			catch(DBException e)
			{
				int a = e.getCode();
				if(a == 2)
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
					//creation de la base
					System.out.println("[i]Base cree.");
				}
				else
				{
					System.out.println("[-]Erreur lors de la connexion. " + e.getMessage());
					return;
				}
			}
			
			ResultSet rs;
		
			//****IMPORTE****
			
			if (TestBase.createFictiveDataBase())//On creer une seconde base de donnees fictive, si on reussi a la creer, on l'importe
			{
				//Importation
				try
				{
					System.out.println("\n[i] Importation");
					db.importer("LieLabTest2.db");
				} catch (DBException e)
				{
					System.out.println("[-] " + e.getMessage());
				}
				//AFFICHAGE ENREGISTREMENT
				rs = null;
				try
				{
					rs = db.getListeEnregistrement();
				} catch (DBException e)
				{
					System.out.println("[-] " + e.getMessage());
				}
				System.out.println("[i] Affichage.");
				try
				{
					System.out.println("NOM\t\tNOM CAT\t\tDUREE\t\tTAILLE");
					while (rs != null && rs.next())
					{
						System.out.println(rs.getString(3) + "\t\t"
								+ rs.getString(4) + "\t\t" + rs.getString(1)
								+ "\t\t" + rs.getString(2));
					}
				} catch (Exception e)
				{
					System.out.println("[-] " + e.getMessage());
				}
				//AFFICHAGE CATEGORIE
				ResultSet l = null;
				try
				{
					l = db.getListeCategorie();
					System.out.println("[i] Affichage categorie.");
					while (l.next())
					{
						System.out.println(l.getString(1));
					}
				} catch (SQLException e)
				{
					System.out.println("[-] " + e.getMessage());
				} catch (DBException e)
				{
					System.out.println("[-] " + e.getMessage());
				}
			}
			//****Exportation****
			MessageDigest md = null;
			try
			{
				md = MessageDigest.getInstance("SHA");
				
			} catch (NoSuchAlgorithmException e2)
			{
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try
			{
				System.out.println("[i] Exportation Base vers export.db");
				db.exporter("export.db", 0, 1);
				
				if( ! sha1(readFile("export.db")).equals(sha1(readFile(db.getFileName()))))
				{
					System.out.println("[-] Les deux fichiers exportés sont differents");
				}
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			catch(Exception e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			try
			{
				System.out.println("[i] Exportation de l'enregistrement 1");
				if( ! sha1(db.recupererEnregistrement(1)).equals(sha1(readFile("export.txt"))))
				{
					System.out.println("[-] L'exportation de l'enregistrement à échoué.");
				}
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			catch(Exception e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			File f = new File("test.wav");
			if(f.exists())
			{
				try
				{
					FileInputStream sourceFile = new java.io.FileInputStream(f);
					byte[] contenu = new byte[(int) f.length()];
					sourceFile.read(contenu);
					sourceFile.close();
					System.out.println((int) f.length());
					System.out.println(contenu.length);
					db.ajouterEnregistrement("Fichierwave", 5, 2, contenu);
					System.out.println("[+] Ajout du fichier test.wav");
				}
				catch (Exception e)
				{
					System.out.println("[-] TEST.WAV (pas important): " + e.getMessage());
				}
			}
			//****Deconnexion****
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
	 * Fonction permettant de creer une base de donnees pour les test de checkRunning.
	 * Elle dispose de 2 categorie Licorne et Dieux avec un enregistrement dans chacune.
	 * @return true si la base s'est bien creer.
	 * @see checkRunning
	 */
	public static boolean createFictiveDataBase()
	{
		BaseDeDonnees db = null;
 		try
		{
			db = new BaseDeDonnees("LieLabTest2.db");
			db.connexion();//connexion
			db.createDatabase();
		}
		catch(DBException e)
		{
			int a = e.getCode();
			if(a == 2)
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
				//creation de la base
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
 			
 			db.ajouterEnregistrement("Hades* ", 7, 1, "azerty".getBytes());
 			db.ajouterEnregistrement("Bella*", 7, 2, "qsdfgh".getBytes());
 			
 			db.deconnexion();
 		}
 		catch(Exception e )
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
			contenu = new byte[(int)fichier.length()];
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

	public static String sha1 ( byte [ ] convertme ) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance( "SHA-1" ) ;
		return byteArray2Hex (md.digest(convertme)) ;
 }

	public static String byteArray2Hex ( byte [ ] hash )
	{
		Formatter formatter = new Formatter ( ) ;
		for( byte b : hash )
		{
			formatter.format ( "%02x" , b) ;
		}
		return formatter.toString ( ) ;
	}

	private static BaseDeDonnees db = null;
	
	@BeforeClass
	public static void init() throws Exception
	{
		try
		{
			db = new BaseDeDonnees("LieLabTest.db");//on creer l'objet BaseDeDonnee sur un fichier special
			db.connexion();//connexion
			db.createDatabase();//creation de la base et effacement d'evantuel table existante
		}
		catch(DBException e)
		{
			int a = e.getCode();//on recupere le code de l'exception
			if(a == 2)//Si c'est une erreur de structure de base on creer la base
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
			else//Sinon on affiche l'erreur et on arrete
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
		
		ResultSet rs = db.getListeCategorie();
		while(rs.next())
		{
			i++;
		}
		rs.close();
		assertTrue(i == 3);
	}
	
	@Test
	public void testRenommerCategorie() throws DBException, SQLException
	{
		int i = 0;
		String nom = null;
		db.modifierCategorie(2, "Licorne");
		
		
		ResultSet rs = db.getListeCategorie();
		while(rs.next())
		{
			i++;
			if(i == 2)
			{
				nom = rs.getString(1);
			}
		}
		rs.close();
		assertTrue(i == 3 && nom.equals("Licorne"));
	}
	
	@Test
	public void testAfficherCategorie() throws DBException, SQLException, NoSuchAlgorithmException
	{
		int i = 0;
		String nom = new String();
		ResultSet rs = db.getListeCategorie();
		while(rs.next())
		{
			i++;
			nom += rs.getString(1);
		}
		rs.close();
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
		
		ResultSet rs = db.getListeCategorie();
		while(rs.next())
		{
			i++;
		}
		rs.close();
		assertTrue(i == 2);
	}
	
	@Test
	public void testAjoutEnregistrement() throws DBException, SQLException
	{
		db.ajouterEnregistrement("Tornado", 24, 2, "abcdefg".getBytes());
		db.ajouterEnregistrement("Esperan", 23, 2, "love".getBytes());
		db.ajouterEnregistrement("Gracia", 22, 3, "mort".getBytes());
		db.ajouterEnregistrement("Chuck", 21, 3, "naissance".getBytes());
		db.ajouterEnregistrement("Tarzan", 20, 2, "vivre".getBytes());
		db.ajouterEnregistrement("Jane", 19, 2, "???".getBytes());
		db.ajouterEnregistrement("Jilano", 18, 3, "erreur".getBytes());
		int i = 0;
		ResultSet rs = db.getListeEnregistrement();
		while(rs.next())
		{
			i++;
		}
		rs.close();
		assertTrue(i == 7);
	}
	
	
	@Test( expected = DBException.class )
	public void testAjoutHorsCatEnregistrement() throws DBException, SQLException
	{
		db.ajouterEnregistrement("Exception", 24, 5, "abcdefg".getBytes());
		assertTrue(false);
	}
	
	
	@Test
	public void testModifEnregistrement() throws DBException, SQLException
	{
		db.modifierEnregistrement(1, "Zeus", 15, 55, 3);
		db.modifierEnregistrement(2, "Taylor", 17, 77, 2);
		db.modifierEnregistrementCategorie(3, "Licorne");
		db.modifierEnregistrementNom(4, "Norris");
		db.modifierEnregistrementTaille(5, 250);
		int i = 0;
		int taille = 0;
		String nom1 = null, nom2 = null, categorie3 = null, nom4 = null;
		ResultSet rs = db.getListeEnregistrement();
		while(rs.next())
		{
			i++;
			switch(rs.getInt("id"))
			{
			case 1:
				nom1 = rs.getString("nom");
				break;
			case 2:
				nom2 = rs.getString("nom");
				break;
			case 3:
				categorie3 = rs.getString("nomcat");
				break;
			case 4:
				nom4 = rs.getString("nom");
				break;
			case 5:
				taille = rs.getInt("taille");
				break;
			}
		}
		rs.close();
		assertTrue(i == 7 && taille == 250 && nom1.equals("Zeus") && nom2.equals("Taylor") && categorie3.equals("Licorne") && nom4.equals("Norris"));
	}
	
	
	@Test
	public void testSuprimmerEnregistrement() throws DBException, SQLException
	{
		db.supprimerEnregistrement(5);
		int i = 0;
		boolean tmp = true;
		ResultSet rs = db.getListeEnregistrement();
		while(rs.next())
		{
			i++;
			if(rs.getInt("id") == 5)
			{
				tmp = false;
			}
			
		}
		rs.close();
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
		assertTrue(enr1.equals("naissance") && enr2.equals("???"));
	}
	
	@Test( expected = DBException.class )
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
		
		ResultSet rs = db.getListeEnregistrement();
		while(rs.next())
		{
			i++;
		}
		rs.close();
		assertTrue(i == 6 && nb == 6);
	}
	
	@AfterClass
	public static void fin() throws DBException
	{
		db.deconnexion();
		db = null;
	}
}
