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



public class TestBase
{

	public static void main(String[] args)
	{
		TestBase.checkRunning();
	}

	/**
	 * Fonction testant le temps d'execution en ecriture passant par l'objet BaseDeDonnees de facons a pouvoir optimiser.
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
			
			
			
			
			//****CATEGORIE****
			//AJOUT
			System.out.println("[i] ajout des categories Poney/Flamment/Pegase");
				try
			{
				db.ajouterCategorie("Poney");
				db.ajouterCategorie("Flamment");
				db.ajouterCategorie("Pegase");
			} catch (DBException e1)
			{
				System.out.println(e1.getMessage());
			}
			
			//MODIFICATION
			System.out.println("[i] modification de Flamment en Licorne");
			try
			{
				db.modifierCategorie(2, "Licorne");
			} catch (DBException e1)
			{
				System.out.println(e1.getMessage());
			}
			
			//AFFICHAGE
			ResultSet l = null;
			try
			{
				l = db.getListeCategorie();
				System.out.println("[i] Affichage.");
				while(l.next())
				{
					System.out.println(l.getString(1));
				}
				l.close();
			}
			catch(SQLException e)
			{
				System.out.println("[-] " + e.getMessage());
			} 
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			//SUPRESSION
			System.out.println("[i] Suppression de la categorie 1.");
			try
			{
				db.supprimerCategorie(1);
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			//AFFICHAGE
			try
			{
				l = db.getListeCategorie();
				System.out.println("[i] Affichage.");
				while(l.next())
				{
					System.out.println(l.getString(1));
				}
				l.close();
			}
			catch(SQLException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			catch(DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			try
			{
				System.out.println("[i] Le nom de la categorie 2 est " + db.getCategorie(2));
				System.out.println("[i] L'id de la categorie \"Pegase\" est " + db.getCategorie("Pegase"));
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			//****ENREGISTREMENT****
			//AJOUT
			System.out.println("[i] ajout de 7 enregistrements.");
			try
			{
				
				db.ajouterEnregistrement("Tornado", 24, 2, "abcdefg".getBytes());
				db.ajouterEnregistrement("Esperan", 23, 2, "love".getBytes());
				db.ajouterEnregistrement("Gracia", 22, 3, "mort".getBytes());
				db.ajouterEnregistrement("Chuck", 21, 3, "naissance".getBytes());
				db.ajouterEnregistrement("Tarzan", 20, 2, "vivre".getBytes());
				db.ajouterEnregistrement("Jane", 19, 2, "???".getBytes());
				db.ajouterEnregistrement("Jilano", 18, 3, "erreur".getBytes());
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
				e.printStackTrace();
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
			}
			
			//AFFICHAGE
			ResultSet rs = null;
			try
			{
				rs = db.getListeEnregistrement();
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			
			System.out.println("[i] Affichage.");
			try
			{
				System.out.println("NOM\t\tNOM CAT\t\tDUREE\t\tTAILLE");
				while(rs != null && rs.next())
				{
					System.out.println(rs.getString(3) + "\t\t" + rs.getString(4) + "\t\t" + rs.getString(1) + "\t\t" + rs.getString(2));
				}
			}
			catch(Exception e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			//MODIFICATION
			System.out.println("[i] Modification des 5 premiers enregistrements. (tout/tous/Categorie/Nom/Taille)");
			try
			{
				db.modifierEnregistrement(1, "Zeus", 15, 55, 3);
				db.modifierEnregistrement(2, "Taylor", 17, 77, 2);
				db.modifierEnregistrementCategorie(3, "Licorne");
				db.modifierEnregistrementNom(4, "Norris");
				db.modifierEnregistrementTaille(5, 250);
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			//AFFICHAGE
			rs = null;
			try
			{
				rs = db.getListeEnregistrement(2);
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
					
					
			System.out.println("[i] Affichage categorie 2.");
			try
			{
				System.out.println("NOM\t\tNOM CAT\t\tDUREE\t\tTAILLE");
				while(rs != null && rs.next())
				{
					System.out.println(rs.getString(3) + "\t\t" + rs.getString(4) + "\t\t" + rs.getString(1) + "\t\t" + rs.getString(2));
				}
			}
			catch(Exception e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			//AFFICHAGE
			rs = null;
			try
			{
				rs = db.getListeEnregistrement(3);
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
					
					
			System.out.println("[i] Affichage categorie 3.");
			try
			{
				System.out.println("NOM\t\tNOM CAT\t\tDUREE\t\tTAILLE");
				while(rs != null && rs.next())
				{
					System.out.println(rs.getString(3) + "\t\t" + rs.getString(4) + "\t\t" + rs.getString(1) + "\t\t" + rs.getString(2));
				}
			}
			catch(Exception e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			//SUPPRESSION
			System.out.println("[i] Suppression de l'enregistrement 5.");
			db.supprimerEnregistrement(5);
			
			try
			{
				System.out.println("[i] Il y a " + db.getNombreEnregistrement() + " d'enregistrement.");
			}
			catch(Exception e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			
			//RECUPERATION
			byte[] tab = null;
			try
			{
				tab = db.recupererEnregistrement(2);
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			if(tab == null)
			{
				tab = "null".getBytes();
			}
			System.out.print("[i] Enregistrement 2: ");
			for(int i = 0; i < tab.length; i++)
			{
				System.out.print((char)tab[i]);
			}
			try
			{
				tab = db.recupererEnregistrement(4);
			}
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
			if(tab == null)
			{
				tab = "null".getBytes();
			}
			System.out.print("\n[i] Enregistrement 4: ");
			for(int i = 0; i < tab.length; i++)
			{
				System.out.print((char)tab[i]);
			}
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
				l = null;
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
}
