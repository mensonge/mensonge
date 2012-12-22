package mensonge.core.BaseDeDonnees;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mensonge.core.tools.IObserver;

public class BaseDeDonneesControlleur implements IBaseDeDonnees
{
	/**
	 * Le logger
	 */
	private static Logger logger = Logger.getLogger("BDD");
	
	private static String SUJET_EXISTANT = "Sujet existant";
	private static String SUJET_INEXISTANT = "Sujet inexistant";
	private static String CATEGORIE_EXISTANTE = "Categorie existante";
	private static String CATEGORIE_INEXISTANTE = "Categorie inexistante";
	private static String ENREGISTREMENT_INEXISTANT = "Enregistrement inexistant";
	private static String ENREGISTREMENT_EXISTANT = "Enregistrement existant";
	private static String ENREGISTREMENT_ID_INEXISTANT = "Enregistrement id inexistant";
	private static String BASE_INDISPONIBLE = "La base de données est indisponible";
	private static String ERREUR_MODIFICATION_ENREGISTREMENT = "Erreur lors de la modification de l'enregistrement";
	private static String ERREUR_RECUPERATION_CATEGORIE = "Erreur lors de la recuperation de categorie";
	private static String ERREUR_RECUPERATION_SUJET = "Erreur lors de la recuperation de sujet";
	private static String DUREE_INVALIDE = "Durée invalide, elle doit être superieur à 0"; 
	private static String TAILLE_INVALIDE = "Taille invalide, elle doit être superieur à 0";
	private static String NOM_VIDE = "Le nom est vide";
	private static String CONTENU_VIDE = "Le contenu est vide";
	
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
			throw new DBException(BASE_INDISPONIBLE);
		}
		else if(this.bdd.getConnexion() == null)
		{
			throw new DBException(BASE_INDISPONIBLE);
		}
	}

	public boolean connexion() throws DBException
	{
		boolean retour = true;;
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
			retour = false;
		}
		if( ! retour)
		{
			try
			{
				this.bdd.createDatabase();
				retour = true;
			}
			catch (SQLException e)
			{
				throw new DBException("Erreur lors de la creation de la base", e);
			}
		}
		return retour;
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
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_INEXISTANTE);
				throw new DBException(CATEGORIE_INEXISTANTE);
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
				throw new DBException(SUJET_INEXISTANT);
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
		if(duree < 0)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, DUREE_INVALIDE);
			throw new DBException(DUREE_INVALIDE);
		}
		if(nom == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, NOM_VIDE);
			throw new DBException(NOM_VIDE);
		}
		if(enregistrement == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, CONTENU_VIDE);
			throw new DBException(CONTENU_VIDE);
		}
		try
		{
			if( ! this.bdd.categorieExiste(idCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_INEXISTANTE);
				throw new DBException(CATEGORIE_INEXISTANTE);
			}
			if( ! this.bdd.sujetExiste(idSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_INEXISTANT);
				throw new DBException(SUJET_INEXISTANT);
			}
			if (this.bdd.enregistrementExist(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_EXISTANT);
				throw new DBException(ENREGISTREMENT_EXISTANT);
			}
			this.bdd.ajouterEnregistrement(nom, duree, idCat, enregistrement, idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException(ERREUR_MODIFICATION_ENREGISTREMENT, e);
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
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_INEXISTANT);
				throw new DBException(ENREGISTREMENT_INEXISTANT);
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
		
		if(duree < 0)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, DUREE_INVALIDE);
			throw new DBException(DUREE_INVALIDE);
		}
		if(nom == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, NOM_VIDE);
			throw new DBException(NOM_VIDE);
		}
		if(enregistrement == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, CONTENU_VIDE);
			throw new DBException(CONTENU_VIDE);
		}
		try
		{
			if( ! this.bdd.categorieExiste(idCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_INEXISTANTE);
				throw new DBException(CATEGORIE_INEXISTANTE);
			}
			if( ! this.bdd.sujetExiste(idSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_INEXISTANT);
				throw new DBException(SUJET_INEXISTANT);
			}
			if (this.bdd.enregistrementExist(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_EXISTANT);
				throw new DBException(ENREGISTREMENT_EXISTANT);
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
			}
			this.bdd.modifierEnregistrement(id, nom, duree, enregistrement, idCat, idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException(ERREUR_MODIFICATION_ENREGISTREMENT, e);
		}
	}
	
	
	public void modifierEnregistrement(final int id, final String nom, final int duree, final int taille,
			final int idCat, final int idSuj) throws DBException
	{
		this.baseDisponible();
		if(nom == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, NOM_VIDE);
			throw new DBException(NOM_VIDE);
		}
		if(duree < 0)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, DUREE_INVALIDE);
			throw new DBException(DUREE_INVALIDE);
		}
		
		if(taille < 0)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, TAILLE_INVALIDE);
			throw new DBException(TAILLE_INVALIDE);
		}
		
		try
		{
			if( ! this.bdd.categorieExiste(idCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_INEXISTANTE);
				throw new DBException(CATEGORIE_INEXISTANTE);
			}
			if( ! this.bdd.sujetExiste(idSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_INEXISTANT);
				throw new DBException(SUJET_INEXISTANT);
			}
			if (this.bdd.enregistrementExist(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_EXISTANT);
				throw new DBException(ENREGISTREMENT_EXISTANT);
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
			}
			this.bdd.modifierEnregistrement(id, nom, duree, taille, idCat, idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException(ERREUR_MODIFICATION_ENREGISTREMENT, e);
		}
	}
	
	public void modifierEnregistrementNom(final int id, final String nom) throws DBException
	{
		this.baseDisponible();
		if(nom == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, NOM_VIDE);
			throw new DBException(NOM_VIDE);
		}
		try
		{
			if (this.bdd.enregistrementExist(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_EXISTANT);
				throw new DBException(ENREGISTREMENT_EXISTANT);
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
			}
			this.bdd.modifierEnregistrementNom(id, nom);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException(ERREUR_MODIFICATION_ENREGISTREMENT, e);
		}
	}
	
	public void modifierEnregistrementDuree(final int id, final int duree) throws DBException
	{
		this.baseDisponible();
		if(duree < 0)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, DUREE_INVALIDE);
			throw new DBException(DUREE_INVALIDE);
		}
		try
		{
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
			}
			this.bdd.modifierEnregistrementDuree(id, duree);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException(ERREUR_MODIFICATION_ENREGISTREMENT, e);
		}
	}
	
	public void modifierEnregistrementTaille(final int id, final int taille) throws DBException
	{
		this.baseDisponible();
		if(taille < 0)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, TAILLE_INVALIDE);
			throw new DBException(TAILLE_INVALIDE);
		}
		try
		{
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
			}
			this.bdd.modifierEnregistrementTaille(id, taille);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException(ERREUR_MODIFICATION_ENREGISTREMENT, e);
		}
	}
	
	public void modifierEnregistrementCategorie(final int id, final int idCat) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.categorieExiste(idCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_INEXISTANTE);
				throw new DBException(CATEGORIE_INEXISTANTE);
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
			}
			this.bdd.modifierEnregistrementCategorie(id, idCat);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException(ERREUR_MODIFICATION_ENREGISTREMENT, e);
		}
	}
	
	public void modifierEnregistrementCategorie(final int id, final String nomCat) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.categorieExiste(nomCat))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_INEXISTANTE);
				throw new DBException(CATEGORIE_INEXISTANTE);
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
			}
			this.bdd.modifierEnregistrementCategorie(id, nomCat);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException(ERREUR_MODIFICATION_ENREGISTREMENT, e);
		}
	}
	
	public void modifierEnregistrementSujet(final int id, final int idSuj) throws DBException
	{
		this.baseDisponible();
		
		try
		{
			if( ! this.bdd.sujetExiste(idSuj))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_INEXISTANT);
				throw new DBException(SUJET_INEXISTANT);
			}
			if( ! this.bdd.enregistrementExist(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
			}
			this.bdd.modifierEnregistrementSujet(id, idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, e.getLocalizedMessage());
			throw new DBException(ERREUR_MODIFICATION_ENREGISTREMENT, e);
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
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
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
				BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
				throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
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
			BaseDeDonneesControlleur.logger.log(Level.WARNING, ENREGISTREMENT_ID_INEXISTANT);
			throw new DBException(ENREGISTREMENT_ID_INEXISTANT);
		}
		return retour;
	}
	
	public void ajouterCategorie(final String nom) throws DBException
	{
		this.baseDisponible();
		if(nom == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, NOM_VIDE);
			throw new DBException(NOM_VIDE);
		}
		try
		{
			if(this.bdd.categorieExiste(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_EXISTANTE);
				throw new DBException(CATEGORIE_EXISTANTE);
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
			throw new DBException(ERREUR_RECUPERATION_CATEGORIE, e);
		}
		if(retour == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, ERREUR_RECUPERATION_CATEGORIE);
			throw new DBException(ERREUR_RECUPERATION_CATEGORIE);
		}
		return retour;
	}
	
	public void supprimerCategorie(final int id) throws DBException
	{
		this.baseDisponible();
		try
		{
			if( ! this.bdd.categorieExiste(id))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_INEXISTANT);
				throw new DBException(SUJET_INEXISTANT);
			}
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
		if(nom == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, NOM_VIDE);
			throw new DBException(NOM_VIDE);
		}
		try
		{
			if(this.bdd.categorieExiste(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_EXISTANTE);
				throw new DBException(CATEGORIE_EXISTANTE);
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
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_INEXISTANTE);
				throw new DBException(CATEGORIE_INEXISTANTE);
			}
			retour = this.bdd.getCategorie(idCat);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation de la categories: " + e.getLocalizedMessage());
			throw new DBException(ERREUR_RECUPERATION_CATEGORIE, e);
		}
		if(retour == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, ERREUR_RECUPERATION_CATEGORIE);
			throw new DBException(ERREUR_RECUPERATION_CATEGORIE);
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
				BaseDeDonneesControlleur.logger.log(Level.WARNING, CATEGORIE_INEXISTANTE);
				throw new DBException(CATEGORIE_INEXISTANTE);
			}
			retour = this.bdd.getCategorie(nomCat);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation de la categories: " + e.getLocalizedMessage());
			throw new DBException(ERREUR_RECUPERATION_CATEGORIE, e);
		}
		if(retour == -1)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, ERREUR_RECUPERATION_CATEGORIE);
			throw new DBException(ERREUR_RECUPERATION_CATEGORIE);
		}
		return retour;
	}
	
	public void ajouterSujet(final String nom) throws DBException
	{
		this.baseDisponible();
		if(nom == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, NOM_VIDE);
			throw new DBException(NOM_VIDE);
		}
		try
		{
			if(this.bdd.sujetExiste(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_EXISTANT);
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
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_INEXISTANT);
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
			BaseDeDonneesControlleur.logger.log(Level.WARNING, ERREUR_RECUPERATION_CATEGORIE);
			throw new DBException("Erreur lors de la recuperation des sujet");
		}
		return retour;
	}
	
	public void modifierSujet(final int id, final String nom) throws DBException
	{
		this.baseDisponible();
		if(nom == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, NOM_VIDE);
			throw new DBException(NOM_VIDE);
		}
		try
		{
			if(this.bdd.sujetExiste(nom))
			{
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_EXISTANT);
				throw new DBException(SUJET_EXISTANT);
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
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_INEXISTANT);
				throw new DBException(SUJET_INEXISTANT);
			}
			retour = this.bdd.getSujet(idSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation du sujet: " + e.getLocalizedMessage());
			throw new DBException(ERREUR_RECUPERATION_SUJET, e);
		}
		if(retour == null)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, ERREUR_RECUPERATION_SUJET);
			throw new DBException(ERREUR_RECUPERATION_SUJET);
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
				BaseDeDonneesControlleur.logger.log(Level.WARNING, SUJET_INEXISTANT);
				throw new DBException(SUJET_INEXISTANT);
			}
			retour = this.bdd.getSujet(nomSuj);
		}
		catch (SQLException e)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, "Erreur lors de la recuperation du sujet: " + e.getLocalizedMessage());
			throw new DBException(ERREUR_RECUPERATION_SUJET, e);
		}
		if(retour == -1)
		{
			BaseDeDonneesControlleur.logger.log(Level.WARNING, ERREUR_RECUPERATION_SUJET);
			throw new DBException(ERREUR_RECUPERATION_SUJET);
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
