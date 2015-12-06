#!/system/bin/sh
cd $2
export S1="$1"
shift
shift
$S1/xash_sse2 $* 2>&1
