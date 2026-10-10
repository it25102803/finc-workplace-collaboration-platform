package com.finc.platform.config;

import com.finc.platform.entity.CalendarEvent;
import com.finc.platform.entity.EventType;
import com.finc.platform.entity.EventVisibility;
import com.finc.platform.repository.CalendarEventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SriLankaHolidaySeeder implements CommandLineRunner {

    private final CalendarEventRepository calendarEventRepository;

    public SriLankaHolidaySeeder(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    @Override
    public void run(String... args) {
        Map<LocalDate, String> holidays2026 = new LinkedHashMap<>();

        // 2026 Official Sri Lankan Public, Bank, & Mercantile Holidays & Poya Days
        holidays2026.put(LocalDate.of(2026, 1, 3), "🌕 Duruthu Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 1, 15), "🌾 Tamil Thai Pongal Day");
        holidays2026.put(LocalDate.of(2026, 2, 1), "🌕 Navam Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 2, 4), "🇱🇰 National Independence Day");
        holidays2026.put(LocalDate.of(2026, 2, 16), "🕉️ Mahasivarathri Day");
        holidays2026.put(LocalDate.of(2026, 3, 3), "🌕 Medin Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 3, 20), "🌙 Id Ul-Fitr (Ramazan Festival Day)");
        holidays2026.put(LocalDate.of(2026, 4, 1), "🌕 Bak Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 4, 3), "✝️ Good Friday");
        holidays2026.put(LocalDate.of(2026, 4, 13), "☀️ Sinhala & Tamil New Year Eve");
        holidays2026.put(LocalDate.of(2026, 4, 14), "☀️ Sinhala & Tamil New Year Day");
        holidays2026.put(LocalDate.of(2026, 5, 1), "🛠️ May Day (Workers' Day)");
        holidays2026.put(LocalDate.of(2026, 5, 27), "🌙 Id Ul-Alha (Hadji Festival Day)");
        holidays2026.put(LocalDate.of(2026, 5, 30), "🌕 Vesak Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 5, 31), "🌕 Day after Vesak Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 6, 29), "🌕 Poson Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 7, 28), "🌕 Esala Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 8, 26), "🕌 Milad-Un-Nabi (Holy Prophet's Birthday)");
        holidays2026.put(LocalDate.of(2026, 8, 27), "🌕 Nikini Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 9, 25), "🌕 Binara Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 10, 25), "🌕 Vap Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 11, 8), "🪔 Deepavali Festival Day");
        holidays2026.put(LocalDate.of(2026, 11, 23), "🌕 Il Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 12, 23), "🌕 Unduvap Full Moon Poya Day");
        holidays2026.put(LocalDate.of(2026, 12, 25), "🎄 Christmas Day");

        for (Map.Entry<LocalDate, String> entry : holidays2026.entrySet()) {
            if (!calendarEventRepository.existsByTitleAndEventDate(entry.getValue(), entry.getKey())) {
                CalendarEvent holiday = new CalendarEvent();
                holiday.setTitle(entry.getValue());
                holiday.setDescription("Official National Holiday in Sri Lanka");
                holiday.setEventType(EventType.HOLIDAY);
                holiday.setEventDate(entry.getKey());
                holiday.setStartTime(entry.getKey().atStartOfDay());
                holiday.setEndTime(entry.getKey().atTime(LocalTime.MAX));
                holiday.setAllDay(true);
                holiday.setVisibility(EventVisibility.PUBLIC);
                calendarEventRepository.save(holiday);
            }
        }
    }
}