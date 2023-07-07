
echo "removing motion model sdk folder in m2..."
rm -r ~/.m2/repository/com/bbn/landsar/motion-model-sdk/

echo "removing motion model sdk from gradle cache..."
ls  ~/.gradle/caches/modules-2/files-2.1/com.bbn.landsar/*
