package mensonge.core;

import java.util.Observer;

public interface Observable
{
	void addObserver(Observer o);
	void removeObserver(Observer o);
}
