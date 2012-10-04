package userinterface;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class Feuille extends DefaultMutableTreeNode
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int id;
	String nom;
	int duree;
	int taille;
	String categorie;
	
	public Feuille(int id, String nom, int duree, int taille, String categorie)
	{
		super(nom);
		this.id = id;
		this.duree = duree;
		this.taille = taille;
		this.nom = nom;
		this.categorie = categorie;
	}
	public Map<String, String> getInfo()
	{
		Map<String, String> retour = new HashMap<String, String>();
		retour.put("Nom", nom);
		retour.put("Duree", Integer.toString(duree) + " Seconde(s)");
		retour.put("Taille", Integer.toString(taille) + " Octet(s)");
		retour.put("Categorie", categorie);
		return retour;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public int getDuree() {
		return duree;
	}
	public void setDuree(int duree) {
		this.duree = duree;
	}
	public int getTaille() {
		return taille;
	}
	public void setTaille(int taille) {
		this.taille = taille;
	}
	public String getCategorie() {
		return categorie;
	}
	public void setCategorie(String categorie) {
		this.categorie = categorie;
	}

}
