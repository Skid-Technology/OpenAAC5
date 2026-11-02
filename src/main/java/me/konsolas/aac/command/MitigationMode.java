package me.konsolas.aac.command;

import me.konsolas.aac.analysis.RiskLevel;

enum MitigationMode {

    OFF, NORMAL, REDUCED;

    boolean checkMitigationMode(RiskLevel riskLevel) {
        switch(this) {
            case OFF:
                return false;
            case NORMAL:
                return riskLevel == RiskLevel.SUSPICIOUS || riskLevel == RiskLevel.HIGH;
            case REDUCED:
                return riskLevel == RiskLevel.HIGH;
            default:
                throw new AssertionError();
        }
    }
}
