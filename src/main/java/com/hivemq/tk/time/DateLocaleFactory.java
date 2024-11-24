package com.hivemq.tk.time;

import com.hivemq.tk.ConcurrentHashMap;
import org.jetbrains.annotations.TestOnly;

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.function.BiFunction;

public class DateLocaleFactory {

    public static final DateLocaleFactory INSTANCE = new DateLocaleFactory(TimeZoneRuleFactory.INSTANCE);
    private final ConcurrentHashMap<DateLocale> dateLocales = new ConcurrentHashMap<>();
    private final DateLocale dummyLocale = new DateLocale("en-quest", new DateFormatSymbols(), TimeZoneRuleFactory.INSTANCE);
    private final TimeZoneRuleFactory timeZoneRuleFactory;
    private final BiFunction<CharSequence, DateLocale, DateLocale> computeDateLocaleBiFunc = this::computeDateLocale;

    public DateLocaleFactory(TimeZoneRuleFactory timeZoneRuleFactory) {
        this.timeZoneRuleFactory = timeZoneRuleFactory;
        for (Locale l : Locale.getAvailableLocales()) {
            String tag = l.toLanguageTag();
            if ("und".equals(tag)) {
                tag = "";
            }
            dateLocales.put(tag, dummyLocale);
        }
    }

    @TestOnly
    public static void load() {
    }

    public DateLocale getLocale(CharSequence id) {
        DateLocale dateLocale = dateLocales.get(id);
        if (dateLocale == null) {
            return null;
        }
        if (dateLocale != dummyLocale) {
            return dateLocale;
        }
        return dateLocales.compute(id, computeDateLocaleBiFunc);
    }

    private DateLocale computeDateLocale(CharSequence key, DateLocale val) {
        if (val != dummyLocale) {
            // Someone was faster than us.
            return val;
        }
        Locale locale = Locale.forLanguageTag(key.toString());
        return new DateLocale(key.toString(), new DateFormatSymbols(locale), timeZoneRuleFactory);
    }
}
