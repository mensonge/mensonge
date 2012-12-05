package mensonge.core.BaseDeDonnees;

import java.util.LinkedList;
import java.util.Map;
import java.awt.List;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultatSelect
{
	public static LinkedList<LigneEnregistrement> convertirResultatSet(ResultSet rs, LinkedList<String> colonne)
	{
		LinkedList<LigneEnregistrement> liste = new LinkedList<LigneEnregistrement>();
		LigneEnregistrement tmp = null;
		try
		{
			while(rs.next())
			{
				tmp = getLigne(rs, colonne);
				if(tmp != null)
				{
					liste.add(tmp);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return liste;
	}
	public static LigneEnregistrement getLigne(ResultSet rs, LinkedList<String> colonne)
	{
		LigneEnregistrement ligne = new LigneEnregistrement();
		for(String nomColonne : colonne)
		{
			remplirLigne(rs, nomColonne, ligne);
		}
		return ligne;
	}
	public static void remplirLigne(ResultSet rs, String colonne, LigneEnregistrement ligne)
	{
		try
		{
			if(colonne.equals("id"))
			{
				ligne.setId(rs.getInt(colonne));
			}
			else if(colonne.equals("nom"))
			{
				ligne.setNom(rs.getString(colonne));
			}
			else if(colonne.equals("duree"))
			{
				ligne.setDuree(rs.getInt(colonne));
			}
			else if(colonne.equals("idcat"))
			{
				ligne.setIdCat(rs.getInt(colonne));
			}
			else if(colonne.equals("nomcat"))
			{
				ligne.setNomCat(rs.getString(colonne));
			}
			else if(colonne.equals("idsuj"))
			{
				ligne.setIdSuj(rs.getInt(colonne));
			}
			else if(colonne.equals("nomsuj"))
			{
				ligne.setNomSuj(rs.getString(colonne));
			}
			else if(colonne.equals("taille"))
			{
				ligne.setTaille(rs.getInt(colonne));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
}
