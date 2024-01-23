package ru.practicum.ewm.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStartupListener {

    @EventListener(ApplicationReadyEvent.class)
    public void printParamsAfterStart(ApplicationReadyEvent event) {
        String dbUrl = event.getApplicationContext().getEnvironment().getProperty("spring.datasource.url");
        String dbUser = event.getApplicationContext().getEnvironment().getProperty("spring.datasource.username");
        String dbPass = event.getApplicationContext().getEnvironment().getProperty("spring.datasource.password");
        String statUrl = event.getApplicationContext().getEnvironment().getProperty("services.stats-service.uri");

        log.warn("URL БД: {}", dbUrl);
        log.warn("Логин: {}", dbUser);
        log.warn("Пароль: {}", dbPass);
        log.warn("URL сервиса статистики: {}", statUrl);
    }
}
