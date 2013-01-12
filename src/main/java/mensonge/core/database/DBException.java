package mensonge.core.database;

/**
 * Classe correspondant au exception renvoyées par l'objet BaseDeDonnees
 * 
 * @author Azazel
 * 
 */
public class DBException extends Exception
{
	private static final long serialVersionUID = 1L;
	/**
	 * Code designant le type d'erreur 0 = Aucune erreur 1 = Erreur lors de la connexion 2 = Probléme de structure de la
	 * base 3 = Erreur lors de la verification de la structure de la base 4 = Probleme de deconnexion
	 */
	private int code = 0;

	public DBException(String msg, int errCode)
	{
		super(msg);
		code = errCode;
	}

	/**
	 * Constructeur par defaut
	 * @param msg le message de l'erreur
	 */
	public DBException(String msg)
	{
		super(msg);
	}
	
	/**
	 * Constructeur permettant de recuperer un throwable
	 * @param msg le message de l'erreur
	 */
	public DBException(String msg, Throwable thr)
	{
		super(msg, thr);
	}
	/**
	 * Le getter du code d'erreur
	 * @return le code d'erreur
	 */
	public int getCode()
	{
		return code;
	}

	/**
	 * Le setter du code d'erreur
	 * @param code le nouveau code d'erreur
	 */
	public void setCode(int code)
	{
		this.code = code;
	}
}
