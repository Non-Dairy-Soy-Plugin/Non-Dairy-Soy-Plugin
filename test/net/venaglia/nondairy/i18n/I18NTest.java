/*
 * Copyright 2010 Ed Venaglia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.venaglia.nondairy.i18n;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 29, 2010
 * Time: 3:15:19 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class I18NTest {

    private Map<Locale,ResourceBundle> supportedLocales = Collections.emptyMap();

    @Before
    public void detectSupportedLocales() throws Exception {
        Class<?> klass = I18N.class;
        String bundleName = klass.getPackage().getName() + "." + I18N.RESOURCE_BUNDLE_NAME;

        Map<Locale,ResourceBundle> locales = new LinkedHashMap<Locale,ResourceBundle>();
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, klass.getClassLoader());
                if (!locales.containsKey(bundle.getLocale())) {
                    locales.put(bundle.getLocale(), bundle);
                }
            } catch (MissingResourceException e) {
                // skip it
            }
        }
        supportedLocales = locales;
    }

    private String reportKeyInconsistency(String key,
                                          Collection<Locale> foundIn,
                                          Collection<Locale> notFoundIn) {
        if (foundIn.size() == 1) {
            Locale locale = foundIn.iterator().next();
            return String.format("Resource key '%s' was only defined in resource file for:\n\t%s", key, locale);
        } else {
            return String.format("Resource key '%s' is not defined in resource files for:\n\t%s", key, notFoundIn);
        }
    }

    @Test
    public void testForConsistentKeys() throws Exception {
        assertFalse(supportedLocales.isEmpty());
        System.out.println(String.format("Testing for consistency on %d supported locales: %s", supportedLocales.size(), supportedLocales.keySet()));

        Set<String> allKeys = new LinkedHashSet<String>(256);
        boolean inconsistent = false;
        for (ResourceBundle bundle : supportedLocales.values()) {
            int startSize = allKeys.size();
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                allKeys.add(key);
            }
            inconsistent = inconsistent || startSize > 0 && startSize < allKeys.size();
        }

        if (inconsistent) {
            Set<String> inconsistencies = new LinkedHashSet<String>(256);
            for (String key : allKeys) {
                Collection<Locale> notFoundIn = new LinkedList<Locale>(supportedLocales.keySet());
                Collection<Locale> foundIn = new LinkedList<Locale>();
                for (Iterator<Locale> iterator = notFoundIn.iterator(); iterator.hasNext();) {
                    Locale testLocal = iterator.next();
                    if (supportedLocales.get(testLocal).getString(key) != null) {
                        iterator.remove();
                        foundIn.add(testLocal);
                    }
                }
                if (!notFoundIn.isEmpty()) {
                    inconsistencies.add(reportKeyInconsistency(key, foundIn, notFoundIn));
                }
            }
            assertFalse("Something is wrong with the test!", inconsistencies.isEmpty());
            for (String errorMessage : inconsistencies) {
                System.err.println(errorMessage);
            }
            fail("Keys in resource bundle are inconsistent!");
        }
    }
}
