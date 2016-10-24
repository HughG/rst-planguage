SETLOCAL
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_112\jre

"%LOCALAPPDATA%\programs\python\python35\Scripts\rst2html.py" %~dpn1.rst >%~dpn1.tmp.html
"%JAVA_HOME%\bin\java.exe" -jar out\artifacts\rst_planguage_jar\rst-planguage.jar %~dpn1.tmp.html %~dpn1.html
xcopy /i /y *.css %~dp1
xcopy /i /y *.js %~dp1