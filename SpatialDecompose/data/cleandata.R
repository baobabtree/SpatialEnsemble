#explore the geotiff layers, including distribution, spatial auto-correlation, etc
require(raster);
require(maptools)
require(rpart)
require(randomForest)

features1 = stack("20050403/LT50270292005093PAC01_B1.TIF")

for(i in 2:7){
	tmp = raster(paste("20050403/LT50270292005093PAC01_B",i,".TIF",sep=""));
	features1 = addLayer(features1,tmp)
}
xmin=452880; ymin=4967035; xmax=454004; ymax=4967850;
ext=extent(xmin,xmax,ymin,ymax);


ref = raster("ref30m.img")
# ref[ref>0] = 1
# writeRaster(ref,"ref30m.img",filetype="HFA",overwrite=T)
xmin=452880; ymin = 4967187; xmax = 454002; ymax = 4967850;


ext=extent(xmin,xmax,ymin,ymax);

ref=crop(ref,ext)
all.dat = matrix(nrow = ncell(ref),ncol=16)
all.dat[,15] = values(ref)
all.dat[,16] = 1:ncell(ref)
for(i in 1:7){
	tmp = crop(features1[[i]],ext)
	all.dat[,i] = values(tmp)
}

#read data
all.dat=read.table("all.dat",sep=",")
fit.dat= as.data.frame(all.dat[,c(1:14,15)]);#,16:17
train.subset = sample(999,50);
fit=rpart(V15~V1+V2+V3+V4+V5+V6+V7+V8+V9+V10+V11+V12+V13+V14, #+V16+V17, control=rpart.control(min.split=10),
	data=fit.dat, subset=train.subset, method="class")
res=predict(fit,newdata=as.data.frame(all.dat[,c(1:14,15)]),type="class") #,16:17

map = ref; map[1:ncell(ref)]=as.numeric(res)-1
map[map==1&ref==0]=2;
map[map==0&ref==1]=3;
map[train.subset] = 4;
dev.new();plot(map,col=c("red","green","black","blue","white"))


require(randomForest)
our.data = all.dat[,c(1:14,16:17,15)]
our.data[,17]=as.factor(our.data[,17])
train.subset = sample(999,50);
test.subset = setdiff(1:999, train.subset)
land.rf=randomForest(V15~.,data=our.data[train.subset,], keep.forest=TRUE, ntree=500,importance=TRUE, proximity=TRUE,na.action=na.omit)
print(land.rf)

predict.all=predict(land.rf,newdata=our.data,type="response")
cf=as.numeric(our.data[,17])-1;
cf[predict.all==1&cf==0]=2
cf[predict.all==0&cf==1]=3
map = ref
map[1:ncell(map)] = cf;
map[train.subset] = NA
dev.new();plot(map,col=c("red","green","black","blue"))

all.err=length(which(predict.all!=our.data[,17]))/length(predict.all)
truth.classes=c(0,1);
truth=as.vector(all.dat[,"ref"])
cm=matrix(nrow=length(truth.classes),ncol=length(truth.classes));
for(i in 1:length(truth.classes)){
for(j in 1:length(truth.classes)){
	cm[i,j]=length(which(truth==truth.classes[i]&predict.all==truth.classes[j]));
}
}


#prepare clusters
all.dat = read.table("all.dat",sep=",")
train.loc = read.table("train.dat",sep=",")
train.loc = train.loc[,1]
input.dat = cbind(all.dat[,1:14],0, all.dat[,16:17])
input.dat[train.loc,15] = ref[train.loc]+1;
write.table(input.dat, "input.txt", sep = ",", row.n=F,col.n=F)

require(raster);
ref=raster("~/Research/SDT/Chanhassen/tif/output/refdata_raster.tif");
xmin=452880; ymin=4967035; xmax=454004; ymax=4967850;
ext=extent(xmin,xmax,ymin,ymax);
ref=crop(ref,ext)


i=0;
cls=read.table(paste("preprocess/output",i,".txt",sep=""),sep=",")
cluster.ids=unique(cls[,2])
dict=array(0,max(cluster.ids)+1)
dict[cluster.ids+1]=sample(length(cluster.ids))
c.map=ref;
c.map[cls[,1]]=dict[cls[,2]+1]

jet.colors <-
  colorRampPalette(c("#00007F", "blue", "#007FFF", "cyan",
                     "#7FFF7F", "yellow", "#FF7F00", "red", "#7F0000"))
dev.new();plot(c.map,col=jet.colors(length(cluster.ids)))
writeRaster(c.map,"cluster.img",format="HFA",overwrite=T)

train.map=ref;
train.map[train.loc]=NA;
dev.new();plot(train.map,col=c("red","green"))







features1 = stack("20040331/LT50270292004091PAC02_B1.TIF")

for(i in 2:7){
	tmp = raster(paste("20040331/LT50270292004091PAC02_B",i,".TIF",sep=""));
	features1 = addLayer(features1,tmp)
}

features2 = stack("20040806/LT50270292004219EDC00_B1.TIF")
for(i in 2:7){
	tmp = raster(paste("20040806/LT50270292004219EDC00_B",i,".TIF",sep=""));
	features2 = addLayer(features2,tmp)
}
ref = raster("ref30m.img")
# ref[ref>0] = 1
# writeRaster(ref,"ref30m.img",filetype="HFA",overwrite=T)
xmin=452880; ymin=4967035; xmax=454004; ymax=4967850;
ext=extent(xmin,xmax,ymin,ymax);

ref=crop(ref,ext)
all.dat = matrix(nrow = ncell(ref),ncol=17)
all.dat[,15] = values(ref)
all.dat[,16] = ((1:ncell(ref))-1)%/%37+1
all.dat[,17] = ((1:ncell(ref))-1)%%37+1
for(i in 1:7){
	tmp = crop(features1[[i]],ext)
	all.dat[,i] = values(tmp)
}
for(i in 1:7){
	tmp = crop(features2[[i]],ext)
	all.dat[,7+i] = values(tmp)
}
# write.table(all.dat,"scene1.csv",row.n=F, col.n=F, sep=",")
fit.dat= as.data.frame(all.dat[,1:15]);
train.subset = sample(999,150);
fit=rpart(V15~V1+V2+V3+V4+V5+V6+V7+V8+V9+V10+V11+V12+V13+V14,
	data=fit.dat, subset=train.subset, method="class")
res=predict(fit,newdata=as.data.frame(all.dat),type="class")
map = ref; map[1:ncell(ref)]=as.numeric(res)-1
map[map==1&ref==0]=2;
map[map==0&ref==1]=3;
map[train.subset] = 4;
dev.new();plot(map,col=c("red","green","black","blue","white"))

features1 = crop(features1, ext)
writeRaster(features1, file="March.tif",format="GTiff")







ref=raster("tif/output/refdata_raster.tif")
cti=raster("tif/output/cti3m.tif")
dem=raster("tif/output/dem3m.tif")
mkhd05nir=raster("tif/output/mkhd05_3m.tif",band=1)
mkhd05r=raster("tif/output/mkhd05_3m.tif",band=2)
mkhd05g=raster("tif/output/mkhd05_3m.tif",band=3)
mkhd05ndvi=raster("tif/output/mkhd05_3m_ndvi.tif")
naip03r=raster("tif/output/naip03_3m.tif",band=1)
naip03b=raster("tif/output/naip03_3m.tif",band=2)
naip03g=raster("tif/output/naip03_3m.tif",band=3)
naip08b=raster("tif/output/naip08_3m.tif",band=1)
naip08g=raster("tif/output/naip08_3m.tif",band=2)
naip08r=raster("tif/output/naip08_3m.tif",band=3)
naip08nir=raster("tif/output/naip08_3m.tif",band=4)
naip08ndvi=raster("tif/output/naip08_3m_ndvi.tif")
slope=raster("tif/output/slope3m.tif")

#unify the extent
testShp=readShapePoly("Subset_TestArea.shp");
dryShp=readShapePoly("DrylandPolyTest.shp");
wetShp=readShapePoly("wetPoly.shp")
xmin=testShp@bbox["x","min"];
xmax=testShp@bbox["x","max"];
ymin=testShp@bbox["y","min"];
ymax=testShp@bbox["y","max"];

xmin=450985; ymin=4966981; xmax=452233; ymax=4968530-121; #find good region; Scene1
# xmin=454127; ymin=4968555; xmax=455210; ymax=4970001; #middle 4 (Scene 3 in c4.5.c)

ext1=extent(xmin,xmax-60,ymin,ymax);
# # uncomment below all when doing only earlier patch samples

ref=crop(ref,ext1)
cti=crop(cti,ext1)
cti[is.na(cti)]=summary(cti)@values["Mean",]
dem=crop(dem,ext1)
mkhd05nir=crop(mkhd05nir,ext1)
mkhd05r=crop(mkhd05r,ext1)
mkhd05g=crop(mkhd05g,ext1)
mkhd05ndvi=crop(mkhd05ndvi,ext1)
naip03r=crop(naip03r,ext1)
naip03b=crop(naip03b,ext1)
naip03g=crop(naip03g,ext1)
naip08b=crop(naip08b,ext1)
naip08g=crop(naip08g,ext1)
naip08r=crop(naip08r,ext1)
naip08nir=crop(naip08nir,ext1)
naip08ndvi=crop(naip08ndvi,ext1);
slope=crop(slope,ext1)

ROWS=ref@nrows
COLS=ref@ncols

loc.raster=ref
loc.raster[1:ncell(ref)]=1:ncell(ref)
dir.create("dat/findGoodRegionICDM13/ICDMTrainSize",showWarnings=F);


set.seed(ceiling(sqrt(seeds[i])));
N=60;
# trainWetRep=sample(loc.raster[( ( (loc.raster-1)%%COLS + 1) > (COLS/2+20) & ( (loc.raster-1)%%COLS + 1) < (COLS-20) ) & (ref==1)],N)
# trainDryRep=sample(loc.raster[( ( (loc.raster-1)%%COLS + 1) > (COLS/2+20) & ( (loc.raster-1)%%COLS + 1) < (COLS-20) ) & (ref==0)],N)
trainWetRep=sample(loc.raster[(ref==1)],N)
trainDryRep=sample(loc.raster[(ref==0)],N)
train=ref;
train[1:ncell(train)]=NA
train[trainRep]=1

radius=21
train=buffer(train,width=radius) #note here the width is meters, b/3=no.of.cell

#generate training and test samples
train.loc=loc.raster[train]
test.loc=setdiff(values(loc.raster), train.loc)

	
for(i in 1:8){
dir.create(paste("dat/findGoodRegionICDM13/ICDMTrainSize/", as.character(i), sep=""), showWarnings=F);

trainRep = c( trainWetRep[1:(20+i*5)], trainDryRep[1:(20+i*5)])

train=ref;
train[1:ncell(train)]=NA
train[trainRep]=1

radius=21
train=buffer(train,width=radius) #note here the width is meters, b/3=no.of.cell

#generate training and test samples
train.loc=loc.raster[train]

train.ref=ref[train.loc]
train.cti=cti[train.loc]
train.dem=dem[train.loc]
train.mkhd05nir=mkhd05nir[train.loc]
train.mkhd05r=mkhd05r[train.loc]
train.mkhd05g=mkhd05g[train.loc]
train.mkhd05ndvi=mkhd05ndvi[train.loc]
train.naip03r=naip03r[train.loc]
train.naip03b=naip03b[train.loc]
train.naip03g=naip03g[train.loc]
train.naip08b=naip08b[train.loc]
train.naip08g=naip08g[train.loc]
train.naip08r=naip08r[train.loc]
train.naip08nir=naip08nir[train.loc]
train.naip08ndvi=naip08ndvi[train.loc]
train.slope=slope[train.loc]

test.ref=ref[test.loc]
test.cti=cti[test.loc]
test.dem=dem[test.loc]
test.mkhd05nir=mkhd05nir[test.loc]
test.mkhd05r=mkhd05r[test.loc]
test.mkhd05g=mkhd05g[test.loc]
test.mkhd05ndvi=mkhd05ndvi[test.loc]
test.naip03r=naip03r[test.loc]
test.naip03b=naip03b[test.loc]
test.naip03g=naip03g[test.loc]
test.naip08b=naip08b[test.loc]
test.naip08g=naip08g[test.loc]
test.naip08r=naip08r[test.loc]
test.naip08nir=naip08nir[test.loc]
test.naip08ndvi=naip08ndvi[test.loc]
test.slope=slope[test.loc]




# # #test on wall to wall reference area
 # all.dat=cbind(as.data.frame(cti),as.data.frame(dem),as.data.frame(mkhd05nir),as.data.frame(mkhd05r),as.data.frame(mkhd05g),as.data.frame(mkhd05ndvi),as.data.frame(naip03r),as.data.frame(naip03b),as.data.frame(naip03g),as.data.frame(naip08b),as.data.frame(naip08g),as.data.frame(naip08r),as.data.frame(naip08nir),as.data.frame(naip08ndvi),as.data.frame(slope),as.data.frame(ref));
 # all.dat[,16]=as.factor(all.dat[,16])
 # # names(all.dat)=c("cti","dem","mkhd05nir","mkhd05r","mkhd05g","mkhd05ndvi","naip03r","naip03b","naip03g","naip08b","naip08g","naip08r","naip08nir","naip08ndvi","slope","ref")

all.dat.train=cbind(as.data.frame(train.cti),as.data.frame(train.dem),as.data.frame(train.mkhd05nir),as.data.frame(train.mkhd05r),as.data.frame(train.mkhd05g),as.data.frame(train.mkhd05ndvi),as.data.frame(train.naip03r),as.data.frame(train.naip03b),as.data.frame(train.naip03g),as.data.frame(train.naip08b),as.data.frame(train.naip08g),as.data.frame(train.naip08r),as.data.frame(train.naip08nir),as.data.frame(train.naip08ndvi),as.data.frame(train.slope),as.data.frame(train.ref),as.data.frame(train.loc-1));
all.dat.train[,16]=as.factor(all.dat.train[,16])

all.dat.test=cbind(as.data.frame(test.cti),as.data.frame(test.dem),as.data.frame(test.mkhd05nir),as.data.frame(test.mkhd05r),as.data.frame(test.mkhd05g),as.data.frame(test.mkhd05ndvi),as.data.frame(test.naip03r),as.data.frame(test.naip03b),as.data.frame(test.naip03g),as.data.frame(test.naip08b),as.data.frame(test.naip08g),as.data.frame(test.naip08r),as.data.frame(test.naip08nir),as.data.frame(test.naip08ndvi),as.data.frame(test.slope),as.data.frame(test.ref),as.data.frame(test.loc-1));
all.dat.test[,16]=as.factor(all.dat.test[,16])

# all.dat=cbind(all.dat,1:ncell(ref)-1)

# all.dat=all.dat[,-c(1,2,15)];
all.dat.train=all.dat.train[,-c(1,2,15)]
all.dat.test=all.dat.test[,-c(1,2,15)]



write.table((all.dat.train),file=paste("dat/findGoodRegionICDM13/ICDMTrainSize/", as.character(i), "/landcover.data",sep=""), sep=",", row.n=F, col.n=F, quote=F);
write.table((all.dat.test),file=paste("dat/findGoodRegionICDM13/ICDMTrainSize/", as.character(i), "/landcover.test",sep=""), sep=",", row.n=F, col.n=F, quote=F);
write.table((all.dat.train),file=paste("dat/findGoodRegionICDM13/ICDMTrainSize/", as.character(i), "/landcover.validation",sep="") ,sep=",", row.n=F, col.n=F, quote=F);
# write.table((all.dat),file=paste("dat/findGoodRegionICDM13/ICDMTrainSize/", as.character(i), "/landcover.map", sep="") ,sep=",", row.n=F, col.n=F, quote=F);
# write.table(values(ref), file=paste("dat/findGoodRegionICDM13/ICDMTrainSize/", as.character(i), "/ref.txt",sep="") ,sep=",", row.n=F, col.n=F, quote=F)

}#end of for loop in generating different samples