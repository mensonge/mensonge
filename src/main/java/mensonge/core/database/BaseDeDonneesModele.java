package mensonge.core.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mensonge.core.tools.DataBaseObservable;

/**
 * Classe permettant les interraction avec la base de donnees
 * 
 * @author Azazel
 * 
 */
abstract public class BaseDeDonneesModele extends DataBaseObservable
{
	public static final int EXPORTER_ENREGISTREMENT = 2;
	public static final int EXPORTER_BASE = 1;

	public static final String COLONNE_NOM = "nom";
	public static final String COLONNE_ID = "id";
	public static final String COLONNE_NOMSUJ = "nomSuj";
	public static final String COLONNE_NOMCAT = "nomCat";
	public static final String COLONNE_IDSUJ = "idSuj";
	public static final String COLONNE_IDCAT = "idCat";
	public static final String COLONNE_TAILLE = "taille";
	public static final String COLONNE_DUREE = "duree";
	public static final String ERREUR_MISE_A_JOUR = "Impossible de mettre à jour l'enregistrement";
	
	/**
	 * Le logger
	 */
	protected static Logger logger = Logger.getLogger("BDD");
	/**
	 * Permet d'instancier des objet qui permettrons de faire des requetes.
	 */
	protected Connection connexion = null;
	/**
	 * Nom du fichier fourni pour la base.
	 */
	protected String fileName = null;


	/**
	 * Fonction permettant de se connecter au fichier de la base de donnee fourni au constructeur. De plus, cette
	 * fonction verifie la structure de la base.
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * 
	*/
	abstract protected void connexion() throws ClassNotFoundException, SQLException;

	abstract protected void verifierBase() throws SQLException;
	/**
	 * Deconnecte de la base
	 * @throws SQLException 
	 *
	 */
	abstract public void deconnexion() throws SQLException;

	abstract public void setConnexion(Connection connexion);

	/**
	 * Importe un fichier de BaseDeDonnees sqlite dans la base donnee a laquelle l'objet est connecte. Les categories
	 * existantes (de meme nom) sont fusionnees. Les autres sont ajoutees.
	 * 
	 * @param cheminFichier
	 *            le fichier contenant la base a importer
	 * @throws DBException
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	abstract public void importer(List<LigneEnregistrement> listeCategorie, List<LigneEnregistrement> listeSujet, List<LigneEnregistrement> listeEnregistrement, BaseDeDonneesModele base) throws SQLException;
	
	/**
	 * exporte les enregistrement selon deux methodes. soit au format sqlite soit en wav. (ajoute à la fin du fichier
	 * wav le sujet et la categorie sur 4 octets chacun)
	 * 
	 * @param cheminFichier
	 *            fichier dans lequel sera exporte la base.
	 * @param id
	 *            Correspond à l'identifiant de l'enregistrement à exporter dans le cas d'un type WAV
	 * @param type
	 *            Correspond au type d'exportation, 1 = SQLite, 2 = WAV
	 * @throws SQLException 
	 */
	abstract public void exporterEnregistrement(final String cheminFichier, final int id) throws DBException, SQLException;
	

	abstract public byte[] convertInt(int n);
	
	abstract public void exporterBase(final String cheminFichier) throws DBException;
	
	/**
	 * Permet de recuperer toutes les information de tout les enregistrements avec les colonne suivante dans cette
	 * ordre: duree, taille, nom, nomcat, id
	 * 
	 * @return Le resultat sous forme d'objet ResultSet qui n'est parcourable qu'une fois.
	 * @throws SQLException 
	 * @throws DBException
	 */
	abstract public List<LigneEnregistrement> getListeEnregistrement() throws SQLException;
	
	/**
	 * Permet de recuperer toutes les informations de tout les enregistrements d'une categorie avec les colonnes
	 * suivante: duree, taille, nom, nomcat, id
	 * 
	 * @return Le resultat sous forme d'objet ResultSet qui n'est parcourable qu'une fois.
	 * @throws SQLException 
	 * @throws DBException
	 */
	abstract public List<LigneEnregistrement> getListeEnregistrementCategorie(final int idCat) throws SQLException;
	
	/**
	 * Permet de recuperer toutes les informations de tout les enregistrements d'une categorie avec les colonnes
	 * suivante: duree, taille, nom, nomcat, id
	 * 
	 * @return Le resultat sous forme d'objet ResultSet qui n'est parcourable qu'une fois.
	 * @throws DBException
	 */
	abstract public List<LigneEnregistrement> getListeEnregistrementSujet(final int idSuj) throws SQLException;
	
	/**
	 * Permet de recuperer le nombre d'enregistrement
	 * 
	 * @return le nombre d'enregistrement
	 * @throws DBException
	 */
	abstract public int getNombreEnregistrement() throws SQLException;
	
	/**
	 * Compacte la base de données en effectuant un VACUUM
	 * 
	 * @throws DBException
	 */
	abstract public void compacter();
	
	/**
	 * Permet d'ajouter un enregistrement a la base
	 * 
	 * @param nom
	 *            le nom sous lequel il apparaitra
	 * @param duree
	 *            la duree de cette enregistrement
	 * @param idCat
	 *            la categorie a laquelle il appartient
	 * @param enregistrement
	 *            l'enregistrement sous la forme d'un tableau de byte
	 * @throws DBException
	 */
	abstract public void ajouterEnregistrement(final String nom, final int duree, final int idCat, final byte[] enregistrement,
			final int idSuj) throws SQLException;
	
	/**
	 * Permet de supprimer un enregistrement
	 * 
	 * @param id
	 *            id de l'enregistrement a supprimer.
	 * @throws DBException
	 */
	abstract public void supprimerEnregistrement(final int id) throws SQLException;
	
	/**
	 * Permet de modifier un enregistrement dans son ensemble
	 * 
	 * @param id
	 *            le numero identifiant l'enregistrement
	 * @param nom
	 *            le nouveau nom
	 * @param duree
	 *            la nouvelle duree
	 * @param enregistrement
	 *            le nouvelle enregistrement
	 * @param idCat
	 *            la nouvelle categorie
	 * @throws DBException
	 */
	abstract public void modifierEnregistrement(final int id, final String nom, final int duree, final byte[] enregistrement,
			final int idCat, final int idSuj) throws SQLException;
	
	/**
	 * Permet de modifier un enregistrement sans modifier son contenu
	 * 
	 * @param id
	 *            le numero identifiant l'enregistrement
	 * @param nom
	 *            le nouveau nom
	 * @param duree
	 *            la nouvelle duree
	 * @param taille
	 *            du nouvelle enregistrement
	 * @param idCat
	 *            la nouvelle categorie
	 * @throws DBException
	 */
	abstract public void modifierEnregistrement(final int id, final String nom, final int duree, final int taille,
			final int idCat, final int idSuj) throws SQLException;
	
	/**
	 * Permet de modifier le nom de l'enregistrement
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param nom
	 *            le nouveau nom
	 * @throws DBException
	 */
	abstract public void modifierEnregistrementNom(final int id, final String nom) throws SQLException;
	
	/**
	 * Permet de modier le champ duree d'un enregistrement dans la base. (Pas la duree du veritable enregistrement)
	 * 
	 * @param id
	 *            l'id de l'enregistrement a modifier
	 * @param duree
	 *            la nouvelle duree a ajouter dans la base
	 * @throws DBException
	 */
	abstract public void modifierEnregistrementDuree(final int id, final int duree) throws SQLException;
	
	/**
	 * Permet de de modifier le champ taille d'un enregistrement dans la base. (Pas la taille du veritable
	 * enregistrement)
	 * 
	 * @param id
	 *            l'id de l'enregistrement a modifier
	 * @param taille
	 *            la nouvelle taille a ajouter dans la base
	 * @throws DBException
	 */
	abstract public void modifierEnregistrementTaille(final int id, final int taille) throws SQLException;
	
	/**
	 * Permet de modifier la categorie d'un enregistrement
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param idCat
	 *            nouvelle id de la categorie correspondant a la nouvelle categorie
	 * @throws DBException
	 */
	abstract public void modifierEnregistrementCategorie(final int id, final int idCat) throws SQLException;
	
	/**
	 * Permet de modifier la categorie d'un enregistrement a partir du nom de la categorie
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param nomCat
	 *            le nom de la categorie
	 * @throws DBException
	 */
	abstract public void modifierEnregistrementCategorie(final int id, final String nomCat) throws SQLException;
	
	/**
	 * Permet de modifier la categorie d'un enregistrement
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param idCat
	 *            nouvelle id de la categorie correspondant a la nouvelle categorie
	 * @throws DBException
	 */
	abstract public void modifierEnregistrementSujet(final int id, final int idSuj) throws SQLException;
	
	/**
	 * Permet de modifier la categorie d'un enregistrement a partir du nom de la categorie
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param nomCat
	 *            le nom de la categorie
	 * @throws DBException
	 */
	abstract public void modifierEnregistrementSujet(final int id, final String nomSuj) throws SQLException;
	
	/**
	 * Permet de recuperer l'enregistrement lui-meme et non ses informations
	 * 
	 * @param id
	 *            id de l'enregistrement a recuperer
	 * @return retourne l'enregistrement sous forme de tableau de byte
	 * @throws DBException
	 * @throws SQLException 
	 */
	abstract public byte[] recupererEnregistrement(final int id) throws SQLException;
	
	
	abstract public String getNomEnregistrement(final int id) throws SQLException;
	
	/**
	 * Permet d'ajouter une categorie
	 * 
	 * @param nom
	 *            le nom de la nouvelle categorie
	 * @throws DBException
	 */
	abstract public void ajouterCategorie(final String nom) throws SQLException;
	
	/**
	 * Permet de recuperer la liste des categories avec les colonnes dans cette ordre: nomCat, idCat
	 * 
	 * @return Le resultat sous la forme d'un tableau parcourable dans un sens
	 * @throws DBException
	 */
	abstract public List<LigneEnregistrement> getListeCategorie() throws SQLException;
	
	/**
	 * Supprime une categorie existante (ou non)
	 * 
	 * @param id
	 *            l'id de la categorie a supprimer
	 * @throws DBException
	 */
	abstract public void supprimerCategorie(final int id) throws SQLException;// comment on fait pour les enregistrements de cette
																	// cate ?
	
	/**
	 * Permet de changer le nom d'une categorie
	 * 
	 * @param id
	 *            l'id de la categorie a modifier
	 * @param nom
	 *            le nouveau nom
	 * @throws DBException
	 */
	abstract public void modifierCategorie(final int id, final String nom) throws SQLException;
	
	/**
	 * Recupere le nom de la categorie correspondant a cette id
	 * 
	 * @param idCat
	 *            id de la categorie
	 * @return le nom de la categorie
	 * @throws DBException
	 */
	abstract protected String getCategorie(final int idCat) throws SQLException;
	
	/**
	 * Recupere l'id d'une categorie a partir du nom. S'il y a plusieur categorie du meme nom, il renvera le premier id
	 * 
	 * @param nomCat
	 *            le nom de la categorie
	 * @return l'id de la categorie
	 * @throws DBException
	 */
	protected int getCategorie(final String nomCat) throws SQLException
	{
		int retour;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT idcat FROM categorie WHERE nomcat=?;");// preparation
																							// de la
																							// requete
			ps.setString(1, nomCat);// Remplissage de la requete
			rs = ps.executeQuery();
			retour = rs.getInt(1);
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	/**
	 * Permet d'ajouter une categorie
	 * 
	 * @param nom
	 *            le nom de la nouvelle categorie
	 * @throws DBException
	 */
	public void ajouterSujet(final String nom) throws SQLException
	{
		notifyInProgressAction("Ajout du sujet...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("INSERT INTO sujet (nomsuj) VALUES (?)");// preparation de
																						// la requete
			ps.setString(1, nom);// Remplissage de la requete
			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("Le sujet a été ajouté");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction("Impossible d'ajouter un sujet");
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible d'ajouter le sujet");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Supprime une categorie existante (ou non)
	 * 
	 * @param id
	 *            l'id de la categorie a supprimer
	 * @throws DBException
	 */
	public void supprimerSujet(final int id) throws SQLException// comment on fait pour les enregistrements de cette cate
																// ?
	{
		notifyInProgressAction("Suppression du sujet...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("DELETE FROM sujet WHERE idSuj=?");// preparation de la
																				// requete
			ps.setInt(1, id);// Remplissage de la requete
			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("Le sujet a été supprimé");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction("Impossible de supprimer le sujet");
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de supprimer le sujet");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}

	}

	/**
	 * Permet de recuperer la liste des categories avec les colonnes dans cette ordre: nomCat, idCat
	 * 
	 * @return Le resultat sous la forme d'un tableau parcourable dans un sens
	 * @throws DBException
	 */
	public List<LigneEnregistrement> getListeSujet() throws SQLException
	{
		Statement stat = null;
		ResultSet rs = null;
		List<LigneEnregistrement> retour = null;
		List<String> colonne = new LinkedList<String>();
		colonne.add(COLONNE_NOMSUJ);
		colonne.add(COLONNE_IDSUJ);
		try
		{
			stat = connexion.createStatement();// creation du Statement
			rs = stat.executeQuery("SELECT nomSuj, idSuj FROM sujet;");// execution de la requete
			retour = ResultatSelect.convertirResultatSet(rs, colonne);
		}
		finally
		{
			closeRessource(null, stat, rs);
		}
		return retour;
	}

	/**
	 * Permet de changer le nom d'une categorie
	 * 
	 * @param id
	 *            l'id de la categorie a modifier
	 * @param nom
	 *            le nouveau nom
	 * @throws DBException
	 */
	public void modifierSujet(final int id, final String nom) throws SQLException
	{
		notifyInProgressAction("Renommage du sujet...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("UPDATE sujet SET nomSuj=? WHERE idSuj=?");// preparation
																						// de la
																						// requete
			ps.setString(1, nom);// Remplissage de la requete
			ps.setInt(2, id);
			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("Le sujet a été renommé");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction("Impossible de modifier le sujet");
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de renommer le sujet");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Recupere le nom de la categorie correspondant a cette id
	 * 
	 * @param idSuj
	 *            id de la categorie
	 * @return le nom de la categorie
	 * @throws DBException
	 */
	public String getSujet(final int idSuj) throws SQLException
	{
		String retour;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{

			ps = connexion.prepareStatement("SELECT nomSuj FROM sujet WHERE idSuj=?;");// preparation
																						// de la
																						// requete
			ps.setInt(1, idSuj);// Remplissage de la requete
			rs = ps.executeQuery();// execution de la requete
			retour = rs.getString(1);
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	/**
	 * Recupere l'id d'une categorie a partir du nom. S'il y a plusieur categorie du meme nom, il renvera le premier id
	 * 
	 * @param nomSuj
	 *            le nom de la categorie
	 * @return l'id de la categorie
	 * @throws DBException
	 */
	public int getSujet(final String nomSuj) throws SQLException
	{
		int retour;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT idSuj FROM sujet WHERE nomSuj=?;");// preparation
																						// de la
																						// requete
			ps.setString(1, nomSuj);// Remplissage de la requete
			rs = ps.executeQuery();
			retour = rs.getInt(1);
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	/**
	 * Cette fonction creer la structure de la base de donne.
	 * 
	 * @throws DBException
	 */
	public void createDatabase() throws SQLException, DBException
	{
		Statement stat = null;
		try
		{
			stat = connexion.createStatement();// creation du Statement
			stat.executeUpdate("DROP TABLE if exists enregistrements;");// suppression des table si elle existe
			stat.executeUpdate("DROP TABLE if exists categorie;");
			stat.executeUpdate("DROP TABLE if exists sujet;");
			// Creation des table et verification de la bonne execution des requetes
			
			if (stat.executeUpdate("CREATE TABLE sujet (idsuj  INTEGER PRIMARY KEY AUTOINCREMENT, nomsuj VARCHAR2(128) UNIQUE);") != 0)
			{
				throw new DBException("Erreur de création de la table enregistrement.");
			}
			if (stat.executeUpdate("CREATE TABLE categorie (idcat  INTEGER PRIMARY KEY AUTOINCREMENT, nomcat VARCHAR2(128) UNIQUE);") != 0)
			{
				throw new DBException("Erreur de création de la table categorie.");
			}
			if (stat.executeUpdate("CREATE TABLE enregistrements (id  INTEGER PRIMARY KEY AUTOINCREMENT, enregistrement BLOB, duree INTEGER, taille INTEGER, nom VARCHAR2(128) UNIQUE, idcat INTEGER, idsuj INTEGER, FOREIGN KEY (idSuj) REFERENCES sujet(idSuj), FOREIGN KEY (idCat) REFERENCES categorie(idCat));") != 0)
			{
				throw new DBException("Erreur de création de la table enregistrement.");
			}
		}
		finally
		{
			closeRessource(null, stat, null);
		}
	}

	/**
	 * Verifie si une categorie existe
	 * 
	 * @param idCat
	 *            id de la categorie a verifier
	 * @return true si la categorie existe et false sinon
	 * @throws SQLException 
	 * @throws DBException
	 */
	protected boolean categorieExiste(final int idCat) throws SQLException
	{
		PreparedStatement ps = null;
		boolean retour = false;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT 1 FROM categorie WHERE idcat=?");
			ps.setInt(1, idCat);
			rs = ps.executeQuery();

			if (rs.next())
			{
				retour = true;
			}
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	/**
	 * Verifie si une categorie existe
	 * 
	 * @param nomCat
	 *            nom de la categorie a verifier
	 * @return true si la categorie existe et false sinon
	 * @throws DBException
	 */
	protected boolean categorieExiste(final String nomCat) throws SQLException
	{
		PreparedStatement ps = null;
		boolean retour = false;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT 1 FROM categorie WHERE nomcat=?");
			ps.setString(1, nomCat);
			rs = ps.executeQuery();

			if (rs.next())
			{
				retour = true;
			}
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	/**
	 * Verifie si une categorie existe
	 * 
	 * @param idSuj
	 *            id de la categorie a verifier
	 * @return true si la categorie existe et false sinon
	 * @throws SQLException 
	 */
	protected boolean sujetExiste(final int idSuj) throws SQLException
	{
		PreparedStatement ps = null;
		boolean retour = false;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT 1 FROM sujet WHERE idSuj=?");
			ps.setInt(1, idSuj);
			rs = ps.executeQuery();

			if (rs.next())
			{
				retour = true;
			}
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	/**
	 * Verifie si une categorie existe
	 * 
	 * @param nomSuj
	 *            nom de la categorie a verifier
	 * @return true si la categorie existe et false sinon
	 * @throws DBException
	 */
	protected boolean sujetExiste(final String nomSuj) throws SQLException
	{
		PreparedStatement ps = null;
		boolean retour = false;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT 1 FROM sujet WHERE nomSuj=?");
			ps.setString(1, nomSuj);
			rs = ps.executeQuery();

			if (rs.next())
			{
				retour = true;
			}
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	/**
	 * Verifie l'existance d'un sujet par son nom
	 * 
	 * @param nom
	 *            le nom a verifier
	 * @return true si le nom existe
	 * @throws DBException
	 */
	protected boolean enregistrementExist(final String nom) throws SQLException
	{
		PreparedStatement ps = null;
		boolean retour = false;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT 1 FROM enregistrements WHERE nom=?");
			ps.setString(1, nom);
			rs = ps.executeQuery();

			if (rs.next())
			{
				retour = true;
			}
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	protected boolean enregistrementExist(final int id) throws SQLException
	{
		PreparedStatement ps = null;
		boolean retour = false;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT 1 FROM enregistrements WHERE id=?");
			ps.setInt(1, id);
			rs = ps.executeQuery();

			if (rs.next())
			{
				retour = true;
			}
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}
	
	/**
	 * copie le fichier source dans le fichier resultat retourne vrai si cela réussit
	 */
	private static boolean copyFile(File source, File dest)
	{

		// Declaration et ouverture des flux
		FileInputStream sourceFile = null;
		FileOutputStream destinationFile = null;

		try
		{
			sourceFile = new FileInputStream(source);
			destinationFile = new FileOutputStream(dest);
			// Lecture par segment de 0.5Mo
			byte buffer[] = new byte[512 * 1024];
			int nbLecture;

			while ((nbLecture = sourceFile.read(buffer)) != -1)
			{
				destinationFile.write(buffer, 0, nbLecture);
			}
			sourceFile.close();
			destinationFile.close();
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, e.getLocalizedMessage());
		} finally
		{
			try
			{
				if (destinationFile != null)
				{
					destinationFile.close();
				}
				if (sourceFile != null)
				{
					sourceFile.close();
				}
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, e.getLocalizedMessage());
			}
		}

		return true; // Résultat OK
	}

	/**
	 * Ferme les differente ressources
	 * 
	 * @param ps
	 * @param st
	 * @param rs
	 */
	private static void closeRessource(PreparedStatement ps, Statement st, ResultSet rs)
	{
		if (ps != null)
		{
			try
			{
				ps.close();
			}
			catch (SQLException e)
			{
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		if (st != null)
		{
			try
			{
				st.close();
			}
			catch (SQLException e)
			{
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		if (rs != null)
		{
			try
			{
				rs.close();
			}
			catch (SQLException e)
			{
				logger.log(Level.WARNING, e.getMessage());
			}
		}
	}

	// GETTER et SETTER
	/**
	 * getter de la connexion
	 * 
	 * @return la connexion
	 */
	public Connection getConnexion()
	{
		return connexion;
	}

	/**
	 * Renvoie le chemin du fichier de base de donnée
	 * 
	 * @return le chemin du fichier
	 */
	public String getFileName()
	{
		return fileName;
	}
}
