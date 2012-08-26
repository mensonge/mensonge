package BaseDeDonnee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class BaseDeDonnees
{
	Connection connexion = null;
	String fileName = null;
	
	/**
	 * Constructeur de base.
	 * @param baseDeDonnees Chaine de caractére indiquant le nom du fichier de la base de donnee.
	 */
	public BaseDeDonnees(String baseDeDonnees)
	{
		fileName = baseDeDonnees;
	}
	/**
	 * Fonction permettant de se connecter au fichier de la base de donnee fourni au constructeur.
	 * De plus, cette fonction verifie la structure de la base.
	 * @throws DBException Envoie des exceptions dans le cas d'une erreur de connexion ou d'une mauvaise structure.
	 */
	public void connexion() throws DBException
	{
		try
		{
			//Lance la connection
			Class.forName("org.sqlite.JDBC");
			connexion = DriverManager.getConnection("jdbc:sqlite:" + fileName);
			connexion.setAutoCommit(false);//desactive l'autocommit ce qui est plus securisant et augmente la vitesse
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de l'initialisation de la connexion: " + e.getMessage(), 1);
		}
		try//test la structure de la base
		{
			Statement stat = connexion.createStatement();//creation du Statement
			stat.executeQuery("SELECT id, enregistrement, duree, taille, nom, idcat FROM enregistrements;");//test de la structure
			stat.executeQuery("SELECT idcat, nomcat FROM categorie;");//fermeture du Statement
			stat.close();
		}
		catch(SQLException e)
		{
			throw new DBException("Probleme dans la structure de la base : " + e.getMessage(), 2);
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la verification de la structure de la base : " + e.getMessage(), 3);
		}
	}
	/**
	 * Deconnecte de la base
	 * @throws DBException En cas d'erreur de deconnexion, l'etat de la connexion devient alors inconnu
	 */
	public void deconnexion() throws DBException
	{
		try
		{
			connexion.commit();//On commit les dernier changement au cas ou ... (Ce n'est  pas une action repettitive donc on peut commiter en plus)
			connexion.close();//On close la connexion
			connexion = null;//On remet à null pour des test future
		}
		catch(Exception e)
		{
			throw new DBException("Probleme lors de la deconnexion de la base : " + e.getMessage(), 4);
		}
	}
	public void importer(String cheminFichier) //TODO
	{
		if(connexion == null)
		{
			return;
		}
		//tester presence du fichier
		//etablir une connexion
		//regarder les catégories qui change et ajouter d'eventuelle nouvelle
		//ajouter les enregistrement avec leurs catégorie (modifiée) (ceux qu'il n'existe pas)
	}
	public void exporter(String cheminFichier) //TODO
	{
		if(connexion == null)
		{
			return;
		}
	}
	/**
	 * Permet de recuperer toutes les information de tout les enregistrements avec les colonne suivante: duree, taille, nom, nomcat, id
	 * @return Le resultat sous forme d'objet ResultSet qui n'est parcourable qu'une fois.
	 * @throws DBException
	 */
	public ResultSet getListeEnregistrement() throws DBException
	{
		if(connexion == null)
		{
			return null;
		}
		try
		{
			Statement stat = connexion.createStatement(); //Creation du Statement
			ResultSet rs = stat.executeQuery("SELECT duree, taille, nom, nomcat, id FROM enregistrements JOIN categorie USING (idcat);"); //Execution de la requete
			return rs;
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de la liste des enregistrement: " + e.getMessage(), 1);
		}
	}
	/**
	 * Permet de recuperer toutes les informations de tout les enregistrements d'une categorie avec les colonnes suivante: duree, taille, nom, nomcat, id
	 * @return Le resultat sous forme d'objet ResultSet qui n'est parcourable qu'une fois.
	 * @throws DBException
	 */
	public ResultSet getListeEnregistrement(int idCat) throws DBException
	{
		if(connexion == null)
		{
			return null;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT duree, taille, nom, nomcat, id FROM enregistrements JOIN categorie USING (idcat) WHERE idcat=?");//Preparation de la requete
			ps.setInt(1, idCat);//on rempli les trous
			ResultSet rs = ps.executeQuery();//On execute
			return rs;
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de la liste des enregistrement: " + e.getMessage(), 1);
		}
	}
	/**
	 * Permet de recuperer le nombre d'enregistrement
	 * @return le nombre d'enregistrement
	 * @throws DBException
	 */
	public int getNombreEnregistrement() throws DBException
	{
		if(connexion == null)
		{
			return -1;
		}
		try
		{
			Statement stat = connexion.createStatement();//Creer le Statement
			ResultSet rs = stat.executeQuery("SELECT count(1) FROM enregistrements;");//On execute la recherche
			int retour = rs.getInt(1);//On recupere le resultat
			rs.close();//On ferme les different objet
			stat.close();
			return retour;
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation du nombre d'enregistrement: " + e.getMessage(), 1);
		}
	}
	/**
	 * Permet d'ajouter un enregistrement à la base
	 * @param nom le nom sous lequel il apparaitra
	 * @param duree la duree de cette enregistrement
	 * @param idCat la categorie a laquelle il appartient
	 * @param enregistrement l'enregistrement sous la forme d'un tableau de byte
	 * @throws DBException
	 */
	public void ajouterEnregistrement(String nom, int duree, int idCat, byte[] enregistrement) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		if( ! this.categorieExiste(idCat))//On verifie si la categorie existe
		{
			throw new DBException("Categorie inexistante.", 3);
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"INSERT INTO enregistrements(enregistrement, duree, taille, nom, idCat) VALUES (?, ?, ?, ?, ?);");//Peparation de la requete
			ps.setBytes(1, enregistrement);
			ps.setInt(2, duree);
			ps.setInt(3, enregistrement.length);
			ps.setString(4, nom);
			ps.setInt(5, idCat);
		
			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de l'ajout de l'enregistrement : " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet de supprimer un enregistrement
	 * @param id id de l'enregistrement a supprimer.
	 */
	public void supprimerEnregistrement(int id)
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("DELETE FROM enregistrements WHERE id=?");//preparation de la requete
			ps.setInt(1,  id);//On rempli les trou
			if(ps.executeUpdate() > 0)//On execute la requete et on test la reussite de cette dernier
			{
				connexion.commit();//On valide le changement
			}
			ps.close();//On ferme les ressources
		}
		catch(Exception e)
		{}
	}
	/**
	 * Permet de modifier un enregistrement dans son ensemble
	 * @param id le numero identifiant l'enregistrement
	 * @param nom le nouveau nom
	 * @param duree la nouvelle duree
	 * @param enregistrement le nouvelle enregistrement
	 * @param idCat la nouvelle categorie
	 * @throws DBException
	 */
	public void modifierEnregistrement(int id, String nom, int duree, byte[] enregistrement, int idCat) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		if( ! this.categorieExiste(idCat))//On test si la categorie est existante
		{
			throw new DBException("Categorie inexistante.", 3);
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET enregistrement=?, nom=?, duree=?, taille=?, idCat=? WHERE id=?;");//On prepare
			ps.setBytes(1, enregistrement);//On rempli
			ps.setString(2, nom);
			ps.setInt(3, duree);
			ps.setInt(4, enregistrement.length);
			ps.setInt(5, idCat);
			ps.setInt(6, id);

			if(ps.executeUpdate() > 0)//On execute et on test la reussite
			{
				connexion.commit();//On valide les changement
			}
			ps.close();//On ferme les ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de l'enregistrement : " + e.getMessage(), 3);
		}
	}
	public void modifierEnregistrement(int id, String nom, int duree, int taille, int idCat) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		if( ! this.categorieExiste(idCat))
		{
			throw new DBException("Categorie inexistante.", 3);
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET nom=?, duree=?, taille=?, idCat=? WHERE id=?;");
			ps.setString(1, nom);
			ps.setInt(2, duree);
			ps.setInt(3, taille);
			ps.setInt(4, idCat);
			ps.setInt(5, id);

			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de l'enregistrement : " + e.getMessage(), 3);
		}
	}
	public void modifierEnregistrementNom(int id, String nom) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET nom=? WHERE id=?;");
			ps.setString(1, nom);
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification du nom: " + e.getMessage(), 3);
		}
	}
	public void modifierEnregistrementDuree(int id, int duree) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET duree=? WHERE id=?;");
			ps.setInt(1, duree);
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de la duree: " + e.getMessage(), 3);
		}
	}
	public void modifierEnregistrementTaille(int id, int taille) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET taille=? WHERE id=?;");
			ps.setInt(1, taille);
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de la taille: " + e.getMessage(), 3);
		}
	}
	public void modifierEnregistrementCategorie(int id, int idCat) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		if( ! this.categorieExiste(idCat))
		{
			throw new DBException("Categorie inexistante.", 3);
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET idCat=? WHERE id=?;");
			ps.setInt(1, idCat);
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de la categorie: " + e.getMessage(), 3);
		}
	}
	public void modifierEnregistrementCategorie(int id, String nomCat) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET idCat=(SELECT idcat FROM categorie WHERE nomCat=?) WHERE id=?;");
			ps.setString(1, nomCat);
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de la categorie: " + e.getMessage(), 3);
		}
	}

	public byte[] recupererEnregistrement(int id) throws DBException
	{
		if(connexion == null)
		{
			return null;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT enregistrement FROM enregistrements WHERE id=?");
			ps.setString(1, Integer.toString(id));
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				return rs.getBytes("enregistrement");
			}
			throw new Exception("Enregistrement inexistant.");
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de l'enregistrement : " + e.getMessage(), 3);
		}
	}
	public void ajouterCategorie(String nom) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("INSERT INTO categorie (nomcat) VALUES (?)");
			ps.setString(1, nom);
			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de l'ajout de la categorie : " + e.getMessage(), 3);
		}
		
	}
	public ResultSet getListeCategorie() throws DBException
	{
		if(connexion == null)
		{
			return null;
		}
		try
		{
			Statement stat = connexion.createStatement();
			ResultSet rs = stat.executeQuery("SELECT nomCat, idcat FROM categorie;");
			//stat.close();
			return rs;
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation des categories: " + e.getMessage(), 3);
		}
	}
	public void supprimerCategorie(int id) throws DBException//comment on fait pour les enregistrements de cette caté ?
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("DELETE FROM categorie WHERE idCat=?");
			ps.setInt(1, id);
			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la suppression de la categorie: " + e.getMessage(), 3);
		}
		
	}
	public void modifierCategorie(int id, String nom) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
		PreparedStatement ps = connexion.prepareStatement("UPDATE categorie SET nomCat=? WHERE idCat=?");
		ps.setString(1, nom);
		ps.setInt(2, id);
		if(ps.executeUpdate() > 0)
		{
			connexion.commit();
		}
		ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de la categorie: " + e.getMessage(), 3);
		}
	}
	public String getCategorie(int idCat) throws DBException
	{
		if(connexion == null)
		{
			return null;
		}
		try
		{

			PreparedStatement ps = connexion.prepareStatement("SELECT nomcat FROM categorie WHERE idcat=?;");
			ps.setInt(1, idCat);
			ResultSet rs = ps.executeQuery();
			return rs.getString(1);
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de la categories: " + e.getMessage(), 3);
		}
	}
	public int getCategorie(String nomCat) throws DBException
	{
		if(connexion == null)
		{
			return -1;
		}
		try
		{

			PreparedStatement ps = connexion.prepareStatement("SELECT idcat FROM categorie WHERE nomcat=?;");
			ps.setString(1, nomCat);
			ResultSet rs = ps.executeQuery();
			return rs.getInt(1);
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de la categories: " + e.getMessage(), 3);
		}
	}	
	public void createDatabase() throws DBException
	{
		try
		{
			Statement stat = connexion.createStatement();
			stat.executeUpdate("DROP TABLE if exists enregistrements;");
			stat.executeUpdate("DROP TABLE if exists categorie;");
			if(stat.executeUpdate("CREATE TABLE categorie (idcat  INTEGER PRIMARY KEY AUTOINCREMENT, nomcat VARCHAR2(128));") != 0)
			{
				throw new Exception("Erreur de creation de la table categorie.");
			}
			if(stat.executeUpdate("CREATE TABLE enregistrements (id  INTEGER PRIMARY KEY AUTOINCREMENT, enregistrement BLOB, duree INTEGER, taille INTEGER, nom VARCHAR2(128), idcat INTEGER);") != 0)//FIXME ajouter la référence pour le champ idcat
			{
				throw new Exception("Erreur de creation de la table enregistrement.");
			}
			connexion.commit();
			stat.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la creation de la base : " + e.getMessage(), 3);
		}
	}

	private boolean categorieExiste(int idCat) throws DBException
	{
		if(connexion == null)
		{
			return false;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT 1 FROM categorie WHERE idcat=?");
			ps.setInt(1, idCat);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next())
			{
				rs.close();
				ps.close();
				return true;
			}
			rs.close();
			ps.close();
			return false;
		}
		catch(Exception e)
		{
			throw new DBException("Probleme lors de la verification de l'existance de la categorie: " + e.getMessage(), 1);
		}
	}

	public static void checkOptiWrite()
	{
		long max = 100;
		long InitTime = System.currentTimeMillis(), endTime;
		BaseDeDonnees db = new BaseDeDonnees("LieLabTestOpti.db");
		try
		{
			
			db.connexion();//connexion
			db.createDatabase();
			db.ajouterCategorie("Poney des bois.");
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
					db.ajouterCategorie("Poney des bois.");
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
		
		for(long i = 0; i < max; i++)
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
			db.deconnexion();
		}
		catch (DBException e)
		{
			System.out.println("[-] Erreur lors de la deconnexion: " + e.getMessage() );
		}
		endTime = System.currentTimeMillis();
		System.out.println("[Opti Write] Le temps ecoule depuis le debut de la fonction est de " + (endTime - InitTime) + " ms.");
		System.out.println("[Opti Write] Ajout de " + max + " enregistrement de chacun 78 bytes soit un total de " + 78*max + " bytes ajoute.");
		System.out.println("[Opti Write] Temps/byte: " + (endTime - InitTime)/(78*max) + " ms\tTemps/enregistrement: " + (endTime - InitTime)/max + " ms");
	}
	public static void checkOptiRead()
	{
		long max = 100;
		long InitTime = System.currentTimeMillis(), endTime;
		BaseDeDonnees db = new BaseDeDonnees("LieLabTestOpti.db");
		try
		{
			
			db.connexion();//connexion
			db.createDatabase();
			db.ajouterCategorie("Poney des bois.");
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
					db.ajouterCategorie("Poney des bois.");
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
		try
		{
			db.ajouterEnregistrement("Statl3r est un demi-elf nain quadri classe", 77, 1, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz".getBytes());
		}
		catch (DBException e)
		{
			System.out.println("[-] Erreur lors de l'ajout : " + e.getMessage() );
		}
		
		for(long i = 0; i < max; i++)
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
			db.deconnexion();
		}
		catch (DBException e)
		{
			System.out.println("[-] Erreur lors de la deconnexion: " + e.getMessage() );
		}
		endTime = System.currentTimeMillis();
		System.out.println("[Opti Read]Le temps ecoule depuis le debut de la fonction est de " + (endTime - InitTime) + " ms.");
		System.out.println("[Opti Read]Le temps par enregistrement :" + (endTime - InitTime)/max + " ms.");
	}
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
			*recupérer l'enregistrement 2
			*recupérer l'enregistrement 4
			
			
			****Manipuler import/export****
			*exporter la base (facultatif dans l'immediat)
			*importer un fichier db 
			*Afficher la liste des categories et des enregistrements
			
			*Deconnexion
			*/
			BaseDeDonnees db = new BaseDeDonnees("LieLabTest.db");
			try
			{
				
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
			System.out.println("[i] ajout des categorie Poney/Flamment/Pegase");
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
			System.out.println("[i] Suppression de l'enregistrement 1.");
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
				System.out.println("[i] L'idee de la categorie \"Pegase\" est " + db.getCategorie("Pegase"));
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
			//****Deconnexion****
			try
			{
				System.out.println("\n[i] Deconnexion.");
				db.deconnexion();
			} 
			catch (DBException e)
			{
				System.out.println("[-] " + e.getMessage());
			}
	}
}
