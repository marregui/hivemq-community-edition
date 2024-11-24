package com.hivemq.tk.time;

import com.hivemq.tk.CharSequenceIntHashMap;
import com.hivemq.tk.FixedTimeZoneRule;
import com.hivemq.tk.Numbers;
import com.hivemq.tk.ObjList;

import java.time.ZoneId;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesProvider;
import java.util.Map;

public class TimeZoneRuleFactory {

    public static final TimeZoneRuleFactory INSTANCE = new TimeZoneRuleFactory();
    public static final int RESOLUTION_MICROS = 1;
    public static final int RESOLUTION_MILLIS = 0;
    private final ObjList<TimeZoneRules> ruleList = new ObjList<>();
    private final CharSequenceIntHashMap ruleMap = new CharSequenceIntHashMap();

    public TimeZoneRuleFactory() {
        int index = 0;
        for (String z : ZoneRulesProvider.getAvailableZoneIds()) {
            final ZoneRules rules = ZoneRulesProvider.getRules(z, true);
            ruleList.add(new TimeZoneRulesMillis(rules));
            ruleList.add(new TimeZoneRulesMicros(rules));
            ruleMap.put(z, index++);
        }

        for (Map.Entry<String, String> e : ZoneId.SHORT_IDS.entrySet()) {
            String key = e.getKey();
            String alias = e.getValue();

            // key already added somehow?
            int i = ruleMap.get(key);
            if (i == -1) {
                // no, good, add
                i = ruleMap.get(alias);
                if (i == -1) {
                    // this could be fixed offset, try parsing value as one
                    long offset = Dates.parseOffset(alias, 0, alias.length());
                    if (offset != Long.MIN_VALUE) {
                        ruleList.add(new FixedTimeZoneRule(Numbers.decodeLowInt(offset) * Dates.MINUTE_MILLIS));
                        ruleList.add(new FixedTimeZoneRule(Numbers.decodeLowInt(offset) * Timestamps.MINUTE_MICROS));
                        ruleMap.put(key, index++);
                    }
                } else {
                    ruleMap.put(key, i);
                }
            }
        }
    }

    public int getTimeZoneRulesIndex(CharSequence id) {
        return ruleMap.get(id);
    }

    public TimeZoneRules getTimeZoneRulesQuick(int index, int resolution) {
        return ruleList.getQuick(2 * index + resolution);
    }
}
