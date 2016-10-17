rem Usage: cscript xslttransformer.vbs [transform file] [input file] [output file]

transform_filename = Wscript.Arguments(0)
input_filename = Wscript.Arguments(1) 
output_filename = Wscript.Arguments(2)

Set xmlInput = CreateObject("Msxml2.DOMDocument")
xmlInput.async = False
xmlInput.validateOnParse = True
xmlInput.load(input_filename)

Set xmlOutput = CreateObject("Msxml2.DOMDocument")

Set xmlStylesheet = CreateObject("Msxml2.DOMDocument")
xmlStylesheet.async = False
xmlStylesheet.validateOnParse = True
xmlStylesheet.load(transform_filename)

xmlInput.transformNodeToObject xmlStylesheet, xmlOutput

rem -- by default XML output is UTF-16. However, the MS manifest tool requires UTF-8 
xmlOutput.removeChild xmlOutput.firstChild
Set xmlPi = xmlOutput.createProcessingInstruction("xml", "version=""1.0"" encoding=""UTF-8"" standalone=""yes""")
xmlOutput.insertBefore xmlPi, xmlOutput.firstChild

xmlOutput.save output_filename
