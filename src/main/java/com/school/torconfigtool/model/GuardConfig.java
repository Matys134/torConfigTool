package com.school.torconfigtool.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * GuardConfig class extends the BaseRelayConfig class.
 * It represents the configuration for a Guard Relay in the Tor network.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GuardConfig extends BaseRelayConfig {
}