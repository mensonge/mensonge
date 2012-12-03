package mensonge.core;

public interface Observable
{
	void addObserver(IObserver o);
	void removeObserver(IObserver o);
}
