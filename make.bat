"%LOCALAPPDATA%\programs\python\python35-32\Scripts\rst2html.py" test.rst >test.tmp.html
cscript //Nologo xslttransformer.vbs requirement-summary.xsl test.tmp.html test.html
