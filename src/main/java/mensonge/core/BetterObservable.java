package mensonge.core;

import java.util.Collections;
import java.util.Observer;
import java.util.Set;
import java.util.WeakHashMap;

public class BetterObservable implements Observable
{

	private Set<Observer> observers = Collections.newSetFromMap(new WeakHashMap<Observer, Boolean>());

	private Object objectWhichNotify;

	public BetterObservable()
	{
		this.objectWhichNotify = this;
	}

	public BetterObservable(Object o)
	{
		this.objectWhichNotify = o;
	}

	public void addObserver(Observer o)
	{
		this.observers.add(o);
	}

	public void removeObserver(Observer o)
	{
		this.observers.remove(o);
	}

/*	private void callWithObservers(String name, Object... args)
	{
		try
		{
			Notifier.call(observers, name, args);
		}
		catch (NotificationException e)
		{
			e.printStackTrace();
		}
	}
*/
}
