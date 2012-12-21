package mensonge.core.BaseDeDonnees;

/**
 * Interface nécessaire pour les plugins notamment pour qu'ils connaissent les méthodes qu'ils auront accès pour traiter
 * avec la base de données
 */
public interface IBaseDeDonnees
{
	void modifierEnregistrement(int id, String nom, int duree, byte[] enregistrement, int idCat, int idSuj)
			throws DBException;

	String getNomEnregistrement(int id) throws DBException;
}
