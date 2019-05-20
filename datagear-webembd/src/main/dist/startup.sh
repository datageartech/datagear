#!/bin/sh

JAVA_HOME=$JAVA_HOME
JAVA_OPTS=$JAVA_OPTS

APP_CLASSPATH=.:lib/datagear-webembd-1.3.0.jar:lib/hamcrest-core-1.3.jar:lib/javax.servlet-3.0.0.v201112011016.jar:lib/jetty-all-8.1.22.v20160922.jar:lib/junit-4.11.jar::lib/log4j-1.2.17.jar:lib/slf4j-api-1.7.9.jar:lib/slf4j-log4j12-1.7.9.jar

APP_NAME="Data Gear"
ECHO_PREFIX="[$APP_NAME]  :"
BORDER="========================================="

APP_MAIN="org.datagear.webembd.App"

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
	echo "$ECHO_PREFIX application is already running, PID is $APP_PID"
	echo "$ECHO_PREFIX starting [Failed]"
else
	echo "$ECHO_PREFIX starting..."
	nohup $JAVA_HOME/bin/java $JAVA_OPTS -cp $APP_CLASSPATH $APP_MAIN >/dev/null 2>&1 &
	readAppPID
	if [ $APP_PID -ne 0 ]; then
		echo "$ECHO_PREFIX PID is $APP_PID"
		echo "$ECHO_PREFIX starting [OK]"
	else
		echo "$ECHO_PREFIX starting [Failed]"
	fi
fi

echo "$BORDER"