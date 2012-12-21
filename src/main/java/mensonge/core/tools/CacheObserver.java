package mensonge.core.tools;

/**
 * Défini un observeur pour le système de cache
 *
 */
public interface CacheObserver extends ActionMessageObserver
{
	/**
	 * Méthode appelée en cas de mise à jour du cache
	 * @param newCacheSize La nouvelle taille du cache
	 */
	void onUpdateCache(long newCacheSize);
}
