package mensonge.userinterface;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mensonge.core.database.LigneEnregistrement;
import mensonge.core.tools.Utils;

public class Feuille extends DefaultMutableTreeNode
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String nom;
	private int duree;
	private long taille;
	private String categorie;
	private String sujet;
	private int idCategorie;
	private int idSujet;

	public Feuille(int id, String nom, int duree, long taille, String categorie, String sujet)
	{
		super(nom);
		this.id = id;
		this.duree = duree;
		this.taille = taille;
		this.nom = nom;
		this.categorie = categorie;
		this.sujet = sujet;
	}


	public Feuille(LigneEnregistrement ligne)
	{
		super(ligne.getNom());
		this.id = ligne.getId();
		this.duree = ligne.getDuree();
		this.taille = ligne.getTaille();
		this.nom = ligne.getNom();
		this.categorie = ligne.getNomCat();
		this.sujet = ligne.getNomSuj();
		this.idCategorie = ligne.getIdCat();
		this.idSujet = ligne.getIdSuj();
	}

	public Map<String, String> getInfo()
	{
		Map<String, String> retour = new HashMap<String, String>();
		retour.put("Nom", nom);
		retour.put("Durée", Utils.getFormattedTimeS(duree));
		retour.put("Taille", Utils.humanReadableByteCount(taille, false));
		retour.put("Catégorie "+ idCategorie, categorie);
		retour.put("Sujet "+ idSujet, sujet);
		return retour;
	}
	
	public String getSujet()
	{
		return sujet;
	}


	public void setSujet(String sujet)
	{
		this.sujet = sujet;
	}


	public int getIdCategorie()
	{
		return idCategorie;
	}


	public void setIdCategorie(int idCategorie)
	{
		this.idCategorie = idCategorie;
	}


	public int getIdSujet()
	{
		return idSujet;
	}


	public void setIdSujet(int idSujet)
	{
		this.idSujet = idSujet;
	}


	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getNom()
	{
		return nom;
	}

	public void setNom(String nom)
	{
		this.nom = nom;
	}

	public int getDuree()
	{
		return duree;
	}

	public void setDuree(int duree)
	{
		this.duree = duree;
	}

	public long getTaille()
	{
		return taille;
	}

	public void setTaille(long taille)
	{
		this.taille = taille;
	}

	public String getCategorie()
	{
		return categorie;
	}

	public void setCategorie(String categorie)
	{
		this.categorie = categorie;
	}

}
