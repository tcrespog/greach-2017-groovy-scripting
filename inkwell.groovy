#!/usr/bin/groovy

//Read an environment variable
String pathValue = System.getenv('PATH')
println pathValue

//Print to standard error
System.err.println('Print to standard error')

//Read standard input
BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in))
String input = inputReader.text
println input

//Execute processes in Windows: run on a Windows OS!
Process echoCmd = ['cmd', '/c', 'echo', 'Hello Windows'].execute()
println echoCmd.text

Process echoPowershell = ['powershell', 'echo', 'Hello Windows'].execute()
println echoPowershell.text

