package mensonge.core.tools;

public class DataBaseObservable extends BetterObservable implements ActionMessageObservable
{
	protected void notifyUpdateDataBase()
	{
		callWithObservers("onUpdateDataBase");
	}

	@Override
	public void notifyInProgressAction(String message)
	{
		callWithObservers("onInProgressAction", message);
	}

	@Override
	public void notifyCompletedAction(String message)
	{
		callWithObservers("onCompletedAction", message);
	}

	@Override
	public void notifyFailedAction(String message)
	{
		callWithObservers("onCompletedAction", message);
	}
}
