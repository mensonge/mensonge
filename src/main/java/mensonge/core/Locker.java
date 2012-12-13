package mensonge.core;

import java.util.LinkedList;
import java.util.List;

/**
 * Classe permettant de bloquer un liste de Blockable et de les debloquer
 * 
 * 
 */
public class Locker
{
	private List<Lockable> targets = new LinkedList<Lockable>();

	/**
	 * Ajouter un élément à la liste
	 * 
	 * @param target
	 */
	public void addTarget(Lockable target)
	{
		targets.add(target);
	}

	/**
	 * Bloque la liste des éléments
	 */
	public void lockUpdate()
	{
		for (Lockable aBloquer : targets)
		{
			aBloquer.lockUpdate();
		}
	}

	/**
	 * Debloque les éléments de la liste
	 */
	public void unlockUpdate()
	{
		for (Lockable aDebloquer : targets)
		{
			aDebloquer.unlockUpdate();
		}
	}

	/**
	 * Bloque la liste des éléments ciblés
	 */
	public void lockUpdate(String target)
	{
		for (Lockable aBloquer : targets)
		{
			if (aBloquer.getClass().getName().equals(target))
			{
				aBloquer.lockUpdate();
			}
		}
	}

	/**
	 * Debloque les éléments ciblés de la liste
	 */
	public void unlockUpdate(String target)
	{
		for (Lockable aDebloquer : targets)
		{
			if (aDebloquer.getClass().getName().equals(target))
			{
				aDebloquer.unlockUpdate();
			}
		}
	}
}
