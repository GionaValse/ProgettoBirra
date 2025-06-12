package ch.supsi.industry.logic;

/**
 * Enumeration representing the destination of beer production.
 * <p>
 * Used to distinguish between beer batches directed to Switzerland or Italy.
 * </p>
 */
public enum Where {

    /** Beer destined for Switzerland. */
    SWISS("Svizzera"),

    /** Beer destined for Italy. */
    ITALY("Italia");

    private final String name;

    /**
     * Constructs a {@code Where} enum with a human-readable name.
     *
     * @param name the display name of the destination.
     */
    Where(final String name) {
        this.name = name;
    }

    /**
     * Returns the display name of the destination.
     *
     * @return the name of the destination.
     */
    @Override
    public String toString() {
        return name;
    }
}

