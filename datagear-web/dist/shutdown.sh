#!/bin/sh

JAVA_HOME=$JAVA_HOME
JAVA_OPTS=$JAVA_OPTS

APP_MAIN="${project.build.finalName}.jar"

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
	echo "stopping... (PID=$APP_PID)"
	kill -9 $APP_PID
	if [ $? -eq 0 ]; then
		echo "stopping [OK]"
	else
		echo "stopping [Failed]"
	fi
else
	echo "application is not running"
	echo "stopping [Failed]"
fi
