#!/bin/sh

JAVA_HOME=$JAVA_HOME
JAVA_OPTS=$JAVA_OPTS
JAVA_VERSION=""

ECHO_PREFIX="[DataGear] :"

APP_MAIN="${productNameJar}"

APP_PID=0

echo "+-----------------------------------------+"
echo "|                                         |"
echo "|            DataGear - v${project.version}            |"
echo "|        http://www.datagear.tech         |"
echo "|                                         |"
echo "|                  (^_^)                  |"
echo "+-----------------------------------------+"
echo ""

if [ -n "$JAVA_HOME" ]; then
	echo "$ECHO_PREFIX using JAVA_HOME '$JAVA_HOME'"
else
	java -version
	echo ""
	echo "$ECHO_PREFIX using previous java runtime"
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
	echo "$ECHO_PREFIX application is already running, PID is $APP_PID"
	echo "$ECHO_PREFIX starting [Failed]"
else
	echo "$ECHO_PREFIX starting..."
	
	if [ -n "$JAVA_HOME" ]; then
		nohup $JAVA_HOME/bin/java $JAVA_OPTS -jar $APP_MAIN >/dev/null 2>&1 &
	else
		nohup java $JAVA_OPTS -jar $APP_MAIN >/dev/null 2>&1 &
	fi
	
	readAppPID
	
	if [ $APP_PID -ne 0 ]; then
		echo "$ECHO_PREFIX PID is $APP_PID"
		echo "$ECHO_PREFIX starting [OK]"
	else
		echo "$ECHO_PREFIX starting [Failed]"
	fi
fi
