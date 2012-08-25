package BaseDeDonnee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Formatter;

import java.io.File;


public class BaseDeDonnees
{
	Connection connexion = null;
	String fileName = null;
	
	public BaseDeDonnees(String baseDeDonnees)
	{
		fileName = baseDeDonnees;
	}
	public void connexion() throws DBException
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
			connexion = DriverManager.getConnection("jdbc:sqlite:" + fileName);
			connexion.setAutoCommit(false);
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de l'initialisation de la connexion: " + e.getMessage(), 1);
		}
		try//test la structure de la base
		{
			Statement stat = connexion.createStatement();
			stat.executeQuery("SELECT id, enregistrement, duree, taille, nom, idcat FROM enregistrements;");
			stat.executeQuery("SELECT idcat, nomcat FROM categorie;");
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
	public void deconnexion() throws DBException
	{
		try
		{
			connexion.close();
			connexion = null;
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
	public ResultSet getListeEnregistrement() throws DBException
	{
		if(connexion == null)
		{
			return null;
		}
		try
		{
			Statement stat = connexion.createStatement();
			ResultSet rs = stat.executeQuery("SELECT duree, taille, nom, nomcat FROM enregistrements JOIN categorie USING (idcat);");
			//stat.close(); --> à vérifier si ça ne ferme pas la connexion à la base
			return rs;
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de la liste des enregistrement: " + e.getMessage(), 1);
		}
	}
	public ResultSet getListeEnregistrement(int idCat) throws DBException
	{
		if(connexion == null)
		{
			return null;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("SELECT duree, taille, nom, nomcat FROM enregistrements JOIN categorie USING (idcat) WHERE idcat=?");
			ps.setInt(1, idCat);
			ResultSet rs = ps.executeQuery();
			return rs;
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation de la liste des enregistrement: " + e.getMessage(), 1);
		}
	}
	public int getNombreEnregistrement() throws DBException
	{
		if(connexion == null)
		{
			return -1;
		}
		try
		{
			Statement stat = connexion.createStatement();
			ResultSet rs = stat.executeQuery("SELECT count(1) FROM enregistrements;");
			//stat.close(); --> à vérifier si ça ne ferme pas la connexion à la base
			return rs.getInt(1); //verifier le bon numero de colonne
		}
		catch(Exception e)
		{
			throw new DBException("Erreur lors de la recuperation du nombre d'enregistrement: " + e.getMessage(), 1);
		}
	}
	public void ajouterEnregistrement(String nom, int duree, int idCat, byte[] enregistrement) throws DBException
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
					"INSERT INTO enregistrements(enregistrement, duree, taille, nom, idCat) VALUES (?, ?, ?, ?, ?);");
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
	public void supprimerEnregistrement(int id)
	{
		if(connexion == null)
		{
			return;
		}
		try
		{
			PreparedStatement ps = connexion.prepareStatement("DELETE FROM enregistrements WHERE id=?");
			ps.setInt(1,  id);
			if(ps.executeUpdate() > 0)
			{
				connexion.commit();
			}
			ps.close();
		}
		catch(Exception e)
		{}
	}
	public void modifierEnregistrement(int id, String nom, int duree, byte[] enregistrement, int idCat) throws DBException
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
					"UPDATE enregistrements SET enregistrement=?, nom=?, duree=?, taille=?, idCat=? WHERE id=?;");
			ps.setBytes(1, enregistrement);
			ps.setString(2, nom);
			ps.setInt(3, duree);
			ps.setInt(4, enregistrement.length);
			ps.setInt(5, idCat);
			ps.setInt(6, id);

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
			ResultSet rs = stat.executeQuery("SELECT nomCat FROM categorie;");
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
}
