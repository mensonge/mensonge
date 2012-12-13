package mensonge.core.tools;

public interface ActionMessageObserver extends IObserver
{
	void onInProgressAction(String message);
	void onCompletedAction(String message);
}
