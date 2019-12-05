<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:import href="DateFormat.xsl" />
    <xsl:output method="xml" indent="yes" encoding="utf-16" omit-xml-declaration="yes"/>



    <xsl:template match="sdf" >



    <html>
      <head><link href="css/Natura2000_SDF.css" rel="stylesheet" type="text/css" />
        <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>
        <style type='text/css'>
          * { font-family: 'Arial Unicode MS'; }
        </style>
      </head>
      <body>
         <div class="SDFContent">
      <a name="top">
        <!-- ANCHOR TOP SECTION -->
        <!-- This span is to avoid problems with the generated html. -->
        <span/>
      </a>
      <!-- STARTING CONTAINER PART-->
      <img id="img2_header_left" src="images/natura2000_logoMedium.jpg"></img>
      <div class="main_title">NATURA 2000 - STANDARD DATA FORM</div>
      <!-- This span is to avoid problems with the generated html. -->

      <p class="main_subtitle">
        For Special Protection Areas (SPA), <br/>Proposed Sites for Community Importance (pSCI),<br/>
        Sites of Community Importance (SCI) and <br/>for Special Areas of Conservation (SAC)
      </p>


      <div class="siteData_title">

        <xsl:choose>
          <xsl:when test="string(siteIdentification/siteCode)">
            <table class="invisible WholeWidth">
              <tr>
                <td width="15%">SITE</td>
                <th>
                  <xsl:value-of select = "siteIdentification/siteCode"/>
                </th>
              </tr>
              <tr>
                <td>SITENAME</td>
                <th>
                  <xsl:value-of select = "siteIdentification/siteName"/>
                </th>
              </tr>
            </table>
          </xsl:when>
          <xsl:otherwise>
            SITE Not available<br/>
            Close this browser window or try again later, please.
          </xsl:otherwise>
        </xsl:choose>
      </div>

      <h1>
        TABLE OF CONTENTS
      </h1>
      <ul>

        <li>
          <a href="#1">1. SITE IDENTIFICATION</a>
        </li>
        <li>
          <a href="#2">2. SITE LOCATION</a>
        </li>
        <xsl:choose>
          <xsl:when test="string(ecologicalInformation/habitatTypes) or string(ecologicalInformation/species)">
            <li>
              <a href="#3">3. ECOLOGICAL INFORMATION</a>
            </li>
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="string(siteDescription)">
            <li>
              <a href="#4">4. SITE DESCRIPTION</a>
            </li>
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="string(siteProtection/nationalDesignations) or string(siteProtection/relations) or string(siteProtection/siteDesignationAdditional)">
            <li>
              <a href="#5">5. SITE PROTECTION STATUS</a>
            </li>
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="string(siteManagement/managementBodies) or string(siteManagement/managementPlans) or string(siteManagement/conservationMeasures)">
            <li>
              <a href="#6">6. SITE MANAGEMENT</a>
            </li>
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="string(map/pdfProvided) or string(map/mapReference) or string(map/InspireID)">
            <li>
              <a href="#7">7. MAP OF THE SITE</a>
            </li>
          </xsl:when>
        </xsl:choose>
        <!-- This part is not so clear... ¿Where do I get this data (SiteSlides) from (in the new XML structure)?-->
        <xsl:choose>
          <xsl:when test="string(../SiteSlides)">
            <li>
              <a href="#8">8. SLIDES</a>
            </li>
          </xsl:when>
        </xsl:choose>
      </ul>

      <!--
      <div align="center">
        <input type="button" name="print" value="Print Standard Data Form" onClick="javascript:window.open('\Site.pdf','SDF');"></input>
      </div>
      -->
      <!-- **********************************************************************************************************************************
                                                    ENDING TITLE PAGE PART
          ************************************************************************************************************************************-->
      <!-- **********************************************************************************************************************************
                                                    SARTING SITE IDENTIFICATION PART
          ************************************************************************************************************************************-->
      <a name="1">
        <!-- ANCHOR SITE IDENTIFICATION SECTION -->
        <span/>
      </a>

      <h1>1. SITE IDENTIFICATION</h1>
      <a href="#top" class="BackTopLink">Back to top</a>


      <table class="SDFtable WholeWidth">
        <tr>
          <th width="25%" class="DivisorRight">1.1 Type</th>
          <th width="75%">1.2 Site code</th>
        </tr>
        <tr>
          <td class="DivisorRight">
            <!--<div class="Border">-->
            <xsl:value-of select = "siteIdentification/siteType"/>
            <!--</div>-->
          </td>
          <td>
            <!--<div class="Border">-->
            <xsl:value-of select = "siteIdentification/siteCode"/>
            <!--</div>-->
          </td>
        </tr>
      </table>

      <h2>1.3 Site name</h2>
      <table class="SDFtable WholeWidth">
        <tr>
          <td class="MinimalHeight">
            <xsl:value-of select = "siteIdentification/siteName"/>
          </td>
        </tr>
      </table>

      <table class="SDFtable WholeWidth">
        <tr>
          <th width="50%" class="DivisorRight">1.4 First Compilation date</th>
          <th width="50%">1.5 Update date</th>
        </tr>
      </table>
      <xsl:choose>
        <xsl:when test="string(siteIdentification/compilationDate) or string(siteIdentification/updateDate)">
          <table class="SDFtable WholeWidth">
            <tr>
              <td width="50%" class="DivisorRight">
                <xsl:call-template name="FormatYearMonth">
                  <xsl:with-param name="DateTime" select="siteIdentification/compilationDate" />
                </xsl:call-template>
              </td>
              <td width="50%">
                <xsl:call-template name="FormatYearMonth">
                  <xsl:with-param name="DateTime" select="siteIdentification/updateDate"/>
                </xsl:call-template>
              </td>
            </tr>
          </table>
        </xsl:when>
      </xsl:choose>

      <h2>1.6 Respondent:</h2>
      <xsl:choose>
        <xsl:when test="string(siteIdentification/respondent)">
          <table class="SDFtable WholeWidth">
            <tr>
              <td class="Bold">Name/Organisation:</td>
              <td colspan="4">
                <xsl:value-of select = "siteIdentification/respondent/name"/>
              </td>
            </tr>
            <tr>
              <td class="Bold">Address:</td>
              <td colspan="4">
                <xsl:value-of select = "siteIdentification/respondent/address/adminUnit"/>&#160;
                <xsl:value-of select = "siteIdentification/respondent/address/locatorDesignator"/>&#160;
                <xsl:value-of select = "siteIdentification/respondent/address/locatorName"/>&#160;
                <xsl:value-of select = "siteIdentification/respondent/address/addressArea"/>&#160;
                <xsl:value-of select = "siteIdentification/respondent/address/postName"/>&#160;
                <xsl:value-of select = "siteIdentification/respondent/address/postCode"/>&#160;
                <xsl:value-of select = "siteIdentification/respondent/address/thoroughfare"/>&#160;
              </td>
            </tr>
            <tr>
              <td class="Bold">Email:</td>
              <td colspan="4">
                <xsl:value-of select = "siteIdentification/respondent/email"/>
              </td>
            </tr>
          </table>
        </xsl:when>
      </xsl:choose>


      <xsl:choose>
        <xsl:when test="string(siteIdentification/spaClassificationDate) or string(siteIdentification/spaLegalReference)">
          <h2>1.7 Site indication and designation / classification dates</h2>
          <table class="SDFtable WholeWidth">
            <tr>
              <td width="50%" class="Bold">Date site classified as SPA:</td>
              <td width="50%" class="DivisorTop MinimalHeight">
                <xsl:choose>
                  <xsl:when test="string(siteIdentification/spaClassificationDate)">
                    <xsl:call-template name="FormatYearMonth">
                      <xsl:with-param name="DateTime" select="siteIdentification/spaClassificationDate"/>
                    </xsl:call-template>
                  </xsl:when>
                  <xsl:otherwise>
                    No data
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
            <tr>
              <td width="50%" class="DivisorRight DivisorTop Bold">National legal reference of SPA designation</td>
              <td class="DivisorTop MinimalHeight">
                <xsl:choose>
                  <xsl:when test="string(siteIdentification/spaLegalReference)">
                    <xsl:value-of select="siteIdentification/spaLegalReference"/>
                  </xsl:when>
                  <xsl:otherwise>
                    No data
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
          </table>
        </xsl:when>
      </xsl:choose>

      <xsl:choose>
        <xsl:when test="string(siteIdentification/sciProposalDate) or string(siteIdentification/sciConfirmationDate) or string(siteIdentification/sacDesignationDate) or string(siteIdentification/sacLegalReference)">
          <table class="SDFtable WholeWidth">
            <tr>
              <td width="50%" class="Bold">Date site proposed as SCI:</td>
              <td width="50%" class="MinimalHeight">
                <xsl:choose>
                  <xsl:when test="string(siteIdentification/sciProposalDate)">
                    <xsl:call-template name="FormatYearMonth">
                      <xsl:with-param name="DateTime" select="siteIdentification/sciProposalDate"/>
                    </xsl:call-template>
                  </xsl:when>
                  <xsl:otherwise>
                    No data
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
            <tr>
              <td width="50%" class="Bold">Date site confirmed as SCI:</td>
              <td class="MinimalHeight">
                <xsl:choose>
                  <xsl:when test="string(siteIdentification/sciConfirmationDate)">
                    <xsl:call-template name="FormatYearMonth">
                      <xsl:with-param name="DateTime" select="siteIdentification/sciConfirmationDate"/>
                    </xsl:call-template>
                  </xsl:when>
                  <xsl:otherwise>
                    No data
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
            <tr>
              <td width="50%" class="Bold">Date site designated as SAC:</td>
              <td width="50%" class="MinimalHeight">
                <xsl:choose>
                  <xsl:when test="string(siteIdentification/sacDesignationDate)">
                    <xsl:call-template name="FormatYearMonth">
                      <xsl:with-param name="DateTime" select="siteIdentification/sacDesignationDate"/>
                    </xsl:call-template>
                  </xsl:when>
                  <xsl:otherwise>
                    No data
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
            <tr>
              <td width="50%" class="DivisorRight Bold">National legal reference of SAC designation:</td>
              <td class="DivisorTop MinimalHeight">
                <xsl:choose>
                  <xsl:when test="string(siteIdentification/sacLegalReference)">
                    <!--<xsl:call-template name="FormatYearMonth">
                      <xsl:with-param name="DateTime" select="siteIdentification/sacLegalReference"/>
                    </xsl:call-template>-->
                    <xsl:value-of select="siteIdentification/sacLegalReference"/>
                  </xsl:when>
                  <xsl:otherwise>
                    No data
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
          </table>
        </xsl:when>
      </xsl:choose>

      <xsl:choose>
        <xsl:when test="string(siteIdentification/explanations)">
          <table class="SDFtable WholeWidth">
            <tr>
              <td width="25%" class="Bold DivisorRight">Explanation(s):</td>
              <td width="75%" class="MinimalHeight">
                <xsl:value-of select="siteIdentification/explanations"/>
              </td>
            </tr>
          </table>
        </xsl:when>
      </xsl:choose>
      <!-- **********************************************************************************************************************************
                                                    ENDING SITE IDENTIFICATION PART
          ************************************************************************************************************************************-->
      <H1 class="LineBreak"> </H1>
      <!-- **********************************************************************************************************************************
                                                    SARTING SITE LOCATION PART
          ************************************************************************************************************************************-->
      <a name="2">
        <span/>
      </a>

      <h1>2. SITE LOCATION</h1>
      <a href="#top" class="BackTopLink">Back to top</a>

      <h2>2.1 Site-centre location [decimal degrees]:</h2>
      <xsl:choose>
        <xsl:when test="string(siteLocation/longitude) or string(siteLocation/latitude)">
          <table class="SDFtableNoBorder WholeWidth">
            <tr width="100%">
              <td width="50%">
                <h3>Longitude</h3>
                <xsl:choose>
                  <xsl:when test="string(siteLocation/longitude)">
                    <xsl:value-of select = "siteLocation/longitude"/>
                  </xsl:when>
                  <xsl:otherwise>
                    No data
                  </xsl:otherwise>
                </xsl:choose>
              </td>
              <td width="50%">
                <h3>Latitude</h3>
                <xsl:choose>
                  <xsl:when test="string(siteLocation/latitude)">
                    <xsl:value-of select = "siteLocation/latitude"/>
                  </xsl:when>
                  <xsl:otherwise>
                    No data
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
          </table>
        </xsl:when>
      </xsl:choose>

      <table class="SDFtableNoBorder WholeWidth">
        <tr>
            <td>
                <h2>2.2 Area [ha]:</h2>
            </td>
            <td>
                <h2>2.3 Marine area [%]</h2>
            </td>
        </tr>
      </table>
      <xsl:choose>
        <xsl:when test="string(siteLocation/area) or string(siteLocation/marineArea)">
          <table class="SDFtableNoBorder WholeWidth">
            <tr width="100%">
              <td width="50%">
                <xsl:value-of select = "siteLocation/area"/>
              </td>
              <td width="50%">
                <xsl:value-of select = "siteLocation/marineAreaPercentage"/>
              </td>
            </tr>
          </table>
        </xsl:when>
      </xsl:choose>

    <table class="SDFtableNoBorder WholeWidth">
        <tr>
          <td>
            <h2>2.4 Sitelength [km]:</h2>
          </td>
        </tr>
    </table>
      <xsl:choose>
        <xsl:when test="string(siteLocation/siteLength)">
          <table class="SDFtableNoBorder WholeWidth">
            <tr width="100%">
              <td width="50%">
                <xsl:value-of select = "siteLocation/siteLength"/>
              </td>
            </tr>
          </table>
        </xsl:when>
      </xsl:choose>


      <h2>2.5 Administrative region code and name</h2>
      <xsl:choose>
        <xsl:when test="string(siteLocation/adminRegions/region)">
          <table class="SDFtableNoBorder WholeWidth">
            <tr>
              <td width="25%">
                <h3>NUTS level 2 code</h3>
              </td>
              <td width="75%">
                <h3>Region Name</h3>
              </td>
            </tr>
            <xsl:for-each select="siteLocation/adminRegions/region">
              <tr>
                <td class="CellBorder">
                  <xsl:value-of select="code"/>
                </td>
                <td class="CellBorder">
                  <xsl:value-of select="name"/>
                </td>
              </tr>
            </xsl:for-each>
          </table>
        </xsl:when>
      </xsl:choose>


      <h2>2.6 Biogeographical Region(s)</h2>
      <xsl:choose>
        <xsl:when test="string(siteLocation/biogeoRegions)">
          <xsl:variable name="vNodes" select="siteLocation/biogeoRegions"/>
          <xsl:variable name="vNumCols" select="3"/>
          <xsl:choose>
            <!-- Biogeographical Regions -->
            <xsl:when test="string(siteLocation/biogeoRegions)">
              <table class="SDFtableNoBorder WholeWidth">
                <xsl:for-each select="$vNodes[position() mod $vNumCols = 1]">
                  <xsl:variable name="vCurPos" select="(position()-1)*$vNumCols +1"/>
                  <tr>
                    <xsl:for-each select="$vNodes[position() >= $vCurPos and not(position() > $vCurPos + $vNumCols -1)]">
                      <td width="5%">
                        <xsl:value-of select = "code"/>
                      </td>
                      <td width="5%" align="left">(<xsl:value-of select = "percentage"/> %)

                      </td>
                      <td align="left"></td>
                    </xsl:for-each>
                  </tr>
                </xsl:for-each>
              </table>
            </xsl:when>
          </xsl:choose>

        </xsl:when>
      </xsl:choose>
      <!-- **********************************************************************************************************************************
                                                    ENDING SITE LOCATION PART
          ************************************************************************************************************************************-->
      <!-- **********************************************************************************************************************************
                                                    SARTING ECOLOGICAL INFORMATION PART
          ************************************************************************************************************************************-->
      <!-- xsl:choose>
        <xsl:when test="string(ecologicalInformation)" -->
          <a name="3">
            <span/>
          </a>
          <h1>3. ECOLOGICAL INFORMATION</h1>
          <a href="#top" class="BackTopLink">Back to top</a>
          <!--<p class="note">
                  NOTE: Protected species are shown with red background.
                </p>-->


          <h2>3.1 Habitat types present on the site and assessment for them</h2>
          <xsl:choose>
            <xsl:when test="string(ecologicalInformation/habitatTypes/habitatType)">
              <table class="SDFtable3rd WholeWidth">
                <tr>
                  <th colspan="6" class="MinimalHeight DivisorRight CenterText">
                    Annex I Habitat types
                  </th>
                  <th colspan="4">
                    Site assessment
                  </th>
                </tr>
                <tr>
                  <th class="DivisorTop DivisorRight">
                    Code
                  </th>
                  <th class="DivisorTop DivisorRight">
                    PF
                  </th>
                  <th class="DivisorTop DivisorRight">
                    NP
                  </th>
                  <th class="DivisorTop DivisorRight">
                    Cover [ha]
                  </th>
                  <th class="DivisorTop DivisorRight">
                    Cave [number]
                  </th>
                  <th class="DivisorTop DivisorRight">
                    Data quality
                  </th>
                  <th class="DivisorTop DivisorRight">
                    A|B|C|D
                  </th>
                  <th class="DivisorTop" colspan="3">
                    A|B|C
                  </th>
                </tr>
                <tr>
                  <th class="DivisorTop DivisorRight" width="7.5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="15%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="7.5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="10%">Representativity</th>
                  <th class="DivisorTop DivisorRight" width="15%">Relative Surface</th>
                  <th class="DivisorTop DivisorRight" width="15%">Conservation</th>
                  <th class="DivisorTop " width="15%">Global</th>
                </tr>
                <xsl:for-each select="ecologicalInformation/habitatTypes/habitatType">
                  <!--<xsl:for-each select="HabitatMappingType/HabitatType">-->
                    <tr>
                      <td class="DivisorTop DivisorRight">
                        <xsl:value-of select = "code"/>
                        <xsl:variable name="habcode" select="code"/>
                        <xsl:variable name="habcodeURL" select="concat('HabCodePopup.aspx?habcode=', $habcode)"/>
                        <xsl:variable name="apos">'</xsl:variable>
                        <xsl:variable name="apos2">http://natura2000.eea.europa.eu/Natura2000/</xsl:variable>
                        <!--<img src="images/info.jpg" border="0" alt="info">
                        <xsl:attribute name="onclick">
                          <xsl:value-of select="concat('window.open(', $apos, $habcodeURL, $apos, ',', $apos, 'habpopup', $apos, ',', $apos, 'height=130,width=300,toolbar=no,scrollbars=yes', $apos, ')')"/>
                        </xsl:attribute>
                      </img>&#160; -->
                        <!-- In the following image, the path, to which the link points to has been changed in order to show the linked page from here ("http://natura..." added) -->
                        <img src="images/info.jpg" border="0" alt="info">
                          <xsl:attribute name="onclick">
                            <xsl:value-of select="concat('window.open(', $apos, $apos2, $habcodeURL, $apos, ',', $apos, 'habpopup', $apos, ',', $apos, 'height=130,width=300,toolbar=no,scrollbars=yes', $apos, ')')" />
                          </xsl:attribute>
                        </img>&#160;
                      </td>
                      <td class="DivisorTop DivisorRight">
                        <xsl:if test="priorityFormOfHabitatType='true'">
                          X
                        </xsl:if>&#160;
                      </td>
                      <td class="DivisorTop DivisorRight">
                        <xsl:if test="nonPresenceInSite='true'">
                          X
                        </xsl:if>&#160;
                      </td>
                      <td class="DivisorTop DivisorRight">
                        <xsl:value-of select = "coveredArea"/>&#160;
                      </td>
                      <td class="DivisorTop DivisorRight">
                        <xsl:value-of select = "caves"/>&#160;
                      </td>
                      <td class="DivisorTop DivisorRight">
                        <xsl:value-of select = "observationDataQuality"/>&#160;
                      </td>
                      <td class="DivisorTop DivisorRight">
                        <xsl:value-of select = "representativity"/>&#160;
                      </td>
                      <td class="DivisorTop DivisorRight">
                        <xsl:value-of select = "relativeSurface"/>&#160;
                      </td>
                      <td class="DivisorTop DivisorRight">
                        <xsl:value-of select = "conservation"/>&#160;
                      </td>
                      <td class="DivisorTop">
                        <xsl:value-of select = "global"/>&#160;
                      </td>
                    </tr>
                  <!--</xsl:for-each>-->
                </xsl:for-each>
              </table>
              <ul class="legend">
                <li>
                  <b>PF:</b> for the habitat types that can have a non-priority as well as a priority form (6210, 7130, 9430) enter "X" in the column PF to indicate the priority form.
                </li>
                <li>
                  <b>NP:</b> in case that a habitat type no longer exists in the site enter: x (optional)
                </li>
                <li>
                  <b>Cover:</b> decimal values can be entered
                </li>
                <li>
                  <b>Caves:</b> for habitat types 8310, 8330 (caves) enter the number of caves if estimated surface is not available.
                </li>
                <li>
                  <b>Data quality:</b> G = 'Good' (e.g. based on surveys); M = 'Moderate' (e.g. based on partial data with some extrapolation); P = 'Poor' (e.g. rough estimation)
                </li>
              </ul>
              <!-- Fin Tabla nueva -->
            </xsl:when>
          </xsl:choose>

          <h2>3.2 Species referred to in Article 4 of Directive 2009/147/EC and listed in Annex II of Directive 92/43/EEC and site evaluation for them</h2>
          <xsl:choose>
            <xsl:when test="string(ecologicalInformation/species)">
              <table class="SDFtable3rd WholeWidth">
                <tr>
                  <th colspan="5" class="MinimalHeight DivisorRight CenterText">
                    Species
                  </th>
                  <th colspan="6" class="DivisorRight">
                    Population in the site
                  </th>
                  <th colspan="4">
                    Site assessment
                  </th>
                </tr>
                <tr>
                  <th class="DivisorTop DivisorRight">
                    G
                  </th>
                  <th class="DivisorTop DivisorRight">
                    Code
                  </th>
                  <th class="DivisorTop DivisorRight">
                      Scientific Name
                  </th>
                  <th class="DivisorTop DivisorRight">
                    S
                  </th>
                  <th class="DivisorTop DivisorRight">
                    NP
                  </th>
                  <th class="DivisorTop DivisorRight">
                    T
                  </th>
                  <th class="DivisorTop DivisorRight" colspan="2">
                    Size
                  </th>
                  <th class="DivisorTop DivisorRight">
                    Unit
                  </th>
                  <th class="DivisorTop DivisorRight">
                    Cat.
                  </th>
                  <th class="DivisorTop DivisorRight">
                    D.qual.
                  </th>
                  <th class="DivisorTop DivisorRight">
                    A|B|C|D
                  </th>
                  <th class="DivisorTop DivisorRight" colspan="3">
                    A|B|C
                  </th>
                </tr>
                <tr>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="15%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="7.5%">Min</th>
                  <th class="DivisorTop DivisorRight" width="7.5%">Max</th>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="5%"> </th>
                  <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                  <th class="DivisorTop DivisorRight" width="7.5%">Pop.</th>
                  <th class="DivisorTop DivisorRight" width="7.5%">Con.</th>
                  <th class="DivisorTop DivisorRight" width="7.5%">Iso.</th>
                  <th class="DivisorTop DivisorRight" width="7.5%">Glo.</th>
                </tr>

                <xsl:for-each select="ecologicalInformation/species">
                  <xsl:for-each select="speciesPopulation">
                    <xsl:if test="string(motivations/motivation) = ''">
                      <tr>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "speciesGroup"/>
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "speciesCode"/>
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <!--<xsl:value-of select = "scientificName"/>-->
                          <xsl:variable name="Link" select="translate(scientificName,' ','+')"/>
                          <xsl:variable name="URL" select="concat('http://eunis.eea.europa.eu/species-names-result.jsp?&amp;pageSize=10&amp;scientificName=', $Link, '&amp;relationOp=2&amp;typeForm=0&amp;showGroup=true&amp;showOrder=true&amp;showFamily=true&amp;showScientificName=true&amp;showVernacularNames=true&amp;showValidName=true&amp;searchSynonyms=true&amp;sort=2&amp;ascendency=0')"/>
                          <a>
                            <xsl:attribute name="href">
                              <xsl:value-of select="$URL"/>
                            </xsl:attribute>
                            <xsl:attribute name="target">
                              blank
                            </xsl:attribute>
                            <xsl:value-of select="scientificName"/>
                          </a>

                        </td>
                        <td class="DivisorTop DivisorRight">
                           <xsl:if test="sensitiveInfo='true'">
                            Yes
                          </xsl:if>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:if test="nonPresenceInSite='true'">
                            X
                          </xsl:if>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "populationType"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "populationSize/lowerBound"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "populationSize/upperBound"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "populationSize/countingUnit"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "abundanceCategory"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "dataQuality"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "population"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "conservation"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "isolation"/>&#160;
                        </td>
                        <td class="DivisorTop">
                          <xsl:value-of select = "global"/>&#160;
                        </td>
                      </tr>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>
              </table>
              <ul class="legend">
                <li>
                  <b>Group:</b> A = Amphibians, B = Birds, F = Fish, I = Invertebrates, M = Mammals, P = Plants, R = Reptiles
                </li>
                <li>
                  <b>S:</b> in case that the data on species are sensitive and therefore have to be blocked for any public access enter: yes
                </li>
                <li>
                  <b>NP:</b> in case that a species is no longer present in the site enter: x (optional)
                </li>
                <li>
                  <b>Type:</b> p = permanent, r = reproducing, c = concentration, w = wintering (for plant and non-migratory species use permanent)
                </li>
                <li>
                  <b>Unit:</b> i = individuals, p = pairs or other units according to the Standard list of population units and codes in accordance with Article 12 and 17 reporting (see <a href="http://bd.eionet.europa.eu/activities/Natura_2000/reference_portal">reference portal</a>)
                </li>
                <li>
                  <b>Abundance categories (Cat.):</b> C = common, R = rare, V = very rare, P = present - to fill if data are deficient (DD) or in addition to population size information
                </li>
                <li>
                  <b>Data quality:</b> G = 'Good' (e.g. based on surveys); M = 'Moderate' (e.g. based on partial data with some extrapolation); P = 'Poor' (e.g. rough estimation); VP = 'Very poor' (use this category only, if not even a rough estimation of the population size can be made, in this case the fields for population size can remain empty, but the field "Abundance categories" has to be filled in)
                </li>
              </ul>
            </xsl:when>
          </xsl:choose>

          <h2>3.3 Other important species of flora and fauna (optional)</h2>
          <xsl:if test="string(ecologicalInformation/species/speciesPopulation/motivations/motivation)">
            <xsl:choose>
              <xsl:when test="string(ecologicalInformation/species)">
                <table class="SDFtable3rd WholeWidth">
                  <tr>
                    <th colspan="5" class="MinimalHeight DivisorRight CenterText">
                      <h3>Species</h3>
                    </th>
                    <th colspan="4" class="DivisorRight">
                      <h3>Population in the site</h3>
                    </th>
                    <th colspan="6">
                      <h3>Motivation</h3>
                    </th>
                  </tr>
                  <tr>
                    <th class="DivisorTop DivisorRight">
                      Group
                    </th>
                    <th class="DivisorTop DivisorRight">
                      CODE
                    </th>
                    <th class="DivisorTop DivisorRight">
                      Scientific Name
                    </th>
                    <th class="DivisorTop DivisorRight">
                      S
                    </th>
                    <th class="DivisorTop DivisorRight">
                      NP
                    </th>
                    <th class="DivisorTop DivisorRight" colspan="2">
                      Size
                    </th>
                    <th class="DivisorTop DivisorRight">
                      Unit
                    </th>
                    <th class="DivisorTop DivisorRight">
                      Cat.
                    </th>
                    <th class="DivisorTop DivisorRight" colspan="2">
                      Species Annex
                    </th>
                    <th class="DivisorTop DivisorRight" colspan="4">
                      Other categories
                    </th>
                  </tr>
                  <tr>
                    <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                    <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                    <th class="DivisorTop DivisorRight" width="15%">&#160;</th>
                    <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                    <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                    <th class="DivisorTop DivisorRight" width="10%">Min</th>
                    <th class="DivisorTop DivisorRight" width="10%">Max</th>
                    <th class="DivisorTop DivisorRight" width="5%">&#160;</th>
                    <th class="DivisorTop DivisorRight" width="10%">C|R|V|P</th>
                    <th class="DivisorTop DivisorRight" width="5%">IV</th>
                    <th class="DivisorTop DivisorRight" width="5%">V</th>
                    <th class="DivisorTop DivisorRight" width="5%">A</th>
                    <th class="DivisorTop DivisorRight" width="5%">B</th>
                    <th class="DivisorTop DivisorRight" width="5%">C</th>
                    <th class="DivisorTop DivisorRight" width="5%">D</th>
                  </tr>
                  <xsl:for-each select="ecologicalInformation/species/speciesPopulation">
                    <xsl:if test="string(motivations/motivation) != ''">
                      <tr>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "speciesGroup"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "speciesCode"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:variable name="Link" select="translate(scientificName,' ','+')"/>
                          <xsl:variable name="URL" select="concat('http://eunis.eea.europa.eu/species-names-result.jsp?&amp;pageSize=10&amp;scientificName=', $Link, '&amp;relationOp=2&amp;typeForm=0&amp;showGroup=true&amp;showOrder=true&amp;showFamily=true&amp;showScientificName=true&amp;showVernacularNames=true&amp;showValidName=true&amp;searchSynonyms=true&amp;sort=2&amp;ascendency=0')"/>
                          <a>
                            <xsl:attribute name="href">
                              <xsl:value-of select="$URL" />
                            </xsl:attribute>
                            <xsl:attribute name="target">
                              blank
                            </xsl:attribute>
                            <xsl:value-of select="scientificName"/>
                          </a>

                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:if test="sensitiveInfo='true'">
                            Yes
                          </xsl:if>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:if test="nonPresenceInSite='true'">
                            X
                          </xsl:if>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "populationSize/lowerBound"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "populationSize/upperBound"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "populationSize/countingUnit"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select = "abundanceCategory"/>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:for-each select="motivations">
                            <xsl:if test="motivation = 'IV'">X</xsl:if>
                          </xsl:for-each>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:for-each select="motivations">
                            <xsl:if test="motivation = 'V'">X</xsl:if>
                          </xsl:for-each>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:for-each select="motivations">
                            <xsl:if test="motivation = 'A'">X</xsl:if>
                          </xsl:for-each>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:for-each select="motivations">
                            <xsl:if test="motivation = 'B'">X</xsl:if>
                          </xsl:for-each>&#160;
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:for-each select="motivations">
                            <xsl:if test="motivation = 'C'">X</xsl:if>
                          </xsl:for-each>&#160;
                        </td>
                        <td class="DivisorTop">
                          <xsl:for-each select="motivations">
                            <xsl:if test="motivation = 'D'">X</xsl:if>
                          </xsl:for-each>&#160;
                        </td>
                      </tr>
                    </xsl:if>
                  </xsl:for-each>
                </table>
                <ul class="legend">
                  <li>
                    <b>Group:</b> A = Amphibians, B = Birds, F = Fish, Fu = Fungi, I = Invertebrates, L = Lichens, M = Mammals, P = Plants, R = Reptiles
                  </li>
                  <li>
                    <b>CODE:</b> for Birds, Annex IV and V species the code as provided in the reference portal should be used in addition to the scientific name
                  </li>
                  <li>
                    <b>S:</b> in case that the data on species are sensitive and therefore have to be blocked for any public access enter: yes
                  </li>
                  <li>
                    <b>NP:</b> in case that a species is no longer present in the site enter: x (optional)
                  </li>
                  <li>
                    <b>Unit:</b> i = individuals, p = pairs or other units according to the standard list of population units and codes in accordance with Article 12 and 17 reporting, (see <a href="http://bd.eionet.europa.eu/activities/Natura_2000/reference_portal">reference portal</a>)
                  </li>
                  <li>
                    <b>Cat.:</b> Abundance categories: C = common, R = rare, V = very rare, P = present
                  </li>
                  <li>
                    <b>Motivation categories: </b>
                    <b>IV, V:</b> Annex Species (Habitats Directive), <b>A:</b> National Red List data; <b>B:</b> Endemics; <b>C:</b> International Conventions; <b>D:</b> other reasons
                  </li>
                </ul>
              </xsl:when>
            </xsl:choose>
          </xsl:if>
        <!--  /xsl:when>
      </xsl:choose -->
      <!-- **********************************************************************************************************************************
                                                    ENDING ECOLOGICAL INFORMATION PART
          ************************************************************************************************************************************-->
      <!-- **********************************************************************************************************************************
                                                    SARTING SITE DESCRIPTION PART
          ************************************************************************************************************************************-->
      <!-- xsl:choose>
        <xsl:when test="string(siteDescription) or string(otherSiteCharacteristics) or string(qualityAndImportance) or string(impacts) or string(ownership) or string(documentation)" -->
          <xsl:variable name="coverSum">
            <xsl:call-template name="coverSum">
              <xsl:with-param name="habitatList" select="/*/*/*/habitatClass"/>
            </xsl:call-template>
          </xsl:variable>
          <a name="4">
            <span/>
          </a>
          <h1>4. SITE DESCRIPTION</h1>
          <a href="#top" class="BackTopLink">Back to top</a>

          <h2>4.1 General site character</h2>
          <xsl:choose>
            <xsl:when test="string(siteDescription)">

              <table class="SDFtable WholeWidth">
                <tr>
                  <td width="90%" class="">
                    <h3>Habitat class</h3>
                  </td>
                  <td width="10%">
                    <h3>% Cover</h3>
                  </td>
                </tr>
                <xsl:for-each select="siteDescription/habitatClass">
                  <tr>
                    <td class="DivisorTop DivisorRight">
                      <xsl:value-of select="code"/>
                    </td>
                    <td class="DivisorTop">
                      <xsl:value-of select="coveragePercentage"/>
                    </td>
                  </tr>
                </xsl:for-each>
                <tr>
                  <td class="DivisorTop DivisorRight">
                    <h4>Total Habitat Cover</h4>
                  </td>
                  <td class="DivisorTop">
                    <xsl:value-of select="$coverSum"/>
                  </td>
                </tr>
              </table>
            </xsl:when>
          </xsl:choose>

          <xsl:choose>
            <xsl:when test="string(siteDescription/otherSiteCharacteristics)">
              <h3>Other Site Characteristics</h3>
              <p class="Border WholeWidth">
                <xsl:value-of select="siteDescription/otherSiteCharacteristics"/>
              </p>
            </xsl:when>
          </xsl:choose>

          <h2>4.2 Quality and importance</h2>
          <xsl:choose>
            <xsl:when test="string(siteDescription/qualityAndImportance)">
              <p class="Border">
                <xsl:value-of select = "siteDescription/qualityAndImportance"/>
              </p>
            </xsl:when>
          </xsl:choose>


          <h2>4.3 Threats, pressures and activities with impacts on the site</h2>
          <xsl:choose>
            <xsl:when test="string(siteDescription/impacts)">
              <p>The most important impacts and activities with high effect on the site</p>
              <div class="WholeWidth">
                <table class="HalfWidthTable">
                  <tr>
                    <td colspan="4">Negative Impacts</td>
                  </tr>
                  <tr>
                    <td width="25%" class="DivisorTop DivisorRight">Rank</td>
                    <td width="25%" class="DivisorTop DivisorRight">Threats and pressures [code]</td>
                    <td width="25%" class="DivisorTop DivisorRight">Pollution (optional) [code]</td>
                    <td width="25%" class="DivisorTop">inside/outside [i|o|b]</td>
                  </tr>
                  <xsl:for-each select="siteDescription/impacts/impact">
                    <xsl:if test="natureOfImpact='Negative'">
                      <tr>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select="rank"/>
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select="code"/>
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select="pollutionCode"/>
                        </td>
                        <td class="DivisorTop">
                          <xsl:value-of select="occurrence"/>
                        </td>
                      </tr>
                    </xsl:if>
                  </xsl:for-each>
                </table>

                <table class="HalfWidthTable">
                  <tr>
                    <td colspan="4">Positive Impacts</td>
                  </tr>
                  <tr>
                    <td width="25%" class="DivisorTop DivisorRight">Rank</td>
                    <td width="30%" class="DivisorTop DivisorRight">Activities, management [code]</td>
                    <td width="25%" class="DivisorTop DivisorRight">Pollution (optional) [code]</td>
                    <td width="20%" class="DivisorTop">inside/outside [i|o|b]</td>
                  </tr>
                  <xsl:for-each select="siteDescription/impacts/impact">
                    <xsl:if test="natureOfImpact='Positive'">
                      <tr>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select="rank"/>
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select="code"/>
                        </td>
                        <td class="DivisorTop DivisorRight">
                          <xsl:value-of select="pollutionCode"/>
                        </td>
                        <td class="DivisorTop">
                          <xsl:value-of select="occurrence"/>
                        </td>
                      </tr>
                    </xsl:if>
                  </xsl:for-each>
                </table>
                <p class="mapLegend">
                  Rank: H = high, M = medium, L = low<br />
                  Pollution: N = Nitrogen input, P = Phosphor/Phosphate input, A = Acid input/acidification,<br />
                  T = toxic inorganic chemicals, O = toxic organic chemicals, X = Mixed pollutions<br />
                  i = inside, o = outside, b = both
                </p>
              </div>
            </xsl:when>
          </xsl:choose>

          <h2>4.4 Ownership (optional)</h2>
          <xsl:choose>
            <xsl:when test="string(siteDescription/ownership/ownershipPart)">
              <table class="HalfWidthTable">
                <tr>
                  <td colspan="2" class="DivisorRight">Type</td>
                  <td>[%]</td>
                </tr>
                <tr>
                  <td width="10%" rowspan="4" class="DivisorTop DivisorRight">Public</td>
                  <td width="50%" class="DivisorTop DivisorRight">National/Federal</td>
                  <td width="40%" class="DivisorTop">
                    <xsl:value-of select="sum(siteDescription/ownership/ownershipPart[ownershiptype='publicNational']/percent)" />
                  </td>
                </tr>
                <tr>
                  <td class="DivisorTop DivisorRight">State/Province</td>
                  <td class="DivisorTop">
                    <xsl:value-of select="sum(siteDescription/ownership/ownershipPart[ownershiptype='publicState']/percent)" />
                  </td>
                </tr>
                <tr>
                  <td class="DivisorTop DivisorRight">Local/Municipal</td>
                  <td class="DivisorTop">
                    <xsl:value-of select="sum(siteDescription/ownership/ownershipPart[ownershiptype='publicLocal']/percent)" />
                  </td>
                </tr>
                <tr>
                  <td class="DivisorTop DivisorRight">Any Public</td>
                  <td class="DivisorTop">
                    <xsl:value-of select="sum(siteDescription/ownership/ownershipPart[ownershiptype='publicAny']/percent)" />
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="DivisorTop DivisorRight">Joint or Co-Ownership</td>
                  <td class="DivisorTop">
                    <xsl:value-of select="sum(siteDescription/ownership/ownershipPart[ownershiptype='joint']/percent)" />
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="DivisorTop DivisorRight">Private</td>
                  <td class="DivisorTop">
                    <xsl:value-of select="sum(siteDescription/ownership/ownershipPart[ownershiptype='private']/percent)" />
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="DivisorTop DivisorRight">Unknown</td>
                  <td class="DivisorTop">
                    <xsl:value-of select="sum(siteDescription/ownership/ownershipPart[ownershiptype='unknown']/percent)" />
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="DivisorTop DivisorRight">sum</td>
                  <td class="DivisorTop">
                    <xsl:value-of select="sum(siteDescription/ownership/ownershipPart/percent)" />
                  </td>
                </tr>
              </table>
              <div class="clearFloats"></div>
              </xsl:when>
          </xsl:choose>

          <h2>4.5 Documentation</h2>
          <xsl:choose>
            <xsl:when test="string(siteDescription/documentation)">
              <p class="Border WholeWidth">
                <xsl:value-of select = "siteDescription/documentation/description"/>
              </p>
              <xsl:choose>
                <xsl:when test="string(siteDescription/documentation/links/link)">
                  <label class="top">Link(s):</label>
                  <span class="underlinedText">
                    <xsl:for-each select="siteDescription/documentation/links/link">
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="." />
                        </xsl:attribute>
                        <xsl:value-of select = "."/>
                      </a>
                      <br />
                    </xsl:for-each>
                  </span>
                </xsl:when>
              </xsl:choose>
            </xsl:when>
          </xsl:choose>
        <!--  /xsl:when>
      </xsl:choose -->
      <!-- **********************************************************************************************************************************
                                                    ENDING SITE DESCRIPTION PART
          ************************************************************************************************************************************-->
      <!-- **********************************************************************************************************************************
                                                    SARTING SITE PROTECTION STATUS PART
          ************************************************************************************************************************************-->
      <!--  xsl:choose>
        <xsl:when test="string(siteProtection/nationalDesignations) or string(siteProtection/relations) or string(siteProtection/siteDesignation)" -->
          <a name="5">
            <span/>
          </a>
          <h1>5. SITE PROTECTION STATUS (optional)</h1>
          <a href="#top" class="BackTopLink">Back to top</a>

          <h2>5.1 Designation types at national and regional level:</h2>
          <xsl:choose>
            <xsl:when test="string(siteProtection/nationalDesignations)">
              <xsl:variable name="numNatDesigns" select="count(siteProtection/nationalDesignations/nationalDesignation)"></xsl:variable>
              <xsl:variable name="numRowsNatDesigns" select="$numNatDesigns div 3"></xsl:variable>
              <table class="SDFtableNoBorder WholeWidth">
                <tr>
                  <td width="15%">
                    <h3>Code</h3>
                  </td>
                  <td width="15%">
                    <h3>Cover [%]</h3>
                  </td>
                  <td width="5%">
                  </td>
                  <td width="15%">
                    <h3>Code</h3>
                  </td>
                  <td width="15%">
                    <h3>Cover [%]</h3>
                  </td>
                  <td width="5%">
                  </td>
                  <td width="15%">
                    <h3>Code</h3>
                  </td>
                  <td width="15%">
                    <h3>Cover [%]</h3>
                  </td>
                </tr>

                <xsl:call-template name="design" />

              </table>
            </xsl:when>
          </xsl:choose>

          <h2>5.2 Relation of the described site with other sites:</h2>
          <xsl:choose>
            <xsl:when test="string(siteProtection/relations/nationalRelationships)">
              <br />
              designated at national or regional level:
              <table class="SDFtableNoBorder WholeWidth">
                <tr>
                  <td width="15%">
                    <h3>Type code</h3>
                  </td>
                  <td width="65%%">
                    <h3>Site name</h3>
                  </td>
                  <td width="8%">
                    <h3>Type</h3>
                  </td>
                  <td width="12%">
                    <h3>Cover [%]</h3>
                  </td>
                </tr>
                <xsl:for-each select="siteProtection/relations/nationalRelationships/nationalRelationship">
                  <tr>
                    <td class="CellBorder">
                      <xsl:value-of select="designationCode"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

              </table><br />
            </xsl:when>
          </xsl:choose>

          <xsl:choose>
            <xsl:when test="string(siteProtection/relations/internationalRelationships)">
              designated at international level:
              <table class="SDFtableNoBorder WholeWidth">
                <tr>
                  <td width="23%">
                    <h3>Type</h3>
                  </td>
                  <td width="57%%">
                    <h3>Site name</h3>
                  </td>
                  <td width="8%">
                    <h3>Type</h3>
                  </td>
                  <td width="12%">
                    <h3>Cover [%]</h3>
                  </td>
                </tr>

                <xsl:variable name="numberRamsar" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='ramsar'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='ramsar']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberRamsar"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberBiogenetic" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='biogenetic'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='biogenetic']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberBiogenetic"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberEurodiploma" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='eurodiploma'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='eurodiploma']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberEurodiploma"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberBiosphere" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='biosphere'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='biosphere']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberBiosphere"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberBarcelona" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='barcelona'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='barcelona']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberBarcelona"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberBucharest" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='bucharest'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='bucharest']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberBucharest"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberWorldHeritage" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='worldHeritage'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='worldHeritage']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberWorldHeritage"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberHelcom" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='helcom'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='helcom']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberHelcom"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberOspar" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='ospar'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='ospar']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberOspar"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberMarine" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='protectedMarine'])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='protectedMarine']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberMarine"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>

                <xsl:variable name="numberOther" select="count(siteProtection/relations/internationalRelationships/internationalRelationship[convention='other']) + count(siteProtection/relations/internationalRelationships/internationalRelationship[convention=''])" />
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='other']">
                  <tr>
                    <xsl:if test="position() = 1">
                      <td class="CellBorder">
                        <xsl:attribute name="rowspan">
                          <xsl:value-of select="$numberOther"/>
                        </xsl:attribute>
                        <xsl:value-of select="convention"/>
                      </td>
                    </xsl:if>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>
                <xsl:for-each select="siteProtection/relations/internationalRelationships/internationalRelationship[convention='']">
                  <tr>
                    <xsl:choose>
                      <xsl:when test="position() = 1">
                        <td class="CellBorder">
                          <xsl:attribute name="rowspan">
                            <xsl:value-of select="$numberOther"/>
                          </xsl:attribute>
                          Other
                        </td>
                      </xsl:when>
                    </xsl:choose>
                    <td class="CellBorder">
                      <xsl:value-of select="siteName"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="type"/>
                    </td>
                    <td class="CellBorder">
                      <xsl:value-of select="cover"/>
                    </td>
                  </tr>
                </xsl:for-each>
              </table>
            </xsl:when>
          </xsl:choose>

          <h2>5.3 Site designation (optional)</h2>
          <xsl:choose>
            <xsl:when test="string(siteProtection/siteDesignationAdditional)">
              <p class="Border WholeWidth">
                <xsl:value-of select = "siteProtection/siteDesignationAdditional"/>
              </p>

            </xsl:when>
          </xsl:choose>

        <!--  /xsl:when>
      </xsl:choose -->
      <!-- **********************************************************************************************************************************
                                                    ENDING SITE PROTECTION STATUS PART
          ************************************************************************************************************************************-->
      <!-- **********************************************************************************************************************************
                                                    SARTING SITE MANAGEMENT PART
          ************************************************************************************************************************************-->
      <xsl:choose>
        <xsl:when test="string(siteManagement/managementBodies) or string(siteManagement/managementPlans) or string(siteManagement/conservationMeasures)">
          <a name="6">
            <span/>
          </a>
          <h1>6. SITE MANAGEMENT</h1>
          <a href="#top" class="BackTopLink">Back to top</a>

          <h2>6.1 Body(ies) responsible for the site management:</h2>
          <xsl:choose>
            <xsl:when test="string(siteManagement/managementBodies)">
              <xsl:for-each select="siteManagement/managementBodies/managementBody">
                <table class="SDFtable WholeWidth">
                  <tr>
                    <td width="25%">Organisation:</td>
                    <td class="CellBorderBottom">
                      <xsl:value-of select="organisation"/>
                    </td>
                  </tr>
                  <tr>
                    <td width="25%">Address:</td>
                    <td class="CellBorderBottom">
                      <xsl:value-of select="address"/>
                    </td>
                  </tr>
                  <tr>
                    <td width="25%">Email:</td>
                    <td class="CellBorderBottom">
                      <xsl:value-of select="email"/>
                    </td>
                  </tr>
                </table>
              </xsl:for-each>
            </xsl:when>
          </xsl:choose>


          <h2>6.2 Management Plan(s):</h2>
          An actual management plan does exist:
          <xsl:for-each select="siteManagement/managementPlans">
            <xsl:variable name="exists" select="exists"/>
            <table class="SDFtable WholeWidth">
              <tr>
                <td width="4%" class="top">
                  <div class="CheckType">
                    <xsl:if test="$exists = 'Y'">X</xsl:if>
                  </div>
                </td>
                <td width="10%" class="top">
                  Yes
                </td>
                <td width="86%">
                  <xsl:for-each select="managementPlan">
                    Name: <xsl:value-of select="name"/><br />
                    Link: <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="url"/>
                      </xsl:attribute>
                      <xsl:value-of select="url"/>
                    </a><br />
                    <div class="CellBorderBottom"></div><br />
                  </xsl:for-each>
                </td>
              </tr>
              <tr>
                <td width="4%" class="top">
                  <div class="CheckType">
                    <xsl:if test="$exists = 'P'">X</xsl:if>
                  </div>
                </td>
                <td colspan="2" class="top">No, but in preparation</td>
              </tr>
              <tr>
                <td width="4%" class="top">
                  <div class="CheckType">
                    <xsl:if test="$exists = 'N' or $exists = ''">X</xsl:if>
                  </div>
                </td>
                <td colspan="2" class="top">No</td>
              </tr>
            </table>
          </xsl:for-each>

          <h2>6.3 Conservation measures (optional)</h2>
          <xsl:choose>
            <xsl:when test="string(siteManagement/conservationMeasures)">
              <p class="Border WholeWidth">
                <xsl:value-of select = "siteManagement/conservationMeasures"/>
              </p>
            </xsl:when>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>
      <!-- **********************************************************************************************************************************
                                                    ENDING SITE MANAGEMENT PART
          ************************************************************************************************************************************-->
      <!-- **********************************************************************************************************************************
                                                    SARTING MAP OF SITES PART
          ************************************************************************************************************************************-->
      <h1>7. MAP OF THE SITES</h1>
      <xsl:choose>
        <xsl:when test="string(map/pdfProvided) or string(map/mapReference) or string(map/InspireID)">
          <a name="7">
            <span/>
          </a>
          <a href="#top" class="BackTopLink">Back to top</a>
          <div class="clearFloats"></div>

          <table class="SDFtableNoBorder">
            <xsl:choose>
              <xsl:when test="map/InspireID">
                <tr>
                  <td width="7%">INSPIRE ID:</td>
                  <td width="93%" class="CellBorder">
                    <xsl:value-of select="map/InspireID"/>
                  </td>
                </tr>
                <tr>
                  <td>
                    <br />
                    <br />
                  </td>
                </tr>
              </xsl:when>
            </xsl:choose>
            <xsl:choose>
              <xsl:when test="map/pdfProvided">
                <xsl:variable name="PDF" select="map/pdfProvided"/>
                <tr>
                  <td colspan="2">Map delivered as PDF in electronic format (optional)</td>
                </tr>

                <tr>
                    <td>
                          <table>
                              <tbody>
                                  <tr>
                                    <td style="width:30px;">
                                        <div class="CheckType">
                                          <xsl:if test="$PDF = 'true'">X</xsl:if>
                                        </div>
                                    </td>
                                    <td style="width:30px;">
                                        Yes
                                    </td>
                                    <td style="width:30px;">
                                        <div class="CheckType">
                                            <xsl:if test="$PDF = 'false'">X</xsl:if>
                                        </div>
                                    </td>
                                    <td style="width:30px;">
                                        No
                                    </td>
                                  </tr>
                              </tbody>
                          </table>
                    </td>
                    <td>

                    </td>
                </tr>

                <tr>
                  <td>
                    <br />
                    <br />
                  </td>
                  <td></td>
                  <td></td>
                  <td></td>
                  <td></td>
                </tr>
              </xsl:when>
            </xsl:choose>
            <xsl:choose>
              <xsl:when test="map/mapReference">
                <tr>
                  <td colspan="2">Reference(s) to the original map used for the digitalisation of the electronic boundaries (optional).</td>
                </tr>
                <tr>
                  <td colspan="2" class="CellBorder" style="height:50px;">
                    <xsl:value-of select="map/mapReference"/>
                  </td>
                </tr>
              </xsl:when>
            </xsl:choose>
          </table>
        </xsl:when>
      </xsl:choose>
      <!-- **********************************************************************************************************************************
                                                    ENDING MAP OF SITES PART
          ************************************************************************************************************************************-->
    </div>
    <!-- End div class="SDFContent" -->
      </body>
    </html>



  </xsl:template>

  <xsl:template name="coverSum">
    <xsl:param name="habitatList"/>
    <xsl:choose>
      <xsl:when test="$habitatList">
        <xsl:variable name="recursive_result">
          <xsl:call-template name="coverSum">
            <xsl:with-param name="habitatList" select="$habitatList[position() > 1]"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="number($habitatList[1]/coveragePercentage) + $recursive_result"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="0"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="design" match="siteProtection/nationalDesignations">
    <xsl:apply-templates select="siteProtection/nationalDesignations/nationalDesignation[position() mod 3 = 1]" mode="row" />
  </xsl:template>

  <xsl:template match="nationalDesignations/nationalDesignation" mode="row">
    <tr>
      <xsl:apply-templates select=". | following-sibling::nationalDesignation[position() &lt; 3]" />
    </tr>
  </xsl:template>

  <xsl:template match="nationalDesignation">
    <td class="CellBorder">
      <xsl:value-of select="designationCode"/>
    </td>
    <td class="CellBorder">
      <xsl:value-of select="cover"/>
    </td>
    <td></td>
  </xsl:template>

  <xsl:template match="nationalDesignation4567" mode="cell">
    <td></td>
  </xsl:template>


  <!-- ************************************************************************************************************************************
  *****************************************************************************************************************************************
                                              END OF THE PART FOR THE NEW STRUCTURE OF THE XMLs
  *****************************************************************************************************************************************
  **************************************************************************************************************************************-->

</xsl:stylesheet>