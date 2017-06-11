#explore the geotiff layers, including distribution, spatial auto-correlation, etc
require(raster);
require(maptools)
require(rpart)
require(randomForest)

ref=raster("~/Research/SDT/Data_Wetlands_BigStoneSwanLake_Cloquet/subareaTruth2.tif");
ref[ref>0]=1;
f1=raster("~/Research/SDT/Data_Wetlands_BigStoneSwanLake_Cloquet/subareaSwanLakeResampled1.tif",1);
f2=raster("~/Research/SDT/Data_Wetlands_BigStoneSwanLake_Cloquet/subareaSwanLakeResampled2.tif",1);
f3=raster("~/Research/SDT/Data_Wetlands_BigStoneSwanLake_Cloquet/subareaSwanLakeResampled3.tif",1);
f4=raster("~/Research/SDT/Data_Wetlands_BigStoneSwanLake_Cloquet/subareaSwanLakeResampled4.tif",1);

ext=extent(406160,407247,4907645,4908523)
ref_=crop(ref,ext)
f1_=crop(f1,ext)
f2_=crop(f2,ext)
f3_=crop(f3,ext)
f4_=crop(f4,ext)
all.dat=cbind(values(f1_),values(f2_),values(f3_),values(f4_),values(ref_));
all.dat=as.data.frame(all.dat)
 all.dat[,5]=as.factor(all.dat[,5])
names(all.dat)=c("f1","f2","f3","f4","class")

fit = rpart(class ~ ., data=all.dat, method = "class")
predict.i = predict(fit, all.dat, type="vector");
predict.i= predict.i-1;
res=ref_; res[1:ncell(ref_)]=predict.i

res[res==0&ref_==1]=2; res[res==1&ref_==0]=3;
dev.new();plot(res,col=c("red","green","black","blue"))