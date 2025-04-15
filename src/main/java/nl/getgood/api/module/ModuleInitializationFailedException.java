package nl.getgood.api.module;

/**
 * Thrown when a module fails to initialize.
 * Created on 03/08/2024 at 15:49
 * by Luca Warmenhoven.
 */
public class ModuleInitializationFailedException extends Exception {

    public ModuleInitializationFailedReason reason = ModuleInitializationFailedReason.UNKNOWN;

    public ModuleInitializationFailedException(ModuleInitializationFailedReason reason) {
        super();
        this.reason = reason;
    }

    public ModuleInitializationFailedException(ModuleInitializationFailedReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public ModuleInitializationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleInitializationFailedException(Throwable cause) {
        super(cause);
    }

    public ModuleInitializationFailedException() {
        super();
    }
}
