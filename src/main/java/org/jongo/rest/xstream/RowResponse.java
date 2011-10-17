package org.jongo.rest.xstream;

import java.util.Map;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class RowResponse {
    private final int roi;
    private final Map<String, String> columns;// = new HashMap<String, String>();

    public RowResponse(int roi, Map<String, String> columns) {
        this.roi = roi;
        this.columns = columns;
    }

    public int getRoi() {
        return roi;
    }

    public Map<String, String> getColumns() {
        return columns;
    }
}
