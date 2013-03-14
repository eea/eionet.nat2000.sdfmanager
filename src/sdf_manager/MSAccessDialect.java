/*
* Created on Sep 23, 2005
*
* TODO To change the template for this generated file go to
* Window - Preferences - Java - Code Style - Code Templates
*/
package sdf_manager;

import java.sql.Types;

import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;

/**
* @author Suchak.Jani  w/ modifications by Eric Klimas
* This code is blatently ripped off from the Hibernate forums discussion
* on MS Access and Hibernate found here:
* <a href="http://forums.hibernate.org/viewtopic.php?p=2178009&sid=876222db25ab13214a3729dbe1e494b6">
* http://forums.hibernate.org/viewtopic.php?p=2178009&sid=876222db25ab13214a3729dbe1e494b6</a>
* <p>
* The code has been modified by Eric to:
* <ol>
* <li>WorkallO_BATCH no longer exists</li>
* <li>Be placed in a different package (org.hibernate.unsupported.dialect) so nobody gets the idea
* that this is actively supported by hibernate or anybody for that matter.  <b>USE AT YOUR OWN RISK...</b></li>
* </ol>
*/
public class MSAccessDialect extends Dialect {

    /**
     *
     */
    public MSAccessDialect() {

            super();
            registerColumnType( Types.BIT, "BIT" );
            registerColumnType( Types.BIGINT, "INTEGER" );
            registerColumnType( Types.SMALLINT, "SMALLINT" );
            registerColumnType( Types.TINYINT, "BYTE" );
            registerColumnType( Types.INTEGER, "INTEGER" );
            registerColumnType( Types.CHAR, "VARCHAR(1)" );
            registerColumnType( Types.VARCHAR, "VARCHAR($l)" );
            registerColumnType( Types.FLOAT, "DOUBLE" );
            registerColumnType( Types.DOUBLE, "DOUBLE" );
            registerColumnType( Types.DATE, "DATETIME" );
            registerColumnType( Types.TIME, "DATETIME" );
            registerColumnType( Types.TIMESTAMP, "DATETIME" );
            registerColumnType( Types.VARBINARY, "VARBINARY($l)" );
            registerColumnType( Types.NUMERIC, "NUMERIC" );

            getDefaultProperties().setProperty(Environment.STATEMENT_BATCH_SIZE, "0");

    }


    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    @Override
    public String getIdentityColumnString() {
        return "IDENTITY NOT NULL";
    }

    @Override
    public String getIdentitySelectString() {
        return "select @@IDENTITY";
    }

    @Override
    public boolean hasDataTypeInIdentityColumn() {
        return false;
    }


    public boolean supportsForUpdate() {
            return false;
    }

    /**
     * Returns for update syntax for access, which is non-existant, so I *think*
     * we return an empty string...
     * @return String an beautifully constructed empty string...
     */
    @Override
    public String getForUpdateString() {
            return "";
    }



    @Override
    public String getNoColumnsInsertString() {
        return "DEFAULT VALUES";
    }

    @Override
    public boolean supportsParametersInInsertSelect() {
   return false;
    }


    @Override
    public String getAddColumnString() {
            return "add";
    }
    @Override
    public String getNullColumnString() {
            return " null";
    }

    @Override
    public boolean qualifyIndexName() {
            return false;
    }

}
