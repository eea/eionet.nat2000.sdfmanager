/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sdf_manager;

/**
 *
 * @author anon
 */
interface Importer {

    /**
     *
     * @param fileName
     * @return
     */
    public boolean processDatabase(String fileName);

    /**
     * 
     * @param fileName
     */
    public void initLogFile(String fileName);
    
}
