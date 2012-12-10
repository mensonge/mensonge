package mensonge.core;

/**
 * Interface permettant à une classe d'être observable et donc d'ajouter/supprimer des observers
 * 
 */
public interface Observable
{
	/**
	 * Ajoute un observer à l'observable
	 * 
	 * @param observer
	 *            Observer à ajouter
	 */
	void addObserver(IObserver observer);

	/**
	 * Supprimer un observer à l'observable
	 * 
	 * @param observer
	 *            Observer à supprimer
	 */
	void removeObserver(IObserver observer);
}
