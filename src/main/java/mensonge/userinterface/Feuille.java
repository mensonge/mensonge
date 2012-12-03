package mensonge.userinterface;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mensonge.core.Utils;

public class Feuille extends DefaultMutableTreeNode
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int id;
	String nom;
	int duree;
	long taille;
	String categorie;
	String sujet;

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

	public Map<String, String> getInfo()
	{
		Map<String, String> retour = new HashMap<String, String>();
		retour.put("Nom", nom);
		retour.put("Duree", Integer.toString(duree) + " Seconde(s)");
		retour.put("Taille", Utils.humanReadableByteCount(taille, false));
		retour.put("Categorie", categorie);
		retour.put("Sujet", sujet);
		return retour;
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
