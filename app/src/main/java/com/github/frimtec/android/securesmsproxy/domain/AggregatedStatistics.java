package com.github.frimtec.android.securesmsproxy.domain;

public record AggregatedStatistics(long sendCount, long receiveCount, long sendBlockCount, long loopbackCount) {
}
