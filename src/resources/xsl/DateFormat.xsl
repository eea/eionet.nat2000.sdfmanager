<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template name="FormatDate">
    <xsl:param name="DateTime" />
    <!-- Old date format 2004-07-01T22:00:00 -->
    <xsl:variable name="year">
      <xsl:value-of select ="substring($DateTime,1,4)"/>
    </xsl:variable>
    <xsl:variable name ="month-temp">
      <xsl:value-of select ="substring-after($DateTime,'-')"/>
    </xsl:variable>
    <xsl:variable name ="day-temp">
      <xsl:value-of select ="substring-after($month-temp,'-')"/>
    </xsl:variable>
    <xsl:variable name ="month">
      <xsl:value-of select ="substring-before($month-temp,'-')"/>
    </xsl:variable>
    <xsl:variable name ="day">
      <xsl:value-of select ="substring($day-temp,1,2)"/>
    </xsl:variable>

    <xsl:if test="(string-length($day) &lt; 2)">
      <xsl:value-of select="0"/>
    </xsl:if>
    <xsl:value-of select="$day"/>
    <xsl:value-of select="'-'"/>
    <xsl:choose>
      <xsl:when test="$month = '01'">Jan</xsl:when>
      <xsl:when test="$month = '02'">Feb</xsl:when>
      <xsl:when test="$month = '03'">Mar</xsl:when>
      <xsl:when test="$month = '04'">Apr</xsl:when>
      <xsl:when test="$month = '05'">May</xsl:when>
      <xsl:when test="$month = '06'">Jun</xsl:when>
      <xsl:when test="$month = '07'">Jul</xsl:when>
      <xsl:when test="$month = '08'">Aug</xsl:when>
      <xsl:when test="$month = '09'">Sep</xsl:when>
      <xsl:when test="$month = '10'">Oct</xsl:when>
      <xsl:when test="$month = '11'">Nov</xsl:when>
      <xsl:when test="$month = '12'">Dec</xsl:when>
    </xsl:choose>
    <xsl:value-of select="'-'"/>
    <xsl:value-of select="$year"/>
  </xsl:template>

  <xsl:template name="FormatYearMonth">
    <xsl:param name="DateTime" />
    <!-- Old date format 2004-07-01T22:00:00 -->
    <xsl:variable name="year">
      <xsl:value-of select ="substring($DateTime,1,4)"/>
    </xsl:variable>
    <xsl:variable name ="month-temp">
      <xsl:value-of select ="substring-after($DateTime,'-')"/>
    </xsl:variable>
    <!--<xsl:variable name ="day-temp">
      <xsl:value-of select ="substring-after($month-temp,'-')"/>
    </xsl:variable>-->
    <xsl:variable name ="month">
      <xsl:value-of select ="substring-before($month-temp,'-')"/>
    </xsl:variable>
    <!--<xsl:variable name ="day">
      <xsl:value-of select ="substring($day-temp,1,2)"/>
    </xsl:variable>

    <xsl:if test="(string-length($day) &lt; 2)">
      <xsl:value-of select="0"/>
    </xsl:if>
    <xsl:value-of select="$day"/>
    <xsl:value-of select="'-'"/>-->
    <xsl:choose>
      <xsl:when test="$month = '01'">Jan</xsl:when>
      <xsl:when test="$month = '02'">Feb</xsl:when>
      <xsl:when test="$month = '03'">Mar</xsl:when>
      <xsl:when test="$month = '04'">Apr</xsl:when>
      <xsl:when test="$month = '05'">May</xsl:when>
      <xsl:when test="$month = '06'">Jun</xsl:when>
      <xsl:when test="$month = '07'">Jul</xsl:when>
      <xsl:when test="$month = '08'">Aug</xsl:when>
      <xsl:when test="$month = '09'">Sep</xsl:when>
      <xsl:when test="$month = '10'">Oct</xsl:when>
      <xsl:when test="$month = '11'">Nov</xsl:when>
      <xsl:when test="$month = '12'">Dec</xsl:when>
    </xsl:choose>
    <xsl:value-of select="$year"/>
    <xsl:value-of select="'-'"/>
    <xsl:value-of select="$month-temp"/>
  </xsl:template>
</xsl:stylesheet>