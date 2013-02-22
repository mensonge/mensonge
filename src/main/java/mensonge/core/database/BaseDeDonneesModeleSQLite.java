package mensonge.core.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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

public class BaseDeDonneesModeleSQLite extends BaseDeDonneesModele
{
	/**
	 * Constructeur de base.
	 * 
	 * @param baseDeDonnees
	 *            Chaine de caractére indiquant le nom du fichier de la base de donnee.
	 * @throws DBException
	 */
	public BaseDeDonneesModeleSQLite(final String baseDeDonnees)
	{
		fileName = baseDeDonnees;
	}

	/**
	 * Fonction permettant de se connecter au fichier de la base de donnee fourni au constructeur. De plus, cette
	 * fonction verifie la structure de la base.
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * 
	*/
	protected void connexion() throws ClassNotFoundException, SQLException
	{
			// Lance la connection
			Class.forName("org.sqlite.JDBC");
			connexion = DriverManager.getConnection("jdbc:sqlite:" + fileName);
			// desactive l'autocommit ce qui est plus securisant et augmente la vitesse
			connexion.setAutoCommit(true);
	}

	protected void verifierBase() throws SQLException
	{
		Statement stat = null;
		try
		// test la structure de la base
		{
			stat = connexion.createStatement();// creation du Statement
			stat.executeQuery("SELECT id, enregistrement, duree, taille, nom, idcat, idsuj FROM enregistrements;");
			stat.executeQuery("SELECT idcat, nomcat FROM categorie;");
			stat.executeQuery("SELECT idsuj, nomsuj FROM sujet;");
			// fermeture du Statement
			stat.close();
		}
		finally
		{
			closeRessource(null, stat, null);
		}
	}
	/**
	 * Deconnecte de la base
	 * @throws SQLException 
	 *
	 */
	public void deconnexion() throws SQLException
	{
			connexion.close();// On close la connexion
			connexion = null;// On remet a null pour des test future
	}

	public void setConnexion(Connection connexion)
	{
		this.connexion = connexion;
	}

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
	public void importer(List<LigneEnregistrement> listeCategorie, List<LigneEnregistrement> listeSujet, List<LigneEnregistrement> listeEnregistrement, BaseDeDonneesModele base) throws SQLException
	{
		notifyInProgressAction("Importation de la base de données...");
		Map<Integer, Integer> bijectionCategorie = new HashMap<Integer, Integer>();
		Map<Integer, Integer> bijectionSujet = new HashMap<Integer, Integer>();

		for (LigneEnregistrement sujet : listeSujet)
		{
			this.ajouterSujet(sujet.getNomSuj());
			bijectionSujet.put(sujet.getIdSuj(), this.getSujet(sujet.getNomSuj()));
			// ajouter au a la bijection des sujets
		}

		for (LigneEnregistrement categorie : listeCategorie)
		{
			this.ajouterCategorie(categorie.getNomCat());
			bijectionCategorie.put(categorie.getIdCat(), this.getCategorie(categorie.getNomCat()));
			// ajouter au a la bijection des sujets
		}
		// Pour tous les enregistrements, on effectue la bijection des categories et des sujets
		for (LigneEnregistrement enregistrement : listeEnregistrement)
		{
			byte sample[] = base.recupererEnregistrement(enregistrement.getId());
			int idCat = enregistrement.getIdCat();
			int idSuj = enregistrement.getIdSuj();
			idCat = bijectionCategorie.get(idCat);
			idSuj = bijectionSujet.get(idSuj);
			enregistrement.setIdCat(idCat);
			enregistrement.setIdSuj(idSuj);

			this.ajouterEnregistrement(enregistrement.getNom(), enregistrement.getDuree(), enregistrement.getIdCat(), sample,
					enregistrement.getIdSuj());
		}
		notifyCompletedAction("La base de données a été importée");
	}

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
	public void exporterEnregistrement(final String cheminFichier, final int id) throws DBException, SQLException
	{
			notifyInProgressAction("Exportation de l'enregistrement...");
			File dest = new File(cheminFichier);
			// récupérer un enregistrement
			byte[] echantillon = this.recupererEnregistrement(id);
			int sujet = 0;
			byte categorie = 0;
			// coller l'enregistrement dans un fichier
			FileOutputStream destinationFile = null;
			List<LigneEnregistrement> liste = this.getListeEnregistrement();
			
			for (LigneEnregistrement ligne : liste)
			{
				if (ligne.getId() == id)
				{
					sujet = ligne.getIdCat();
					categorie = (byte) ligne.getIdSuj();
				}
			}

			try
			{
				ByteBuffer bb;
				destinationFile = new FileOutputStream(dest);
				destinationFile.write(echantillon);
				bb = ByteBuffer.allocate(Integer.SIZE/8*2);
				bb.putInt(categorie);
				bb.putInt(sujet);
				destinationFile.write(bb.array());
				destinationFile.flush();
				destinationFile.close();
				
			}
			catch (IOException e)
			{
				if(destinationFile != null)
				{
					try
					{
						destinationFile.close();
					}
					catch (IOException e1)
					{
						notifyFailedAction("Impossible d'exporter l'enregistrement");
						throw new DBException("Erreur lors de la copie de l'échantillon dans le fichier", e1);
					}
				}
				notifyFailedAction("Impossible d'exporter l'enregistrement");
				throw new DBException("Erreur lors de la copie de l'échantillon dans le fichier", e);
			}
			notifyCompletedAction("La base de données a été exportée");
	}

	public byte[] convertInt(int n)
	{
		byte[] retour = new byte[4];
		
		return retour;
	}
	public void exporterBase(final String cheminFichier) throws DBException
	{
		notifyInProgressAction("Exportation de la base de données...");
		File src = new File(fileName);
		File dest = new File(cheminFichier);
		if (!copyFile(src, dest))
		{
			notifyFailedAction("Impossible d'exporter la base de données");
			throw new DBException("Impossible de copier le fichier de la base");
		}
		notifyCompletedAction("La base de données a été exportée");
	}
	
	/**
	 * Permet de recuperer toutes les information de tout les enregistrements avec les colonne suivante dans cette
	 * ordre: duree, taille, nom, nomcat, id
	 * 
	 * @return Le resultat sous forme d'objet ResultSet qui n'est parcourable qu'une fois.
	 * @throws SQLException 
	 * @throws DBException
	 */
	public List<LigneEnregistrement> getListeEnregistrement() throws SQLException 
	{
		Statement stat = null;
		ResultSet rs = null;
		List<LigneEnregistrement> retour = null;
		List<String> colonne = new LinkedList<String>();
		colonne.add(COLONNE_DUREE);
		colonne.add(COLONNE_TAILLE);
		colonne.add(COLONNE_NOM);
		colonne.add(COLONNE_NOMCAT);
		colonne.add(COLONNE_ID);
		colonne.add(COLONNE_NOMSUJ);
		colonne.add(COLONNE_IDCAT);
		colonne.add(COLONNE_IDSUJ);
		try
		{
			stat = connexion.createStatement(); // Creation du Statement

			rs = stat
					.executeQuery("SELECT duree, taille, nom, nomcat, id, nomsuj, en.idcat, en.idsuj FROM enregistrements en, categorie ca, sujet su WHERE en.idcat = ca.idcat AND en.idsuj = su.idsuj ORDER BY nomcat, nom;"); // Execution
																																																								// //
																																																								// de
																																																								// //
																																																								// requete
			retour = ResultatSelect.convertirResultatSet(rs, colonne);

		}
		finally
		{
			closeRessource(null, stat, rs);
		}
		return retour;
	}

	/**
	 * Permet de recuperer toutes les informations de tout les enregistrements d'une categorie avec les colonnes
	 * suivante: duree, taille, nom, nomcat, id
	 * 
	 * @return Le resultat sous forme d'objet ResultSet qui n'est parcourable qu'une fois.
	 * @throws SQLException 
	 * @throws DBException
	 */
	public List<LigneEnregistrement> getListeEnregistrementCategorie(final int idCat) throws SQLException
	{
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<LigneEnregistrement> retour = null;
		List<String> colonne = new LinkedList<String>();
		colonne.add(COLONNE_DUREE);
		colonne.add(COLONNE_TAILLE);
		colonne.add(COLONNE_NOM);
		colonne.add(COLONNE_NOMCAT);
		colonne.add(COLONNE_ID);
		colonne.add(COLONNE_NOMSUJ);
		colonne.add(COLONNE_IDSUJ);
		colonne.add(COLONNE_IDCAT);
		try
		{
			ps = connexion
					.prepareStatement("SELECT duree, taille, nom, nomcat, id, nomsuj, en.idsuj, en.idcat FROM enregistrements en, categorie ca, sujet su WHERE en.idcat = ca.idcat AND en.idsuj = su.idsuj AND en.idcat=? ORDER BY nom");
			ps.setInt(1, idCat);// on rempli les trous
			rs = ps.executeQuery();// On execute
			retour = ResultatSelect.convertirResultatSet(rs, colonne);
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	/**
	 * Permet de recuperer toutes les informations de tout les enregistrements d'une categorie avec les colonnes
	 * suivante: duree, taille, nom, nomcat, id
	 * 
	 * @return Le resultat sous forme d'objet ResultSet qui n'est parcourable qu'une fois.
	 * @throws DBException
	 */
	public List<LigneEnregistrement> getListeEnregistrementSujet(final int idSuj) throws SQLException
	{
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<LigneEnregistrement> retour = null;
		List<String> colonne = new LinkedList<String>();
		colonne.add(COLONNE_DUREE);
		colonne.add(COLONNE_TAILLE);
		colonne.add(COLONNE_NOM);
		colonne.add(COLONNE_NOMCAT);
		colonne.add(COLONNE_ID);
		colonne.add(COLONNE_NOMSUJ);
		colonne.add(COLONNE_IDSUJ);
		colonne.add(COLONNE_IDCAT);
		try
		{
			ps = connexion
					.prepareStatement("SELECT duree, taille, nom, nomcat, id, nomsuj, en.idsuj, en.idcat FROM enregistrements en, categorie ca, sujet su WHERE en.idcat = ca.idcat AND en.idsuj = su.idsuj AND en.idsuj=? ORDER BY nom");// Preparation
			// de
			// la
			// requete
			ps.setInt(1, idSuj);// on rempli les trous
			rs = ps.executeQuery();// On execute
			retour = ResultatSelect.convertirResultatSet(rs, colonne);
		}
		finally
		{
			closeRessource(ps, null, rs);
		}
		return retour;
	}

	/**
	 * Permet de recuperer le nombre d'enregistrement
	 * 
	 * @return le nombre d'enregistrement
	 * @throws DBException
	 */
	public int getNombreEnregistrement() throws SQLException
	{
		Statement stat = null;
		ResultSet rs = null;
		try
		{
			stat = connexion.createStatement();// Creer le Statement
			rs = stat.executeQuery("SELECT count(1) FROM enregistrements;");// On execute la recherche
			int retour = rs.getInt(1);// On recupere le resultat
			rs.close();// On ferme les differents objets
			stat.close();
			return retour;
		}
		finally
		{
			closeRessource(null, stat, rs);
		}
	}

	/**
	 * Compacte la base de données en effectuant un VACUUM
	 * 
	 * @throws DBException
	 */
	public void compacter()
	{
		notifyInProgressAction("Compactage de la base de données...");
		// hack foireux pour poouvoir recevoir l'event de l'action qui n'est pas recu sinon la méthode est bloquante...
		new Thread()
		{
			@Override
			public void run()
			{
				Statement stat = null;
				try
				{
					stat = connexion.createStatement();
					// Pour l'automatique ça serait : "PRAGMA auto_vacuum = 1"
					stat.execute("VACUUM");
					stat.close();
					notifyCompletedAction("La base de données a été compactée");
				}
				catch (SQLException e)
				{
					notifyFailedAction("Une erreur est survenue pendant le compactage de la base de données");
				} finally
				{
					closeRessource(null, stat, null);
				}
			}
		}.start();
	}

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
	public void ajouterEnregistrement(final String nom, final int duree, final int idCat, final byte[] enregistrement,
			final int idSuj) throws SQLException
	{
		notifyInProgressAction("Ajout de l'enregistrement dans la base de données...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion
					.prepareStatement("INSERT INTO enregistrements(enregistrement, duree, taille, nom, idCat, idSuj) VALUES (?, ?, ?, ?, ?, ?);");
			ps.setBytes(1, enregistrement);
			ps.setInt(2, duree);
			ps.setInt(3, enregistrement.length);
			ps.setString(4, nom);
			ps.setInt(5, idCat);
			ps.setInt(6, idSuj);
			
			if (ps.executeUpdate() > 0)
			{
				notifyUpdateDataBase();
				notifyCompletedAction("L'enregistrement a été ajouté dans la base de données");
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible d'ajouter l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Permet de supprimer un enregistrement
	 * 
	 * @param id
	 *            id de l'enregistrement a supprimer.
	 * @throws DBException
	 */
	public void supprimerEnregistrement(final int id) throws SQLException
	{
		notifyInProgressAction("Suppresion de l'enregistrement...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("DELETE FROM enregistrements WHERE id=?");
			ps.setInt(1, id);// On rempli les trou
			if (ps.executeUpdate() > 0)// On execute la requete et on test la reussite de cette dernier
			{
				notifyUpdateDataBase();
				notifyCompletedAction("L'enregistrement a été supprimé");
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de supprimer l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

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
	public void modifierEnregistrement(final int id, final String nom, final int duree, final byte[] enregistrement,
			final int idCat, final int idSuj) throws SQLException
	{
		notifyInProgressAction("Mise à jour de l'enregistrement...");

		PreparedStatement ps = null;
		try
		{
			ps = connexion
					.prepareStatement("UPDATE enregistrements SET enregistrement=?, nom=?, duree=?, taille=?, idCat=?, idsuj=? WHERE id=?;");// On
																																				// prepare
			ps.setBytes(1, enregistrement);// On rempli
			ps.setString(2, nom);
			ps.setInt(3, duree);
			ps.setInt(4, enregistrement.length);
			ps.setInt(5, idCat);
			ps.setInt(6, idSuj);
			ps.setInt(7, id);

			if (ps.executeUpdate() > 0)// On execute et on test la reussite
			{
				notifyCompletedAction("L'enregistrement a été mis à jour");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction(ERREUR_MISE_A_JOUR);
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction(ERREUR_MISE_A_JOUR);
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

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
	public void modifierEnregistrement(final int id, final String nom, final int duree, final int taille,
			final int idCat, final int idSuj) throws SQLException
	{
		notifyInProgressAction("Mise à jour de l'enregistrement...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion
					.prepareStatement("UPDATE enregistrements SET nom=?, duree=?, taille=?, idCat=?, idsuj=? WHERE id=?;");
			ps.setString(1, nom);// Remplissage de la requete
			ps.setInt(2, duree);
			ps.setInt(3, taille);
			ps.setInt(4, idCat);
			ps.setInt(5, idSuj);
			ps.setInt(6, id);

			if (ps.executeUpdate() > 0)// Execution et test de reussite dans la foulee
			{
				notifyCompletedAction("L'enregistrement a été mis à jour");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction(ERREUR_MISE_A_JOUR);
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction(ERREUR_MISE_A_JOUR);
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Permet de modifier le nom de l'enregistrement
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param nom
	 *            le nouveau nom
	 * @throws DBException
	 */
	public void modifierEnregistrementNom(final int id, final String nom) throws SQLException
	{
		notifyInProgressAction("Renommage de l'enregistrement...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("UPDATE enregistrements SET nom=? WHERE id=?;");
			ps.setString(1, nom);// Remplissage de la requete
			ps.setInt(2, id);

			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("L'enregistrement a été renommé");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction(ERREUR_MISE_A_JOUR);
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de renommer l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Permet de modier le champ duree d'un enregistrement dans la base. (Pas la duree du veritable enregistrement)
	 * 
	 * @param id
	 *            l'id de l'enregistrement a modifier
	 * @param duree
	 *            la nouvelle duree a ajouter dans la base
	 * @throws DBException
	 */
	public void modifierEnregistrementDuree(final int id, final int duree) throws SQLException
	{
		notifyInProgressAction("Changement de la durée de l'enregistrement...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("UPDATE enregistrements SET duree=? WHERE id=?;");
			ps.setInt(1, duree);// Remplissage de la requete
			ps.setInt(2, id);

			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("La durée de l'enregistrement a été mise à jour");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction(ERREUR_MISE_A_JOUR);
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de changer la durée de l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

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
	public void modifierEnregistrementTaille(final int id, final int taille) throws SQLException
	{
		notifyInProgressAction("Changement de la taille de l'enregistrement...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("UPDATE enregistrements SET taille=? WHERE id=?;");
			ps.setInt(1, taille);// Remplissage de la requete
			ps.setInt(2, id);

			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("La taille de l'enregistrement a été mise à jour");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction(ERREUR_MISE_A_JOUR);
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de changer la taille de l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Permet de modifier la categorie d'un enregistrement
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param idCat
	 *            nouvelle id de la categorie correspondant a la nouvelle categorie
	 * @throws DBException
	 */
	public void modifierEnregistrementCategorie(final int id, final int idCat) throws SQLException
	{
		notifyInProgressAction("Changement de la catégorie de l'enregistrement...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("UPDATE enregistrements SET idCat=? WHERE id=?;");
			ps.setInt(1, idCat);// Remplissage de la requete
			ps.setInt(2, id);

			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("La catégorie de l'enregistrement a été mise à jour");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction(ERREUR_MISE_A_JOUR);
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de changer la catégorie de l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Permet de modifier la categorie d'un enregistrement a partir du nom de la categorie
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param nomCat
	 *            le nom de la categorie
	 * @throws DBException
	 */
	public void modifierEnregistrementCategorie(final int id, final String nomCat) throws SQLException
	{
		notifyInProgressAction("Changement de la catégorie de l'enregistrement...");
		PreparedStatement ps = null;
		try
		{
			
			ps = connexion
					.prepareStatement("UPDATE enregistrements SET idCat=(SELECT idcat FROM categorie WHERE nomCat=?) WHERE id=?;");// preparation
																																	// de
																																	// la
																																	// requete
			ps.setString(1, nomCat);// Remplissage de la requete
			ps.setInt(2, id);
			
			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				
				notifyCompletedAction("La catégorie de l'enregistrement a été mise à jour");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction(ERREUR_MISE_A_JOUR);
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de changer la catégorie de l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Permet de modifier la categorie d'un enregistrement
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param idCat
	 *            nouvelle id de la categorie correspondant a la nouvelle categorie
	 * @throws DBException
	 */
	public void modifierEnregistrementSujet(final int id, final int idSuj) throws SQLException
	{
		notifyInProgressAction("Changement du sujet de l'enregistrement...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("UPDATE enregistrements SET idSuj=? WHERE id=?;");
			ps.setInt(1, idSuj);// Remplissage de la requete
			ps.setInt(2, id);

			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("Le sujet de l'enregistrement a été mis à jour");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction(ERREUR_MISE_A_JOUR);
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de changer le sujet de l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Permet de modifier la categorie d'un enregistrement a partir du nom de la categorie
	 * 
	 * @param id
	 *            id de l'enregistrement a modifier
	 * @param nomCat
	 *            le nom de la categorie
	 * @throws DBException
	 */
	public void modifierEnregistrementSujet(final int id, final String nomSuj) throws SQLException
	{
		notifyInProgressAction("Changement du sujet de l'enregistrement...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion
					.prepareStatement("UPDATE enregistrements SET idSuj=(SELECT idsuj FROM sujet WHERE nomSuj=?) WHERE id=?;");// preparation
																																// de
																																// la
																																// requete
			ps.setString(1, nomSuj);// Remplissage de la requete
			ps.setInt(2, id);

			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("Le sujet de l'enregistrement a été mis à jour");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction(ERREUR_MISE_A_JOUR);
			}

		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de changer le sujet de l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Permet de recuperer l'enregistrement lui-meme et non ses informations
	 * 
	 * @param id
	 *            id de l'enregistrement a recuperer
	 * @return retourne l'enregistrement sous forme de tableau de byte
	 * @throws DBException
	 * @throws SQLException 
	 */
	public byte[] recupererEnregistrement(final int id) throws SQLException
	{
		notifyInProgressAction("Récupération de l'enregistrement depuis la base de données...");
		byte[] retour = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT enregistrement FROM enregistrements WHERE id=?");
			ps.setInt(1, id);// Remplissage de la requete
			rs = ps.executeQuery();// execute la requete
			if (rs.next())// s'il y a un retour on renvoie le tableau de byte sinon une exception est levee
			{
				retour = rs.getBytes("enregistrement");
				notifyCompletedAction("L'enregistrement a été récupéré");
				return retour;
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de récupérer l'enregistrement");
			throw e;
		} finally
		{
			closeRessource(ps, null, rs);
		}
		return null;
	}
	
	public String getNomEnregistrement(final int id) throws SQLException
	{
		String retour = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			ps = connexion.prepareStatement("SELECT nom FROM enregistrements WHERE id=?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next())
			{
				retour = rs.getString(COLONNE_NOM);
			}
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
	public void ajouterCategorie(final String nom) throws SQLException
	{
		notifyInProgressAction("Ajout de la catégorie...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("INSERT INTO categorie (nomcat) VALUES (?)");// preparation
																							// de la
																							// requete
			ps.setString(1, nom);// Remplissage de la requete
			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("La catégorie a été ajoutée");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction("Impossible d'ajouter la catégorie");
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible d'ajouter la catégorie");
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
	public List<LigneEnregistrement> getListeCategorie() throws SQLException
	{
		Statement stat = null;
		ResultSet rs = null;
		List<LigneEnregistrement> retour = null;
		List<String> colonne = new LinkedList<String>();
		colonne.add(COLONNE_NOMCAT);
		colonne.add(COLONNE_IDCAT);
		try
		{
			stat = connexion.createStatement();// creation du Statement
			rs = stat.executeQuery("SELECT nomCat, idCat FROM categorie;");// execution de la requete
			retour = ResultatSelect.convertirResultatSet(rs, colonne);
		}
		finally
		{
			closeRessource(null, stat, rs);
		}
		return retour;
	}

	/**
	 * Supprime une categorie existante (ou non)
	 * 
	 * @param id
	 *            l'id de la categorie a supprimer
	 * @throws DBException
	 */
	public void supprimerCategorie(final int id) throws SQLException// comment on fait pour les enregistrements de cette
																	// cate ?
	{
		notifyInProgressAction("Suppresion de la catégorie...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("DELETE FROM categorie WHERE idCat=?");// preparation de
																					// la requete
			ps.setInt(1, id);// Remplissage de la requete
			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("La catégorie a été supprimée");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction("Impossible de supprimer la catégorie");
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de supprimer la catégorie");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
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
	public void modifierCategorie(final int id, final String nom) throws SQLException
	{
		notifyInProgressAction("Renommage de la catégorie...");
		PreparedStatement ps = null;
		try
		{
			ps = connexion.prepareStatement("UPDATE categorie SET nomCat=? WHERE idCat=?");// preparation
																							// de la
																							// requete
			ps.setString(1, nom);// Remplissage de la requete
			ps.setInt(2, id);
			if (ps.executeUpdate() > 0)// execution et test de la reussite de la requete
			{
				notifyCompletedAction("La catégorie a été renommée");
				notifyUpdateDataBase();
			}
			else
			{
				notifyFailedAction("Impossible de modifier la catégorie");
			}
		}
		catch (SQLException e)
		{
			notifyFailedAction("Impossible de renommer la catégorie");
			throw e;
		} finally
		{
			closeRessource(ps, null, null);
		}
	}

	/**
	 * Recupere le nom de la categorie correspondant a cette id
	 * 
	 * @param idCat
	 *            id de la categorie
	 * @return le nom de la categorie
	 * @throws DBException
	 */
	protected String getCategorie(final int idCat) throws SQLException
	{
		String retour = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{

			ps = connexion.prepareStatement("SELECT nomcat FROM categorie WHERE idcat=?;");// preparation
																							// de la
																							// requete
			ps.setInt(1, idCat);// Remplissage de la requete
			rs = ps.executeQuery();// execution de la requete
			retour = rs.getString(1);
			rs.close();
			ps.close();
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

}
