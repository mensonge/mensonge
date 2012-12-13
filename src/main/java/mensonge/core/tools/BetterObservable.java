package mensonge.core.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BetterObservable implements Observable
{
	private static Logger logger = Logger.getLogger("Notifier");
	private Set<IObserver> observers = Collections.newSetFromMap(new WeakHashMap<IObserver, Boolean>());

	
	@Override
	public void addObserver(IObserver o)
	{
		this.observers.add(o);
	}

	@Override
	public void removeObserver(IObserver o)
	{
		this.observers.remove(o);
	}

	protected void callWithObservers(String name, Object... args)
	{
		try
		{
			Notifier.call(observers, name, args);
		}
		catch (IllegalArgumentException e)
		{
			logger.log(Level.WARNING, e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			logger.log(Level.WARNING, e.getMessage());
		}
		catch (InvocationTargetException e)
		{
			logger.log(Level.WARNING, e.getMessage());
		}
	}
}
