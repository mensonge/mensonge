package userinterface;

import javax.swing.tree.DefaultMutableTreeNode;

public class Feuille extends DefaultMutableTreeNode
{
	int id;
	String nom;
	int duree;
	int taille;
	
	public Feuille(int id, String nom, int duree, int taille)
	{
		super(nom);
		this.id = id;
		this.duree = duree;
		this.taille = taille;
		this.nom = nom;
	}
}
