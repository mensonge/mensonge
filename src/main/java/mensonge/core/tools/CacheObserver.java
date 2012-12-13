package mensonge.core.tools;

public interface CacheObserver extends IObserver
{
	void onUpdateCache(long newCacheSize);
}
