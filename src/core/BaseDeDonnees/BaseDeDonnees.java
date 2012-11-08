package core.BaseDeDonnees;

import core.BaseDeDonnees.DBException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
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
			throw new DBException("Erreur lors du parcour des categories en important un fichier: " + e.getMessage(), 3);
		}
		//ajouter les enregistrement avec leurs categorie (modifiee) (ceux qu'il n'existe pas)
	}
	/**
	 * 
	 * @param cheminFichier fichier dans lequel sera exporte la base.
	 * @param id Correspond à l'identifiant de l'enregistrement à exporter dans le cas d'un type WAV
	 * @param type Correspond au type d'exportation, 1 = SQLite, 2 = WAV
	 */
	public void exporter(final String cheminFichier, final int id, final int type ) throws DBException//TODO a tester
	{
		if(connexion == null)
		{
			return;
		}
		if(type == 1)//Exporter la base vers un nouveau fichier
		{
			File src = new File(fileName);
			File dest = new File(cheminFichier);
			if(!src.exists())//Verifie si le fichier existe bien
			{
				throw new DBException("Le fichier " + fileName + " n'existe pas.", 3);
			}
			if(dest.exists())//verifie que la destination n'existe pas, auquel cas, on la supprime
			{
				dest.delete();
			}
			try {
				dest.createNewFile();//Création du nouveau fichier
			}
			catch (Exception e)
			{
				throw new DBException("Impossible de créer le fichier de sortie: " + e.getMessage(), 3);
			}
			boolean tmp = copyFile(src, dest);//copi des fichiers
			if(tmp == false)
			{
				throw new DBException("Impossible de copier le fichier de la base", 3);
			}
		}
		else if(type == 2)//exporter un echantillon
		{
			File dest = new File(cheminFichier);
			if(dest.exists())//verifie que la destination n'existe pas, auquel cas, on la supprime
			{
				dest.delete();
			}
			try {
				dest.createNewFile();//Création du nouveau fichier
			}
			catch (Exception e)
			{
				throw new DBException("Impossible de créer le fichier de sortie: " + e.getMessage(), 3);
			}
			//récupérer un enregistrement
			byte[] echantillon = this.recupererEnregistrement(id);
			//coller l'enregistrement dans un fichier
			FileOutputStream destinationFile = null;
			try
			{
				destinationFile = new FileOutputStream(dest);
				destinationFile.write(echantillon);
				destinationFile.close();
			}
			catch(Exception e)
			{
				throw new DBException("Erreur lors de la copie de l'échantillon dans le fichier: " + e.getMessage(), 3);
			}
			
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
			ResultSet rs = stat.executeQuery("SELECT duree, taille, nom, nomcat, id FROM enregistrements JOIN categorie USING (idcat) ORDER BY nomcat, nom;"); //Execution de la requete
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
			PreparedStatement ps = connexion.prepareStatement("SELECT duree, taille, nom, nomcat, id FROM enregistrements JOIN categorie USING (idcat) WHERE idcat=? ORDER BY nom");//Preparation de la requete
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
				ps.close();
				rs.close();
				return retour;
			}
			throw new DBException("Enregistrement inexistant.", 3);
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
	 * Permet d'ajouter une categorie
	 * @param nom le nom de la nouvelle categorie
	 * @throws DBException
	 */
	public void ajouterSujet(final String nom) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("INSERT INTO sujet (nomsuj) VALUES (?)");//preparation de la requete
			ps.setString(1, nom);//Remplissage de la requete
			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
					connexion.commit();//Validation des modifications
			}
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de l'ajout du sujet : " + e.getMessage(), 3);
		}
		
	}
	/**
	 * Supprime une categorie existante (ou non)
	 * @param id l'id de la categorie a supprimer
	 * @throws DBException
	 */
	public void supprimerSujet(final int id) throws DBException//comment on fait pour les enregistrements de cette cate ?
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("DELETE FROM sujet WHERE idSuj=?");//preparation de la requete
			ps.setInt(1, id);//Remplissage de la requete
			if(ps.executeUpdate() > 0)//execution et test de la reussite de la requete
			{
				connexion.commit();//Validation des modifications
			}
			ps.close();//Fermeture des ressources
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la suppression du sujet: " + e.getMessage(), 3);
		}
		
	}
	/**
	 * Permet de recuperer la liste des categories avec les colonnes dans cette ordre: nomCat, idCat
	 * @return Le resultat sous la forme d'un tableau parcourable dans un sens
	 * @throws DBException
	 */
	public ResultSet getListeSujet() throws DBException
	{
		if(connexion == null)
		{
			return null;
		}
		try
		{
			Statement stat = connexion.createStatement();//creation du Statement
			ResultSet rs = stat.executeQuery("SELECT nomSuj, idsuj FROM sujet;");//execution de la requete
			return rs;
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation des sujets: " + e.getMessage(), 3);
		}
	}
	/**
	 * Permet de changer le nom d'une categorie
	 * @param id l'id de la categorie a modifier
	 * @param nom le nouveau nom
	 * @throws DBException
	 */
	public void modifierSujet(final int id, final String nom) throws DBException
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("UPDATE sujet SET nomSuj=? WHERE idSuj=?");//preparation de la requete
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
				throw new DBException("Erreur lors de la modification du sujet: " + e.getMessage(), 3);
			}
	}
	/**
	 * Recupere le nom de la categorie correspondant a cette id
	 * @param idSuj id de la categorie
	 * @return le nom de la categorie
	 * @throws DBException
	 */
	public String getSujet(final int idSuj) throws DBException
	{
		String retour;
		if(connexion == null)
		{
			return null;
		}
		if( ! this.categorieExiste(idSuj))//test l'existance de la categorie
		{
			throw new DBException("Sujet inexistante.", 3);
		}
		try
		{

			PreparedStatement ps = connexion.prepareStatement("SELECT nomSuj FROM sujet WHERE idSuj=?;");//preparation de la requete
			ps.setInt(1, idSuj);//Remplissage de la requete
			ResultSet rs = ps.executeQuery();//execution de la requete
			retour = rs.getString(1);
			rs.close();
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation du sujet: " + e.getMessage(), 3);
		}
		return retour;
	}
	/**
	 * Recupere l'id d'une categorie a partir du nom. S'il y a plusieur categorie du meme nom, il renvera le premier id
	 * @param nomSuj le nom de la categorie
	 * @return l'id de la categorie
	 * @throws DBException
	 */
	public int getSujet(final String nomSuj) throws DBException
	{
		int retour;
		if(connexion == null)
		{
			return -1;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT idSuj FROM sujet WHERE nomSuj=?;");//preparation de la requete
			ps.setString(1, nomSuj);//Remplissage de la requete
			ResultSet rs = ps.executeQuery();
			retour = rs.getInt(1);
			rs.close();
			ps.close();
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation du sujet: " + e.getMessage(), 3);
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
			stat.executeUpdate("DROP TABLE if exists sujet;");
			//Creation des table et verification de la bonne execution des requetes
			if(stat.executeUpdate("CREATE TABLE categorie (idcat  INTEGER PRIMARY KEY AUTOINCREMENT, nomcat VARCHAR2(128));") != 0)
			{
				throw new Exception("Erreur de creation de la table categorie.");
			}
			if(stat.executeUpdate("CREATE TABLE enregistrements (id  INTEGER PRIMARY KEY AUTOINCREMENT, enregistrement BLOB, duree INTEGER, taille INTEGER, nom VARCHAR2(128), idcat INTEGER, idsuj INTEGER);") != 0)//FIXME ajouter la reference pour le champ idcat
			{
				throw new Exception("Erreur de creation de la table enregistrement.");
			}
			if(stat.executeUpdate("CREATE TABLE sujet (idsuj  INTEGER PRIMARY KEY AUTOINCREMENT, nomsuj VARCHAR2(128));") != 0)//FIXME ajouter la reference pour le champ idcat
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
	 * Verifie si une categorie existe
	 * @param idSuj id de la categorie a verifier
	 * @return true si la categorie existe et false sinon
	 * @throws DBException
	 */
	private boolean sujetExiste(final int idSuj) throws DBException
	{
		if(connexion == null)
		{
			return false;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT 1 FROM sujet WHERE idSuj=?");
			ps.setInt(1, idSuj);
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
			throw new DBException("Probleme lors de la verification de l'existance du sujet: " + e.getMessage(), 1);
		}
	}
	/**
	 * Verifie si une categorie existe
	 * @param nomSuj nom de la categorie a verifier
	 * @return true si la categorie existe et false sinon
	 * @throws DBException
	 */
	private boolean sujetExiste(final String nomSuj) throws DBException
	{
		if(connexion == null)
		{
			return false;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT 1 FROM sujet WHERE nomSuj=?");
			ps.setString(1, nomSuj);
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
			throw new DBException("Probleme lors de la verification de l'existance du sujet: " + e.getMessage(), 1);
		}
	}
	/** copie le fichier source dans le fichier resultat
	 * retourne vrai si cela réussit
	 */
	private static boolean copyFile(File source, File dest)
	{
		try
		{
			// Declaration et ouverture des flux
			java.io.FileInputStream sourceFile = new java.io.FileInputStream(source);

			try
			{
				java.io.FileOutputStream destinationFile = null;

				try{
					destinationFile = new FileOutputStream(dest);

					// Lecture par segment de 0.5Mo 
					byte buffer[] = new byte[512 * 1024];
					int nbLecture;

					while ((nbLecture = sourceFile.read(buffer)) != -1)
					{
						destinationFile.write(buffer, 0, nbLecture);
					}
				}
				finally
				{
					destinationFile.close();
				}
			}
			finally
			{
				sourceFile.close();
			}
		}
		catch (Exception e)
		{
			return false; // Erreur
		}

		return true; // Résultat OK  
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
