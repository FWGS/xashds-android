#!/system/bin/sh
$1/tracker&
cat /data/data/$2/lib/libismemsplit3g1g.so > $1/is3g1g
chmod 777 $1/is3g1g
if $1/is3g1g; then
cat /data/data/$2/lib/libubt.so > $1/ubt
else
cat /data/data/$2/lib/libubt2g.so > $1/ubt
fi
chmod 777 $1/ubt
cd $3
export S1="$1"
shift
shift
shift
$S1/ubt --simd-f32-scalar-vfp --vpaths-list /dev/null --no-vfs-sanity-checks --vfs-kind host-first --vfs-hacks= --fork-controller lo:10010 -- $S1/xash_sse2 $* 2>&1
