package mensonge.core.tools;

/**
 * Interface pour observer la BDD des mises à jour
 *
 */
public interface DataBaseObserver extends IObserver
{
	/**
	 * Appelé quand la BDD a été mise à jour
	 */
	void onUpdateDataBase();
}
