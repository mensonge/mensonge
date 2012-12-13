package mensonge.core;

public interface ActionMessageObserver extends IObserver
{
	void onInProgressAction(String message);
	void onCompletedAction(String message);
}
