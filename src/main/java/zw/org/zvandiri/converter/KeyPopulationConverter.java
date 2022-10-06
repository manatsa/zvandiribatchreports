package zw.org.zvandiri.converter;

import org.springframework.core.convert.converter.Converter;
import zw.org.zvandiri.business.domain.util.KeyPopulation;

/**
 * @author manatsachinyeruse@gmail.com
 */


public class KeyPopulationConverter implements Converter<String, KeyPopulation> {
    @Override
    public KeyPopulation convert(String s) {
        if(s.equals("")) return null;
        return KeyPopulation.get(Integer.valueOf(s));
    }
}
