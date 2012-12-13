package mensonge.core.tools;

/**
 * Interface pour observer la BDD des mises à jour
 *
 */
public interface DataBaseObserver extends ActionMessageObserver
{
	/**
	 * Appelé quand la BDD a été mise à jour
	 */
	void onUpdateDataBase();
}
