package mensonge.core.BaseDeDonnees;

import java.util.LinkedList;
import java.util.Map;
import java.awt.List;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultatSelect
{
	public static LinkedList<Object> convertirResultatSet(ResultSet rs, LinkedList<String> colonne)
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
		return null;
	}
	public static LigneEnregistrement getLigne(ResultSet rs, LinkedList<String> colonne)
	{
		LigneEnregistrement ligne = new LigneEnregistrement();
		
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
				
			}
			else if(colonne.equals("duree"))
			{
				
			}
			else if(colonne.equals(""))
			{
				
			}
			else if(colonne.equals("nom"))
			{
				
			}
			else if(colonne.equals("nom"))
			{
				
			}
			else if(colonne.equals("nom"))
			{
				
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
