package mensonge.core.BaseDeDonnees;

import java.sql.SQLException;

public interface IBaseDeDonnees
{
	void modifierEnregistrement(int id, String nom, int duree, byte[] enregistrement, int idCat, int idSuj) throws DBException;
	String getNomEnregistrement(int id) throws DBException;
}
