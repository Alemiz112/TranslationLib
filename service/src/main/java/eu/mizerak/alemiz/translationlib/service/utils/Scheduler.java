package eu.mizerak.alemiz.translationlib.service.utils;

import eu.mizerak.alemiz.translationlib.common.utils.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class Scheduler {

    public static final ScheduledExecutorService DEFAULT = Executors.newScheduledThreadPool(8, ThreadFactoryBuilder.builder()
            .format("DefaultSchedulerThread-%s")
            .exceptionHandler((t, e) -> log.error("Exception happened in scheduler thread", e))
            .build());
}
