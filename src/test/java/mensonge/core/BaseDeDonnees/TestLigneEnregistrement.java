package mensonge.core.BaseDeDonnees;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestLigneEnregistrement
{
	private static LigneEnregistrement ligne = null;
	
	@Test
	public void testGetter()
	{
		ligne = new LigneEnregistrement(1, "Rorqual", 7, 14, 21, "Baleine", 28, "Sauropode");
		
		assertEquals(ligne.getId(), 1);
		assertEquals(ligne.getNom(), "Rorqual");
		assertEquals(ligne.getDuree(), 7);
		assertEquals(ligne.getTaille(), 14);
		assertEquals(ligne.getIdCat(), 21);
		assertEquals(ligne.getNomCat(), "Baleine");
		assertEquals(ligne.getIdSuj(), 28);
		assertEquals(ligne.getNomSuj(), "Sauropode");
	}
	
	@Test
	public void testSetter()
	{
		ligne = new LigneEnregistrement();
		
		ligne.setId(1);
		ligne.setNom("Rorqual");
		ligne.setDuree(7);
		ligne.setTaille(14);
		ligne.setIdCat(21);
		ligne.setNomCat("Baleine");
		ligne.setIdSuj(28);
		ligne.setNomSuj("Sauropode");
		
		assertEquals(ligne.getId(), 1);
		assertEquals(ligne.getNom(), "Rorqual");
		assertEquals(ligne.getDuree(), 7);
		assertEquals(ligne.getTaille(), 14);
		assertEquals(ligne.getIdCat(), 21);
		assertEquals(ligne.getNomCat(), "Baleine");
		assertEquals(ligne.getIdSuj(), 28);
		assertEquals(ligne.getNomSuj(), "Sauropode");
	}
}
