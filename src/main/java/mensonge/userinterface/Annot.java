package mensonge.userinterface;
public class Annot
{
	private long debut=1;
	private long fin=1;
	private String annotation;
	public Annot()
	{

	}
	public long getDebut()
	{
		return debut;
	}
	public long getFin()
	{
		return fin;
	}
	public String getAnnotation()
	{
		return annotation;
	}
	public void setDebut(long _debut)
	{
		debut=_debut;
	}
	public void setFin(long _fin)
	{
		fin=_fin;
	}
	public void setAnnotation(String _annotation)
	{
		annotation=_annotation;
	}
	public String toString()
	{
		return "";
	}
}
