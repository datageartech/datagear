#!/bin/sh

JAVA_HOME=$JAVA_HOME
JAVA_OPTS=$JAVA_OPTS

APP_NAME="Data Gear"
ECHO_PREFIX="[$APP_NAME]  :"
BORDER="========================================="

APP_MAIN="org.datagear.webappembd.App"

APP_PID=0

echo "$BORDER"
echo "$ECHO_PREFIX using JAVA_HOME '$JAVA_HOME'"

readAppPID(){
	JAVAPS=`$JAVA_HOME/bin/jps -l | grep $APP_MAIN`

	if [ -n "$JAVAPS" ]; then
		APP_PID=`echo $JAVAPS | awk '{print $1}'`
	else
		APP_PID=0
	fi
}

readAppPID

if [ $APP_PID -ne 0 ]; then
	echo "$ECHO_PREFIX stopping... (PID=$APP_PID)"
	kill -9 $APP_PID
	if [ $? -eq 0 ]; then
		echo "$ECHO_PREFIX stopping [OK]"
	else
		echo "$ECHO_PREFIX stopping [Failed]"
	fi
else
	echo "$ECHO_PREFIX application is not running"
	echo "$ECHO_PREFIX stopping [Failed]"
fi

echo "$BORDER"