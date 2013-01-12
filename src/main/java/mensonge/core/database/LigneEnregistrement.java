package mensonge.core.database;

/**
 * Classe represantant une ligne de la table
 * @author Gwen
 *
 */
public class LigneEnregistrement
{
	private int idCat;
	private int idSuj;
	private int id;
	private int duree;
	private int taille;
	private String nomCat, nomSuj, nom;
	
	/**
	 * Constructeur par défaut
	 */
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
	
	/**
	 * Constructeur avec les paramettre
	 * @param id
	 * @param nom
	 * @param duree
	 * @param taille
	 * @param idCat
	 * @param nomCat
	 * @param idSuj
	 * @param nomSuj
	 */
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

	/**
	 * Getter idcat
	 * @return idcat
	 */
	public int getIdCat()
	{
		return idCat;
	}

	/**
	 * seter idcat
	 * @param idCat nouvelle idcat
	 */
	public void setIdCat(int idCat)
	{
		this.idCat = idCat;
	}

	/**
	 * getter idsuj
	 * @return retourne idsuj
	 */
	public int getIdSuj()
	{
		return idSuj;
	}

	/**
	 * setter idsuj
	 * @param idSuj nouveau idsuj
	 */
	public void setIdSuj(int idSuj)
	{
		this.idSuj = idSuj;
	}

	/**
	 * getter id
	 * @return retourn l'id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Setter id
	 * @param id nouvelle id
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * getter de la duree
	 * @return la duree
	 */
	public int getDuree()
	{
		return duree;
	}

	/**
	 * setter duree
	 * @param duree nouvelle duree
	 */
	public void setDuree(int duree)
	{
		this.duree = duree;
	}

	/**
	 * getter taille
	 * @return nouvelle taille
	 */
	public int getTaille()
	{
		return taille;
	}

	/**
	 * setter de la taille
	 * @param taille nouvelle taille
	 */
	public void setTaille(int taille)
	{
		this.taille = taille;
	}

	/**
	 * getter nomcat
	 * @return nomcat
	 */
	public String getNomCat()
	{
		return nomCat;
	}

	/**
	 * setter nomcat
	 * @param nomCat nouveau nomCat
	 */
	public void setNomCat(String nomCat)
	{
		this.nomCat = nomCat;
	}

	/**
	 * getter nomsuj
	 * @return nomsuj
	 */
	public String getNomSuj()
	{
		return nomSuj;
	}

	/**
	 * setter nomsuj
	 * @param nomSuj nouveau nomsuj
	 */
	public void setNomSuj(String nomSuj)
	{
		this.nomSuj = nomSuj;
	}

	/**
	 * getter nom
	 * @return le nom
	 */
	public String getNom()
	{
		return nom;
	}

	/**
	 * setter du nom
	 * @param nom nouveau nom
	 * 
	 */
	public void setNom(String nom)
	{
		this.nom = nom;
	}
	
}
