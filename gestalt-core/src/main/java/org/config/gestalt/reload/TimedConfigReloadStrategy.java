package org.config.gestalt.reload;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.source.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Reloads a sources every specified duration.
 *
 * @author Colin Redmond
 */
public class TimedConfigReloadStrategy extends ConfigReloadStrategy {
    private static final Logger logger = LoggerFactory.getLogger(TimedConfigReloadStrategy.class.getName());

    private final Timer timer = new Timer();
    private final Duration reloadDelay;

    public TimedConfigReloadStrategy(ConfigSource source, Duration reloadDelay) {
        super(source);
        this.reloadDelay = reloadDelay;
        startTimer();
    }

    private void startTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    reload();
                } catch (GestaltException e) {
                    logger.error("Exception reloading source " + source.name() + ", exception " + e, e);
                }
            }
        }, reloadDelay.toMillis(), reloadDelay.toMillis());
    }
}
