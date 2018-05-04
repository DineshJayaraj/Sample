#!/bin/sh
CONTEXT=$1
CONFIG_ENV=$2
#exec java $CONTEXT $CONFIG_ENV -jar RnaDispatchOrder-1.1.jar
echo Context value: $CONTEXT
echo Config_env value: $CONFIG_ENV
exec java $CONTEXT $CONFIG_ENV -jar /deployments/subscription-billing-history-1.0.2-SNAPSHOT.jar
