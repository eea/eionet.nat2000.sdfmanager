/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager;

import java.util.ArrayList;

/**
 *
 * @author charbda
 */
public interface Exporter {

    /**
     *
     * @param filename
     * @return
     */
    public boolean processDatabase(String filename);

    /**
     * 
     * @param filename
     * @return
     */
    public ArrayList createXMLFromDataBase(String filename);

}
