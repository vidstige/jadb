package se.vidstige.jadb.managers;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class which works with properties, uses getprop and setprop methods of android shell
 */
public class PropertyManager implements Iterable<PropertyManager.Entry>{
    private final JadbDevice device;
    private Map<String, String> map;

    public PropertyManager(JadbDevice device) throws IOException, JadbException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(device.executeShell("getprop")));
        this.device = device;
        this.map = parseProp(reader);
        reader.close();
    }

    public int size(){
        return map.size();
    }

    public boolean containsKey(String key){
        return map.containsKey(key);
    }

    public String get(String key){
        return map.get(key);
    }

    public String set(String key, String val) throws JadbException, IOException {
        setProperty(key, val);
        return map.put(key, val);
    }

    @Override
    public Iterator<Entry> iterator() {
        return new PropertyItr();
    }

    private void setProperty(String key, String val) throws JadbException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(device.executeShell("setprop", key, val)));
        String line;
        while((line = reader.readLine()) != null){
            System.out.println(line);
        }
    }

    private Map<String, String> parseProp(BufferedReader bufferedReader) throws IOException {
        final Pattern pattern = Pattern.compile("^\\[([a-zA-Z0-9_.-]*)\\]:.\\[([a-zA-Z0-9_.-]*)\\]");

        HashMap<String, String> result = new HashMap<>();

        String line;
        Matcher matcher = pattern.matcher("");

        while ((line = bufferedReader.readLine()) != null) {
            matcher.reset(line);

            if (matcher.find()) {
                if (matcher.groupCount() < 2) {
                    System.err.println("Property line: " + line + " does not match pattern. Ignoring");
                    continue;
                }
                String key = matcher.group(1);
                String value = matcher.group(2);
                result.put(key, value);
            }
        }

        return result;
    }

    class Entry{
        private Map.Entry<String, String> entry;

        private Entry(Map.Entry<String, String> entry){
            this.entry = entry;
        }

        public String getKey(){
            return entry.getKey();
        }

        public String getValue(){
            return entry.getValue();
        }

        public void setValue(String val) throws IOException, JadbException {
            setProperty(entry.getKey(), val);
            entry.setValue(val);
        }
    }

    private class PropertyItr implements Iterator<Entry> {

        Iterator<Map.Entry<String, String>> it;

        private PropertyItr(){
            it = map.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Entry next() {
            return new Entry(it.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
