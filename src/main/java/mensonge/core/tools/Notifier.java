package mensonge.core.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Classe gérant la notification des observeurs d'un observable
 */
public final class Notifier
{

	private static final Map<String, Method> METHODS = new HashMap<String, Method>();

	/**
	 * Permet d'empêcher l'instanciation de cette classe Utilitaire
	 */
	private Notifier()
	{
	}

	static
	{
		List<Class<? extends IObserver>> interfaces = new ArrayList<Class<? extends IObserver>>();
		interfaces.add(DataBaseObserver.class);
		interfaces.add(ActionMessageObserver.class);
		interfaces.add(CacheObserver.class);
		for (Class<?> i : interfaces)
		{
			Method[] ms = i.getDeclaredMethods();
			for (Method m : ms)
			{
				METHODS.put(m.getName(), m);
			}
		}
	}

	/**
	 * Notifie les observers en invoquant la méthode
	 * 
	 * @param observers
	 *            Liste d'observeurs
	 * @param name
	 *            Nom de la méthode à invoquer
	 * @param args
	 *            Arguments à donner à la méthode invoquée
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void call(Set<IObserver> observers, String name, Object... args) throws IllegalAccessException,
			InvocationTargetException
	{
		Method method = METHODS.get(name);
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