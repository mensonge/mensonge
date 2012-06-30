package core.plugins;

public interface Plugin
{
	public void lancer();
	public void stopper();
	public String getNom();
	public boolean isActive();
}
