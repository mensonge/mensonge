package mensonge.core.BaseDeDonnees;

public interface IBaseDeDonnees
{
	void modifierEnregistrement(int id, String nom, int duree, byte[] enregistrement, int idCat, int idSuj) throws DBException;
	String getNomEnregistrement(int id) throws DBException;
}
