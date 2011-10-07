/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jongo.rest.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoMapConverter implements Converter {
    
    private static final Logger l = LoggerFactory.getLogger(JongoMapConverter.class);

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        Map<String, String> map = (Map<String, String>)o;
        
        for(String key: map.keySet()){
            String val = map.get(key);
            writer.startNode(key);
            writer.setValue(val);
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(HashMap.class);
    }
    
}
