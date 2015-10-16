@REM ----------------------------------------------------------------------------
@REM  Copyright 2001-2006 The Apache Software Foundation.
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM       http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM ----------------------------------------------------------------------------
@REM
@REM   Copyright (c) 2001-2006 The Apache Software Foundation.  All rights
@REM   reserved.

@echo off

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set CMD_LINE_ARGS=%*
goto WinNTGetScriptDir

@REM The 4NT Shell from jp software
:4NTArgs
set CMD_LINE_ARGS=%$
goto WinNTGetScriptDir

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto Win9xGetScriptDir
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto Win9xApp

:Win9xGetScriptDir
set SAVEDIR=%CD%
%0\
cd %0\..\.. 
set BASEDIR=%CD%
cd %SAVEDIR%
set SAVE_DIR=
goto repoSetup

:WinNTGetScriptDir
set BASEDIR=%~dp0\..

:repoSetup
set REPO=


if "%JAVACMD%"=="" set JAVACMD=java

if "%REPO%"=="" set REPO=%BASEDIR%\lib

set CLASSPATH="%BASEDIR%"\etc;"%REPO%"\gluegen-rt-main-2.0.2.jar;"%REPO%"\gluegen-rt-2.0.2.jar;"%REPO%"\gluegen-rt-2.0.2-natives-android-armv6.jar;"%REPO%"\gluegen-rt-2.0.2-natives-linux-amd64.jar;"%REPO%"\gluegen-rt-2.0.2-natives-linux-armv6.jar;"%REPO%"\gluegen-rt-2.0.2-natives-linux-armv6hf.jar;"%REPO%"\gluegen-rt-2.0.2-natives-linux-i586.jar;"%REPO%"\gluegen-rt-2.0.2-natives-macosx-universal.jar;"%REPO%"\gluegen-rt-2.0.2-natives-solaris-amd64.jar;"%REPO%"\gluegen-rt-2.0.2-natives-solaris-i586.jar;"%REPO%"\gluegen-rt-2.0.2-natives-windows-amd64.jar;"%REPO%"\gluegen-rt-2.0.2-natives-windows-i586.jar;"%REPO%"\jogl-all-main-2.0.2.jar;"%REPO%"\jogl-all-2.0.2.jar;"%REPO%"\jogl-all-2.0.2-natives-android-armv6.jar;"%REPO%"\jogl-all-2.0.2-natives-linux-amd64.jar;"%REPO%"\jogl-all-2.0.2-natives-linux-armv6.jar;"%REPO%"\jogl-all-2.0.2-natives-linux-armv6hf.jar;"%REPO%"\jogl-all-2.0.2-natives-linux-i586.jar;"%REPO%"\jogl-all-2.0.2-natives-macosx-universal.jar;"%REPO%"\jogl-all-2.0.2-natives-solaris-amd64.jar;"%REPO%"\jogl-all-2.0.2-natives-solaris-i586.jar;"%REPO%"\jogl-all-2.0.2-natives-windows-amd64.jar;"%REPO%"\jogl-all-2.0.2-natives-windows-i586.jar;"%REPO%"\jzy3d-api-0.9.1.jar;"%REPO%"\jzy3d-jdt-core-0.9.1.jar;"%REPO%"\commons-io-2.3.jar;"%REPO%"\commons-lang3-3.1.jar;"%REPO%"\log4j-1.2.16.jar;"%REPO%"\opencsv-2.1.jar;"%REPO%"\biojava3-core-3.0.7.jar;"%REPO%"\biojava3-alignment-3.0.7.jar;"%REPO%"\forester-lgpl-1.005.jar;"%REPO%"\biojava3-genome-3.0.7.jar;"%REPO%"\biojava3-structure-3.0.7.jar;"%REPO%"\log4j-core-2.0-beta7.jar;"%REPO%"\log4j-api-2.0-beta7.jar;"%REPO%"\xmlunit-1.4.jar;"%REPO%"\biojava3-structure-gui-3.0.7.jar;"%REPO%"\javaws-1.0.jar;"%REPO%"\jmol-13.0.12.jar;"%REPO%"\biojava3-phylo-3.0.7.jar;"%REPO%"\biojava3-modfinder-3.0.7.jar;"%REPO%"\biojava3-ws-3.0.7.jar;"%REPO%"\json-lib-2.3-jdk15.jar;"%REPO%"\commons-beanutils-1.8.0.jar;"%REPO%"\commons-collections-3.2.1.jar;"%REPO%"\commons-lang-2.4.jar;"%REPO%"\commons-logging-1.1.1.jar;"%REPO%"\ezmorph-1.0.6.jar;"%REPO%"\biojava3-aa-prop-3.0.7.jar;"%REPO%"\biojava3-protein-disorder-3.0.7.jar;"%REPO%"\NEVBLAST-1.0.01-SNAPSHOT.jar

set ENDORSED_DIR=
if NOT "%ENDORSED_DIR%" == "" set CLASSPATH="%BASEDIR%"\%ENDORSED_DIR%\*;%CLASSPATH%

if NOT "%CLASSPATH_PREFIX%" == "" set CLASSPATH=%CLASSPATH_PREFIX%;%CLASSPATH%

@REM Reaching here means variables are defined and arguments have been captured
:endInit

%JAVACMD% %JAVA_OPTS%  -classpath %CLASSPATH% -Dapp.name="nevblast" -Dapp.repo="%REPO%" -Dapp.home="%BASEDIR%" -Dbasedir="%BASEDIR%" group4.nevblast.Main %CMD_LINE_ARGS%
if %ERRORLEVEL% NEQ 0 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=%ERRORLEVEL%

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set CMD_LINE_ARGS=
goto postExec

:endNT
@REM If error code is set to 1 then the endlocal was done already in :error.
if %ERROR_CODE% EQU 0 @endlocal


:postExec

if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)

exit /B %ERROR_CODE%
