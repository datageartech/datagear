#!/bin/sh

JAVA_HOME=$JAVA_HOME
JAVA_OPTS=$JAVA_OPTS

DG_ECHO_PREFIX="[DataGear] :"

DG_APP_NAME="${productNameJar}"

DG_APP_PID=0

if [ -n "$JAVA_HOME" ]; then
	echo "$DG_ECHO_PREFIX using JAVA_HOME '$JAVA_HOME'"
else
	java -version
	echo ""
	echo "$DG_ECHO_PREFIX using previous java runtime"
fi

readAppPID()
{
	if [ -n "$JAVA_HOME" ]; then
		JAVAPS=`$JAVA_HOME/bin/jps -lv | grep "$DG_APP_NAME"`
		
		if [ -n "$JAVAPS" ]; then
			DG_APP_PID=`echo $JAVAPS | awk '{print $1}'`
		else
			DG_APP_PID=0
		fi
	else
		JAVAPS=`ps -ef | grep "$DG_APP_NAME" | grep -v grep`
		
		if [ -n "$JAVAPS" ]; then
			DG_APP_PID=`echo $JAVAPS | awk '{print $2}'`
		else
			DG_APP_PID=0
		fi
	fi
}

readAppPID

if [ $DG_APP_PID -ne 0 ]; then
	echo "$DG_ECHO_PREFIX stopping... (PID=$DG_APP_PID)"
	kill -9 $DG_APP_PID
	if [ $? -eq 0 ]; then
		echo "$DG_ECHO_PREFIX stop OK"
	else
		echo "$DG_ECHO_PREFIX stop failed"
	fi
else
	echo "$DG_ECHO_PREFIX application is not running"
	echo "$DG_ECHO_PREFIX stop failed"
fi
