echo "starting.." 
sleep 1
echo "after 1 seconds"
sleep 1
echo "after 2 seconds"
sleep 1
echo "some sample error message" >&2
echo "exiting.."

echo $JAVA_HOME
echo $MY_VAR