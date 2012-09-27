package BaseDeDonnees;

import BaseDeDonnees.DBException;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;


/**
 * Classe permettant les interraction avec la base de donnees
 * @author Azazel
 *
 */
public class BaseDeDonnees
{
	/**
	 * Permet d'instancier des objet qui permettrons de faire des requetes.
	 */
	Connection connexion = null;
	/**
	 * Nom du fichier fourni pour la base.
	 */
	String fileName = null;
	
	/**
	 * Constructeur de base.
	 * @param baseDeDonnees Chaine de caract�re indiquant le nom du fichier de la base de donnee.
	 * @throws DBException 
	 */
	public BaseDeDonnees(final String baseDeDonnees) throws DBException
	{
		if( ! (new File(baseDeDonnees)).exists())//on creer un nouvelle objet File avec lequel on appel la methode exist pour verifier l'existance du fichier. Ensuite le ramasse miette fait le reste.
		{
			throw new DBException("Le fichier \"" + baseDeDonnees + "\" n'existe pas.", 4);//le fichier n'existe pas, on lance une exception
		}
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
			//desactive l'autocommit ce qui est plus securisant et augmente la vitesse
			connexion.setAutoCommit(false);
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
			connexion = null;//On remet a null pour des test future
		}
		catch(Exception e)
		{
			throw new DBException("Probleme lors de la deconnexion de la base : " + e.getMessage(), 4);
		}
	}
	/**
	 * Importe un fichier de BaseDeDonnees sqlite dans la base donnee a laquelle l'objet est connecte.
	 * Les categories existantes (de meme nom) sont fusionnees. Les autres sont ajoutees.
	 * @param cheminFichier le fichier contenant la base a importer
	 * @throws DBException 
	 */
	public void importer(final String cheminFichier) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		//etablir une connexion
		BaseDeDonnees in = new BaseDeDonnees(cheminFichier);
		in.connexion();
		
		//regarder les categories qui change et ajouter d'eventuelle nouvelle
		ResultSet rs = in.getListeCategorie();
		LinkedList<String> listeN = new LinkedList<String>();
		LinkedList<Integer> listeI = new LinkedList<Integer>();
		try
		{
			while(rs != null && rs.next())//On stock dans des listes chainees le resultat
			{
				listeN.add(rs.getString(1));
				listeI.add(new Integer(rs.getInt(2)));
			}
			rs.close();//on ferme le resultat set
			String nomCat;
			for(int i = 0; i < listeN.size(); i++)//on parcour la liste des categories
			{
				nomCat = listeN.get(i);
				if(!this.categorieExiste(nomCat))//On verifie si la categorie existe et si non, on l'ajoute
				{
					this.ajouterCategorie(nomCat);
				}
				rs = in.getListeEnregistrement(listeI.get(i).intValue());//On recupere tous les enregistrement de la categorie dans la base a importer
				int categorie = this.getCategorie(nomCat);//on recupere la categorie dans cette base la.
				while(rs != null && rs.next())//on ajoute tous les enregistrements dans la base
				{
					this.ajouterEnregistrement(rs.getString(3), rs.getInt(1), categorie, in.recupererEnregistrement(rs.getInt(5)));
				}
				rs.close();//On ferme la ressource
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new DBException("Erreur lors du parcour des categories en important un fichier: " + e.getMessage(), 3);
		}
		//ajouter les enregistrement avec leurs categorie (modifiee) (ceux qu'il n'existe pas)
	}
	/**
	 * 
	 * @param cheminFichier fichier dans lequel sera exporte la base.
	 */
	public void exporter(final String cheminFichier) //TODO
	{
		if(connexion == null)
		{
			return;
		}
	}
	/**
	 * Permet de recuperer toutes les information de tout les enregistrements avec les colonne suivante dans cette ordre: duree, taille, nom, nomcat, id
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
	public ResultSet getListeEnregistrement(final int idCat) throws DBException
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
	 * Permet d'ajouter un enregistrement a la base
	 * @param nom le nom sous lequel il apparaitra
	 * @param duree la duree de cette enregistrement
	 * @param idCat la categorie a laquelle il appartient
	 * @param enregistrement l'enregistrement sous la forme d'un tableau de byte
	 * @throws DBException
	 */
	public void ajouterEnregistrement(final String nom, final int duree, final int idCat, final byte[] enregistrement) throws DBException
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
			e.printStackTrace();
			System.exit(0);
			throw new DBException("Erreur lors de l'ajout de l'enregistrement : " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet de supprimer un enregistrement
	 * @param id id de l'enregistrement a supprimer.
	 */
	public void supprimerEnregistrement(final int id)
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
	public void modifierEnregistrement(final int id, final String nom, final int duree, final byte[] enregistrement, final int idCat) throws DBException
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
	/**
	 * Permet de modifier un enregistrement sans modifier son contenu
	 * @param id le numero identifiant l'enregistrement
	 * @param nom le nouveau nom
	 * @param duree la nouvelle duree
	 * @param taille du nouvelle enregistrement
	 * @param idCat la nouvelle categorie
	 * @throws DBException
	 */
	public void modifierEnregistrement(final int id, final String nom, final int duree, final int taille, final int idCat) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		if( ! this.categorieExiste(idCat))//On test si la categorie existe
		{
			throw new DBException("Categorie inexistante.", 3);
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET nom=?, duree=?, taille=?, idCat=? WHERE id=?;");//Preparation de la requete
			ps.setString(1, nom);//Remplissage de la requete
			ps.setInt(2, duree);
			ps.setInt(3, taille);
			ps.setInt(4, idCat);
			ps.setInt(5, id);

			if(ps.executeUpdate() > 0)//Execution et test de reussite dans la foulee
			{
				connexion.commit();//validation des modifications
			}
			ps.close();//fermeture des ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de l'enregistrement : " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet de modifier le nom de l'enregistrement
	 * @param id id de l'enregistrement a modifier
	 * @param nom le nouveau nom
	 * @throws DBException
	 */
	public void modifierEnregistrementNom(final int id, final String nom) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET nom=? WHERE id=?;");//preparation de la requete
			ps.setString(1, nom);//Remplissage de la requete
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
				connexion.commit();//Validation des modifications
			}
			ps.close();//Fermeture des ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification du nom: " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet de modier le champ duree d'un enregistrement dans la base. (Pas la duree du veritable enregistrement)
	 * @param id l'id de l'enregistrement a modifier
	 * @param duree la nouvelle duree a ajouter dans la base
	 * @throws DBException
	 */
	public void modifierEnregistrementDuree(final int id, final int duree) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET duree=? WHERE id=?;");//preparation de la requete
			ps.setInt(1, duree);//Remplissage de la requete
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
				connexion.commit();//Validation des modifications
			}
			ps.close();//Fermeture des ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de la duree: " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet de de modifier le champ taille d'un enregistrement dans la base. (Pas la taille du veritable enregistrement)
	 * @param id l'id de l'enregistrement a modifier
	 * @param taille la nouvelle taille a ajouter dans la base
	 * @throws DBException
	 */
	public void modifierEnregistrementTaille(final int id, final int taille) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET taille=? WHERE id=?;");//preparation de la requete
			ps.setInt(1, taille);//Remplissage de la requete
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
				connexion.commit();//Validation des modifications
			}
			ps.close();//Fermeture des ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de la taille: " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet de modifier la categorie d'un enregistrement
	 * @param id id de l'enregistrement a modifier
	 * @param idCat nouvelle id de la categorie correspondant a la nouvelle categorie
	 * @throws DBException
	 */
	public void modifierEnregistrementCategorie(final int id, final int idCat) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		if( ! this.categorieExiste(idCat))//test l'existance de la categorie
		{
			throw new DBException("Categorie inexistante.", 3);
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET idCat=? WHERE id=?;");//preparation de la requete
			ps.setInt(1, idCat);//Remplissage de la requete
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
				connexion.commit();//Validation des modifications
			}
			ps.close();//Fermeture des ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de la categorie: " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet de modifier la categorie d'un enregistrement a partir du nom de la categorie
	 * @param id id de l'enregistrement a modifier
	 * @param nomCat le nom de la categorie
	 * @throws DBException
	 */
	public void modifierEnregistrementCategorie(final int id, final String nomCat) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		if( ! this.categorieExiste(nomCat))//test l'existance de la categorie
		{
			throw new DBException("Categorie inexistante.", 3);
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement(
					"UPDATE enregistrements SET idCat=(SELECT idcat FROM categorie WHERE nomCat=?) WHERE id=?;");//preparation de la requete
			ps.setString(1, nomCat);//Remplissage de la requete
			ps.setInt(2, id);

			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
				connexion.commit();//Validation des modifications
			}
			ps.close();//Fermeture des ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la modification de la categorie: " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet de recuperer l'enregistrement lui-meme et non ses informations
	 * @param id id de l'enregistrement a recuperer
	 * @return retourne l'enregistrement sous forme de tableau de byte
	 * @throws DBException
	 */
	public byte[] recupererEnregistrement(final int id) throws DBException
	{
		byte[] retour;
		if(connexion == null)
		{
			return null;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT enregistrement FROM enregistrements WHERE id=?");//preparation de la requete
			ps.setInt(1, id);//Remplissage de la requete
			ResultSet rs = ps.executeQuery();//execute la requete
			if(rs.next())//s'il y a un retour on renvoie le tableau de byte sinon une exception est levee
			{
				retour = rs.getBytes("enregistrement");
				rs.close();
				return retour;
			}
			throw new Exception("Enregistrement inexistant.");
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de l'enregistrement : " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet d'ajouter une categorie
	 * @param nom le nom de la nouvelle categorie
	 * @throws DBException
	 */
	public void ajouterCategorie(final String nom) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("INSERT INTO categorie (nomcat) VALUES (?)");//preparation de la requete
			ps.setString(1, nom);//Remplissage de la requete
			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
					connexion.commit();//Validation des modifications
			}
			ps.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
			System.exit(0);
			throw new DBException("Erreur lors de l'ajout de la categorie : " + e.getMessage(), 3);
		}
		
	}
	/**
	 * Permet de recuperer la liste des categories avec les colonnes dans cette ordre: nomCat, idCat
	 * @return Le resultat sous la forme d'un tableau parcourable dans un sens
	 * @throws DBException
	 */
	public ResultSet getListeCategorie() throws DBException
	{
		if(connexion == null)
		{
			return null;
		}
		try
		{
			Statement stat = connexion.createStatement();//creation du Statement
			ResultSet rs = stat.executeQuery("SELECT nomCat, idcat FROM categorie;");//execution de la requete
			return rs;
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation des categories: " + e.getMessage(), 3);
		}
	}
	/**
	 * Supprime une categorie existante (ou non)
	 * @param id l'id de la categorie a supprimer
	 * @throws DBException
	 */
	public void supprimerCategorie(final int id) throws DBException//comment on fait pour les enregistrements de cette cate ?
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("DELETE FROM categorie WHERE idCat=?");//preparation de la requete
			ps.setInt(1, id);//Remplissage de la requete
			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
				connexion.commit();//Validation des modifications
			}
			ps.close();//Fermeture des ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la suppression de la categorie: " + e.getMessage(), 3);
		}
		
	}
	/**
	 * Permet de changer le nom d'une categorie
	 * @param id l'id de la categorie a modifier
	 * @param nom le nouveau nom
	 * @throws DBException
	 */
	public void modifierCategorie(final int id, final String nom) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("UPDATE categorie SET nomCat=? WHERE idCat=?");//preparation de la requete
			ps.setString(1, nom);//Remplissage de la requete
			ps.setInt(2, id);
			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
				connexion.commit();//Validation des modifications
			}
			ps.close();//Fermeture des ressources
			}
			catch(Exception e)
			{
				throw new DBException("Erreur lors de la modification de la categorie: " + e.getMessage(), 3);
			}
	}
	/**
	 * Recupere le nom de la categorie correspondant a cette id
	 * @param idCat id de la categorie
	 * @return le nom de la categorie
	 * @throws DBException
	 */
	public String getCategorie(final int idCat) throws DBException
	{
		String retour;
		if(connexion == null)
		{
			return null;
		}
		if( ! this.categorieExiste(idCat))//test l'existance de la categorie
		{
			throw new DBException("Categorie inexistante.", 3);
		}
		try
		{

			PreparedStatement ps = connexion.prepareStatement("SELECT nomcat FROM categorie WHERE idcat=?;");//preparation de la requete
			ps.setInt(1, idCat);//Remplissage de la requete
			ResultSet rs = ps.executeQuery();//execution de la requete
			retour = rs.getString(1);
			rs.close();
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de la categories: " + e.getMessage(), 3);
		}
		return retour;
	}
	/**
	 * Recupere l'id d'une categorie a partir du nom. S'il y a plusieur categorie du meme nom, il renvera le premier id
	 * @param nomCat le nom de la categorie
	 * @return l'id de la categorie
	 * @throws DBException
	 */
	public int getCategorie(final String nomCat) throws DBException
	{
		int retour;
		if(connexion == null)
		{
			return -1;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT idcat FROM categorie WHERE nomcat=?;");//preparation de la requete
			ps.setString(1, nomCat);//Remplissage de la requete
			ResultSet rs = ps.executeQuery();
			retour = rs.getInt(1);
			rs.close();
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de la categories: " + e.getMessage(), 3);
		}
		return retour;
	}	
	/**
	 * Cette fonction creer la structure de la base de donne.
	 * @throws DBException
	 */
	public void createDatabase() throws DBException
	{
		try
		{
			Statement stat = connexion.createStatement();//creation du Statement
			stat.executeUpdate("DROP TABLE if exists enregistrements;");//suppression des table si elle existe
			stat.executeUpdate("DROP TABLE if exists categorie;");
			//Creation des table et verification de la bonne execution des requetes
			if(stat.executeUpdate("CREATE TABLE categorie (idcat  INTEGER PRIMARY KEY AUTOINCREMENT, nomcat VARCHAR2(128));") != 0)
			{
				throw new Exception("Erreur de creation de la table categorie.");
			}
			if(stat.executeUpdate("CREATE TABLE enregistrements (id  INTEGER PRIMARY KEY AUTOINCREMENT, enregistrement BLOB, duree INTEGER, taille INTEGER, nom VARCHAR2(128), idcat INTEGER);") != 0)//FIXME ajouter la reference pour le champ idcat
			{
				throw new Exception("Erreur de creation de la table enregistrement.");
			}
			connexion.commit();//Validation des modifications
			stat.close();//Fermeture des ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la creation de la base : " + e.getMessage(), 3);
		}
	}
	/**
	 * Verifie si une categorie existe
	 * @param idCat id de la categorie a verifier
	 * @return true si la categorie existe et false sinon
	 * @throws DBException
	 */
	private boolean categorieExiste(final int idCat) throws DBException
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
				rs.close();//Fermeture des ressources
				ps.close();
				return true;
			}
			rs.close();//Fermeture des ressources
			ps.close();
			return false;
		}
		catch(Exception e)
		{
			throw new DBException("Probleme lors de la verification de l'existance de la categorie: " + e.getMessage(), 1);
		}
	}
	/**
	 * Verifie si une categorie existe
	 * @param nomCat nom de la categorie a verifier
	 * @return true si la categorie existe et false sinon
	 * @throws DBException
	 */
	private boolean categorieExiste(final String nomCat) throws DBException
	{
		if(connexion == null)
		{
			return false;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT 1 FROM categorie WHERE nomcat=?");
			ps.setString(1, nomCat);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next())
			{
				rs.close();//Fermeture des ressources
				ps.close();
				return true;
			}
			rs.close();//Fermeture des ressources
			ps.close();
			return false;
		}
		catch(Exception e)
		{
			throw new DBException("Probleme lors de la verification de l'existance de la categorie: " + e.getMessage(), 1);
		}
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
			
			if (BaseDeDonnees.createFictiveDataBase())//On creer une seconde base de donnees fictive, si on reussi a la creer, on l'importe
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
 			db.ajouterCategorie("Dieux");
 			db.ajouterCategorie("Licorne");
 			
 			db.ajouterEnregistrement("Hades", 7, 1, "azerty".getBytes());
 			db.ajouterEnregistrement("Bella", 7, 2, "qsdfgh".getBytes());
 			
 			db.deconnexion();
 		}
 		catch(Exception e )
 		{
 			return false;
 		}
 		return true;
	}

	//GETTER et SETTER
	public Connection getConnexion() {
		return connexion;
	}
	public void setConnexion(Connection connexion) {
		this.connexion = connexion;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
