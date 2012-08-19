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

//mettre des bloc try ...

public class BaseDeDonnees
{
	Connection connexion = null;
	String fileName;
	
	public BaseDeDonnees(String baseDeDonnees)
	{
		fileName = baseDeDonnees;
	}
	public void connexion() //TODO
	{
		try
		{
			connexion = DriverManager.getConnection("jdbc:sqlite:" + fileName);
			connexion.setAutoCommit(false);
		}
		catch(Exception e)
		{
			
		}
		try
		{
			Statement stat = connexion.createStatement();
			stat.executeQuery("SELECT id, enregistrement, duree, taille, nom, idcat FROM enregistrements;");
			stat.executeQuery("SELECT idcat, nomcat FROM categorie;");
		}
		catch(SQLException e)
		{
			
		}
		catch(Exception e)
		{
			
		}
	}
	public void deconnexion()
	{
		try
		{
			connexion.close();
			connexion = null;
		}
		catch(Exception e)
		{
		
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
	public ResultSet getListeEnregistrement()
	{
		if(connexion == null)
		{
			return null;
		}
		Statement stat = connexion.createStatement();
		ResultSet rs = stat.executeQuery("SELECT duree, taille, nom, nomcat FROM enregistrements JOIN categorie USING idcat;");
		//stat.close(); --> à vérifier si ça ne ferme pas la connexion à la base
		if(rs.next())
		{
			return rs;
		}
		return null;
	}
	public ResultSet getListeEnregistrement(int idCat)
	{
		if(connexion == null)
		{
			return null;
		}
		PreparedStatement ps = connexion.prepareStatement("SELECT duree, taille, nom, nomcat FROM enregistrements JOIN categorie USING idcat WHERE idcat=?");
		ps.setString(1, idCat);
		ResultSet rs = ps.executeQuery();
		if(rs.next())
		{
			return rs;
		}
		return null;
	}
	public int getNombreEnregistrement()
	{
		if(connexion == null)
		{
			return null;
		}
		Statement stat = connexion.createStatement();
		ResultSet rs = stat.executeQuery("SELECT count(1) FROM enregistrements;");
		//stat.close(); --> à vérifier si ça ne ferme pas la connexion à la base
		return rs.getInt(0); //verifier le bon numero de colonne
	}
	public void ajouterEnregistrement(String nom, int duree, int idCat, byte[] enregistrement)
	{
		if(connexion == null)
		{
			return;
		}
		PreparedStatement ps = connexion.prepareStatement(
		"INSERT INTO enregistrements(enregistrement, duree, taille, nom, idCat) VALUES (?, ?, ?, ?, ?);");
		ps.setString(1, enregistrement);
		ps.setString(2, duree);
		ps.setString(3, enregistrement.length);
		ps.setString(4, nom);
		ps.setString(5, idCat);

		if(ps.executeUpdate() > 0)
		{
			connexion.commit();
		}
	}
	public void supprimerEnregistrement(int id)
	{
		if(connexion == null)
		{
			return;
		}
		PreparedStatement ps = connexion.prepareStatement("DELETE FROM enregistrements WHERE id=?");
		ps.setString(1, id);
		if(ps.executeUpdate() > 0)
		{
			connexion.commit();
		}
	}
	public void modifierEnregistrement(int id, String nom, int duree, byte[] enregistrement)
	{
		if(connexion == null)
		{
			return;
		}
		//pk pas de idCat ?
		PreparedStatement ps = connexion.prepareStatement(
		"UPDATE enregistrements SET enregistrement=?, nom=?, duree=?, taille=? WHERE id=?;");
		ps.setString(1, enregistrement);
		ps.setString(2, nom);
		ps.setString(3, duree);
		ps.setString(4, enregistrement.length);
		ps.setString(5, id);

		if(ps.executeUpdate() > 0)
		{
			connexion.commit();
		}
	}
	public byte[] recupererEnregistrement(int id)
	{
		if(connexion == null)
		{
			return null;
		}
		PreparedStatement ps = connexion.prepareStatement("SELECT enregistrement FROM enregistrements WHERE id=?");
		ps.setString(1, id);
		ResultSet rs = ps.executeQuery();
		if(rs.next())
		{
			return rs.getBytes("enregistrement");
		}
		return null;
	}
	public void ajouterCategorie(String nom)
	{
		if(connexion == null)
		{
			return;
		}
		PreparedStatement ps = connexion.prepareStatement("INSERT INTO categorie (nomCat) VALUES ('?')");
		ps.setString(1, nom);

		if(ps.executeUpdate() > 0)
		{
			connexion.commit();
		}
		
	}
	public ResultSet getListeCategorie()
	{
		if(connexion == null)
		{
			return null;
		}
		Statement stat = connexion.createStatement();
		ResultSet rs = stat.executeQuery("SELECT nomCat FROM categorie;");
		//stat.close(); --> à vérifier si ça ne ferme pas la connexion à la base
		if(rs.next())
		{
			return rs;
		}
		return null;
	}
	public void supprimerCategorie(int id)//comment on fait pour les enregistrements de cette caté ?
	{
		if(connexion == null)
		{
			return;
		}
		PreparedStatement ps = connexion.prepareStatement("DELETE FROM categorie WHERE idCat=?");
		ps.setString(1, id);
		if(ps.executeUpdate() > 0)
		{
			connexion.commit();
		}
	}
	public void modifierCategorie(int id, String nom)
	{
		if(connexion == null)
		{
			return;
		}
		PreparedStatement ps = connexion.prepareStatement("UPDATE categorie SET nomCat='?' WHERE idCat=?");
		ps.setString(1, nom);
		ps.setString(2, id);
		if(ps.executeUpdate() > 0)
		{
			connexion.commit();
		}
	}
	private void createDatabase() //TODO
	{
		Statement stat = connexion.createStatement();
		stat.executeUpdate("DROP TABLE if exists test;");
		stat.executeUpdate("CREATE TABLE test (id  INTEGER PRIMARY KEY AUTOINCREMENT, file_path VARCHAR2(4096) NOT NULL, file BLOB, timestamp DATE NOT NULL);");
	}
}
