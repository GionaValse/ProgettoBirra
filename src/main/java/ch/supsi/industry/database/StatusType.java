package ch.supsi.industry.database;

public enum StatusType {
    ACTIVE("active"),
    INACTIVE("inactive"),
    ERROR("error");

    private String value;

    StatusType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
