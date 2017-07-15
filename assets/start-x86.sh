#!/system/bin/sh
cd $2
export S1="$1"
export S3="$3"
shift
shift
$S1/$S3 $* 2>&1
