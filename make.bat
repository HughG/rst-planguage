"%LOCALAPPDATA%\programs\python\python35\Scripts\rst2html.py" %~dpn1.rst >%~dpn1.tmp.html
cscript //Nologo xslttransformer.vbs requirement-summary.xsl %~dpn1.tmp.html %~dpn1.html
xcopy /i /y *.css %~dp1