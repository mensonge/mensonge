package mensonge.core.BaseDeDonnees;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.awt.image.BadDepthException;

import mensonge.core.tools.DataBaseObservable;
import mensonge.core.tools.IObserver;

public class BaseDeDonneesControlleur implements IBaseDeDonnees
{
	/**
	 * Le logger
	 */
	private static Logger logger = Logger.getLogger("BDD");
	
	private BaseDeDonneesModele bdd;
	
	public BaseDeDonneesControlleur(String cheminFichier) throws DBException
	{
		File fichier = new File(cheminFichier);
		
		this.bdd = new BaseDeDonneesModele(cheminFichier);
		if( ! fichier.exists())
		{
			try
			{
				this.bdd.createDatabase();
			}
			catch (SQLException e)
			{
				throw new DBException("Erreur lors de la creation de la base", e);
			}
		}
	}
	
	private void baseDisponible() throws DBException
	{
		if(this.bdd == null)
		{
			throw new DBException("La base de données est indisponible");
		}
		else if(this.bdd.getConnexion() == null)
		{
			throw new DBException("La base de données est indisponible");
		}
	}
	
	public boolean connexion() throws DBException
	{
		try
		{
			this.bdd.connexion();
		}
		catch (SQLException e)
		{
			throw new DBException("Erreur lors de l'initialisation de la connexion", e);
		}
		catch (ClassNotFoundException e)
		{
			throw new DBException("Impossible de trouver le pilote pour la base de données ", e);
		}
		try
		{
			this.bdd.verifierBase();
		}
		catch (SQLException e)
		{
			throw new DBException("Problème dans la structure de la base", e);
		}
		return true;
	}
	
	public boolean deconnexion()
	{
		try
		{
			this.bdd.deconnexion();
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
		}
		this.bdd.setConnexion(null);
		return true;
	}
	
	public List<LigneEnregistrement> getListeEnregistrement() throws DBException
	{
		this.baseDisponible();
		List<LigneEnregistrement> retour = null;
		try
		{
			retour = this.bdd.getListeEnregistrement();
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation des enregistrements", e);
		}
		if(retour == null)
		{
			throw new DBException("Erreur lors de la recuperation des enregistrements");
		}
		return retour;
	}
	
	public List<LigneEnregistrement> getListeEnregistrementCategorie(final int idCat) throws DBException
	{
		this.baseDisponible();
		List<LigneEnregistrement> retour = null;
		try
		{
			if(this.bdd.categorieExiste(idCat))
			{
				retour = this.bdd.getListeEnregistrementCategorie(idCat);
			}
			else
			{
				throw new DBException("Categorie inexistante");
			}
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation de la liste des enregistrements", e);
		}
		if(retour == null)
		{
			throw new DBException("Erreur lors de la recuperation des enregistrements");
		}
		return retour;
	}
	
	public List<LigneEnregistrement> getListeEnregistrementSujet(final int idSuj) throws DBException
	{
		this.baseDisponible();
		List<LigneEnregistrement> retour = null;
		try
		{
			if(this.bdd.sujetExiste(idSuj))
			{
				retour = this.bdd.getListeEnregistrementSujet(idSuj);
			}
			else
			{
				throw new DBException("Sujet inexistante");
			}
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation de la liste des enregistrements", e);
		}
		if(retour == null)
		{
			throw new DBException("Erreur lors de la recuperation des enregistrements");
		}
		return retour;
	}

	public int getNombreEnregistrement() throws DBException
	{
		this.baseDisponible();
		int retour = 0;
		
		try
		{
			retour = this.bdd.getNombreEnregistrement();
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation du nombre enregistrements", e);
		}
		return retour;
	}
	
	public void compacter() throws DBException
	{
		this.baseDisponible();
		this.bdd.compacter();
	}
	
	public void ajouterEnregistrement(final String nom, final int duree, final int idCat, final byte[] enregistrement,
			final int idSuj) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.categorieExiste(idCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Categorie inexistante");
				throw new DBException("Categorie inexistante");
			}
			if( ! this.bdd.sujetExiste(idSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet inexistant");
				throw new DBException("Sujet inexistant");
			}
			if (this.bdd.enregistrementExist(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Nom enregistrement existant");
				throw new DBException("Nom enregistrement existant");
			}
			this.bdd.ajouterEnregistrement(nom, duree, idCat, enregistrement, idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrements", e);
		}
	}
	
	public void supprimerEnregistrement(final int id) throws DBException
	{
		this.baseDisponible();
		try
		{
			if(this.bdd.enregistrementExist(id))
			{
				this.bdd.supprimerEnregistrement(id);
			}
			else
			{
				throw new DBException("Enregistrement inexistant");
			}
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la suppression d'un enregistrements", e);
		}
	}
	
	public void modifierEnregistrement(final int id, final String nom, final int duree, final byte[] enregistrement,
			final int idCat, final int idSuj) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.categorieExiste(idCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Categorie inexistante");
				throw new DBException("Categorie inexistante");
			}
			if( ! this.bdd.sujetExiste(idSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet inexistant");
				throw new DBException("Sujet inexistant");
			}
			if (this.bdd.enregistrementExist(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Nom enregistrement existant");
				throw new DBException("Nom enregistrement existant");
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			this.bdd.modifierEnregistrement(id, nom, duree, enregistrement, idCat, idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrements", e);
		}
	}
	
	
	public void modifierEnregistrement(final int id, final String nom, final int duree, final int taille,
			final int idCat, final int idSuj) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.categorieExiste(idCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Categorie inexistante");
				throw new DBException("Categorie inexistante");
			}
			if( ! this.bdd.sujetExiste(idSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet inexistant");
				throw new DBException("Sujet inexistant");
			}
			if (this.bdd.enregistrementExist(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Nom enregistrement existant");
				throw new DBException("Nom enregistrement existant");
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			this.bdd.modifierEnregistrement(id, nom, duree, taille, idCat, idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrements", e);
		}
	}
	
	public void modifierEnregistrementNom(final int id, final String nom) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if (this.bdd.enregistrementExist(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Nom enregistrement existant");
				throw new DBException("Nom enregistrement existant");
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			this.bdd.modifierEnregistrementNom(id, nom);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrements", e);
		}
	}
	
	public void modifierEnregistrementDuree(final int id, final int duree) throws DBException
	{
		this.baseDisponible();
		if(duree < 0)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Durée invalide, elle doit être superieur à 0");
			throw new DBException("Durée invalide, elle doit être superieur à 0");
		}
		try
		{
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			this.bdd.modifierEnregistrementDuree(id, duree);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrements", e);
		}
	}
	
	public void modifierEnregistrementTaille(final int id, final int taille) throws DBException
	{
		this.baseDisponible();
		if(taille < 0)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Taille invalide, elle doit être superieur à 0");
			throw new DBException("Taille invalide, elle doit être superieur à 0");
		}
		try
		{
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			this.bdd.modifierEnregistrementTaille(id, taille);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrements", e);
		}
	}
	
	public void modifierEnregistrementCategorie(final int id, final int idCat) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.categorieExiste(idCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Categorie inexistante");
				throw new DBException("Categorie inexistante");
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			this.bdd.modifierEnregistrementCategorie(id, idCat);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrements", e);
		}
	}
	
	public void modifierEnregistrementCategorie(final int id, final String nomCat) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.categorieExiste(nomCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Categorie inexistante");
				throw new DBException("Categorie inexistante");
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			this.bdd.modifierEnregistrementCategorie(id, nomCat);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrements", e);
		}
	}
	
	public void modifierEnregistrementSujet(final int id, final int idSuj) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.sujetExiste(idSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet inexistante");
				throw new DBException("Sujet inexistante");
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			this.bdd.modifierEnregistrementSujet(id, idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrements", e);
		}
	}
	
	public void modifierEnregistrementSujet(final int id, final String nomSuj) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.sujetExiste(nomSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet inexistante");
				throw new DBException("Sujet inexistante");
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			this.bdd.modifierEnregistrementSujet(id, nomSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification d'un enregistrementse", e);
		}
	}
	
	public byte[] recupererEnregistrement(final int id) throws DBException
	{
		this.baseDisponible();
		byte[] retour = null;
		try
		{
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
				throw new DBException("Enregistrement id inexistant");
			}
			retour = this.bdd.recupererEnregistrement(id);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation du contenu", e);
		}
		if(retour == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation du contenu");
			throw new DBException("Erreur lors de la recuperation du contenu");
		}
		return retour;
	}
	
	public String getNomEnregistrement(final int id) throws DBException
	{
		this.baseDisponible();
		String retour = null;
		
		try
		{
			retour = this.bdd.getNomEnregistrement(id);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation du nom");
			throw new DBException("Erreur lors de la recuperation du nom", e);
		}
		if(retour == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Enregistrement id inexistant");
			throw new DBException("Enregistrement id inexistant");
		}
		return retour;
	}
	
	public void ajouterCategorie(final String nom) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if(this.bdd.categorieExiste(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Categorie existante");
				throw new DBException("Categorie existante");
			}
			this.bdd.ajouterCategorie(nom);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de l'ajout de categorie", e);
		}
	}
	
	public List<LigneEnregistrement> getListeCategorie() throws DBException
	{
		this.baseDisponible();
		List<LigneEnregistrement> retour = null;
		
		try
		{
			retour = this.bdd.getListeCategorie();
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation des categories", e);
		}
		if(retour == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation des categories");
			throw new DBException("Erreur lors de la recuperation des categories");
		}
		return retour;
	}
	
	public void supprimerCategorie(final int id) throws DBException
	{
		this.baseDisponible();
		try
		{
			this.bdd.supprimerCategorie(id);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la suppression de la categories: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de la suppression de la categories", e);
		}
	}
	
	public void modifierCategorie(final int id, final String nom) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if(this.bdd.categorieExiste(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Categorie existante");
				throw new DBException("Categorie existante");
			}
			this.bdd.modifierCategorie(id, nom);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la modification de la categories: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification de la categories", e);
		}
	}
	
	public String getCategorie(final int idCat) throws DBException
	{
		this.baseDisponible();
		String retour = null;
		try
		{
			if( ! this.bdd.categorieExiste(idCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Categorie inexistante");
				throw new DBException("Categorie inexistante");
			}
			retour = this.bdd.getCategorie(idCat);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation de la categories: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation de la categories", e);
		}
		if(retour == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation de la categories");
			throw new DBException("Erreur lors de la recuperation de la categories");
		}
		return retour;
	}
	
	public int getCategorie(final String nomCat) throws DBException
	{
		this.baseDisponible();
		int retour = -1;
		try
		{
			if( ! this.bdd.categorieExiste(nomCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Categorie inexistante");
				throw new DBException("Categorie inexistante");
			}
			retour = this.bdd.getCategorie(nomCat);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation de la categories: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation de la categories", e);
		}
		if(retour == -1)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation de la categories");
			throw new DBException("Erreur lors de la recuperation de la categories");
		}
		return retour;
	}
	
	public void ajouterSujet(final String nom) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if(this.bdd.sujetExiste(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet existant");
				throw new DBException("Sujete existant");
			}
			this.bdd.ajouterSujet(nom);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de l'ajout de sujet", e);
		}
	}
	
	public void supprimerSujet(final int id) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.sujetExiste(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet inexistant");
				throw new DBException("Sujete inexistant");
			}
			this.bdd.supprimerSujet(id);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la suppression du sujet", e);
		}
	}
	
	public List<LigneEnregistrement> getListeSujet() throws DBException
	{
		this.baseDisponible();
		List<LigneEnregistrement> retour = null;
		
		try
		{
			retour = this.bdd.getListeSujet();
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation des sujet", e);
		}
		if(retour == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation des categories");
			throw new DBException("Erreur lors de la recuperation des sujet");
		}
		return retour;
	}
	
	public void modifierSujet(final int id, final String nom) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if(this.bdd.sujetExiste(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet existant");
				throw new DBException("Sujet existant");
			}
			this.bdd.modifierSujet(id, nom);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la modification du sujet: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de la modification du sujet", e);
		}
	}
	
	public String getSujet(final int idSuj) throws DBException
	{
		this.baseDisponible();
		String retour = null;
		try
		{
			if( ! this.bdd.sujetExiste(idSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet inexistant");
				throw new DBException("Sujet inexistant");
			}
			retour = this.bdd.getSujet(idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation du sujet: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation du sujet", e);
		}
		if(retour == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation du sujet");
			throw new DBException("Erreur lors de la recuperation du sujet");
		}
		return retour;
	}
	
	public int getSujet(final String nomSuj) throws DBException
	{
		this.baseDisponible();
		int retour = -1;
		try
		{
			if( ! this.bdd.sujetExiste(nomSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Sujet inexistant");
				throw new DBException("Sujet inexistant");
			}
			retour = this.bdd.getSujet(nomSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation du sujet: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de la recuperation du sujet", e);
		}
		if(retour == -1)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation du sujet");
			throw new DBException("Erreur lors de la recuperation du sujet");
		}
		return retour;
	}
	
	public void createDatabase() throws DBException
	{
		this.baseDisponible();
		
		try
		{
			this.bdd.createDatabase();
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la creation de la base");
			throw new DBException("Erreur lors de la creation de la base", e);
		}
	}
	
	public void importer(final String cheminFichier) throws DBException
	{
		List<LigneEnregistrement> listeSujet = null;
		List<LigneEnregistrement> listeCategorie = null;
		List<LigneEnregistrement> listeEnregistrement = null;
		
		BaseDeDonneesModele baseImporte = new BaseDeDonneesModele(cheminFichier);

		try
		{
			baseImporte.connexion();
		}
		catch (ClassNotFoundException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la connexion: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de la creation de la connexion", e);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la creation de la connexion: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de la creation de la connexion", e);
		}
		
		//On calcule les categories
		try
		{
			listeSujet = baseImporte.getListeSujet();
			for (LigneEnregistrement sujet : listeSujet)
			{
				while (this.bdd.sujetExiste(sujet.getNomSuj()))
				{
					sujet.setNomSuj(sujet.getNomSuj() + ".new");
				}
			}
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de l'importation sujet: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de l'importation sujet", e);
		}
		
		//On calcule les sujets
		try
		{
			listeCategorie = baseImporte.getListeCategorie();
			for (LigneEnregistrement categorie : listeCategorie)
			{
				while (this.bdd.categorieExiste(categorie.getNomCat()))
				{
					categorie.setNomCat(categorie.getNomCat() + ".new");
				}
			}
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de l'importation categorie: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de l'importation categorie", e);
		}
		
		//On calcule les enregistrements
		try
		{
			listeEnregistrement = baseImporte.getListeEnregistrement();
			for (LigneEnregistrement enregistrement : listeEnregistrement)
			{
				while (this.bdd.enregistrementExist(enregistrement.getNom()))
				{
					enregistrement.setNom(enregistrement.getNom() + ".new");
				}
			}
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de l'importation categorie: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de l'importation categorie", e);
		}
		
		if(listeCategorie != null && listeEnregistrement != null && listeSujet != null)
		{
			try
			{
				this.bdd.importer(listeCategorie, listeSujet, listeEnregistrement, baseImporte);
			}
			catch (SQLException e)
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de l'importation des données: " + e.getLocalizedMessage());
				throw new DBException("Erreur lors de l'importation des données", e);
			}
		}
	}
	
	public void exporterBase(final String cheminFichier) throws DBException
	{
		this.baseDisponible();
		String fileName = this.bdd.getFileName();
		File src = new File(fileName);
		File dest = new File(cheminFichier);
		
		if (!src.exists())// Verifie si le fichier existe bien
		{
			throw new DBException("Le fichier de base de données (" + fileName + ") n'existe pas.");
		}
		if (dest.exists())// verifie que la destination n'existe pas, auquel cas, on la supprime
		{
			dest.delete();
		}
		try
		{
			dest.createNewFile();// Création du nouveau fichier
		}
		catch (IOException e)
		{
			throw new DBException("Impossible de créer le fichier de sortie", e);
		}
		this.bdd.exporterBase(cheminFichier);
	}
	
	public void exporterEnregistrement(final String cheminFichier, final int id) throws DBException
	{
		this.baseDisponible();
		File dest = new File(cheminFichier);
		if (dest.exists())// verifie que la destination n'existe pas, auquel cas, on la supprime
		{
			dest.delete();
		}
		try
		{
			dest.createNewFile();// Création du nouveau fichier
		}
		catch (IOException e)
		{
			throw new DBException("Impossible de créer le fichier de sortie ", e);
		}
		
		try
		{
			this.bdd.exporterEnregistrement(cheminFichier, id);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de l'exportation de l'enregistrement: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors de l'exportation de l'enregistrement", e);
		}
	}
	
	public void addObserver(IObserver o)
	{
		this.bdd.addObserver(o);
	}
	
	 public void removeObserver(IObserver o)
	 {
		 this.bdd.removeObserver(o);
	 }
	 
	 public boolean enregistrementExist(final String nom) throws DBException
	 {
		 this.baseDisponible();
		 boolean retour = false;
		 
		 try
		{
			retour = this.bdd.enregistrementExist(nom);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors du test d'existance: " + e.getLocalizedMessage());
			throw new DBException("Erreur lors du test d'existance", e);
		}
		 
		 return retour;
	 }
	 
}
