package com.darts.mis.domain;

/**
 * REN: new or renew, start, end, price, yearly or not
 * UPG: upgrade, is like a cancel and renew, start, end, price, yearly or not
 * REM: cancel, start = end = cancel day
 * CRE: credit, no impact on revenues, except that there are 5 of them that have adjustments over 0 days (probably manual stuff)
 */
public enum SubscriptionEditOperation {
    REN, UPG, CRE, REM
}
