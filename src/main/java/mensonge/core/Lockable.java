package mensonge.core;

/**
 * Interface définissant un composant où le rafraichissement peut être bloqué à des fins d'optimisation
 * 
 */
public interface Lockable
{
	/**
	 * a pour objectif de bloquer le rafraichissement
	 */
	void lockUpdate();

	/**
	 * a pour objectif de debloquer le rafraichissement
	 */
	void unlockUpdate();
}
