package mensonge.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Notifier
{

	private static final Map<String, Method> methods = new HashMap<String, Method>();

	private Notifier()
	{
		// Permet d'empêcher l'instanciation de cette classe Utilitaire
	}

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

	public static void call(Set<IObserver> observers, String name, Object... args) throws IllegalAccessException,
			InvocationTargetException
	{
		Method method = methods.get(name);
		callMethodOnObservers(observers, method, args);
	}

	private static void callMethodOnObservers(Set<IObserver> observers, Method m, Object... arguments)
			throws IllegalAccessException, InvocationTargetException
	{
		for (IObserver o : observers)
		{
			m.invoke(o, arguments);
		}
	}

}