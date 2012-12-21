package mensonge.core.tools;

/**
 * Interface définissant les observeurs des états des actions en cours d'un observable
 * 
 */
public interface ActionMessageObserver extends IObserver
{
	/**
	 * Méthode appelée pour définir une action en cours
	 * 
	 * @param message
	 *            Message définissant l'action en cours
	 */
	void onInProgressAction(String message);

	/**
	 * Méthode appelée pour définir une action terminée
	 * 
	 * @param message
	 *            Message définissant la fin de l'action
	 */
	void onCompletedAction(String message);
}
