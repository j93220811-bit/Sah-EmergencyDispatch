package hospital.model;
public enum SeverityLevel {
    RED(1), YELLOW(2), GREEN(3);
    private final int rank;
    SeverityLevel(int rank) { this.rank = rank; }
    public int getRank() { return rank; }
    @Override public String toString() { return name(); }
}
