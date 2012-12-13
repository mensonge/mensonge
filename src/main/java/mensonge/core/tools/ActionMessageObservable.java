package mensonge.core.tools;

interface ActionMessageObservable
{
	void notifyInProgressAction(String message);

	void notifyCompletedAction(String message);

	void notifyFailedAction(String message);
}
