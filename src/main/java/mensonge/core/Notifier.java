package mensonge.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Notifier
{

	static private HashMap<String, Method> methods = new HashMap<String, Method>();

	static
	{
		List<Class<? extends IObserver>> interfaces = new ArrayList<Class<? extends IObserver>>();
		interfaces.add(DataBaseObserver.class);
		for (Class<?> i : interfaces)
		{
			Method[] ms = i.getDeclaredMethods();
			for (Method m : ms)
			{
				methods.put(m.getName(), m);
			}
		}
	}

	static public void call(Set<IObserver> observers, String name, Object... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Method method = methods.get(name);
		callMethodOnObservers(observers, method, args);
	}

	static private void callMethodOnObservers(Set<IObserver> observers, Method m, Object... arguments) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		for (IObserver o : observers)
		{
			m.invoke(o, arguments);
		}
	}

}