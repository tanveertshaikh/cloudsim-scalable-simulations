@REM SBT launcher script

.\sbt-dist\bin\sbt.bat %*

sbt clean compile test
sbt clean compile run
