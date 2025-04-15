package nl.getgood.api.event;

import nl.getgood.api.Logger;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created on 04/08/2024 at 15:53
 * by Luca Warmenhoven.
 */
public class EventManager
{
    private static final EventPriority[] priorities = EventPriority.values();

    /**
     * A map containing sorted arrays of listeners.
     */
    private static final Map<String, Set<EventListenerEntry>> eventListeners = Map.of();

    /**
     * Adds an event listener.
     *
     * @param listener  The listener to add.
     * @param eventName The name of the event to add the listener to.
     */
    public static void addEventListener(Class<? extends EventListener> listener, String eventName)
    {
        Set<EventListenerEntry> listeners = eventListeners.containsKey(eventName) ? eventListeners.get(eventName) :
                new TreeSet<>(Comparator.comparingInt((EventListenerEntry entry) ->
                                                              entry.method.getAnnotation(EventTarget.class).priority().ordinal()));

        for (Method method : listener.getDeclaredMethods())
        {
            if (
                    method.isAnnotationPresent(EventTarget.class)
                            && method.getParameterTypes().length == 1
                            && method.getParameterTypes()[0].equals(Event.class)
            )
            {
                EventTarget target = method.getAnnotation(EventTarget.class);

                if (target.eventName().equals(eventName))
                    listeners.add(new EventListenerEntry(listener, method));
            }
        }

        eventListeners.put(eventName, listeners);
    }

    /**
     * Removes an event listener.
     *
     * @param listener  The listener to remove.
     * @param eventName The name of the event to remove the listener from.
     */
    public static void removeEventListener(Class<? extends EventListener> listener, String eventName)
    {
        Set<EventListenerEntry> listeners = eventListeners.get(eventName);

        if (listeners == null)
            return;

        listeners.remove(listener);
        eventListeners.put(eventName, listeners);
    }

    /**
     * Dispatches an event to all listeners.
     *
     * @param event The event to dispatch.
     */
    public static void dispatchEvent(Event event)
    {
        Set<EventListenerEntry> listeners = eventListeners.get(event.getEventName());

        if (listeners == null)
            return;

        for (EventListenerEntry entry : listeners)
        {
            if  (event.isCancelled() )
                break;
            try
            {
                entry.method.invoke(entry.listener, event);
            }
            catch (Exception e)
            {
                Logger.getLogger().error("Failed to dispatch event: " + event.getEventName());
            }
        }
    }

    /**
     * An entry in the event listener map.
     */
    private record EventListenerEntry(Class<? extends EventListener> listener, Method method) {}

}

