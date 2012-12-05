package mensonge.core.BaseDeDonnees;

public class LigneEnregistrement
{
	private int idCat;
	private int idSuj;
	private int id;
	private int duree;
	private int taille;
	private String nomCat, nomSuj, nom;
	
	public LigneEnregistrement()
	{
		this.id = 0;
		this.nom = null;
		this.duree = 0;
		this.taille = 0;
		this.idCat = 0;
		this.nomCat = null;
		this.idSuj = 0;
		this.nomSuj = null;
	}
	public LigneEnregistrement(int id, String nom, int duree, int taille, int idCat, String nomCat, int idSuj, String nomSuj)
	{
		this.id = id;
		this.nom = nom;
		this.duree = duree;
		this.taille = taille;
		this.idCat = idCat;
		this.nomCat = nomCat;
		this.idSuj = idSuj;
		this.nomSuj = nomSuj;
	}

	public int getIdCat()
	{
		return idCat;
	}

	public void setIdCat(int idCat)
	{
		this.idCat = idCat;
	}

	public int getIdSuj()
	{
		return idSuj;
	}

	public void setIdSuj(int idSuj)
	{
		this.idSuj = idSuj;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getDuree()
	{
		return duree;
	}

	public void setDuree(int duree)
	{
		this.duree = duree;
	}

	public int getTaille()
	{
		return taille;
	}

	public void setTaille(int taille)
	{
		this.taille = taille;
	}

	public String getNomCat()
	{
		return nomCat;
	}

	public void setNomCat(String nomCat)
	{
		this.nomCat = nomCat;
	}

	public String getNomSuj()
	{
		return nomSuj;
	}

	public void setNomSuj(String nomSuj)
	{
		this.nomSuj = nomSuj;
	}

	public String getNom()
	{
		return nom;
	}

	public void setNom(String nom)
	{
		this.nom = nom;
	}
	
}
