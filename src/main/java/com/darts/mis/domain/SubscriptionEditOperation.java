package com.darts.mis.domain;

/**
 * REN: new or renew, start, end, price, yearly or not
 * UPG: upgrade, is like a cancel and renew, start, end, price, yearly or not
 * REM: cancel, start = end = cancel day
 * CRE: credit, no impact on revenues
 */
public enum SubscriptionEditOperation {
    REN, UPG, CRE, REM
}
