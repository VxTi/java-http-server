package nl.getgood.api.event;

/**
 * Created on 04/08/2024 at 15:38
 * by Luca Warmenhoven.
 */
public enum EventPriority
{
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4);

    private final int priority;

    EventPriority(int priority)
    {
        this.priority = priority;
    }

    public int getPriority()
    {
        return priority;
    }
}
