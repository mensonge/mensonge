package mensonge.core.tools;

/**
 * Interface définissant les méthodes des observables qui veulent notifier les actions qu'ils sont entrain d'effectuer.
 * Action en cours, action terminée, l'action a échouée,...
 * 
 */
interface ActionMessageObservable
{
	/**
	 * Méthode à appeler pour définir une action en cours
	 * 
	 * @param message
	 *            Message définissant l'action en cours
	 */
	void notifyInProgressAction(String message);

	/**
	 * Méthode à appeler pour spécifier une action terminée
	 * 
	 * @param message
	 *            Message définissant la fin de l'action
	 */
	void notifyCompletedAction(String message);

	/**
	 * Méthode à appeler pour spécifier qu'une action a échouée
	 * 
	 * @param message
	 *            Message définissant la raison
	 */
	void notifyFailedAction(String message);
}
