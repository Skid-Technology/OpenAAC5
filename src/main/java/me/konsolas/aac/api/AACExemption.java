package me.konsolas.aac.api;

public class AACExemption {

    private final String reason;

    public AACExemption(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return this.reason;
    }
}
