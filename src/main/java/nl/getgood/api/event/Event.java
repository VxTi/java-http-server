package nl.getgood.api.event;

/**
 * Event class.
 * This class is used to create events that can be listened to.
 * Created on 04/08/2024 at 15:50
 * by Luca Warmenhoven.
 */
public class Event
{

    private boolean cancelled = false;
    private final String eventName;
    private final Object[] args;

    /**
     * Creates a new event.
     * @param eventName the name of the event.
     * @param args the arguments of the event.
     */
    public Event(String eventName, Object... args)
    {
        this.eventName = eventName;
        this.args = args;
    }

    /**
     * Dispatches the event.
     */
    public final void dispatch()
    {
        EventManager.dispatchEvent(this);
    }

    /**
     * @return true if the event is cancelled.
     */
    public final boolean isCancelled()
    {
        return cancelled;
    }

    /**
     * Stops the event from propagating to other listeners.
     */
    public final boolean stopPropagation()
    {
        return cancelled = true;
    }

    public final String getEventName()
    {
        return eventName;
    }

    public final Object[] getArguments()
    {
        return args;
    }
}
