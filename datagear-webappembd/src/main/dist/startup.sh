#!/bin/sh

JAVA_HOME=$JAVA_HOME
JAVA_OPTS=$JAVA_OPTS
JAVA_VERSION=""

APP_CLASSPATH=.:lib/datagear-webappembd-1.11.1.jar:lib/javax.servlet-3.0.0.v201112011016.jar:lib/jetty-all-8.1.22.v20160922.jar:lib/slf4j-api-1.7.9.jar:lib/slf4j-log4j12-1.7.9.jar:lib/log4j-1.2.17.jar

APP_NAME="Data Gear"
ECHO_PREFIX="[$APP_NAME]  :"
BORDER="========================================="

APP_MAIN="org.datagear.webappembd.App"

APP_PID=0

echo "$BORDER"

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
		nohup $JAVA_HOME/bin/java $JAVA_OPTS -cp $APP_CLASSPATH $APP_MAIN >/dev/null 2>&1 &
	else
		nohup java $JAVA_OPTS -cp $APP_CLASSPATH $APP_MAIN >/dev/null 2>&1 &
	fi
	
	readAppPID
	
	if [ $APP_PID -ne 0 ]; then
		echo "$ECHO_PREFIX PID is $APP_PID"
		echo "$ECHO_PREFIX starting [OK]"
	else
		echo "$ECHO_PREFIX starting [Failed]"
	fi
fi

echo "$BORDER"