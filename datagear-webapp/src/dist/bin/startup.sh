#!/bin/sh

JAVA_HOME=$JAVA_HOME
JAVA_OPTS=$JAVA_OPTS

#this make application can run at other path
DG_APP_HOME=$DG_APP_HOME

if [ -n "$DG_APP_HOME" ]; then
	DG_APP_HOME=$DG_APP_HOME
else
	DG_APP_HOME=`dirname "$0"`
	DG_APP_HOME=`cd "$DG_APP_HOME" >/dev/null; pwd`
fi

DG_ECHO_PREFIX="[DataGear] :"

DG_APP_NAME="${productNameJar}"
DG_APP_FULL_NAME="$DG_APP_HOME/$DG_APP_NAME"
DG_SPRING_OPTS="--spring.config.additional-location=$DG_APP_HOME/config/application.properties"

export DG_APP_HOME=$DG_APP_HOME

DG_APP_PID=0

echo "  ____        _         ____                 "
echo " |  _ \  __ _| |_ __ _ / ___| ___  __ _ _ __ "
echo " | | | |/ _\` | __/ _\` | |  _ / _ \/ _\` | '__|"
echo " | |_| | (_| | |_ (_| | |_| |  __/ (_| | |   "
echo " |____/ \__,_|\__\__,_|\____|\___|\__,_|_|   "
echo ""
echo "  DataGear-v${project.version}  http://www.datagear.tech";
echo ""

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
		JAVAPS=`$JAVA_HOME/bin/jps -l | grep "$DG_APP_NAME"`
		
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
	echo "$DG_ECHO_PREFIX application is already running, PID is $DG_APP_PID"
	echo "$DG_ECHO_PREFIX start failed"
else
	echo "$DG_ECHO_PREFIX starting..."
	
	if [ -n "$JAVA_HOME" ]; then
		nohup $JAVA_HOME/bin/java $JAVA_OPTS -jar "$DG_APP_FULL_NAME" $DG_SPRING_OPTS >/dev/null 2>&1 &
	else
		nohup java $JAVA_OPTS -jar "$DG_APP_FULL_NAME" $DG_SPRING_OPTS >/dev/null 2>&1 &
	fi
	
	readAppPID
	
	if [ $DG_APP_PID -ne 0 ]; then
		echo "$DG_ECHO_PREFIX PID is $DG_APP_PID"
		echo "$DG_ECHO_PREFIX start OK"
		echo "$DG_ECHO_PREFIX it may take some seconds for use"
	else
		echo "$DG_ECHO_PREFIX start failed"
	fi
fi
