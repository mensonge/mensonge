package mensonge.core.tools;

public interface CacheObserver extends ActionMessageObserver
{
	void onUpdateCache(long newCacheSize);
}
