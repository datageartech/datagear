#!/bin/sh

JAVA_HOME=$JAVA_HOME
JAVA_OPTS=$JAVA_OPTS
JAVA_VERSION=""

APP_MAIN="${productNameJar}"

APP_PID=0

if [ -n "$JAVA_HOME" ]; then
	echo "using JAVA_HOME '$JAVA_HOME'"
else
	java -version
	echo ""
	echo "using previous java runtime"
fi

readAppPID()
{
	if [ -n "$JAVA_HOME" ]; then
		JAVAPS=`$JAVA_HOME/bin/jps -l | grep "$APP_MAIN"`
		
		if [ -n "$JAVAPS" ]; then
			APP_PID=`echo $JAVAPS | awk '{print $1}'`
		else
			APP_PID=0
		fi
	else
		JAVAPS=`ps -ef | grep "$APP_MAIN" | grep -v grep`
		
		if [ -n "$JAVAPS" ]; then
			APP_PID=`echo $JAVAPS | awk '{print $2}'`
		else
			APP_PID=0
		fi
	fi
}

readAppPID

if [ $APP_PID -ne 0 ]; then
	echo "application is already running, PID is $APP_PID"
	echo "starting [Failed]"
else
	echo "starting..."
	
	if [ -n "$JAVA_HOME" ]; then
		nohup $JAVA_HOME/bin/java $JAVA_OPTS -jar $APP_MAIN >/dev/null 2>&1 &
	else
		nohup java $JAVA_OPTS -jar $APP_MAIN >/dev/null 2>&1 &
	fi
	
	readAppPID
	
	if [ $APP_PID -ne 0 ]; then
		echo "PID is $APP_PID"
		echo "starting [OK]"
	else
		echo "starting [Failed]"
	fi
fi
