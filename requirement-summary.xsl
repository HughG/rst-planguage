<s:stylesheet
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:h="http://www.w3.org/1999/xhtml"
    xmlns:s="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
>
    <!-- =========================================================================================================== -->
    <!--
        Helper templates.
    -->

    <!-- Replace-all in a string, because XSLT 1 lacks that function; from http://stackoverflow.com/a/3067130/320456 -->
    <s:template name="string-replace-all">
        <s:param name="text" />
        <s:param name="replace" />
        <s:param name="by" />
        <s:choose>
            <s:when test="$text = '' or $replace = '' or not($replace)" >
                <!-- Prevent this routine from hanging -->
                <s:value-of select="$text" />
            </s:when>
            <s:when test="contains($text, $replace)">
                <s:value-of select="substring-before($text,$replace)" />
                <s:value-of select="$by" />
                <s:call-template name="string-replace-all">
                    <s:with-param name="text" select="substring-after($text,$replace)" />
                    <s:with-param name="replace" select="$replace" />
                    <s:with-param name="by" select="$by" />
                </s:call-template>
            </s:when>
            <s:otherwise>
                <s:value-of select="$text" />
            </s:otherwise>
        </s:choose>
    </s:template>

    <!-- =========================================================================================================== -->
    <!--
        Mode: none
    -->

    <!-- Default: copy elements and attributes recursively (but not comments, PIs, etc.) -->
    <s:template match="@*|node()">
        <s:copy>
            <s:apply-templates select="@*|node()"/>
        </s:copy>
    </s:template>

    <!--
        Match the summary section and create a table using the field names in the single field list in that section as
        the headers (minus the trailing ":") and the values of the corresponding fiel bodies from each function
        requirement.  A function requirement is a section whose ID begins with "fr-", that is, whose heading begins with
        "FR:"
    -->
    <s:template match="id('summary')">
        <s:apply-templates/>
        <table class="summary">
            <colgroup>
                <col span="1" class="summary-col-tag"/>
            </colgroup>
            <s:apply-templates select="id('summary')" mode="summary-headers"/>
            <s:apply-templates select="//*[contains(concat(' ', normalize-space(@class), ' '), ' section ')][starts-with(@id, 'fr-')]" mode="summary-body"/>
        </table>
    </s:template>

    <!-- Don't copy the summary spec -->
    <s:template match="*[contains(concat(' ', normalize-space(@class), ' '), ' summary-spec ')]">
    </s:template>

    <!-- In the main text, omit fields where the field-body is empty. -->
    <s:template match="*[contains(concat(' ', normalize-space(@class), ' '), ' field ')][*[contains(concat(' ', normalize-space(@class), ' '), ' field-body ')][string-length() = 0]]">
    </s:template>

    <!--
        Modify internal link text, doing a string replace to add a soft break after each ".".
    -->
    <s:template match="h:a[contains(concat(' ', normalize-space(@class), ' '), ' reference ') and contains(concat(' ', normalize-space(@class), ' '), ' internal ')]">
        <s:variable name="text-with-breaks">
            <s:call-template name="string-replace-all">
                <s:with-param name="text" select="text()" />
                <s:with-param name="replace" select="'.'" />
                <s:with-param name="by" select="'.&#x200b;'" />
            </s:call-template>
        </s:variable>
        <s:copy>
            <s:apply-templates select="@*"/>
            <s:value-of select="$text-with-breaks"/>
        </s:copy>
    </s:template>

    <!-- =========================================================================================================== -->
    <!--
        Mode: summary-headers

        Just copy the text of the field names (minus trailing ":") into <th> elements, plus an initial "Tag" header.
    -->

    <!-- Don't copy text from within the summary -->
    <s:template match="text()" mode="summary-headers">
    </s:template>

    <s:template match="*[contains(concat(' ', normalize-space(@class), ' '), ' field-list ')]" mode="summary-headers">
        <tr>
            <th>Tag</th>
            <s:apply-templates mode="summary-headers"/>
        </tr>
    </s:template>

    <s:template match="h:th[contains(concat(' ', normalize-space(@class), ' '), ' field-name ')]" mode="summary-headers">
        <th>
            <s:value-of select="substring(text(), 1, string-length(text()) - 1)"/>
        </th>
    </s:template>

    <!-- =========================================================================================================== -->
    <!--
        Mode: summary-body

        Copy the text of the field bodies into <td> elements, adding an initial cell with the FR tag as a link.
    -->

    <!-- Don't copy text from within the summary -->
    <s:template match="text()" mode="summary-body">
    </s:template>

    <!--
        Copy the section heading as a link, doing a string replace to add a soft break after each ".",
        then copy the field bodies.
    -->
    <s:template match="*[contains(concat(' ', normalize-space(@class), ' '), ' section ')]" mode="summary-body">
        <s:variable name="heading-with-breaks">
            <s:call-template name="string-replace-all">
                <s:with-param name="text" select="h:h3[1]" />
                <s:with-param name="replace" select="'.'" />
                <s:with-param name="by" select="'.&#x200b;'" />
            </s:call-template>
        </s:variable>
        <tr>
            <td><a href="#{@id}"><s:value-of select="$heading-with-breaks"/></a></td>
            <s:apply-templates mode="summary-body"/>
        </tr>
    </s:template>

    <!--
        Match a field list, then iterate over the field names from the summary section, copying in the matching field
        body from this list.
    -->
    <s:template match="*[contains(concat(' ', normalize-space(@class), ' '), ' field-list ')]" mode="summary-body">
        <s:variable name="this-field-list" select="." />
        <s:for-each select="id('summary')//h:th[contains(concat(' ', normalize-space(@class), ' '), ' field-name ')]">
            <s:variable name="required-field-name" select="text()" />
            <s:apply-templates select="$this-field-list//h:th[contains(concat(' ', normalize-space(@class), ' '), ' field-name ')][text() = $required-field-name]/following-sibling::h:td"/>
        </s:for-each>
    </s:template>
</s:stylesheet>