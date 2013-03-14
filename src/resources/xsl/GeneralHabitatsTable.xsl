<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="GeneralHabitatClasses">
          <h1>General Habitat Classes</h1>
          <table cellspacing="0" style="border: 1px solid rgb(153, 153, 153);" cellpading="0">
            <tr>
              <th bgcolor="#87cefa" style="text-align: left; padding-right: 2em; border: 1px solid rgb(153, 153, 153);">Habitat code</th>
              <th bgcolor="#87cefa" style="text-align: left; padding-right: 2em; border: 1px solid rgb(153, 153, 153);">Description (English)</th>
              <th bgcolor="#87cefa" style="text-align: left; padding-right: 2em; border: 1px solid rgb(153, 153, 153);">Description (French)</th>
            </tr>
            <xsl:for-each select="habitatClass">
              <tr>
                <td style="padding-right: 2em; border: 1px solid rgb(153, 153, 153);">
                  <xsl:value-of select="HABITATCODE"/>
                </td>
                <td style="padding-right: 2em; border: 1px solid rgb(153, 153, 153);">
                  <xsl:value-of select="DESCRIPTION_EN"/>
                </td>
                <td style="padding-right: 2em; border: 1px solid rgb(153, 153, 153);">
                  <xsl:value-of select ="DESCRIPTION_FR"/>
                </td>
              </tr>
            </xsl:for-each>
          </table>
		<h3>Codelist maintained by DG Environment and the European Environment Agency (EEA)</h3>
    </xsl:template>
</xsl:stylesheet>
